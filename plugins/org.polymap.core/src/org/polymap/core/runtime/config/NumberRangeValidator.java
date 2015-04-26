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
package org.polymap.core.runtime.config;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NumberRangeValidator
        extends PropertyValidator<Number> {

    public static final NumberFormat    nf = NumberFormat.getInstance();
    
    @Override
    public boolean test( Number value ) {
        try {
            if (args.length != 2) {
                throw new RuntimeException( getClass().getSimpleName() + ": 2 arguments are expected" );
            }
            Double arg1 = nf.parse( args[0] ).doubleValue();
            Double arg2 = nf.parse( args[1] ).doubleValue();
            
            value = value.doubleValue();
            
            if (((Comparable)arg1).compareTo( arg2 ) > 0) {
                throw new RuntimeException( getClass().getSimpleName() + ": value of arg1 must be smaller than value of arg2" );
            }
            
            return ((Comparable)value).compareTo( arg1 ) >= 0 
                    && ((Comparable)value).compareTo( arg2 ) <= 0;
        }
        catch (ParseException e) {
            throw new RuntimeException( e );
        }
    }
    
}
