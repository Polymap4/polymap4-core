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
package org.polymap.rhei.filter;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * The interface that {@link FilterEditor} provides its subclasses. 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFilterEditorSite {

    Composite getPageBody();

    IFormEditorToolkit getToolkit();

    Composite newFormField( Composite parent, String propName, Class propType, IFormField field, IFormFieldValidator validator );

    Composite newFormField( Composite parent, String propName, Class propType, IFormField field, IFormFieldValidator validator, String label );

    Object getFieldValue( String propAntragsart );

    Composite createStandardLayout( Composite parent );

    void addStandardLayout( Composite composite );

}
