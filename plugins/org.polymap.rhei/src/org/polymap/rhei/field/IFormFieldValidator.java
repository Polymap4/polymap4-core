/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 * $Id: $
 */
package org.polymap.rhei.field;

/**
 * A validator bridges the gap between {@link IFormField} and the data model. It
 * checks if a given user input is valid and it performs transformations from
 * the type of the value that comes from the user interface into the type of the
 * underlying data model and vice versa. For example, there are several
 * possibilities to request a float value from the user. A
 * {@link StringFormField} can be used or an {@link PicklistFormField}. A
 * {@link NumberValidator} can be combined with a string field in order to check
 * user input and transform the string to a float value.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormFieldValidator {

    public static final Object          INVALID = new Object();


    /**
     * Check the given user provided value for validity.
     * 
     * @param value
     * @return Null if the value is valid, or the error message if the value is
     *         invalid.
     */
    public String validate( Object fieldValue );
    
    /**
     * Transforms the given user input value to model value.
     * 
     * @param fieldValue The user input value to transform, might be null.
     * @return Transformed value, or null if fieldValue is null.
     * @throws Exception
     */
    public Object transform2Model( Object fieldValue )
    throws Exception;
    
    public Object transform2Field( Object modelValue )
    throws Exception;
    
}
