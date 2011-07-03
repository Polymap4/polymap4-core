/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BetweenValidator
        implements IFormFieldValidator {

    private static Log log = LogFactory.getLog( BetweenValidator.class );

    private IFormFieldValidator     delegate;
    

    public BetweenValidator( IFormFieldValidator delegate ) {
        this.delegate = delegate;
    }


    public Object transform2Field( Object modelValue )
    throws Exception {
        return modelValue;
    }


    public Object transform2Model( Object fieldValue )
    throws Exception {
        Object[] values = (Object[])fieldValue;
        return values;
    }


    public String validate( Object fieldValue ) {
        if (fieldValue != null) {
            Object[] values = (Object[])fieldValue;
            String result = delegate.validate( values[0] );
            if (result != null) {
                return result;
            }
            result = delegate.validate( values[1] );
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
