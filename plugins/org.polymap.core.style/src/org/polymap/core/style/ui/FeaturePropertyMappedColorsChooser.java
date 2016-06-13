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
import java.util.Map;

import java.awt.Color;
import java.io.IOException;

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
import com.google.common.collect.Multisets;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;
import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;
import org.polymap.core.ui.StatusDispatcher.Style;

/**
 * Color chooser which loads all values for a selected property and calculates/mappes
 * a color scheme to them.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyMappedColorsChooser {

    private static final int MAX_VALUES = 100;


    public class Triple {

        private final String label;

        private final int count;

        private Color color;


        public Triple( String label, int count, Color color ) {
            this.label = label;
            this.count = count;
            this.color = color;
        }


        public String label() {
            return label;
        }


        public Color color() {
            return color;
        }


        public void setColor( Color color ) {
            this.color = color;
        }
    }

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyMappedColorsChooser" );

    // private final ExpressionMappedColors property;

    private final FeatureStore featureStore;

    private final FeatureType featureType;

    private String propertyName;

    private final List<Triple> triples = Lists.newArrayList();

    private Composite tableViewer;

    private final Color defaultColor;

    private ScrolledComposite scrolledComposite;

    private final Map<String,Color> initialColors;


    public FeaturePropertyMappedColorsChooser( String propertyName, Color defaultColor, Map<String,Color> initialColors,
            FeatureStore featureStore, FeatureType featureType ) {
        this.propertyName = propertyName;
        this.defaultColor = defaultColor;
        this.featureStore = featureStore;
        this.featureType = featureType;
        this.initialColors = initialColors;
    }


    public String title() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( FeaturePropertyMappedColorsChooser.class );


    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        // property chooser
        final Combo propertyCombo = new Combo( parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        List<String> properties = properties();
        propertyCombo.setItems( properties.toArray( new String[properties.size()] ) );
        propertyCombo.select( properties.indexOf( propertyName ) );
        propertyCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                propertyName = properties.get( propertyCombo.getSelectionIndex() );
                try {
                    updateTable( propertyCombo );
                }
                catch (IOException e1) {
                    StatusDispatcher.handleError( "error during load of property values", e1 );
                }
            }
        } );

        scrolledComposite = new ScrolledComposite( parent, SWT.V_SCROLL | SWT.H_SCROLL );
        scrolledComposite.setExpandHorizontal( true );
        scrolledComposite.setExpandVertical( true );

        tableViewer = new Composite( scrolledComposite, SWT.NONE );
        tableViewer.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        scrolledComposite.setContent( tableViewer );
        tableViewer.setLayout( FormLayoutFactory.defaults().spacing( 0 ).create() );

        scrolledComposite.setMinSize( tableViewer.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

        on( propertyCombo ).top( 0 ).left( 0 ).right( MAX_VALUES );
        on( scrolledComposite ).top( propertyCombo ).bottom( MAX_VALUES ).height( 250 ).width( 300 );

        try {
            updateTable( propertyCombo );
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    private void updateTable( Composite parent ) throws IOException {
        triples.clear();
        if (propertyName != null) {
            // load all values and count them
            SortedMultiset<String> allValues = TreeMultiset.create();
            FeatureCollection featureCollection = featureStore.getFeatures();
            FeatureIterator iterator = featureCollection.features();
            // color for empty or null is also the default color
            allValues.add( "" );
            while (iterator.hasNext()) {
                SimpleFeature feature = (SimpleFeature)iterator.next();
                Object rawValue = feature.getAttribute( propertyName );
                String value = rawValue == null ? "" : rawValue.toString().trim();
                allValues.add( value );
            }

            int overallEntryCount = allValues.entrySet().size();
            if (overallEntryCount > MAX_VALUES) {
                StatusDispatcher.handle(
                        new Status( IStatus.WARNING, CorePlugin.PLUGIN_ID,
                                i18n.get( "tooManyEntries", overallEntryCount, MAX_VALUES ), null ),
                        Style.SHOW, Style.LOG );
            }
            else {
                int currentEntryCount = 1;
                for (Entry<String> entry : Multisets.copyHighestCountFirst( allValues ).entrySet()) {
                    String label = entry.getElement();
                    Color color = StringUtils.isBlank( label ) ? defaultColor : initialColors.get( label );
                    if (color == null) {
                        int colorCode = (256 * 256 * 256 / overallEntryCount) * currentEntryCount;
                        System.err.println( new Color( colorCode ).toString() + ": " + overallEntryCount + "; "
                                + currentEntryCount + " = " + colorCode );
                        color = new Color( colorCode );
                    }
                    Triple triple = new Triple( label, entry.getCount(), color );
                    triples.add( triple );
                    currentEntryCount++;
                }
            }
        }
        redrawTable();
    }


    private void redrawTable() {
        UIUtils.disposeChildren( tableViewer );
        Button last = null;
        for (final Triple triple : triples) {
            Button color = new Button( tableViewer, SWT.NONE );
            color.setBackground(
                    UIUtils.getColor( triple.color.getRed(), triple.color.getGreen(), triple.color.getBlue() ) );
            color.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    ColorChooser cc = new ColorChooser(
                            new RGB( triple.color.getRed(), triple.color.getGreen(), triple.color.getBlue() ) );
                    UIService.instance().openDialog( cc.title(), dialogParent -> {
                        cc.createContents( dialogParent );
                    }, () -> {
                        RGB rgb = cc.getRGB();
                        triple.setColor( new Color( rgb.red, rgb.green, rgb.blue ) );
                        color.setBackground( new org.eclipse.swt.graphics.Color( color.getDisplay(), rgb ) );
                        if (rgb.red * rgb.blue * rgb.green > 8000000) {
                            color.setForeground( new org.eclipse.swt.graphics.Color( color.getDisplay(), 0, 0, 0 ) );
                        }
                        else {
                            color.setForeground(
                                    new org.eclipse.swt.graphics.Color( color.getDisplay(), 255, 255, 255 ) );
                        }
                        return true;
                    } );
                }

            } );

            Label text = new Label( tableViewer, SWT.NONE );
            text.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
            if (StringUtils.isBlank( triple.label() )) {
                if (triple.count == 1) {
                    text.setText( i18n.get( "others" ) + " <small>(0)</small>" );
                }
                else {
                    text.setText( i18n.get( "nullOrEmptyOrOthers" ) + " <small>(" + (triple.count - 1) + ")</small>" );
                }
            }
            else {
                text.setText( triple.label() + " <small>(" + triple.count + ")</small>" );
            }

            // layout
            on( color ).height( 18 ).width( 18 );
            if (last != null) {
                on( color ).top( last, 3 );
                on( text ).top( last, 5 );
            }
            else {
                on( color ).top( 0 );
                on( text ).top( 1 );
            }
            on( text ).left( color, 5 );

            last = color;
        }
        tableViewer.layout( true );
        scrolledComposite.setMinSize( tableViewer.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    }


    private List<String> properties() {
        Collection<PropertyDescriptor> schemaDescriptors = featureType.getDescriptors();
        GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
        final List<String> allowedProperties = Lists.newArrayList();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                // if (Number.class.isAssignableFrom(
                // descriptor.getType().getBinding() )) {
                allowedProperties.add( descriptor.getName().getLocalPart() );
                // }
            }
        }
        return allowedProperties;
    }


    public String propertyName() {
        return propertyName;
    }


    public List<Triple> triples() {
        return triples;
    }
}
