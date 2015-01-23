/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.ui;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Factory of {@link FormLayout} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormLayoutFactory {

    private static Log log = LogFactory.getLog( FormLayoutFactory.class );
    
    /**
     * Creates a new factory with every field set to <code>0</code>.
     * 
     * @see FormLayout#FormLayout()
     * @return Newly created factory instance.
     */
    public static FormLayoutFactory defaults() {
        return new FormLayoutFactory();
    }
    
    
    // instance *******************************************
    
    private FormLayout          layout;
    
    public FormLayoutFactory() {
        layout = new FormLayout();
    }
    
    public <T extends Composite> T applyTo( T composite ) {
        composite.setLayout( create() );
        return composite;
    }
    
    public FormLayoutFactory margins( int top, int right, int bottom, int left ) {
        layout.marginBottom = bottom;
        layout.marginLeft = left;
        layout.marginTop = top;
        layout.marginRight = right;
        return this;
    }
    
    public FormLayoutFactory margins( int width, int height ) {
        layout.marginWidth = width;
        layout.marginHeight = height;
        return this;
    }

    public FormLayoutFactory margins( int widthAndHeight ) {
        layout.marginWidth = widthAndHeight;
        layout.marginHeight = widthAndHeight;
        return this;
    }

    public FormLayoutFactory spacing( int spacing ) {
        layout.spacing = spacing;
        return this;
    }
    
    public FormLayout create() {
        return layout;
    }
    
}
