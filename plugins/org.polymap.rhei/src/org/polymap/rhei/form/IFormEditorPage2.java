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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.field.IFormField;

/**
 * This interface provides a way to explicitly handle load/store of the page. So
 * custom controls can be loaded/stored. If the page registeres
 * {@link IFormField}s then these fields are automatically loaded/stored.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormEditorPage2
        extends IFormEditorPage {

    boolean isDirty();
    
    boolean isValid();
    
    void doLoad( IProgressMonitor monitor ) 
    throws Exception;
    
    void doSubmit( IProgressMonitor monitor )
    throws Exception;
    
    /**
     * Dispose any resource this page may have aquired in {@link #createFormContent(IFormEditorPageSite)}.
     * Form fields that were created via {@link IFormEditorPageSite#newFormField(org.eclipse.swt.widgets.Composite, org.opengis.feature.Property, org.polymap.rhei.field.IFormField, org.polymap.rhei.field.IFormFieldValidator)}
     * are automatically disposed and must not be disposed in this method.
     */
    void dispose();

}
