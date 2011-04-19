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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class IntegerValidator
        implements IFormFieldValidator {

    private static Log log = LogFactory.getLog( IntegerValidator.class );

    
    public String validate( Object value ) {
        if (value instanceof String) {
            try {
                Integer.parseInt( (String)value );
                log.debug( "value: " + value + " valid!" );
                return null;
            }
            catch (NumberFormatException e) {
                log.debug( "value: " + value + " INVALID!" );
                return "Eingabe ist keine ganze Zahl: " + value;
            }
        }
        return null;
    }


    public Object transform2Model( Object fieldValue )
    throws Exception {
        if (fieldValue == null) {
            return null;
        }
        else if (fieldValue instanceof Integer) {
            return (Integer)fieldValue;
        }
        else if (fieldValue instanceof String) {
            return Integer.valueOf( (String)fieldValue );
        }
        else {
            throw new RuntimeException( "Unhandled field value type: " + fieldValue );
        }
    }


    public Object transform2Field( Object modelValue )
    throws Exception {
        return modelValue != null ? ((Integer)modelValue).toString() : null;
    }


}
