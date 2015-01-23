/* 
 * polymap.org
 * Copyright (C) 2011-2013, Falko Bräutigam. All rigths reserved.
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
 */
package org.polymap.core.runtime.mp;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.runtime.mp.ForEach.Processor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SyncExecutor<S,T>
        implements ForEachExecutor<S,T> {

    private static Log log = LogFactory.getLog( SyncExecutor.class );

    public static class SyncFactory implements Factory {
        public ForEachExecutor newExecutor( ForEach foreach ) {
            return new SyncExecutor( foreach );
        }
    }

    // instance *******************************************
    
    private ForEach             forEach;
    
    private Iterator<S>         source;
    
    private Processor[]         chain;

    
    public SyncExecutor( ForEach<S,T> forEach ) {
        this.forEach = forEach;
        this.source = forEach.source().iterator();
        
        List<Processor> procs = forEach.processors();
        this.chain = procs.toArray( new Processor[ procs.size() ] );
    }

    public boolean hasNext() {
        return source.hasNext();
    }

    public T next() {
        try {
            Object next = source.next();
            for (int i=0; i<chain.length; i++) {
                next = chain[i].apply( next );
            }
            return (T)next;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void setChunkSize( int chunkSize ) {
    }
    
}
