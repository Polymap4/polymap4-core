/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.NumberRange;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantNumber}.
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class ConstantNumberEditor
        extends StylePropertyEditor<ConstantNumber> {

    private static final IMessages i18n = Messages.forPrefix( "ConstantNumber" );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Number.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
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
        NumberRange nd = (NumberRange)prop.info().getAnnotation( NumberRange.class );
        if (nd != null) {
            int digits = nd.digits();
            double currentValue = (double)prop.get().constantNumber.get();
            double factorX = Math.pow( 10, digits );
            s.setDigits( digits );
            s.setMinimum( (int)(nd.from() * factorX) );
            s.setMaximum( (int)(nd.to() * factorX) );
            s.setIncrement( (int)(nd.increment() * factorX) );
            s.setPageIncrement( (int)(nd.increment() * factorX * 10) );
            s.setSelection( (int)(currentValue * factorX) );
        }
        s.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = s.getSelection();
                int digits = s.getDigits();
                prop.get().constantNumber.set( selection / Math.pow( 10, digits ) );
            }
        } );
        return contents;
    }
}
