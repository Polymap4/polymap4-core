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

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ExpressionMappedColors;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates numbers based on feature attributes.
 *
 * @author Steffen Stundzig
 */
class TableBasedExpressionMappedColorsEditor
        extends StylePropertyEditor<ExpressionMappedColors> {

    private static final IMessages i18n = Messages.forPrefix( "TableBasedExpressionMappedColors" );


    @Override
    public String label() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( TableBasedExpressionMappedColorsEditor.class );


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Color.class.isAssignableFrom( targetType( site ) ) && site.featureType.isPresent() ? super.init( site )
                : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ExpressionMappedColors>() {

            @Override
            public ExpressionMappedColors initialize( ExpressionMappedColors proto ) throws Exception {
                proto.propertyName.set( "dxf_color" );
                proto.setDefaultColor( new Color( 255, 0, 0 ) );
                proto.expressions.clear();
                proto.colorValues.clear();
                proto.add( ff.literal( "1"), new Color( 0, 255, 0 ) );
                proto.add( ff.literal( "191"), new Color( 0, 0, 255 ) );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        // Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN
        // | SWT.READ_ONLY );
        //
        // Collection<PropertyDescriptor> schemaDescriptors =
        // featureType.getDescriptors();
        // GeometryDescriptor geometryDescriptor =
        // featureType.getGeometryDescriptor();
        // final List<String> columns = Lists.newArrayList();
        // for (PropertyDescriptor descriptor : schemaDescriptors) {
        // if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor
        // )) {
        // if (Number.class.isAssignableFrom( descriptor.getType().getBinding() )) {
        // columns.add( descriptor.getName().getLocalPart() );
        // }
        // }
        // }
        // combo.setItems( columns.toArray( new String[columns.size()] ) );
        // combo.select( columns.indexOf( prop.get().propertyValue.get() ) );
        //
        // combo.addSelectionListener( new SelectionAdapter() {
        //
        // @Override
        // public void widgetSelected( SelectionEvent e ) {
        // prop.get().propertyValue.set( columns.get( combo.getSelectionIndex() ) );
        // }
        // } );
        return contents;
    }
}
