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

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * The primary interface between a form field and the {@link FormEditor}.
 * <p>
 * A {@link FormEditor} exposes its implemention of the interface via this
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormFieldSite {

    public String getFieldName();

//    public Class getFieldType();

    /**
     * Returns the current value of this field from the backend store. The value
     * is transformed via {@link IFormFieldValidator#transform2Field(Object)} in
     * order to represent the value type corresponding to the particular
     * {@link IFormField}.So this method can be used to initialize or
     * {@link IFormField#load()} the state of the UI.
     * 
     * @throws Exception When the value could not be validated/transformed with
     *         the {@link IFormFieldValidator} of this field.
     */
    public Object getFieldValue()
    throws Exception;


    /**
     * Changes the value of this field as the result of a submit action. The
     * value is transformed via
     * {@link IFormFieldValidator#transform2Model(Object)}. This method should
     * be called by {@link IFormField#store()}.
     * 
     * @throws Exception When the value could not be validated/transformed with
     *         the {@link IFormFieldValidator} of this field.
     */
    public void setFieldValue( Object value )
    throws Exception;
    
    public boolean isValid();
    
    public boolean isDirty();
    
    public void addChangeListener( IFormFieldListener l );
    
    public void removeChangeListener( IFormFieldListener l );
    
    /**
     *
     * @param source XXX
     * @param eventCode One of the constants in {@link IFormFieldListener}.
     * @param newValue
     */
    public void fireEvent( Object source, int eventCode, Object newValue ); 
    
    public IFormEditorToolkit getToolkit();
    
    /**
     *
     * @return The current error message, or null if no error.
     */
    public String getErrorMessage();

    public void setErrorMessage( String msg );
    
}
