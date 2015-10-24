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

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Static methods to support working with streams. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Streams {

    /**
     * Re-throw any checked Exception as {@link RuntimeException}.
     * <pre>
     *     List<Class> types = ...;
     *     types.map( cl -> Streams.runtimeException( () -> cl.newInstance() ) ) );
     * </pre>
     *
     * @param producer
     * @return The value created by the producer.
     */
    public static <T> T runtimeException( Callable<T> producer ) {
        try {
            return producer.call();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    /**
     * Provides an {@link Iterable} interface for the given stream.
     * <p>
     * Shortcut for:
     * <pre>
     * {@link StreamIterable}.of( stream );
     * </pre>
     * 
     * @param stream
     * @return {@link StreamIterable} of the given Stream.
     */
    public static <T> Iterable<T> iterable( Stream<T> stream ) {
        return StreamIterable.of( stream );
    }
    
    
    /**
     * Handle checked exceptions in lambda {@link Function} or {@link Predicate}
     * calls.
     * <p/>
     * <b>Example:</b>
     * 
     * <pre>
     * try (ExceptionCollector&lt;SpecificException or ?&gt; excs = Streams.exceptions()) {
     *     return list.stream()
     *             .map( o -> excs.check( () -> doSomething( o ) ) )
     *             .filter( o -> excs.check( () -> checkSomething( o ) ) ) );
     * }
     * </pre>
     * 
     * You <b>must</b> specify a type param in order to make things work. If unsure
     * use <b>?</b> type parameter.
     * <p/>
     * The try-with-resources statement automatically closes the collector and throws
     * a <code>SpecificException</code> if one of invocations of
     * <code>excs.handle()</code> did collect an Exception. A SpecificException is
     * thrown as checked Exception, all other Exception types are wrapped within a
     * RuntimeException.
     *
     * @return Newly created {@link ExceptionCollector}
     */
    public static ExceptionCollector exceptions() {
        return new ExceptionCollector();
    }

    
    /**
     * 
     */
    public static class ExceptionCollector<E extends Exception>
            implements AutoCloseable {
        
        private Exception       exception;
        
        @Override
        public void close() throws E {
            if (exception != null) {
                throw (E)exception;
            }
        }

        public <T> T check( Callable<T> callable ) {
            try {
                return callable.call();
            }
            catch (Exception e) {
                exception = e;
                throw new CollectedException( e );
            }
        }
        
//        public <T> Predicate<T> collect( Predicate<? super T> predicate ) {
//            
//        }

        /**
         * 
         */
        static class CollectedException
                extends RuntimeException {

            public CollectedException( Throwable cause ) {
                super( cause );
            }
            
        }

    }
    
    
}
