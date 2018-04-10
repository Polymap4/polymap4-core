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

import org.polymap.core.runtime.Numbers;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.model.feature.MappedValues.Mapped;
import org.polymap.core.style.model.feature.NumberRange;
import org.polymap.core.style.model.feature.ScaleMappedPrimitives;
import org.polymap.core.style.model.feature.ScaleMappedValues.ScaleRange;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.UIService;
import org.polymap.core.style.ui.feature.IntervalBuilder.Interval;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

/**
 * Editor that creates a (linear) gradient of {@link Number}s between adjustable
 * bounds with an adjustable number of breakpoints. The numbers a mapped to
 * an adjustable map scale range.
 *
 * @author Steffen Stundzig
 * @author Falko Bräutigam
 */
public class NumberGradient2MapScaleEditor<N extends Number>
        extends StylePropertyEditor<ScaleMappedPrimitives<N>> {

    private static final Log log = LogFactory.getLog( NumberGradient2MapScaleEditor.class );
    
    private static final IMessages i18n = Messages.forPrefix( "NumberGradient2MapScaleEditor", "AbstractGradient2FilterEditor" );

    public static final double      UNINITIALIZED = Double.NaN;
    
    /** Map scale value range lower bound. */
    private double      lowerBound;

    /** Map scale value range upper bound. */
    private double      upperBound;

    /** Generated number gradient minimum value. */
    private double      minimumValue = UNINITIALIZED;

    /** Generated number gradient maximum value. */
    private double      maximumValue = UNINITIALIZED;

    private int         breakpoints;

    private Color       defaultFgColor;

    private double      mapScale = UNINITIALIZED;
    
    private double      maxScale = UNINITIALIZED;

    private NumberRange annotation;
    

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
        prop.createValue( ScaleMappedPrimitives.defaults() );
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
        List<Mapped<ScaleRange,N>> values = prop.get().values();
        
        // defaults
        if (values.isEmpty()) {
            lowerBound = 5000;
            upperBound = maxScale != -1 ? (int)maxScale : 1000000;
            minimumValue = annotation.from();
            maximumValue = annotation.to();
            breakpoints = 5;
        }
        // from prop
        else {
            lowerBound = values.get( 0 ).key().max.get();
            upperBound = values.get( values.size() - 1 ).key().min.get();
            minimumValue = values.get( 0 ).value().doubleValue();
            maximumValue = values.get( values.size() - 1 ).value().doubleValue();
            breakpoints = values.size() - 3;  // 3 intervals -> no extra breakpoint
        }
    }


    @Override
    public Composite createContents( Composite parent ) {
        initValues();

        Composite contents = super.createContents( parent );
        Button button = new Button( parent, SWT.FLAT|SWT.PUSH|SWT.LEFT );
        defaultFgColor = button.getForeground();
        button.addSelectionListener( UIUtils.selectionListener( ev -> {
            Dialog dialog = new Dialog();
            UIService.instance().openDialog( dialog.title(), dialogParent -> {
                dialog.createContents( dialogParent );
            }, () -> {
                submit();
                updateButton( button );
                return true;
            });
        }));
        updateButton( button );
        return contents;
    }

    
    protected N toTargetType( double d ) {
        Double round = Numbers.roundToDigits( d, annotation.digits() );
        return Numbers.cast( round, site().targetType() );        
    }
    
    
    protected void submit() {
        prop.get().clear();

        // first interval: always starts at scale 0
        prop.get().add( 0, lowerBound, toTargetType( minimumValue ) );

        for (Interval i : new IntervalBuilder().calculate( lowerBound, upperBound, minimumValue, maximumValue, breakpoints )) {
             prop.get().add( i.start, i.end, toTargetType( i.value ) );
        }
        // last interval: always ends at scale POSITIVE_INFINITY
        prop.get().add( upperBound, Double.POSITIVE_INFINITY, toTargetType( maximumValue ) );
    }

    
    protected void updateButton( Button button ) {
        if (minimumValue != UNINITIALIZED && maximumValue != UNINITIALIZED) {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits( annotation.digits() );
            df.setMinimumFractionDigits( annotation.digits() );

            button.setText( i18n.get( "rechoose", df.format( minimumValue ), df.format( maximumValue ) ) );
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
        return lowerBound != UNINITIALIZED && upperBound != UNINITIALIZED 
                && minimumValue != UNINITIALIZED && maximumValue != UNINITIALIZED; 
    }
    
    
    /**
     * 
     */
    public class Dialog {

        private Spinner stepsSpinner;

        private Spinner mappedMaximumSpinner;

        private Spinner mappedMinimumSpinner;

        private Spinner upperBoundSpinner;

        private Spinner lowerBoundSpinner;


        public String title() {
            return i18n.get( "dialogTitle" );
        }


        public void createContents( Composite parent ) {
            NumberRange range = (NumberRange)prop.info().getAnnotation( NumberRange.class );
            int digits = range.digits();
            double factorX = Math.pow( 10, digits );

            lowerBoundSpinner = new Spinner( parent, SWT.BORDER );
            lowerBoundSpinner.setToolTipText( i18n.get( "lowerBoundTooltip" ) );
            upperBoundSpinner = new Spinner( parent, SWT.BORDER );
            upperBoundSpinner.setToolTipText( i18n.get( "upperBoundTooltip" ) );

            lowerBoundSpinner.setMinimum( 1 );
            lowerBoundSpinner.setMaximum( Integer.MAX_VALUE );
            lowerBoundSpinner.setIncrement( 1000 );
            lowerBoundSpinner.setPageIncrement( 10000 );
            lowerBoundSpinner.setSelection( (int)lowerBound );
            lowerBoundSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = lowerBoundSpinner.getSelection();
                lowerBound = selection;
                upperBoundSpinner.setMinimum( selection );
            }));

            upperBoundSpinner.setMinimum( 1 );
            upperBoundSpinner.setMaximum( Integer.MAX_VALUE );
            upperBoundSpinner.setIncrement( 1000 );
            upperBoundSpinner.setPageIncrement( 20000 );
            upperBoundSpinner.setSelection( (int)upperBound );
            upperBoundSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = upperBoundSpinner.getSelection();
                upperBound = selection;
                lowerBoundSpinner.setMaximum( selection );
            }));
            upperBoundSpinner.setMinimum( (int)lowerBound );
            lowerBoundSpinner.setMaximum( (int)upperBound );
            
            mappedMinimumSpinner = new Spinner( parent, SWT.BORDER );
            mappedMinimumSpinner.setDigits( digits );
            mappedMinimumSpinner.setMinimum( (int)(range.from() * factorX) );
            mappedMinimumSpinner.setMaximum( (int)(range.to() * factorX) );
            mappedMinimumSpinner.setIncrement( (int)(range.increment() * factorX) );
            mappedMinimumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
            mappedMinimumSpinner.setSelection( (int)(minimumValue * factorX) );
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
            mappedMaximumSpinner.setSelection( (int)(maximumValue * factorX) );
            mappedMaximumSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                int selection = mappedMaximumSpinner.getSelection();
                maximumValue = selection / Math.pow( 10, digits );
                // minimumMappedSpinner.setMaximum( selection );
            }));

            stepsSpinner = new Spinner( parent, SWT.BORDER );
            stepsSpinner.setToolTipText( i18n.get( "stepsTooltip" ) );
            stepsSpinner.setDigits( 0 );
            stepsSpinner.setMinimum( 0 );
            stepsSpinner.setMaximum( 30 );
            stepsSpinner.setSelection( breakpoints );
            stepsSpinner.addSelectionListener( UIUtils.selectionListener( ev -> {
                breakpoints = stepsSpinner.getSelection();
            }));

            int labelWidth = 100;
            int spinnerWidth = 120;
            parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );

            Label scaleLine = new Label( parent, SWT.NONE );
            scaleLine.setText( i18n.get( "currentScale", (int)mapScale, (int)maxScale ) );
            FormDataFactory.on( scaleLine ).fill().noBottom();
            
            Label l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "lowerBound" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( scaleLine, 3 ).left( 0 );
            FormDataFactory.on( lowerBoundSpinner ).width( spinnerWidth ).top( scaleLine ).left( l, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "mapsTo" ) );
            FormDataFactory.on( l ).top( scaleLine, 3 ).left( lowerBoundSpinner );
            FormDataFactory.on( mappedMinimumSpinner ).width( spinnerWidth ).top( scaleLine ).left( l, -10 ).right( 100, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "upperBound" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( lowerBoundSpinner, 3 ).left( 0 );
            FormDataFactory.on( upperBoundSpinner ).width( spinnerWidth ).top( lowerBoundSpinner, 0 ).left( l, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "mapsTo" ) );
            FormDataFactory.on( l ).top( lowerBoundSpinner, 3 ).left( upperBoundSpinner );
            FormDataFactory.on( mappedMaximumSpinner ).width( spinnerWidth ).top( lowerBoundSpinner ).left( l, -10 ).right( 100, -10 );

            l = new Label( parent, SWT.RIGHT );
            l.setText( i18n.get( "breakpoints" ) );
            FormDataFactory.on( l ).width( labelWidth ).top( upperBoundSpinner, 3 ).left( 0 );
            FormDataFactory.on( stepsSpinner ).width( spinnerWidth ).top( upperBoundSpinner, 0 ).left( l, -10 );
        }

    }

}
