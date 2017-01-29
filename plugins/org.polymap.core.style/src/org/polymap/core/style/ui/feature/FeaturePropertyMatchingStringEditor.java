/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.ui.feature;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.PropertyMatchingStringFilter;
import org.polymap.core.style.model.feature.RelationalOperator;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * Matches a feature property against a string.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyMatchingStringEditor
        extends StylePropertyEditor<PropertyMatchingStringFilter> {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyMatchingString" );

    private static Log log = LogFactory.getLog( FeaturePropertyMatchingStringEditor.class );

    private final static List<RelationalOperator> content = Lists.newArrayList( RelationalOperator.eq,
            RelationalOperator.neq );


    @Override
    public String label() {
        return i18n.get( "title" );
    }

    private List<String> columns;


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        if (Filter.class.isAssignableFrom( targetType( site ) ) && site.featureType.isPresent() && super.init( site )) {
            columns = allowedProperties();
            return true;
        }
        return false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( PropertyMatchingStringFilter.defaults() );
    }


    @Override
    public Composite createContents( Composite parent ) {
        final Composite contents = super.createContents( parent );
        contents.setLayout( FormLayoutFactory.defaults().create() );

        final Combo propertyCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        propertyCombo.setItems( columns.toArray( new String[columns.size()] ) );
        propertyCombo.select( columns.indexOf( prop.get().leftProperty.get() ) );
        propertyCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().leftProperty.set( columns.get( propertyCombo.getSelectionIndex() ) );
            }
        } );

        final Combo expressionCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        expressionCombo.setItems( content.stream().map( RelationalOperator::value ).toArray( String[]::new ) );
        expressionCombo.select( content.indexOf( prop.get().relationalStringOperator.get() ) );
        expressionCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().relationalStringOperator.set( content.get( expressionCombo.getSelectionIndex() ) );
            }
        } );

        final Text literalText = new Text( contents, SWT.BORDER );
        final String rightLiteral = prop.get().rightLiteral.get();
        literalText.setText( StringUtils.isBlank( rightLiteral ) ? "" : rightLiteral );
        literalText.addModifyListener( new ModifyListener() {

            @Override
            public void modifyText( ModifyEvent ev ) {
                prop.get().rightLiteral.set( literalText.getText() );
            }

        } );

        FormDataFactory.on( propertyCombo ).left( 0 ).right( 40 );
        FormDataFactory.on( expressionCombo ).left( propertyCombo, 1 ).right( 60 );
        FormDataFactory.on( literalText ).left( expressionCombo, 1 ).right( 100 );
        return contents;
    }


    protected List<String> allowedProperties() {
        Collection<PropertyDescriptor> schemaDescriptors = featureType().getDescriptors();
        GeometryDescriptor geometryDescriptor = featureType().getGeometryDescriptor();
        final List<String> result = Lists.newArrayList();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                result.add( descriptor.getName().getLocalPart() );
            }
        }
        return result;
    }


    @Override
    public boolean isValid() {
        return !StringUtils.isBlank( (String)prop.get().leftProperty.get() )
                && prop.get().relationalStringOperator.get() != null
                && !StringUtils.isBlank( (String)prop.get().rightLiteral.get() );
    }
}
