/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows to return a Stream with {@link Iterable} interface from a method. The
 * caller can do both, operate on the stream or iterate over to elements. The
 * advantage is that in both cases no Collection is constructed from the Stream.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class StreamIterable<T>
        implements Iterable<T> {

    private static Log log = LogFactory.getLog( StreamIterable.class );
    
    public static <T> StreamIterable<T> of( Stream<T> stream ) {
        return new StreamIterableImpl( stream );
    }
    
    public static <T> StreamIterable<T> of( Iterable<T> it ) {
        if (it instanceof StreamIterable) {
            return (StreamIterable<T>)it;
        }
        else {
            return new IterableStreamImpl( it );
        }
    }
    
    // instance *******************************************
    
    public abstract Stream<T> stream();
    
    /**
     * 
     */
    static class StreamIterableImpl<TT>
            extends StreamIterable<TT> {
        
        private Stream<TT>          stream;

        private boolean             done;

        public StreamIterableImpl( Stream<TT> stream ) {
            this.stream = stream;
        }

        @Override
        public Iterator<TT> iterator() {
            if (done) {
                throw new IllegalStateException( "StreamIterable does not support multiple Iterators." );
            }
            done = true;
            return stream.iterator();
        }

        public Stream<TT> stream() {
            return stream;
        }
    }
    
    /**
     * 
     */
    static class IterableStreamImpl<TT>
            extends StreamIterable<TT> {
        
        private Iterable<TT>        it;

        public IterableStreamImpl( Iterable<TT> it ) {
            this.it = it;
        }

        @Override
        public Iterator<TT> iterator() {
            return it.iterator();
        }

        @Override
        public Stream<TT> stream() {
            return StreamSupport.stream( spliterator(), false );
        }
    }
    
}
