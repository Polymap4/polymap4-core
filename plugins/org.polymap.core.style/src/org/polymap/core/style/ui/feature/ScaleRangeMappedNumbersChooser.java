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

import static org.polymap.core.ui.FormDataFactory.on;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.NumberRange;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * Chooser which loads scales and add a lower and upper
 * bound spinner, and also high and low values for the mapped numbers.
 *
 * @author Steffen Stundzig
 */
public class ScaleRangeMappedNumbersChooser {

    private static final IMessages i18n = Messages.forPrefix( "ScaleRangeMappedNumbersChooser" );

    private NumberRange range;

    private Number mappedMinimum;

    private Number mappedMaximum;

    private Integer lowerBound;

    private Integer upperBound;

    private Integer steps;

    private Spinner stepsSpinner;

    private Spinner mappedMaximumSpinner;

    private Spinner mappedMinimumSpinner;

    private Spinner upperBoundSpinner;

    private Spinner lowerBoundSpinner;


    public ScaleRangeMappedNumbersChooser( Integer lowerBound, Integer upperBound, Number mappedMinimum,
            Number mappedMaximum, Integer steps, NumberRange range ) {
        this.lowerBound = lowerBound == null ? 0 : lowerBound;
        this.upperBound = upperBound == null ? 500000 : upperBound;
        this.mappedMinimum = mappedMinimum == null ? range.from() : mappedMinimum;
        this.mappedMaximum = mappedMaximum == null ? range.to() : mappedMaximum;
        this.steps = steps;
        this.range = range;
    }


    public String title() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( ScaleRangeMappedNumbersChooser.class );


    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );

        final int digits = range.digits();
        final double factorX = Math.pow( 10, digits );

        lowerBoundSpinner = new Spinner( parent, SWT.BORDER );
        upperBoundSpinner = new Spinner( parent, SWT.BORDER );

        lowerBoundSpinner.setMinimum( Integer.MIN_VALUE );
        lowerBoundSpinner.setMaximum( Integer.MAX_VALUE );
        lowerBoundSpinner.setIncrement( 1000 );
        lowerBoundSpinner.setPageIncrement(20000 );
        lowerBoundSpinner.setSelection( lowerBound );
        lowerBoundSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = lowerBoundSpinner.getSelection();
                lowerBound = selection;
                upperBoundSpinner.setMinimum( selection );
            }
        } );

        upperBoundSpinner.setMinimum( Integer.MIN_VALUE );
        upperBoundSpinner.setMaximum( Integer.MAX_VALUE );
        upperBoundSpinner.setIncrement( 1000 );
        upperBoundSpinner.setPageIncrement(20000 );
        upperBoundSpinner.setSelection( upperBound );
        upperBoundSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = upperBoundSpinner.getSelection();
                upperBound = selection;
                lowerBoundSpinner.setMaximum( selection );
            }
        } );
        upperBoundSpinner.setMinimum( lowerBound );
        lowerBoundSpinner.setMaximum( upperBound );
        
        mappedMinimumSpinner = new Spinner( parent, SWT.BORDER );
        mappedMinimumSpinner.setDigits( digits );
        mappedMinimumSpinner.setMinimum( (int)(range.from() * factorX) );
        mappedMinimumSpinner.setMaximum( (int)(range.to() * factorX) );
        mappedMinimumSpinner.setIncrement( (int)(range.increment() * factorX) );
        mappedMinimumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
        mappedMinimumSpinner.setSelection( (int)(mappedMinimum.doubleValue() * factorX) );
        mappedMinimumSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = mappedMinimumSpinner.getSelection();
                mappedMinimum = selection / Math.pow( 10, digits );
                // maximumMappedSpinner.setMinimum( selection );
            }
        } );

        mappedMaximumSpinner = new Spinner( parent, SWT.BORDER );
        mappedMaximumSpinner.setDigits( digits );
        mappedMaximumSpinner.setMinimum( (int)(range.from() * factorX) );
        mappedMaximumSpinner.setMaximum( (int)(range.to() * factorX) );
        mappedMaximumSpinner.setIncrement( (int)(range.increment() * factorX) );
        mappedMaximumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
        mappedMaximumSpinner.setSelection( (int)(mappedMaximum.doubleValue() * factorX) );
        mappedMaximumSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = mappedMaximumSpinner.getSelection();
                mappedMaximum = selection / Math.pow( 10, digits );
                // minimumMappedSpinner.setMaximum( selection );
            }
        } );

        stepsSpinner = new Spinner( parent, SWT.BORDER );
        stepsSpinner.setDigits( 0 );
        stepsSpinner.setMinimum( 1 );
        stepsSpinner.setMaximum( 100 );
        stepsSpinner.setSelection( steps );
        stepsSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                steps = stepsSpinner.getSelection();
            }
        } );

        final int labelWidth = 120;
        final int spinnerWidth = 120;
        Label l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "lowerBound" ) );
        on( l ).width( labelWidth ).top( 3 ).left( 0 );
        on( lowerBoundSpinner ).width( spinnerWidth ).top( 0 ).left( l, -13 );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "mapsTo" ) );
        on( l ).top( 3 ).left( lowerBoundSpinner, 0 );
        on( mappedMinimumSpinner ).width( spinnerWidth ).top( 0 ).left( l, 0 );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "upperBound" ) );
        on( l ).width( labelWidth ).top( lowerBoundSpinner, 3 ).left( 0 );
        on( upperBoundSpinner ).width( spinnerWidth ).top( lowerBoundSpinner, 0 ).left( l, -13 );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "mapsTo" ) );
        on( l ).top( lowerBoundSpinner, 3 ).left( upperBoundSpinner );
        on( mappedMaximumSpinner ).width( spinnerWidth ).top( lowerBoundSpinner, 0 ).left( l, 0 );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "steps" ) );
        on( l ).width( labelWidth ).top( upperBoundSpinner, 3 ).left( 0 );
        on( stepsSpinner ).width( spinnerWidth ).top( upperBoundSpinner, 0 ).left( l, -13 );
    }


    public Number mappedMinimum() {
        return mappedMinimum;
    }


    public Number mappedMaximum() {
        return mappedMaximum;
    }


    public Integer lowerBound() {
        return lowerBound;
    }


    public Integer upperBound() {
        return upperBound;
    }


    public Integer steps() {
        return steps;
    }
}
