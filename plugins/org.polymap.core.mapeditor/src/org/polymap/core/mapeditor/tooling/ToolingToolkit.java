/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.eclipse.jface.preference.ColorSelector;

import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.UIUtils;

/**
 * Factory for UI elements used in tool panels.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ToolingToolkit
        implements IToolingToolkit {

    private static Log log = LogFactory.getLog( ToolingToolkit.class );
    
    public static final String  CUSTOM_VARIANT_VALUE = "tooling";
        
    public static final Color   textBackground = UIUtils.getColor( 0xFF, 0xFE, 0xE1 );
    public static final Color   textBackgroundDisabled = UIUtils.getColor( 0xF9, 0xF7, 0xF7 );
    public static final Color   textBackgroundFocused = UIUtils.getColor( 0xff, 0xf0, 0xd2 );
    public static final Color   backgroundFocused = UIUtils.getColor( 0xF0, 0xF0, 0xFF );
    public static final Color   labelForeground = UIUtils.getColor( 0x70, 0x70, 0x70 );
    public static final Color   labelForegroundFocused = UIUtils.getColor( 0x00, 0x00, 0x20 );

    
    @Override
    public void dispose() {
    }


    protected int stylebits( int... styles ) {
        int result = SWT.NONE;
        for (int style : styles) {
            assert style != 0;
            result |= style;
        }
        return result;
    }


    /**
     * Adapts a control to be used in a form that is associated with this toolkit.
     * This involves adjusting colors and optionally adding handlers to ensure focus
     * tracking and keyboard management.
     * 
     * @param control a control to adapt
     * @param trackFocus if <code>true</code>, form will be scrolled horizontally
     *        and/or vertically if needed to ensure that the control is visible when
     *        it gains focus. Set it to <code>false</code> if the control is not
     *        capable of gaining focus.
     * @param trackKeyboard if <code>true</code>, the control that is capable of
     *        gaining focus will be tracked for certain keys that are important to
     *        the underlying form (for example, PageUp, PageDown, ScrollUp,
     *        ScrollDown etc.). Set it to <code>false</code> if the control is not
     *        capable of gaining focus or these particular key event are already used
     *        by the control.
     */
    public <T extends Control> T adapt( T control, boolean trackFocus, boolean trackKeyboard) {
        UIUtils.setVariant( control, CUSTOM_VARIANT_VALUE );
        return control;
    }
    
    protected Composite adapt( Composite composite ) {
        UIUtils.setVariant( composite, CUSTOM_VARIANT_VALUE );
        return composite;
    }
    
    @Override
    public Label createLabel( Composite parent, String text, int... styles ) {
        Label control = adapt( new Label( parent, stylebits( styles ) ), false, false );
        if (text != null) {
            control.setText( text );
        }
        return control;
    }
    
    @Override
    public CCombo createCombo( Composite parent, Iterable<String> values, int... styles ) {
        CCombo control = adapt( new CCombo( parent, stylebits( styles ) | SWT.BORDER ), false, false );
//        combo.setBackground( textBackground );
        control.setVisibleItemCount( 12 );
        for (String value : values) {
            control.add( value );
        }
        return control;
    }
    
    @Override
    public CCombo createCombo( Composite parent, String[] values, int... styles ) {
        return createCombo( parent, Lists.newArrayList( values ), styles );
    }


    @Override
    public Button createButton( Composite parent, String text, int... styles ) {
        Button control = adapt( new Button( parent, stylebits( styles ) ), true, true );
        if (text != null) {
            control.setText( text );
        }
        return control;
    }


    @Override
    public Spinner createSpinner( Composite parent, int... styles ) {
        Spinner control = adapt( new Spinner( parent, stylebits( styles ) | SWT.BORDER ), true, true );
        return control;
    }


    @Override
    public ColorSelector createColorSelector( Composite parent ) {
        ColorSelector cs = new ColorSelector( parent );
        adapt( cs.getButton(), true, true );
        return cs;
    }


    @Override
    public Composite createComposite( Composite parent, int... styles ) {
        Composite result = adapt( new Composite( parent, stylebits( styles ) ) );
        result.setLayout( ColumnLayoutFactory.defaults().margins( 3, 3 ).spacing( 3 ).create() );
        return result;
    }

}
