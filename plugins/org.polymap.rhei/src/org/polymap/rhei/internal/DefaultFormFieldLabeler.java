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
package org.polymap.rhei.internal;

import org.apache.commons.lang.StringUtils;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultFormFieldLabeler
        implements IFormFieldLabel {

    private IFormFieldSite      site;
    
    private String              label;
    
    private int                 maxWidth = 100;
    
    
    /**
     * Use the field name as label. 
     */
    public DefaultFormFieldLabeler() {
    }

    public DefaultFormFieldLabeler( String label ) {
        if (label != null && label.equals( NO_LABEL )) {
            this.label = label;
            this.maxWidth = 0;
        }
        else {
            this.label = label;
        }
    }

    public void init( IFormFieldSite _site ) {
        this.site = _site;    
    }

    public void dispose() {
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        return toolkit.createLabel( parent, label != null
                ? label
                : StringUtils.capitalize( site.getFieldName() ) );
    }
    
    public void setMaxWidth( int maxWidth ) {
        this.maxWidth = maxWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

}
