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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Store;

/**
 * Encode/decode primitive types into/from string representation that can be
 * stored and searched in the Lucene index.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
class ValueCoder {
    
    static final int                PRECISION_STEP_32 = 4;
    static final int                PRECISION_STEP_64 = 8;
    
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
     * @param name 
     * @param targetType Optional value that specifies which type is to be used
     *        for the encode.
     * @param store 
     * @param index
     */
    public static Fieldable encode( String name, Object value, Class targetType, Store store, boolean index ) {
        if (value == null) {
            return null;
        }
        
        Class type = targetType != null ? targetType : value.getClass();
        
        // all numbers as double
        if (Number.class.isAssignableFrom( type )) {
            NumericField result = null;
            
            if (Integer.class.isAssignableFrom( type )) {
                result = new NumericField( name, PRECISION_STEP_32, store, index );
                result.setIntValue( (Integer)value );
            }
            else if (Double.class.isAssignableFrom( type )) {
                result = new NumericField( name, PRECISION_STEP_64, store, index );
                result.setDoubleValue( (Double)value );
            }
            else if (Float.class.isAssignableFrom( type )) {
                result = new NumericField( name, PRECISION_STEP_32, store, index );
                result.setFloatValue( (Float)value );
            }
            else if (Long.class.isAssignableFrom( type )) {
                result = new NumericField( name, PRECISION_STEP_64, store, index );
                result.setLongValue( (Long)value );
            }
            else {
                result = new NumericField( name, PRECISION_STEP_64, store, index );
                result.setDoubleValue( ((Number)value).doubleValue() );
            }
            return result;
        }
        // Date
        else if (Date.class.isAssignableFrom( type )) {
            NumericField result = new NumericField( name, PRECISION_STEP_64, store, index );
            result.setLongValue( ((Date)value).getTime() );
            return result;
        }
        // Boolean
        else if (Boolean.class.isAssignableFrom( type )) {
            return new Field( name, ((Boolean)value).booleanValue() ? "1" : "0", 
                    store, index ? Field.Index.NOT_ANALYZED : Field.Index.NO );
        }
        // String
        else if (String.class.isAssignableFrom( type )) {
            return new Field( name, (String)value, store, index ? Field.Index.NOT_ANALYZED : Field.Index.NO );
        }
        else {
            throw new RuntimeException( "Unknown value type: " + type );
        }
    }
    

    /**
     *
     * @param field
     * @param targetType Optional value that specifies which type is to be used
     *        for the encode.
     */
    public static Object decode( Fieldable field, Class targetType ) {
        try {
            // Number
            if (Number.class.isAssignableFrom( targetType )) {
                if (Integer.class.isAssignableFrom( targetType ) ) {
                    return new Integer( field.stringValue() );
                }
                else if (Long.class.isAssignableFrom( targetType) ) {
                    return new Long( field.stringValue() );
                }
                else if (Float.class.isAssignableFrom( targetType) ) {
                    return new Float( field.stringValue() );
                }
                else if (Double.class.isAssignableFrom( targetType) ) {
                    return new Double( field.stringValue() );
                }
                else {
                    throw new RuntimeException( "Unhandled number type: " + targetType );
                }
            }
            // Date
            else if (Date.class.isAssignableFrom( targetType )) {
                return new Date( Long.parseLong( field.stringValue() ) );
            }
            // Boolean
            else if (Boolean.class.isAssignableFrom( targetType )) {
                return field.stringValue().equals( "0" ) ? Boolean.FALSE : Boolean.TRUE;
            }
            // String
            else if (String.class.isAssignableFrom( targetType )) {
                return field.stringValue();
            }
            else {
                throw new RuntimeException( "Unknown value type: " + targetType );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( "Error while decoding field: " + field, e );
        }
    }
    
}
