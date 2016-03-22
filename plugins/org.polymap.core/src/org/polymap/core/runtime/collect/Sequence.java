/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.runtime.collect;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class Sequence<T, E extends Exception> {

    private static Log log = LogFactory.getLog( Sequence.class );
    
    /**
     * 
     *
     * @param elements
     * @return Newly created instance of {@link Sequence}.
     */
    public static <T> Sequence<T,RuntimeException> of( T... elements ) {
        return Sequence.of( Arrays.asList( elements ) );
    }
    
    /**
     * 
     *
     * @param src
     * @return Newly created instance of {@link Sequence}.
     */
    public static <T,E extends Exception> Sequence<T,RuntimeException> of( Iterable<T> src ) {
        return new Sequence( new ExceptionIterable() {
            @Override
            public ExceptionIterator iterator() {
                Iterator<T> it = src.iterator();
                return () -> it.hasNext() ? it.next() : null;
            }
        }); 
    }
    
    
    // instance *******************************************

    protected ExceptionIterable<T,E>    delegate;


    protected Sequence( ExceptionIterable<T,E> delegate ) {
        this.delegate = delegate;
    }

    
    /**
     * Returns the same sequence of elements but allow predicates and functions to throw
     * a checked {@link Exception}.
     * <p/>
     * <b>Example:</b>
     * <pre>
     * {@code sequence.<ExecutionException>withExceptions()}
     * </pre>
     *
     * @return Newly created instance of {@link ExceptionSequence}.
     */
    public <EE extends Exception> Sequence<T,EE> withExceptions() {
        return new Sequence( delegate );    
    }
    
    
    public Sequence<T,E> filter( Predicate<T,E> predicate ) {
        delegate = new ExceptionIterable() {
            ExceptionIterable<T,E>  _delegate = delegate;
            @Override
            public ExceptionIterator iterator() {
                ExceptionIterator<T,E> it = _delegate.iterator();
                return () -> it.next();
            }
        }; 
        return this;
    }

    
    public int count() throws E {
        int result = 0;
        ExceptionIterator<T,E> it = delegate.iterator();
        for (T elm=it.next(); elm != null; elm=it.next()) {
            ++result;
        }
        return result;
    }
    

    // test ***********************************************
    
    /**
     * @throws Exception 
     * @throws ExecutionException 
     * 
     */
    public static void main( String[] args ) throws Exception {
        int loops = 100000;
        int elements = 1000;
    
        Integer[] numbers = new Integer[elements]; 
        Arrays.fill( numbers, 1 );
        
        //
        Timer timer = new Timer();
        int result = 0;
        for (int i=0; i<loops; i++) {
            result += Sequence.of( numbers )
                    .filter( elm -> nonzero( elm ) )
                    .filter( elm -> elm < 3 )
                    .count();
        }
        System.out.println( result + " - loops: " + loops + "x" + elements + " in " + timer.elapsedTime() + "ms" ); 

        //
        timer = new Timer();
        result = 0;
        for (int i=0; i<loops; i++) {
            result += Arrays.asList( numbers ).stream()
                    .filter( elm -> nonzero( elm ) )
                    .filter( elm -> elm < 3 )
                    .count();
        }

        System.out.println( result + " - loops: " + loops + "x" + elements + " in " + timer.elapsedTime() + "ms" ); 
        //
        timer = new Timer();
        result = 0;
        for (int i=0; i<loops; i++) {
            int count = 0;
            for (Integer elm : numbers) {
                if (nonzero( elm )) {
                    if (elm < 3) {
                        count ++;
                    }
                }
            }
            result += count;
        }
        System.out.println( result + " - loops: " + loops + "x" + elements + " in " + timer.elapsedTime() + "ms" ); 
    }
    
    
    protected static boolean nonzero( Integer elm ) {
        return elm != 0;
    }
    
}
