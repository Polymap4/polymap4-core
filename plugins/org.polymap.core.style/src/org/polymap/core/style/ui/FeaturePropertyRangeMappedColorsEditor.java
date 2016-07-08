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

import java.awt.Color;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

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
import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.model.FilterMappedColors;
import org.polymap.core.ui.UIUtils;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates a number based on a feature attribute and with min and max
 * values.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyRangeMappedColorsEditor
        extends StylePropertyEditor<FilterMappedColors> {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyRangeMappedColorsEditor" );

    private Number lowerBound;

    private Number upperBound;

    private Color minimumValue;

    private Color maximumValue;

    private int steps;

    private String propertyName;


    @Override
    public String label() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( FeaturePropertyRangeMappedColorsEditor.class );


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Color.class.isAssignableFrom( targetType( site ) ) && site.featureStore.isPresent()
                && site.featureType.isPresent() ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<FilterMappedColors>() {

            @Override
            public FilterMappedColors initialize( FilterMappedColors proto ) throws Exception {
                proto.encodedFilters.clear();
                proto.colorValues.clear();
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {

        initialize();

        Composite contents = super.createContents( parent );
        final Button button = new Button( parent, SWT.BORDER);
        button.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                final FeaturePropertyRangeMappedColorsChooser cc = new FeaturePropertyRangeMappedColorsChooser(
                        propertyName, lowerBound, upperBound, minimumValue, maximumValue, steps, featureStore,
                        featureType );

                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    if (cc.propertyName() != null && cc.lowerBound() != null && cc.upperBound() != null
                            && cc.mappedMinimum() != null && cc.mappedMaximum() != null) {
                        propertyName = cc.propertyName();
                        lowerBound = cc.lowerBound();
                        upperBound = cc.upperBound();
                        maximumValue = cc.mappedMaximum();
                        minimumValue = cc.mappedMinimum();
                        boolean isInteger = cc.isInteger();
                        steps = cc.steps();

                        prop.get().encodedFilters.clear();
                        prop.get().colorValues.clear();

                        // lowerBound
                        Literal l = null;
                        if (isInteger) {
                            l = ff.literal( lowerBound.intValue() );
                        }
                        else {
                            l = ff.literal( lowerBound.doubleValue() );
                        }
                        prop.get().add( ff.lessOrEqual( ff.property( propertyName ), l ),
                                new Color( minimumValue.getRGB() ) );

                        // only linear currently
                        int singleMappedStep = (maximumValue.getRGB() - minimumValue.getRGB()) / (cc.steps() + 1);
                        double singleSrcStep = (upperBound.doubleValue() - lowerBound.doubleValue()) / cc.steps();
                        for (int i = 1; i <= cc.steps(); i++) {
                            Number literalNumber = lowerBound.doubleValue() + (singleSrcStep * i);
                            if (isInteger) {
                                l = ff.literal( literalNumber.intValue() );
                            }
                            else {
                                l = ff.literal( literalNumber.doubleValue() );
                            }
                            prop.get().add( ff.less( ff.property( propertyName ), l ),
                                    new Color( minimumValue.getRGB() + (singleMappedStep * i) ) );
                        }
                        if (isInteger) {
                            l = ff.literal( upperBound.intValue() );
                        }
                        else {
                            l = ff.literal( upperBound.doubleValue() );
                        }
                        prop.get().add( ff.greaterOrEqual( ff.property( propertyName ), l ), maximumValue );
                        prop.get().fake.set( String.valueOf( System.currentTimeMillis() ) );
                        updateButton( button );
                    }
                    return true;
                } );
            }
        } );
        updateButton( button );

        return contents;
    }


    private void initialize() {
        // lowerBound is first literal value
        // upperBound is last literal value
        List<Filter> filters = prop.get().filters();
        if (filters.size() >= 1) {
            final Filter first = filters.get( 0 );
            if (first instanceof BinaryComparisonOperator) {
                BinaryComparisonOperator piet = (BinaryComparisonOperator)first;
                if (propertyName == null) {
                    propertyName = ((PropertyName)piet.getExpression1()).getPropertyName();
                }
                String lower = (String)((Literal)piet.getExpression2()).getValue();
                try {
                    lowerBound = Integer.parseInt( lower );
                }
                catch (NumberFormatException nfe) {
                    lowerBound = Double.parseDouble( lower );
                }
            }
            final Filter last = filters.get( filters.size() - 1 );
            if (last instanceof BinaryComparisonOperator) {
                BinaryComparisonOperator piet = (BinaryComparisonOperator)last;
                String upper = (String)((Literal)piet.getExpression2()).getValue();
                try {
                    upperBound = Integer.parseInt( upper );
                }
                catch (NumberFormatException nfe) {
                    upperBound = Double.parseDouble( upper );
                }
            }
        }
        // minimumValue is first value
        // maximumValue is last value
        List<Color> values = prop.get().values();
        if (!values.isEmpty()) {
            minimumValue = values.get( 0 );
            maximumValue = values.get( values.size() - 1 );
        }

        steps = values.size() - 1;
        if (steps <= 0) {
            steps = 10;
        }
    }


    protected void updateButton( Button button ) {
        if (!StringUtils.isBlank( propertyName )) {
            button.setText( i18n.get( "chooseFrom", propertyName ) );
            // green, all ok
            button.setBackground( StylePlugin.okColor() );
//            button.setForeground( UIUtils.getColor( 74, 74, 74 ) );
        }
        else {
            button.setText( i18n.get( "choose" ) );
            // red not ok
            button.setBackground( StylePlugin.errorColor() );
        }
        button.setForeground( UIUtils.getColor( 74, 74, 74 ) );
    }
    
    @Override
    public boolean isValid() {
        return !StringUtils.isBlank( propertyName ) && lowerBound != null && upperBound != null
                && minimumValue != null && maximumValue != null && steps != 0;
    }
}
