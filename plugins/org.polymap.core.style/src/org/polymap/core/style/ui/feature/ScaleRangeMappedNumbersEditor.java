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

import java.util.List;

import java.text.DecimalFormat;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.lite.RendererUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.model.feature.NumberRange;
import org.polymap.core.style.model.feature.ScaleMappedNumbers;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.UIService;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
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

    private static final Log log = LogFactory.getLog( ScaleRangeMappedNumbersEditor.class );
    
    private static final IMessages i18n = Messages.forPrefix( "ScaleRangeMappedNumbersEditor" );

    private static final IMessages chooser_i18n = Messages.forPrefix( "ScaleRangeMappedNumbersChooser" );

    private Integer     lowerBound;

    private Integer     upperBound;

    private Number      minimumValue;

    private Number      maximumValue;

    private int         breakpoints;

    private Color       defaultFgColor;

    private double      mapScale = -1;
    
    private double      maxScale = -1;

    private NumberRange annotation;
    

    @Override
    public String label() {
        return i18n.get( "title" );
    }

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
                return proto;
            }
        });
    }


    protected void initValues() {
        // calculate map scale
        if (site().maxExtent.isPresent()) { 
            try {
                ReferencedEnvelope maxExtent = site().maxExtent.get();
                Point mapSize = site().mapSize.get();
                maxScale = RendererUtilities.calculateScale( maxExtent, mapSize.x, mapSize.y, 120d );
                ReferencedEnvelope mapExtent = new ReferencedEnvelope( site().mapExtent.get(), maxExtent.getCoordinateReferenceSystem() );
                mapScale = RendererUtilities.calculateScale( mapExtent, mapSize.x, mapSize.y, 120d );
                System.out.println( "Scale: " + mapScale + " -- " + maxScale + " for maxExtent: " + maxExtent );
            }
            catch (TransformException | FactoryException e) {
                StatusDispatcher.handleError( "Unable to calculate map scale.", e );
            }
        }

        annotation = (NumberRange)prop.info().getAnnotation( NumberRange.class );
        List<Double> values = prop.get().numbers();
        List<Number> scales = prop.get().scales();
        
        // defaults
        if (values.isEmpty()) {
            lowerBound = 1000;
            upperBound = maxScale != -1 ? (int)maxScale : 1000000;
            minimumValue = annotation.from();
            maximumValue = annotation.to();
            breakpoints = 10;
        }
        // from prop
        else {
            minimumValue = values.get( 0 );
            maximumValue = values.get( values.size() - 1 );
            lowerBound = scales.get( 0 ).intValue();
            upperBound = scales.get( values.size() - 1 ).intValue();
            breakpoints = values.size();
        }
    }


    @Override
    public Composite createContents( Composite parent ) {
        initValues();

        Composite contents = super.createContents( parent );
        Button button = new Button( parent, SWT.FLAT|SWT.PUSH|SWT.LEFT );
        defaultFgColor = button.getForeground();
        button.addSelectionListener( UIUtils.selectionListener( ev -> {
            ScaleRangeMappedNumbersChooser chooser = new ScaleRangeMappedNumbersChooser();

            UIService.instance().openDialog( chooser.title(), dialogParent -> {
                chooser.createContents( dialogParent );
            }, () -> {
                submit();
                updateButton( button );
                return true;
            });
        }));
        updateButton( button );

        return contents;
    }


    protected void submit() {
        prop.get().scales.clear();
        prop.get().numberValues.clear();

        // only linear currently
        double valueStep = (maximumValue.doubleValue() - minimumValue.doubleValue()) / breakpoints;
        double scaleStep = (upperBound.doubleValue() - lowerBound.doubleValue()) / breakpoints;
        double currentValue = minimumValue.doubleValue();
        double currentScale = lowerBound.doubleValue();
        int intervals = breakpoints + 1;

        // in order to cover the entire scale range we add 0th interval starting at scale 0
        // if user did not explicitly cover lower limits
        if (lowerBound > 0 || currentValue > annotation.from()) {
            prop.get().add( currentValue, 0d );
            currentValue += valueStep;
            currentScale += scaleStep;
            intervals --;
        }
        
        for (int i = 0; i < intervals; i++) {
            prop.get().add( roundToDigits( currentValue ), currentScale );
            currentValue += valueStep;
            currentScale += scaleStep;
        }
        // don't add another number -> last intervall is open

        prop.get().fake.set( String.valueOf( System.currentTimeMillis() ) );
    }

    
    protected double roundToDigits( double d ) {
        double digitsScale = Math.pow( 10, annotation.digits() );
        return Math.round( d * digitsScale  ) / digitsScale; 
    }
    
    
    protected void updateButton( Button button ) {
        if (minimumValue != null && maximumValue != null) {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits( annotation.digits() );
            df.setMinimumFractionDigits( annotation.digits() );

            button.setText( i18n.get( "chooseBetween", df.format( minimumValue ), df.format( maximumValue ) ) );
            button.setForeground( defaultFgColor );
            button.setBackground( StylePlugin.okColor() );
        }
        else {
            button.setText( i18n.get( "choose" ) );
            button.setForeground( StylePlugin.errorColor() );
            button.setBackground( StylePlugin.okColor() );
        }
    }
    
    
    @Override
    public boolean isValid() {
        return lowerBound != null && upperBound != null && minimumValue != null && maximumValue != null && breakpoints != 0;
    }
    
    
    /**
     * Chooser which loads scales and add a lower and upper
     * bound spinner, and also high and low values for the mapped numbers.
     */
    public class ScaleRangeMappedNumbersChooser {

        private Spinner stepsSpinner;

        private Spinner mappedMaximumSpinner;

        private Spinner mappedMinimumSpinner;

        private Spinner upperBoundSpinner;

        private Spinner lowerBoundSpinner;


        public String title() {
            return chooser_i18n.get( "title" );
        }


        public void createContents( Composite parent ) {
            NumberRange range = (NumberRange)prop.info().getAnnotation( NumberRange.class );
            int digits = range.digits();
            double factorX = Math.pow( 10, digits );

            lowerBoundSpinner = new Spinner( parent, SWT.BORDER );
            lowerBoundSpinner.setToolTipText( "First interval covers everything up to this lower bounds\nand maps to the given value" );
            upperBoundSpinner = new Spinner( parent, SWT.BORDER );
            upperBoundSpinner.setToolTipText( "Last interval covers everything bigger than this upper bounds\nand and maps to the given value" );

            lowerBoundSpinner.setMinimum( Integer.MIN_VALUE );
            lowerBoundSpinner.setMaximum( Integer.MAX_VALUE );
            lowerBoundSpinner.setIncrement( 1000 );
            lowerBoundSpinner.setPageIncrement(20000 );
            lowerBoundSpinner.setSelection( lowerBound );
            lowerBoundSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = lowerBoundSpinner.getSelection();
                lowerBound = selection;
                upperBoundSpinner.setMinimum( selection );
            }));

            upperBoundSpinner.setMinimum( Integer.MIN_VALUE );
            upperBoundSpinner.setMaximum( Integer.MAX_VALUE );
            upperBoundSpinner.setIncrement( 1000 );
            upperBoundSpinner.setPageIncrement( 20000 );
            upperBoundSpinner.setSelection( upperBound );
            upperBoundSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = upperBoundSpinner.getSelection();
                upperBound = selection;
                lowerBoundSpinner.setMaximum( selection );
            }));
            upperBoundSpinner.setMinimum( lowerBound );
            lowerBoundSpinner.setMaximum( upperBound );
            
            mappedMinimumSpinner = new Spinner( parent, SWT.BORDER );
            mappedMinimumSpinner.setDigits( digits );
            mappedMinimumSpinner.setMinimum( (int)(range.from() * factorX) );
            mappedMinimumSpinner.setMaximum( (int)(range.to() * factorX) );
            mappedMinimumSpinner.setIncrement( (int)(range.increment() * factorX) );
            mappedMinimumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
            mappedMinimumSpinner.setSelection( (int)(minimumValue.doubleValue() * factorX) );
            mappedMinimumSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = mappedMinimumSpinner.getSelection();
                minimumValue = selection / Math.pow( 10, digits );
                // maximumMappedSpinner.setMinimum( selection );
            }));

            mappedMaximumSpinner = new Spinner( parent, SWT.BORDER );
            mappedMaximumSpinner.setDigits( digits );
            mappedMaximumSpinner.setMinimum( (int)(range.from() * factorX) );
            mappedMaximumSpinner.setMaximum( (int)(range.to() * factorX) );
            mappedMaximumSpinner.setIncrement( (int)(range.increment() * factorX) );
            mappedMaximumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
            mappedMaximumSpinner.setSelection( (int)(maximumValue.doubleValue() * factorX) );
            mappedMaximumSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = mappedMaximumSpinner.getSelection();
                maximumValue = selection / Math.pow( 10, digits );
                // minimumMappedSpinner.setMaximum( selection );
            }));

            stepsSpinner = new Spinner( parent, SWT.BORDER );
            stepsSpinner.setToolTipText( "For X breakpoints X+1 intervalls are generated" );
            stepsSpinner.setDigits( 0 );
            stepsSpinner.setMinimum( 2 );
            stepsSpinner.setMaximum( 100 );
            stepsSpinner.setSelection( breakpoints );
            stepsSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                breakpoints = stepsSpinner.getSelection();
            }));

            int labelWidth = 100;
            int spinnerWidth = 120;
            parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );

            Label scaleLine = new Label( parent, SWT.NONE );
            scaleLine.setText( chooser_i18n.get( "currentScale", (int)mapScale, (int)maxScale ) );
            FormDataFactory.on( scaleLine ).fill().noBottom();
            
            Label l = new Label( parent, SWT.RIGHT );
            l.setText( chooser_i18n.get( "lowerBound" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( scaleLine, 3 ).left( 0 );
            FormDataFactory.on( lowerBoundSpinner ).width( spinnerWidth ).top( scaleLine ).left( l, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( chooser_i18n.get( "mapsTo" ) );
            FormDataFactory.on( l ).top( scaleLine, 3 ).left( lowerBoundSpinner );
            FormDataFactory.on( mappedMinimumSpinner ).width( spinnerWidth ).top( scaleLine ).left( l, -10 ).right( 100, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( chooser_i18n.get( "upperBound" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( lowerBoundSpinner, 3 ).left( 0 );
            FormDataFactory.on( upperBoundSpinner ).width( spinnerWidth ).top( lowerBoundSpinner, 0 ).left( l, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( chooser_i18n.get( "mapsTo" ) );
            FormDataFactory.on( l ).top( lowerBoundSpinner, 3 ).left( upperBoundSpinner );
            FormDataFactory.on( mappedMaximumSpinner ).width( spinnerWidth ).top( lowerBoundSpinner ).left( l, -10 ).right( 100, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( chooser_i18n.get( "breakpoints" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( upperBoundSpinner, 3 ).left( 0 );
            FormDataFactory.on( stepsSpinner ).width( spinnerWidth ).top( upperBoundSpinner, 0 ).left( l, -10 );
        }

    }

}
