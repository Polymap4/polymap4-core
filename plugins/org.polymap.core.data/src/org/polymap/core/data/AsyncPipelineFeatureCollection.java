/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.PipelineFeatureSource.FeatureResponseHandler;
import org.polymap.core.data.feature.DataSourceProcessor;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class AsyncPipelineFeatureCollection
        extends AbstractPipelineFeatureCollection
        implements FeatureCollection<SimpleFeatureType, SimpleFeature> {

    private static final Log log = LogFactory.getLog( AsyncPipelineFeatureCollection.class );

    protected static final List<Feature>    END_OF_RESPONSE = ListUtils.EMPTY_LIST;
    
    protected static final int              DEFAULT_QUEUE_SIZE = 5000 / DataSourceProcessor.DEFAULT_CHUNK_SIZE;

    private static int                      fetcherCount = 0;
    
    protected PipelineFeatureSource         fs;

    protected Query                         query;
    
    private int                             size = -1;
    
    private SessionContext                  sessionContext;


    protected AsyncPipelineFeatureCollection( PipelineFeatureSource fs, Query query, SessionContext sessionContext ) {
        super( fs.getSchema() );
        this.fs = fs;
        this.query = query;
        this.sessionContext = sessionContext;
        fs.addFeatureListener( this );
    }

    protected Iterator openIterator() {
        log.debug( "..." );
        return new AsyncPipelineIterator();
    }

    protected void closeIterator( Iterator it ) {
        log.debug( "close= " + it );
        ((AsyncPipelineIterator)it).close();
    }

    public int size() {
        if (size < 0) {
            size = fs.getFeaturesSize( query );
        }
        return size;
    }

    public ReferencedEnvelope getBounds() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     * 
     */
    class AsyncPipelineIterator
            implements Iterator {

        /**
         * The FIFO between the fetcher and the calling thread. Holds chunks of
         * features to minimize synchronization overhead between the threads.
         */
        BlockingQueue<List<Feature>>    queue = new ArrayBlockingQueue( DEFAULT_QUEUE_SIZE );

        List<Feature>                   buffer;
        
        Iterator<Feature>               bufferIt;
        
        Throwable                       resultException;
        
        boolean                         endOfResponse;
        
        
        protected AsyncPipelineIterator() {
            final Runnable fetcher = new Runnable() {
                private volatile int fetcherNumber = fetcherCount++;
                public void run() {
                    try {
                        fs.fetchFeatures( query, new FeatureResponseHandler() {
                            public void handle( List<Feature> features )
                            throws Exception {
                                if (checkEnd()) {
                                    //log.info( "Async fetcher[" + fetcherNumber + "]: queue=" + queue.size() );
                                    queue.put( features );
                                    Thread.yield();
                                }
                            }
                            public void endOfResponse()
                            throws Exception {
                                if (checkEnd()) {
                                    queue.put( END_OF_RESPONSE );
                                }
                            }
                            boolean checkEnd() 
                            throws InterruptedException {
                                // FIXME cancel job immediately
                                return queue != null;
                            }
                        });
                        log.debug( "Async fetcher: done." );
                        //return Status.OK_STATUS;
                    }
                    catch (Throwable e) {
                        try {
                            log.warn( "Async fetcher: error.", e );
                            resultException = e;
                            if (queue != null) {
                                queue.put( END_OF_RESPONSE );
                            }
                        }
                        catch (InterruptedException e1) {
                            log.error( e1 );
                            //queue = null;
                        }
                        //return Status.CANCEL_STATUS;
                    }
                }
            };
            Polymap.executorService().execute( new Runnable() {
                public void run() {
                    if (sessionContext != null) {
                        sessionContext.execute( fetcher );
                    }
                    else {
                        fetcher.run();
                    }
                }
            });
        }
        
        public void close() {
            queue = null;
            buffer = null;
        }
        
        public boolean hasNext() {
            if (bufferIt != null && bufferIt.hasNext()) {
                return true;
            }
            else if (/*!queue.isEmpty() ||*/ !endOfResponse) {
                while (true) {
                    try {
                        List<Feature> chunk = queue.take();
                        if (chunk == END_OF_RESPONSE) {
                            endOfResponse = true;
                            if (resultException != null) {
                                log.warn( "##### Async: result exception: " + resultException );
                                if (resultException instanceof RuntimeException) {
                                    throw (RuntimeException)resultException;
                                } 
                                else {
                                    throw new RuntimeException( resultException );
                                }
                            } 
                            else {
                                return false;
                            }
                        }
                        else {
                            buffer = chunk;
                            bufferIt = buffer.iterator();
                            return true;
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

        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException( "No such element." );
            }
            return bufferIt.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
