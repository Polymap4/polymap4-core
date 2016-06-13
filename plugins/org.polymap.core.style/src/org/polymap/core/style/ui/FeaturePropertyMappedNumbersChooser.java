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

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.Collection;
import java.util.List;

import java.io.IOException;
import java.text.DecimalFormat;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.NumberRange;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;

/**
 * Chooser which loads all values for a selected property and add a lower and upper
 * bound spinner, and also high and low values for the mapped numbers.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyMappedNumbersChooser {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyMappedNumbersChooser" );

    // private final ExpressionMappedColors property;

    private final FeatureStore featureStore;

    private final FeatureType featureType;

    private String propertyName;

    private NumberRange range;

    private Double mappedMinimum;

    private Double mappedMaximum;

    private Double lowerBound;

    private Double upperBound;


    public FeaturePropertyMappedNumbersChooser( String propertyName, Double lowerBound, Double upperBound,
            Double mappedMinimum, Double mappedMaximum, NumberRange range, FeatureStore featureStore,
            FeatureType featureType ) {
        this.propertyName = propertyName;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.mappedMinimum = mappedMinimum;
        this.mappedMaximum = mappedMaximum;
        this.range = range;
        this.featureStore = featureStore;
        this.featureType = featureType;
    }


    public String title() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( FeaturePropertyMappedNumbersChooser.class );


    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        // property chooser
        final Combo propertyCombo = new Combo( parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        final Label currentValues = new Label( parent, SWT.NONE );
        int digits = range.digits();
        double factorX = Math.pow( 10, digits );

        List<String> properties = properties();
        propertyCombo.setItems( properties.toArray( new String[properties.size()] ) );
        propertyCombo.select( properties.indexOf( propertyName ) );
        propertyCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                propertyName = properties.get( propertyCombo.getSelectionIndex() );
                try {
                    updateSpinner( currentValues, digits );
                }
                catch (IOException e1) {
                    StatusDispatcher.handleError( "error during load of property values", e1 );
                }
            }
        } );

        final Spinner lowerBoundSpinner = new Spinner( parent, SWT.BORDER );
        final Spinner upperBoundSpinner = new Spinner( parent, SWT.BORDER );
        final Spinner mappedMinimumSpinner = new Spinner( parent, SWT.BORDER );
        final Spinner mappedMaximumSpinner = new Spinner( parent, SWT.BORDER );
        final Spinner granularitySpinner = new Spinner( parent, SWT.BORDER );

        lowerBoundSpinner.setDigits( digits );
        // lowerBoundSpinner.setIncrement( (int)(range.increment() * factorX) );
        // lowerBoundSpinner.setPageIncrement( (int)(range.increment() * factorX *
        // 10) );
        double currentLowerBound = lowerBound != null ? lowerBound.doubleValue() : 0.0;
        lowerBoundSpinner.setSelection( (int)(currentLowerBound * factorX) );
        lowerBoundSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = lowerBoundSpinner.getSelection();
                lowerBound = selection / Math.pow( 10, digits );
                upperBoundSpinner.setMinimum( selection );
            }
        } );

        upperBoundSpinner.setDigits( digits );
        // upperBoundSpinner.setMinimum( (int)(range.from() * factorX) );
        // upperBoundSpinner.setMaximum( (int)(range.to() * factorX) );
        // upperBoundSpinner.setIncrement( (int)(range.increment() * factorX) );
        // upperBoundSpinner.setPageIncrement( (int)(range.increment() * factorX *
        // 10) );
        double currentUpperBound = upperBound != null ? upperBound.doubleValue() : 1000000.0;
        upperBoundSpinner.setSelection( (int)(currentUpperBound * factorX) );
        upperBoundSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = upperBoundSpinner.getSelection();
                upperBound = selection / Math.pow( 10, digits );
                lowerBoundSpinner.setMaximum( selection );
            }
        } );

        mappedMinimumSpinner.setDigits( digits );
        mappedMinimumSpinner.setMinimum( (int)(range.from() * factorX) );
        mappedMinimumSpinner.setMaximum( (int)(range.to() * factorX) );
        mappedMinimumSpinner.setIncrement( (int)(range.increment() * factorX) );
        mappedMinimumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
        double currentMinimum = mappedMinimum != null ? mappedMinimum.doubleValue() : range.from();
        mappedMinimumSpinner.setSelection( (int)(currentMinimum * factorX) );
        mappedMinimumSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = lowerBoundSpinner.getSelection();
                mappedMinimum = selection / Math.pow( 10, digits );
                // maximumMappedSpinner.setMinimum( selection );
            }
        } );

        mappedMaximumSpinner.setDigits( digits );
        mappedMaximumSpinner.setMinimum( (int)(range.from() * factorX) );
        mappedMaximumSpinner.setMaximum( (int)(range.to() * factorX) );
        mappedMaximumSpinner.setIncrement( (int)(range.increment() * factorX) );
        mappedMaximumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
        double currentMaximum = mappedMaximum != null ? mappedMaximum.doubleValue() : range.to();
        mappedMaximumSpinner.setSelection( (int)(currentMaximum * factorX) );
        mappedMaximumSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = upperBoundSpinner.getSelection();
                mappedMaximum = selection / Math.pow( 10, digits );
                // minimumMappedSpinner.setMaximum( selection );
            }
        } );

        on( propertyCombo ).top( 0 ).left( 0 ).right( 100 );
        on( currentValues ).top( propertyCombo ).left( 0 ).right( 100 ).width( 450 );
        on( lowerBoundSpinner ).top( currentValues );
        on( mappedMinimumSpinner ).top( currentValues ).left( lowerBoundSpinner );
        on( upperBoundSpinner ).top( lowerBoundSpinner );
        on( mappedMaximumSpinner ).top( lowerBoundSpinner ).left( upperBoundSpinner );

        try {
            updateSpinner( currentValues, digits );
        }
        catch (IOException e1) {
            StatusDispatcher.handleError( "error during load of property values", e1 );
        }
    }


    private void updateSpinner( Label label, int digits ) throws IOException {
        if (!StringUtils.isBlank( propertyName )) {
            // load all values and count min max
            double min = Double.MAX_VALUE;
            double max = 0.0;

            FeatureCollection featureCollection = featureStore.getFeatures();
            FeatureIterator iterator = featureCollection.features();
            // color for empty or null is also the default color
            boolean valuesFound = false;
            while (iterator.hasNext()) {
                SimpleFeature feature = (SimpleFeature)iterator.next();
                Object rawValue = feature.getAttribute( propertyName );
                if (rawValue != null && rawValue instanceof Number) {
                    Number currentValue = (Number)rawValue;
                    valuesFound = true;
                    min = Math.min( currentValue.doubleValue(), min );
                    max = Math.max( currentValue.doubleValue(), max );
                }
            }
            if (valuesFound) {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits( digits );
                df.setMinimumFractionDigits( digits );
                label.setText( i18n.get( "currentValues", df.format( min ), df.format( max ) ) );
            }
            else {
                label.setText( i18n.get( "noValues" ) );
            }
        }
        else {
            label.setText( "" );
        }
    }


    private List<String> properties() {
        Collection<PropertyDescriptor> schemaDescriptors = featureType.getDescriptors();
        GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
        final List<String> allowedProperties = Lists.newArrayList();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                if (Number.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                    allowedProperties.add( descriptor.getName().getLocalPart() );
                }
            }
        }
        return allowedProperties;
    }


    public String propertyName() {
        return propertyName;
    }


    public Double minimum() {
        return mappedMinimum;
    }


    public Double maximum() {
        return mappedMaximum;
    }
}
