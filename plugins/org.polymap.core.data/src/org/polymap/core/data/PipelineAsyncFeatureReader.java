/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.polymap.core.data.PipelineFeatureSource.FeatureResponseHandler;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.session.SessionContext;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class PipelineAsyncFeatureReader<T extends FeatureType, F extends Feature>
        implements FeatureReader<T,F> {

    private static final Log log = LogFactory.getLog( PipelineAsyncFeatureReader.class );

    protected static final List<Feature>    END_OF_RESPONSE = Collections.EMPTY_LIST;
    
    protected static final int              DEFAULT_QUEUE_SIZE = 3;  //2560 / DataSourceProcessor.DEFAULT_CHUNK_SIZE;

    private static volatile int             fetcherCount = 0;

    protected PipelineFeatureSource fs;
    
    protected Query                 query;
    /**
     * The FIFO between the fetcher and the calling thread. Holds chunks of
     * features to minimize synchronization overhead between the threads.
     */
    private BlockingQueue<List<Feature>> queue = new ArrayBlockingQueue( DEFAULT_QUEUE_SIZE );

    /** The current chunk iterated via {@link #bufIt}. */
    private List<Feature>           buf;
    
    /** Iterator of {@link #buf}. */
    private Iterator<Feature>       bufIt;
    
    private volatile Throwable      resultException;
    
    private boolean                 endOfResponse;

    
    public PipelineAsyncFeatureReader( PipelineFeatureSource fs, Query query, SessionContext sessionContext ) {
        this.fs = fs;
        this.query = query;

        // start Fetcher
        Fetcher fetcher = new Fetcher();
        Polymap.executorService().submit( () -> {
            if (sessionContext != null) {
                sessionContext.execute( fetcher );
            }
            else {
                fetcher.run();
            }
        });
    }


    @Override
    public T getFeatureType() {
        return (T)fs.getSchema();
    }


    protected class Fetcher 
            implements Runnable {
        
        private volatile int fetcherNumber = fetcherCount++;
        
        @Override
        public void run() {
            try {
                fs.fetchFeatures( query, new FeatureResponseHandler() {
                    @Override
                    public void handle( List<Feature> features ) throws Exception {
                        if (checkEnd()) {
                            log.debug( "Async fetcher[" + fetcherNumber + "]: queue=" + queue.size() + ", chunk=" + features.size() );
                            queue.put( features );
                            Thread.yield();
                        }
                    }
                    @Override
                    public void endOfResponse() throws Exception {
                        if (checkEnd()) {
                            queue.put( END_OF_RESPONSE );
                        }
                    }
                    boolean checkEnd() throws InterruptedException {
                        // FIXME cancel job immediately
                        return queue != null;
                    }
                });
                log.debug( "Async fetcher: done." );
            }
            catch (Throwable e) {
                try {
                    resultException = e;
                    if (queue != null) {
                        queue.put( END_OF_RESPONSE );
                    }
                }
                catch (InterruptedException e1) {
                    log.error( e1 );
                }
            }
        }
    }
    
    
    @Override
    public void close() {
        queue = null;
        buf = null;
    }
    
    
    @Override
    public boolean hasNext() {
        if (bufIt != null && bufIt.hasNext()) {
            return true;
        }
        else if (/*!queue.isEmpty() ||*/ !endOfResponse) {
            while (true) {
                try {
                    List<Feature> chunk = queue.take();
                    if (chunk == END_OF_RESPONSE) {
                        endOfResponse = true;
                        if (resultException != null) {
                            throw Throwables.propagate( resultException );
                        } 
                        else {
                            return false;
                        }
                    }
                    else {
                        buf = chunk;
                        bufIt = buf.iterator();
                        if (bufIt.hasNext()) {
                            return true;
                        }
                    }
                }
                catch (InterruptedException e) {
                    // XXX again?
                    log.warn( "Interrupted, again..." );
                }
            }
        }
        else {
            log.info( "hasNext(): FALSE" );
            close();
            return false;
        }
    }

    
    @Override
    public F next() {
        if (!hasNext()) {
            throw new NoSuchElementException( "No such element." );
        }
        return (F)bufIt.next();
    }

    
    //@Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
