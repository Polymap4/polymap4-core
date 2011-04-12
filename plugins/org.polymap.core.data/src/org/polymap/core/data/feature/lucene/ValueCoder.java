/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as 
 * indicated by the @authors tag.
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
package org.polymap.core.data.feature.lucene;

import java.util.Date;
import java.util.Locale;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Encode/decode primitive types into/from string representation that can be
 * stored and searched in the Lucene index.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
class ValueCoder {

    /** Lucene indexable number format. */
    static final NumberFormat       nf;
    
    static {
        nf = NumberFormat.getNumberInstance( Locale.ENGLISH );
        nf.setMinimumIntegerDigits( 10 );  // 32bit int
        nf.setMinimumFractionDigits( 0 );
        nf.setGroupingUsed( false );
    }


    /**
     * 
     * @param value
     * @param targetType Optional value that specifies which type is to be used
     *        for the encode.
     */
    public static String encode( Object value, Class targetType ) {
        if (value == null) {
            return null;
        }
        
        Class type = targetType != null ? targetType : value.getClass();
        
        // all numbers as double
        if (Number.class.isAssignableFrom( type )) {
            return nf.format( ((Number)value).doubleValue() );                    
        }
        // Date
        else if (Date.class.isAssignableFrom( type )) {
            return String.valueOf( ((Date)value).getTime() );
        }
        // Boolean
        else if (Boolean.class.isAssignableFrom( type )) {
            return ((Boolean)value).toString();
        }
        // String
        else if (String.class.isAssignableFrom( type )) {
            return (String)value;
        }
        else {
            throw new RuntimeException( "Unknown value type: " + type );
        }
    }
    

    /**
     *
     * @param encoded
     * @param targetType Optional value that specifies which type is to be used
     *        for the encode.
     */
    public static Object decode( String encoded, Class targetType ) {
        // all numbers as double
        if (Number.class.isAssignableFrom( targetType )) {
            try {
                Number result = nf.parse( encoded );
                if (Integer.class.isAssignableFrom( targetType ) ) {
                    return result.intValue();
                }
                else if (Long.class.isAssignableFrom( targetType) ) {
                    return result.longValue();
                }
                else if (Float.class.isAssignableFrom( targetType) ) {
                    return result.floatValue();
                }
                else if (Double.class.isAssignableFrom( targetType) ) {
                    return result.doubleValue();
                }
                else {
                    throw new RuntimeException( "Unhandled number type: " + targetType );
                }
            }
            catch (ParseException e) {
                throw new RuntimeException( "Error while parsing number: " + encoded, e );
            }                    
        }
        // Date
        else if (Date.class.isAssignableFrom( targetType )) {
            return new Date( Long.parseLong( encoded ) );
        }
        // Boolean
        else if (Boolean.class.isAssignableFrom( targetType )) {
            return Boolean.valueOf( encoded );
        }
        // String
        else if (String.class.isAssignableFrom( targetType )) {
            return encoded;
        }
        else {
            throw new RuntimeException( "Unknown value type: " + targetType );
        }
        
    }
    
}
