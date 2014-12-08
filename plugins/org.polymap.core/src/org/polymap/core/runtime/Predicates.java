/* 
 * polymap.org
 * Copyright (C) 2014, Falko Br�utigam. All rights reserved.
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

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

/**
 * Static factory methods creating general purpose {@link Predicate} instances.
 * 
 * @see com.google.common.base.Predicates
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class Predicates {

    /**
     * Returns a predicate that evaluates to {@code true} if the object reference
     * being tested references the same object of the given target.
     * 
     * @see <a href="http://code.google.com/p/guava-libraries/issues/detail?id=355">Guava Issue 355</a>
     */
    public static <T> SameAsPredicate<T> sameAs( T target ) {
        return new SameAsPredicate( target );
    }
    
    
    /**
     * Implements the {@link Predicates#isSame(Object)} method.
     */
    private static class SameAsPredicate<T> 
            implements Predicate<T>, Serializable {

        private static final long   serialVersionUID = 0;
        
        private T                   target;
        
        SameAsPredicate( T target ) {
            assert target != null;
            this.target = target;
        }

        @Override 
        public boolean apply( @Nullable T t ) {
            return target == t;
        }

        @Override 
        public int hashCode() {
            return ~target.hashCode();
        }

        @Override 
        public boolean equals( @Nullable Object obj ) {
            if (obj instanceof SameAsPredicate) {
                SameAsPredicate<?> that = (SameAsPredicate<?>) obj;
                // http://stackoverflow.com/questions/6323713/does-guava-provide-a-predicates-ist-a-la-predicates-equaltot-t-for-identit
                return target == that.target;
            }
            return false;
        }
        
        @Override 
        public String toString() {
            return "isSame(" + target + ")";
        }
    }

}