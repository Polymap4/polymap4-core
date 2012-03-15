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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;


/**
 * The basic interface all form fields must implement. 
 * <p>
 * Subclasses should provide as much event types as possible. The possible
 * event types are defined as constant of {@link IFormFieldListener}. Events
 * are send by calling {@link IFormFieldSite#fireEvent(Object, int, Object)}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormField {

    /**
     * Validation strategy constant (value <code>0</code>) indicating that
     * the editor should perform validation after every key stroke.
     *
     * @see #setValidateStrategy
     */
    public static final int VALIDATE_ON_KEY_STROKE = 0;

    /**
     * Validation strategy constant (value <code>1</code>) indicating that
     * the editor should perform validation only when the text widget 
     * loses focus.
     *
     * @see #setValidateStrategy
     */
    public static final int VALIDATE_ON_FOCUS_LOST = 1;


    public void init( IFormFieldSite site );
    
    public void dispose();
    
    public Control createControl( Composite parent, IFormEditorToolkit toolkit );


    /**
     * The value of a disabled field cannot be modified by the user. It changes its
     * visual representation to signal this to the user. However, the value can be
     * changed via {@link #setValue(Object)}. If the field is disabled when the form
     * is stored, then the fields value is not stored back.
     * 
     * @param enabled Flag that indicates the new enable state of this form field.
     * @return this
     */
    public IFormField setEnabled( boolean enabled );
    
    public void store() 
    throws Exception;
    
    public void load() 
    throws Exception;

    /**
     * Explicitly set the value of this field. This causes events to be fired
     * just like the value was typed in.
     * 
     * @return this
     */
    public IFormField setValue( Object value );
    
}
