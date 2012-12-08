/*
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
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
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version ($Revision$)
 */
public interface IFormEditorPageSite {

    public void setFormTitle( String string );

    public void setActivePage( String pageId );

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

    /**
     *
     * @param source XXX
     * @param eventCode One of the constants in {@link IFormFieldListener}.
     * @param newValue
     */
    public void fireEvent( Object source, String fieldName, int eventCode, Object newValue );

    public void setFieldValue( String fieldName, Object value );

    public void setFieldEnabled( String fieldName, boolean enabled );

    /**
     * Reload all field from the backend.
     */
    public void reloadEditor()
    throws Exception;
    
    /**
     * True if any field of the page is dirty and/or if {@link IFormEditorPage2}
     * has reported that it is dirty.
     *
     * @return True if the page has unsaved changes.
     */
    public boolean isDirty();

}
