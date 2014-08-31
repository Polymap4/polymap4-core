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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;

import org.polymap.core.runtime.mp.ForEachExecutor.Factory;

/**
 * A <code>ForEach</code> statement allows to define several processing steps that
 * are applied to the elements of a {@link #source} collection. The processing steps
 * are specified as (reusable) {@link Function} objects. The processing steps are
 * independent from each other, so is is possible to execute them in a pipeline
 * and/or distributed over several threads for an {@link Parallel} processor. The
 * actual mode of execution is provided by {@link ForEachExecutor} instances.
 * <p/>
 * <b>Example:</b>
 * <pre>
 * result = ForEach.in( source )
 *     .doParallel( new Function<String,StringBuilder>() {
 *          public final StringBuilder apply( String elm ) {
 *               return new StringBuilder( elm );
 *           }
 *      })
 *     .doParallel( new Parallel<StringBuilder,StringBuilder>() {
 *          public final StringBuilder apply( StringBuilder elm ) {
 *              return elm.insert( 0, '\'' ).append( '\'' );
 *          }
 *      });
 * </pre>
 * <b>Execution:</b><p>
 * The number of concurrent threads used and the size of the chunks are defined
 * by the {@link AsyncExecutor}, which is used by default.
 * <p/>
 * The ForEach loop is <b>executed</b> by calling {@link #iterator()}. So this simplest
 * way to execute is to use the Iterable interface and iterate over the result elements.
 * Some examples for other uses:
 * <ul>
 * <li>Execute and just get the resulting number of elements : <code>Iterables.size( forEach );</code></li>
 * <li>Store result elements in an {@link ArrayList} : <code>Lists.newArrayList( forEach );</code></li>
 * </ul>
 *
 * @param <S> The type of the source elements.
 * @param <T> The type of the target elements.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ForEach<S,T>
        implements Iterable<T> {

    public static Factory      defaultExecutorFactory = new AsyncExecutor.AsyncFactory();

    /**
     * Creates a new instance for the given source elements.
     * <p/>
     * The number of concurrent threads used and the size of the chunks are defined
     * by the {@link AsyncExecutor}, which is used by default.
     * 
     * @param <S> The type of the source elements.
     * @param <T> The type of the target elements.
     * @param source
     * @return The newly created instance.
     */
    public static <S,T> ForEach<S,T> in( Iterable<S> source ) {
        return new ForEach( source );
    }

    /**
     * @see ForEach#in(Iterable)
     */
    public static <S,T> ForEach<S,T> in( final Iterator<S> source ) {
        return new ForEach( new Iterable<S>() {
            public Iterator<S> iterator() {
                return source;
            }
        });
    }

   
    // instance *******************************************

    private Factory         executorFactory = defaultExecutorFactory;
    
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

    public ForEach withFactory( Factory factory ) {
        this.executorFactory = factory;
        return this;
    }

    public ForEach doParallel( Function func ) {
        Processor proc = new Processor( func );
        proc.maxThreads = Integer.MAX_VALUE;
        processors.add( proc );
        return this;
    }


    public ForEach doSerial( Function func ) {
        Processor proc = new Processor( func );
        proc.maxThreads = 1;
        processors.add( proc );
        return this;
    }


    @Override
    public Iterator<T> iterator() {
        ForEachExecutor<S,T> executor = executorFactory.newExecutor( this );
        executor.setChunkSize( chunkSize );
        return executor;
    }


    // public T[] toArray() {
    // List<T> list = toList();
    // return list.toArray( new T[ list.size() ] );
    // }

//    public List<T> asList() {
//        List<T> result = new ArrayList( 128 );
//        for (T elm : this) {
//            result.add( elm );
//        }
//        return result;
//    }

    
//    public int start() {
//        int count = 0;
//        for (T elm : this) {
//            ++count;
//        }
//        return count;
//    }


    // executor

    public List<Processor> processors() {
        return processors;
    }
    
    public Iterable<S> source() {
        return source;
    }

    /**
     * Additional information about a {@link Function}. 
     */
    static class Processor<S1,T1>
            implements Function<S1,T1> {
    
        public Function<S1,T1>  delegate;
        public int              maxChunkSize = -1;
        public int              maxThreads = -1;

        public Processor( Function<S1,T1> delegate ) {
            this.delegate = delegate;
        }

        @Override
        public T1 apply( S1 input ) {
            return delegate.apply( input );
        }

        @Override
        public boolean equals( Object obj ) {
            return delegate.equals( obj );
        }
    }

}
