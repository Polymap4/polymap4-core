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
package org.polymap.rhei.field;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * The basic interface of all form field labels. 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version ($Revision$)
 */
public interface IFormFieldLabel {
    
    /** 
     * Signals that no label should be displayed and no space is to be reserved. 
     */
    public static final String  NO_LABEL = "_nolabel_";
    
    public void init( IFormFieldSite site );

    public void dispose();
    
    public Control createControl( Composite parent, IFormEditorToolkit toolkit );
    
    public int getMaxWidth();
    
}
