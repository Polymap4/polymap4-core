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
package org.polymap.core.runtime;

/**
 * Statis helper methods that deal with {@link Number}s.  
 *
 * @author Falko Bräutigam
 */
public class Numbers {
    
    /**
     * Returns the value of the specified number as an other Number type, which may
     * involve rounding or truncation.
     * 
     * @param n The number to cast.
     * @param target The target type.
     */
    public static <R extends Number> R cast( Number n, Class<R> target ) {
        assert n != null;
        if (target.isAssignableFrom( n.getClass() )) {
            return (R)n;
        }
        else if (target.isAssignableFrom( Integer.class )) {
            return (R)Integer.valueOf( n.intValue() );
        }
        else if (target.isAssignableFrom( Long.class )) {
            return (R)Long.valueOf( n.longValue() );
        }
        else if (target.isAssignableFrom( Float.class )) {
            return (R)Float.valueOf( n.floatValue() );
        }
        else if (target.isAssignableFrom( Double.class )) {
            return (R)Double.valueOf( n.doubleValue() );
        }
        else if (target.isAssignableFrom( Short.class )) {
            return (R)Short.valueOf( n.shortValue() );
        }
        else if (target.isAssignableFrom( Byte.class )) {
            return (R)Byte.valueOf( n.byteValue() );
        }
        else {
            throw new RuntimeException( "Unhandled Number type: " + target );
        }
    }
    
    
    public static Double roundToDigits( Double d, int digits ) {
        double digitsScale = Math.pow( 10, digits );
        return Math.round( d * digitsScale  ) / digitsScale; 
    }
    
}
