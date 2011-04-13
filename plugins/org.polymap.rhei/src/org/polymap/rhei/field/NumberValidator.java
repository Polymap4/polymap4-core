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
 *
 * $Id$
 */
package org.polymap.rhei.field;

import java.util.Locale;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class NumberValidator
        implements IFormFieldValidator {

    private static Log log = LogFactory.getLog( NumberValidator.class );

    private NumberFormat            nf;

    private Class<? extends Number> targetClass;


    /**
     * Creates an instance with min integer/fraction digits set to 0 and max
     * integer/fraction digits set to 10.
     * 
     * @param locale The locale to use. Null indicates the the current default
     *        locale is to be used.
     */
    public NumberValidator( Class<? extends Number> targetClass, Locale locale ) {
        this( targetClass, locale, 10, 10, 0, 0 );
    }
    
    
    /**
     * Creates an instance with min integer and min fraction digits set to 0.
     * 
     * @param maxIntegerDigits
     * @param maxFractionDigits
     * @param locale The locale to use. Null indicates the the current default
     *        locale is to be used.
     */
    public NumberValidator( Class<? extends Number> targetClass, Locale locale, 
            int maxIntegerDigits, int maxFractionDigits ) {
        this( targetClass, locale, maxIntegerDigits, maxFractionDigits, 0, 0 );
    }
    
    
    /**
     * 
     * @param maxIntegerDigits
     * @param maxFractionDigits
     * @param minIntegerDigits
     * @param minFractionDigits
     * @param locale The locale to use. Null indicates the the current default
     *        locale is to be used.
     */
    public NumberValidator( Class<? extends Number> targetClass, Locale locale, 
            int maxIntegerDigits, int maxFractionDigits, 
            int minIntegerDigits, int minFractionDigits ) {
        
        this.targetClass = targetClass;
        
        nf = locale != null ? NumberFormat.getNumberInstance( locale ) : NumberFormat.getNumberInstance();
        nf.setMaximumIntegerDigits( maxIntegerDigits );
        nf.setMaximumFractionDigits( maxFractionDigits );
        nf.setMinimumIntegerDigits( minIntegerDigits );
        nf.setMinimumFractionDigits( minFractionDigits );
    }
    

    public String validate( Object fieldValue ) {
        if (fieldValue instanceof String) {
            try {
                transform2Model( fieldValue );
                log.debug( "value: " + fieldValue + " valid!" );
                return null;
            }
            catch (Exception e) {
                log.debug( "value: " + fieldValue + " INVALID!", e );
                return "Eingabe ist keine korrekte Zahlenangabe: " + fieldValue 
                        + "\nAnzahl Stellen vor dem Komma: " + nf.getMinimumIntegerDigits() + "-" + nf.getMaximumIntegerDigits()
                        + "\nAnzahl Stellen nach dem Komma: " + nf.getMinimumFractionDigits() + "-" + nf.getMaximumFractionDigits();
            }
        }
        return null;
    }

    
    public Object transform2Model( Object fieldValue )
    throws Exception {
        if (fieldValue == null) {
            return null;
        }
        else if (fieldValue instanceof String) {
            ParsePosition pp = new ParsePosition( 0 );
            Number result = nf.parse( (String)fieldValue, pp );

            if (pp.getErrorIndex() > -1 || pp.getIndex() < ((String)fieldValue).length()) {
                throw new ParseException( "field value: " + fieldValue, pp.getErrorIndex() );
            }

            log.debug( "value: " + fieldValue + " -> " + result.doubleValue() );
            
            // XXX check max digits
            
            if (Float.class.isAssignableFrom( targetClass )) {
                return Float.valueOf( result.floatValue() );
            }
            else if (Double.class.isAssignableFrom( targetClass )) {
                return Double.valueOf( result.floatValue() );
            }
            else if (Integer.class.isAssignableFrom( targetClass )) {
                return Integer.valueOf( result.intValue() );
            }
            else {
                throw new RuntimeException( "Unsupported target type: " + targetClass );
            }
        }
        else {
            throw new RuntimeException( "Unhandled field value type: " + fieldValue );
        }
    }
    
    
    public Object transform2Field( Object modelValue )
    throws Exception {
        return  modelValue == null ? null : nf.format( targetClass.cast( modelValue ) );
    }
    
}
