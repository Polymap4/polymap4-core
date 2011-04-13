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
package org.polymap.rhei.form;

import org.opengis.feature.Property;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormEditorPageSite {

    public void setFormTitle( String string );

    public Composite getPageBody();

    public IFormEditorToolkit getToolkit();

    /**
     *
     * @param parent
     * @param prop
     * @param field
     * @param validator A validator, or null if the {@link NullValidator} should be used.
     * @param label
     */
    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator, String label );
    
    /**
     *
     * @param parent
     * @param prop
     * @param field
     * @param validator A validator, or null if the {@link NullValidator} should be used.
     */
    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator );

    public void addFieldListener( IFormFieldListener listener );
    
    public void removeFieldListener( IFormFieldListener listener );

    public void setFieldValue( String fieldName, Object value );

    public void setFieldEnabled( String fieldName, boolean enabled );

}
