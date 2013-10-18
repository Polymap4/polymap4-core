/*
 * polymap.org
 * Copyright 2011-2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Factory for {@link FormData} instances with simplified API.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class FormDataFactory {

    private static Log log = LogFactory.getLog( FormDataFactory.class );

    // static factories ***********************************
    
    /**
     * Equivalent of calling <code>new SimpleFormData().fill()</code>
     */
    public static FormDataFactory filled() {
        return new FormDataFactory().fill();
    }
    
    /**
     * Equivalent of calling <code>new SimpleFormData().fill()</code>
     */
    public static FormDataFactory defaults() {
        return new FormDataFactory().fill();
    }
    
    /**
     * Equivalent of calling <code>new SimpleFormData(defaultOffset)</code>
     */
    public static FormDataFactory offset( int defaultOffset) {
        return new FormDataFactory( defaultOffset );
    }
    
    
    // instance *******************************************
    
    private FormData        formData;

    private int             defaultOffset;


    /**
     * Constructs a new instance with defaultOffset 0.
     */
    public FormDataFactory() {
        this( 0 );
    }

    public FormDataFactory( FormData other ) {
        this( 0 );
        formData.bottom = other.bottom;
        formData.top = other.top;
        formData.left = other.left;
        formData.right = other.right;
        formData.width = other.width;
        formData.height = other.height;
    }

    public FormDataFactory( int defaultOffset ) {
        this.formData = new FormData();
        this.defaultOffset = defaultOffset;
    }

    public <T extends Composite> T applyTo( T composite ) {
        composite.setLayoutData( create() );
        return composite;
    }
    
    /**
     * Equivalent of calling:
     * <code>left( 0 ).top( 0 ).right( 100 ).bottom( 100 )</code>
     */
    public FormDataFactory fill() {
        return left( 0 ).top( 0 ).right( 100 ).bottom( 100 );
    }

    public FormData create() {
        return formData;
    }

    public FormDataFactory height( int height ) {
        formData.height = height;
        return this;
    }
    
    public FormDataFactory width( int width ) {
        formData.width = width;
        return this;
    }
    
    // left

    public FormDataFactory left( int num ) {
        return left( num, defaultOffset );
    }

    public FormDataFactory left( int num, int offset ) {
        formData.left = num > -1 ? new FormAttachment( num, offset ) : null;
        return this;
    }

    public FormDataFactory left( Control control ) {
        formData.left = new FormAttachment( control, defaultOffset );
        return this;
    }

    public FormDataFactory left( Control control, int offset ) {
        formData.left = new FormAttachment( control, offset );
        return this;
    }

    // right

    public FormDataFactory right( int num ) {
        return right( num, -defaultOffset );
    }

    public FormDataFactory right( int num, int offset ) {
        formData.right = num > -1 ? new FormAttachment( num, offset ) : null;
        return this;
    }

    public FormDataFactory right( Control control ) {
        formData.right = new FormAttachment( control, -defaultOffset );
        return this;
    }

    public FormDataFactory right( Control control, int offset ) {
        formData.right = new FormAttachment( control, offset );
        return this;
    }

    // top

    public FormDataFactory top( int num ) {
        return top( num, defaultOffset );
    }

    public FormDataFactory top( int num, int offset ) {
        formData.top = num != -1 ? new FormAttachment( num, offset ) : null;
        return this;
    }

    public FormDataFactory top( Control control ) {
        formData.top = new FormAttachment( control, defaultOffset );
        return this;
    }

    public FormDataFactory top( Control control, int offset ) {
        formData.top = new FormAttachment( control, offset );
        return this;
    }

    // bottom

    public FormDataFactory bottom( int num ) {
        return bottom( num, -defaultOffset );
    }

    public FormDataFactory bottom( int num, int offset ) {
        formData.bottom = num != -1 ? new FormAttachment( num, offset ) : null;
        return this;
    }

    public FormDataFactory bottom( Control control ) {
        formData.bottom = new FormAttachment( control, -defaultOffset );
        return this;
    }

    public FormDataFactory bottom( Control control, int offset ) {
        formData.bottom = new FormAttachment( control, offset );
        return this;
    }

}
