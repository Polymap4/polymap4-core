/* 
 * polymap.org
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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
package org.polymap.core.style.ui.feature;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.runtime.Numbers;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.NumberRange;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.UIUtils;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantNumber}.
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class ConstantNumberEditor
        extends StylePropertyEditor<ConstantNumber> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantNumberEditor", "ConstantEditor" );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Number.class.isAssignableFrom( targetType( site ) ) && super.init( site );
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantNumber>() {
            @Override
            public ConstantNumber initialize( ConstantNumber proto ) throws Exception {
                NumberRange ad = (NumberRange)prop.info().getAnnotation( NumberRange.class );
                if (ad != null) {
                    proto.constantNumber.set( ad.defaultValue() );
                }
                else {
                    throw new RuntimeException( "No Number value range defined for: " + prop );
                }
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Spinner s = new Spinner( contents, SWT.BORDER );
        NumberRange a = (NumberRange)prop.info().getAnnotation( NumberRange.class );
        if (a != null) {
            int digits = a.digits();
            double currentValue = Numbers.cast( (Number)prop.get().constantNumber.get(), Double.class );
            double digitsFactor = Math.pow( 10, digits );
            s.setDigits( digits );
            s.setMinimum( (int)(a.from() * digitsFactor) );
            s.setMaximum( (int)(a.to() * digitsFactor) );
            s.setIncrement( (int)(a.increment() * digitsFactor) );
            s.setPageIncrement( (int)(a.increment() * digitsFactor * 10) );
            s.setSelection( (int)(currentValue * digitsFactor) );
        }
        s.addSelectionListener( UIUtils.selectionListener( ev -> {
            int sel = s.getSelection();
            int digits = s.getDigits();
            Double round = Numbers.roundToDigits( sel / Math.pow( 10, digits ), digits );
            prop.get().constantNumber.set( Numbers.cast( round, site().targetType() ) );
        }));
        return contents;
    }
}
