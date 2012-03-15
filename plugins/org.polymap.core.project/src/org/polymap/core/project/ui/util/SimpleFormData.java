/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
 */
package org.polymap.core.project.ui.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

/**
 * Factory for {@link FormData} instances with simplified API.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleFormData {

    private static Log log = LogFactory.getLog( SimpleFormData.class );

    private FormData        formData;

    private int             defaultOffset;


    public SimpleFormData() {
        this( 0 );
    }

    public SimpleFormData( FormData other ) {
        this( 0 );
        formData.bottom = other.bottom;
        formData.top = other.top;
        formData.left = other.left;
        formData.right = other.right;
        formData.width = other.width;
        formData.height = other.height;
    }

    public SimpleFormData( int defaultOffset ) {
        this.formData = new FormData();
        this.defaultOffset = defaultOffset;
    }

    /**
     * Equivalent of calling:
     * <code>left( 0 ).top( 0 ).right( 100 ).bottom( 100 )</code>
     */
    public SimpleFormData fill() {
        return left( 0 ).top( 0 ).right( 100 ).bottom( 100 );
    }

    public FormData create() {
        return formData;
    }

    public SimpleFormData height( int height ) {
        formData.height = height;
        return this;
    }
    
    // left

    public SimpleFormData left( int num ) {
        return left( num, defaultOffset );
    }

    public SimpleFormData left( int num, int offset ) {
        formData.left = new FormAttachment( num, offset );
        return this;
    }

    public SimpleFormData left( Control control ) {
        formData.left = new FormAttachment( control, defaultOffset );
        return this;
    }

    public SimpleFormData left( Control control, int offset ) {
        formData.left = new FormAttachment( control, offset );
        return this;
    }

    // right

    public SimpleFormData right( int num ) {
        return right( num, -defaultOffset );
    }

    public SimpleFormData right( int num, int offset ) {
        formData.right = new FormAttachment( num, offset );
        return this;
    }

    public SimpleFormData right( Control control ) {
        formData.right = new FormAttachment( control, -defaultOffset );
        return this;
    }

    public SimpleFormData right( Control control, int offset ) {
        formData.right = new FormAttachment( control, offset );
        return this;
    }

    // top

    public SimpleFormData top( int num ) {
        return top( num, defaultOffset );
    }

    public SimpleFormData top( int num, int offset ) {
        formData.top = new FormAttachment( num, offset );
        return this;
    }

    public SimpleFormData top( Control control ) {
        formData.top = new FormAttachment( control, defaultOffset );
        return this;
    }

    public SimpleFormData top( Control control, int offset ) {
        formData.top = new FormAttachment( control, offset );
        return this;
    }

    // bottom

    public SimpleFormData bottom( int num ) {
        return bottom( num, -defaultOffset );
    }

    public SimpleFormData bottom( int num, int offset ) {
        formData.bottom = new FormAttachment( num, offset );
        return this;
    }

    public SimpleFormData bottom( Control control ) {
        formData.bottom = new FormAttachment( control, -defaultOffset );
        return this;
    }

    public SimpleFormData bottom( Control control, int offset ) {
        formData.bottom = new FormAttachment( control, offset );
        return this;
    }

}
