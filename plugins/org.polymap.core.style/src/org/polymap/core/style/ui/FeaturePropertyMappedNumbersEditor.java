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

import java.util.List;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

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
            public ExpressionMappedNumbers<Double> initialize( ExpressionMappedNumbers<Double> proto )
                    throws Exception {
                proto.propertyName.set( "" );
                proto.expressions.clear();
                proto.defaultNumberValue.set( null );
                proto.numberValues.clear();
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        // lowerBound is first literal value
        // upperBound is last literal value
        Double lowerBound = null;
        Double upperBound = null;
        List<Expression> expressions = prop.get().expressions();
        if (!expressions.isEmpty()) {
            Expression exp = expressions.get( 0 );
            if (exp instanceof Literal) {
                lowerBound = (Double)((Literal)exp).getValue();
            }
            exp = expressions.get( expressions.size() - 1 );
            if (exp instanceof Literal) {
                upperBound = (Double)((Literal)exp).getValue();
            }
        }
        // minimumValue is first value
        // maximumValue is last value
        Double minimumValue = null;
        Double maximumValue = null;
        List<Double> values = prop.get().values();
        if (!values.isEmpty()) {
            minimumValue = values.get( 0 );
            maximumValue = values.get( values.size() - 1 );
        }

        Integer steps = values.size();

        final FeaturePropertyMappedNumbersChooser cc = new FeaturePropertyMappedNumbersChooser(
                (String)prop.get().propertyName.get(), lowerBound, upperBound, minimumValue, maximumValue, steps,
                (NumberRange)prop.info().getAnnotation( NumberRange.class ), featureStore, featureType );

        Composite contents = super.createContents( parent );
        final Button button = new Button( parent, SWT.PUSH );
        button.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {

                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    if (cc.propertyName() != null) {
                        prop.get().propertyName.set( cc.propertyName() );
                        prop.get().expressions.clear();
                        prop.get().numberValues.clear();
                        
                        prop.get().add( ff.function( "lessThan", ff.property( cc.propertyName() ), ff.literal( cc.lowerBound() )), cc.mappedMinimum() );
//                        prop.get().add( ff.lessOrEqual( ff.property( cc.propertyName() ), ff.literal( cc.lupperBound() ), cc.minimum() ));
                        prop.get().add( ff.function( "greaterEqualThan", ff.property( cc.propertyName() ), ff.literal( cc.upperBound() )), cc.mappedMaximum() );
                        updateButton( cc, button );
                    }
                    return true;
                } );
            }
        } );
        updateButton( cc, button );

        return contents;
    }


    protected void updateButton( FeaturePropertyMappedNumbersChooser cc, Button button ) {
        if (!StringUtils.isBlank( cc.propertyName() )) {
            if (cc.mappedMinimum() != null && cc.mappedMaximum() != null) {
                button.setText( i18n.get( "chooseBetween", cc.propertyName(), cc.mappedMinimum(), cc.mappedMaximum() ) );
            }
            else {
                button.setText( i18n.get( "chooseFrom", cc.propertyName(), cc.mappedMinimum(), cc.mappedMaximum() ) );
            }
        }
        else {
            button.setText( i18n.get( "choose" ) );
        }
    }
}
