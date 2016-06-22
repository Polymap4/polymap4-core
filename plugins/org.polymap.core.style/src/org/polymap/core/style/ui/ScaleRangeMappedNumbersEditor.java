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

import java.text.DecimalFormat;

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
import org.polymap.core.style.model.NumberRange;
import org.polymap.core.style.model.ScaleMappedNumbers;
import org.polymap.core.ui.UIUtils;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates a number based on a feature attribute and with min and max
 * values.
 *
 * @author Steffen Stundzig
 */
public class ScaleRangeMappedNumbersEditor
        extends StylePropertyEditor<ScaleMappedNumbers> {

    private static final IMessages i18n = Messages.forPrefix( "ScaleRangeMappedNumbersEditor" );

    private Integer lowerBound;

    private Integer upperBound;

    private Number minimumValue;

    private Number maximumValue;

    private int steps;


    @Override
    public String label() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( ScaleRangeMappedNumbersEditor.class );


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Double.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ScaleMappedNumbers<Double>>() {

            @Override
            public ScaleMappedNumbers<Double> initialize( ScaleMappedNumbers<Double> proto ) throws Exception {
                proto.scales.clear();
                proto.numberValues.clear();
                proto.defaultNumberValue.set( null );
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
                final ScaleRangeMappedNumbersChooser cc = new ScaleRangeMappedNumbersChooser( lowerBound, upperBound,
                        minimumValue, maximumValue, steps,
                        (NumberRange)prop.info().getAnnotation( NumberRange.class ) );

                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    if (cc.lowerBound() != null && cc.upperBound() != null && cc.mappedMinimum() != null
                            && cc.mappedMaximum() != null) {
                        lowerBound = cc.lowerBound();
                        upperBound = cc.upperBound();
                        maximumValue = cc.mappedMaximum();
                        minimumValue = cc.mappedMinimum();
                        steps = cc.steps();

                        prop.get().scales.clear();
                        prop.get().numberValues.clear();

                        prop.get().defaultNumberValue.set( minimumValue );

                        // only linear currently
                        double singleMappedStep = (maximumValue.doubleValue() - minimumValue.doubleValue())
                                / (cc.steps() + 1);
                        double singleSrcStep = (upperBound.doubleValue() - lowerBound.doubleValue()) / cc.steps();
                        for (int i = 0; i <= cc.steps(); i++) {
                            prop.get().add( minimumValue.doubleValue() + (singleMappedStep * i),
                                    lowerBound.doubleValue() + (singleSrcStep * i) );
                        }

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
        List<Double> values = prop.get().numbers();
        if (prop.get().defaultNumberValue.get() != null) {
            minimumValue = (Number)prop.get().defaultNumberValue.get();
        }

        if (!values.isEmpty()) {
            maximumValue = values.get( values.size() - 1 );
        }

        List<Number> scales = prop.get().scales();
        if (!scales.isEmpty()) {
            lowerBound = scales.get( 0 ).intValue();
            upperBound = scales.get( values.size() - 1 ).intValue();
        }

        steps = values.size() - 1;
        if (steps <= 0) {
            steps = 10;
        }
    }


    protected void updateButton( Button button ) {
        if (minimumValue != null && maximumValue != null) {
            int digits = ((NumberRange)prop.info().getAnnotation( NumberRange.class )).digits();
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits( digits );
            df.setMinimumFractionDigits( digits );

            button.setText( i18n.get( "chooseBetween", df.format( minimumValue ), df.format( maximumValue ) ) );
            // green, all ok
            button.setBackground( StylePlugin.okColor() );
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
        return lowerBound != null && upperBound != null
                && minimumValue != null && maximumValue != null && steps != 0;
    }
}
