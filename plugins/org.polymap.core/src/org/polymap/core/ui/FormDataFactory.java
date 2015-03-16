/*
 * polymap.org
 * Copyright 2011-2013, Falko Bräutigam. All rights reserved.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Control;

/**
 * Factory for {@link FormData} instances with simplified API.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormDataFactory {

    private static Log log = LogFactory.getLog( FormDataFactory.class );

    public enum Alignment {
        /** the side will be attached to the top side of the specified control */
        TOP,
        /** the side will be attached to the bottom side of the specified control */
        BOTTOM,
        /** the side will be attached to the left side of the specified control */
        LEFT,
        /** the side will be attached to the right side of the specified control */
        RIGHT,
        /** the side will be centered on the same side of the specified control */
        CENTER
    }

    // static factories ***********************************
    
    /**
     * Equivalent of calling <code>new FormDataFactory().fill()</code>
     */
    public static FormDataFactory filled() {
        return new FormDataFactory().fill();
    }
    
    /**
     * Equivalent of calling <code>new FormDataFactory()</code> which produces a
     * {@link FormLayout} with no attachments set.
     */
    public static FormDataFactory defaults() {
        return new FormDataFactory();
    }
    
    
    /**
     * Equivalent of calling <code>new FormDataFactory(defaultOffset)</code> which
     * produces a default {@link FormLayout} and sets the default offset for this
     * factory, effectivly setting extra margins.
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

    public <T extends Control> T applyTo( T control ) {
        control.setLayoutData( create() );
        return control;
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
    
    /**
     * Clears the seetings for the left side of the control. 
     */
    public FormDataFactory clearLeft() {
        formData.left = null;
        return this;
    }
    
    public FormDataFactory clearRight() {
        formData.right = null; 
        return this;
    }
    
    public FormDataFactory clearTop() {
        formData.top = null; 
        return this;
    }
    
    public FormDataFactory clearBottom() {
        formData.bottom = null; 
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
        return left( control, defaultOffset );
    }

    public FormDataFactory left( Control control, int offset ) {
        formData.left = control != null ? new FormAttachment( control, offset ) : null;
        return this;
    }

    /**
     * See {@link FormAttachment#FormAttachment(Control, int, int)}.
     */
    public FormDataFactory left( Control control, int offset, Alignment align ) {
        formData.left = control != null ? new FormAttachment( control, offset, alignment( align ) ) : null;
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
        return right( control, -defaultOffset );
    }

    public FormDataFactory right( Control control, int offset ) {
        formData.right = control != null ? new FormAttachment( control, offset ) : null;
        return this;
    }

    public FormDataFactory right( Control control, int offset, Alignment align ) {
        formData.right = control != null ? new FormAttachment( control, offset, alignment( align ) ) : null;
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
        return top( control, defaultOffset );
    }

    public FormDataFactory top( Control control, int offset ) {
        formData.top = control != null ? new FormAttachment( control, offset ) : null;
        return this;
    }

    public FormDataFactory top( Control control, int offset, Alignment align ) {
        formData.top = control != null ? new FormAttachment( control, offset, alignment( align ) ) : null;
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
        return bottom( control, -defaultOffset );
    }

    public FormDataFactory bottom( Control control, int offset ) {
        formData.bottom = control != null ? new FormAttachment( control, offset ) : null;
        return this;
    }

    public FormDataFactory bottom( Control control, int offset, Alignment align ) {
        formData.bottom = control != null ? new FormAttachment( control, offset, alignment( align ) ) : null;
        return this;
    }

    // alignment
    
    protected int alignment( Alignment align ) {
        switch (align) {
            case TOP : return SWT.TOP;
            case BOTTOM : return SWT.BOTTOM;
            case LEFT : return SWT.LEFT;
            case RIGHT : return SWT.RIGHT;
            case CENTER : return SWT.CENTER;
            default : throw new RuntimeException( "Unknown alignment!? Should never happen." );
        }
    }

}
