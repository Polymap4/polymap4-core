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
package org.polymap.core.style.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ExpressionMappedNumbers;
import org.polymap.core.style.model.NumberRange;
import org.polymap.core.style.model.PropertyNumber;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates a number based on a feature attribute and with min and max
 * values.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyMappedNumbersEditor
        extends StylePropertyEditor<ExpressionMappedNumbers<Double>> {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyMappedNumbersEditor" );


    @Override
    public String label() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( FeaturePropertyMappedNumbersEditor.class );


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Number.class.isAssignableFrom( targetType( site ) ) && site.featureType.isPresent() ? super.init( site )
                : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ExpressionMappedNumbers<Double>>() {

            @Override
            public ExpressionMappedNumbers<Double> initialize( ExpressionMappedNumbers<Double> proto ) throws Exception {
                proto.propertyName.set( "" );
                NumberRange ad = (NumberRange)prop.info().getAnnotation( NumberRange.class );
                proto.minimumValue.set( ad.from() );
                proto.maximumValue.set( ad.to() );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        final Button button = new Button( parent, SWT.PUSH );
        button.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                FeaturePropertyMappedNumbersChooser cc = new FeaturePropertyMappedNumbersChooser(
                        (String)prop.get().propertyName.get(), null, null, (Double)prop.get().minimumValue.get(),
                        (Double)prop.get().maximumValue.get(),
                        (NumberRange)prop.info().getAnnotation( NumberRange.class ), featureStore, featureType );
                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    if (cc.propertyName() != null) {
                        prop.get().propertyName.set( cc.propertyName() );
                        // prop.get().setDefaultColor( cc.defaultColor() );
                        prop.get().minimumValue.set( cc.minimum() );
                        prop.get().maximumValue.set( cc.maximum() );
                        updateButton( button );
                    }
                    return true;
                } );
            }
        } );

        return contents;
    }


    protected void updateButton( Button button ) {
        if (!StringUtils.isBlank( (String)prop.get().propertyName.get() )) {
            if (prop.get().minimumValue.get() != null && prop.get().maximumValue.get() != null) {
                button.setText( i18n.get( "chooseBetween", prop.get().propertyName.get(), prop.get().minimumValue.get(),
                        prop.get().maximumValue.get() ) );
            }
            else {
                button.setText( i18n.get( "chooseFrom", prop.get().propertyName.get(), prop.get().minimumValue.get(),
                        prop.get().maximumValue.get() ) );
            }
        }
        else {
            button.setText( i18n.get( "choose" ) );
        }
    }
}
