/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 */
package org.polymap.rhei.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * Provides a default layout for classes that implement {@link IFormEditorPage}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class DefaultFormPageLayouter {

    private static Log log = LogFactory.getLog( DefaultFormPageLayouter.class );

    public static final int         DEFAULT_FIELD_SPACING_H = 3;
    public static final int         DEFAULT_FIELD_SPACING_V = 1;
    public static final int         DEFAULT_SECTION_SPACING = 6;

    private Composite               lastLayoutElm = null;


    public Layout newLayout() {
        if (lastLayoutElm != null) {
            // close last element of the previous section
            ((FormData)lastLayoutElm.getLayoutData()).bottom = new FormAttachment( 100, -2 );
        }
        lastLayoutElm = null;
        return new FormLayout();
    }


    public Composite setFieldLayoutData( Composite field, int height ) {
        setFieldLayoutData( field );
        ((FormData)field.getLayoutData()).height = height;
        return field;
    }

    public Composite setFieldLayoutData( Composite field ) {
        assert field.getParent().getLayout() instanceof FormLayout;

        // defines the minimum width of the entire form before horiz. scrollbar starts to appear
        FormData layoutData = new FormData( 40, SWT.DEFAULT );
        layoutData.left = new FormAttachment( 0, DEFAULT_FIELD_SPACING_H );
        layoutData.right = new FormAttachment( 100, -DEFAULT_FIELD_SPACING_H );
        layoutData.top = lastLayoutElm != null
                ? new FormAttachment( lastLayoutElm, DEFAULT_FIELD_SPACING_V )
                : new FormAttachment( 0 );
        field.setLayoutData( layoutData );

        lastLayoutElm = field;
        return field;
    }

}
