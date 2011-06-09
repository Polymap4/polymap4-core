/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A <code>ForEach</code> statement allows to define several processing steps that
 * are applied to the elements of a {@link #source} collection. The processing steps
 * are specified as (reusable) {@link Processor} objects. The processing steps are
 * independent from each other, so is is possible to execute them in a pipeline
 * and/or distributed over several threads for an {@link Parallel} processor. The
 * actual mode of execution is provided by {@link ForEachExecutor} instances.
 * <p/>
 * <b>Example:</b>
 * <pre>
 * result = ForEach.in( source )
 *     .doFirst( new Parallel<StringBuilder,String>() {
 *          public final StringBuilder process( String elm ) {
 *               return new StringBuilder( elm );
 *           }
 *      })
 *     .doNext( new Parallel<StringBuilder,StringBuilder>() {
 *          public final StringBuilder process( StringBuilder elm ) {
 *              return elm.insert( 0, '\'' ).append( '\'' );
 *          }
 *      })
 *     .asList();
 *
 * @param <S> The type of the source elements.
 * @param <T> The type of the target elements.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ForEach<T, S>
        implements Iterable<T> {

    public static ForEachExecutor.Factory      executorFactory = new AsyncExecutor.AsyncFactory();
    
    /**
     * Creates a new instance for the given source elements.
     * 
     * @param <S> The type of the source elements.
     * @param <T> The type of the target elements.
     * @param source
     * @return The newly created instance.
     */
    public static <S, T> ForEach<T, S> in( Iterable<S> source ) {
        return new ForEach( source );
    }

   
    // instance *******************************************

    private Iterable<S>     source;

    private List<Processor> processors = new ArrayList();
    
    private int             chunkSize = -1;


    protected ForEach( Iterable<S> source ) {
        this.source = source;
    }


    public ForEach chunked( int _chunkSize ) {
        this.chunkSize = _chunkSize;
        return this;
    }


    public ForEach doFirst( Processor proc ) {
        processors.add( proc );
        return this;
    }


    public ForEach doNext( Processor proc ) {
        processors.add( proc );
        return this;
    }


    public Iterator<T> iterator() {
        ForEachExecutor<T, S> executor = executorFactory.newExecutor( this );
        executor.setChunkSize( chunkSize );
        return executor;
    }


    // public T[] toArray() {
    // List<T> list = toList();
    // return list.toArray( new T[ list.size() ] );
    // }

    public List<T> asList() {
        List<T> result = new ArrayList( 128 );
        for (T elm : this) {
            result.add( elm );
        }
        return result;
    }

    
    // executor
    
    List<Processor> processors() {
        return processors;
    }
    
    Iterable<S> source() {
        return source;
    }
    
}
