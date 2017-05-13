/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.collect.Consumer;
import org.polymap.core.runtime.collect.Function;
import org.polymap.core.runtime.collect.Predicate;

/**
 * EXPERIMENTAL: Extended conditional operator.
 * <ul>
 * <li>eliminates the need for a temporary variable holding the to-be-checked value</li>
 * <li>allows performing tasks without return value (unlike plain conditional if)</li>
 * <li>supports checked {@link Exception}s (unlike {@link Optional})</li>
 * </ul>
 *
 * <h3>Simple if-else</h3>
 * 
 * <pre>
 * String value = calculate();
 * if (value.equals( "..." ) {
 *     doSomething( value )
 * }
 * else {
 *     ...
 * }
 * </pre>
 * 
 * <h3>Conditional if</h3>
 * 
 * <pre>
 * String value = calculate();
 * return value.equals( ".." ) 
 *         ? doSomething( value ) 
 *         : doSomethingElse( value );
 * </pre>
 * 
 * <h3>Using {@link Check}</h3>
 * 
 * <pre>
 * Check.of( calculate() ).whenever( v -> v.equals( "..." )
 *         .result( v -> doSomething( v ) );
 * </pre>
 * 
 * @author Falko Bräutigam
 */
public class Check<T> {

    private static final Log log = LogFactory.getLog( Check.class );
    
    public static <U> Check<U> of( U value ) {
        return new Check( value );
    }
    
    
    // instance *******************************************
    
    private T           value;
    
    private boolean     conditionMet = true;
    

    protected Check( T value ) {
        this.value = value;
    }

    public <E extends Exception> Check<T> _if( Predicate<T,E> condition ) throws E {
        conditionMet = conditionMet && condition.test( value );
        return this;
    }

    public <E extends Exception> Check<T> _do( Consumer<T,E> task ) throws E {
        if (conditionMet) {
            task.accept( value );
        }
        return this;
    }

    public <E extends Exception> Check<T> _else( Consumer<T,E> task ) throws E {
        if (!conditionMet) {
            task.accept( value );
        }
        return this;
    }

    public <R,E extends Exception> R _return( Function<T,R,E> task ) throws E {
        return conditionMet ? task.apply( value ) : null;
    }

    public <R,E extends Exception> R _return( Function<T,R,E> ifTrue, Function<T,R,E> ifFalse ) throws E {
        return conditionMet ? ifTrue.apply( value ) : ifFalse.apply( value );
    }

    public <E extends Exception> Check<T> whenever( Predicate<T,E> condition ) throws E {
        return _if( condition );
    }
    
    public <E extends Exception> Check<T> and( Predicate<T,E> condition ) throws E {
        return _if( condition );
    }
    
    public <E extends Exception> Check<T> perform( Consumer<T,E> task ) throws E {
        return _do( task );
    }
    
    public <E extends Exception> Check<T> orElse( Consumer<T,E> task ) throws E {
        return _else( task );
    }

    public <R,E extends Exception> R result( Function<T,R,E> task ) throws E {
        return _return( task );
    }
    
    public <R,E extends Exception> R result( Function<T,R,E> ifTrue, Function<T,R,E> ifFalse ) throws E {
        return _return( ifTrue, ifFalse );
    }
    
    
    // test ***********************************************
    
    public static void main( String[] args ) {
        int value = 1;
        Check.of( value ).whenever( v -> v < 10 ).result( v -> v+1, v -> v );
        
        Check.of( value ).whenever( v -> v < 10 )
                .perform( v -> System.out.println( v ) )
                .orElse( v -> System.err.println( v ) );

    }
    
}
