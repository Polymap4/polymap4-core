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

import org.eclipse.jface.action.Action;

/**
 * Provides a page for a {@link FormEditor}. The form content consists of form
 * fields that are created via
 * {@link IFormEditorPageSite#newFormField(org.eclipse.swt.widgets.Composite, org.opengis.feature.Property, org.polymap.rhei.field.IFormField, org.polymap.rhei.field.IFormFieldValidator)
 * IFormEditorPageSite.newFormField()}. If you need more control over UI
 * elements and/or submit/load, then implement the {@link IFormEditorPage2} interface.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormEditorPage {

    String getTitle();


    /**
     * An array of actions that are contributed to the toolbar of the editor of
     * this page.
     * 
     * @return The actions to be added to the toolbar of the editor of this
     *         page, or null if no actions are to be contributed.
     */
    Action[] getEditorActions();
    
    String getId();
    
    void createFormContent( IFormEditorPageSite site );

}
