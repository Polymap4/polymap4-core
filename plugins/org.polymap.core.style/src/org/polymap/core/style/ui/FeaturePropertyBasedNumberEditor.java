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

import java.util.Collection;
import java.util.List;

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.FeaturePropertyBasedNumber;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates numbers based on feature attributes.
 *
 * @author Steffen Stundzig
 */
class FeaturePropertyBasedNumberEditor
        extends StylePropertyEditor<FeaturePropertyBasedNumber> {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyBasedNumber" );


    @Override
    public String label() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( FeaturePropertyBasedNumberEditor.class );


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Number.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<FeaturePropertyBasedNumber>() {

            @Override
            public FeaturePropertyBasedNumber initialize( FeaturePropertyBasedNumber proto ) throws Exception {
                proto.value.set( "" );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN );

        Collection<PropertyDescriptor> schemaDescriptors = featureStore.getSchema().getDescriptors();
        GeometryDescriptor geometryDescriptor = featureStore.getSchema().getGeometryDescriptor();
        final List<String> columns = Lists.newArrayList();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
//                if (Double.class.isAssignableFrom( descriptor.getType().getBinding() )
//                        || Integer.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                    columns.add( descriptor.getName().getLocalPart() );
//                }
            }
        }
        combo.setItems( columns.toArray( new String[columns.size()] ) );
        combo.select( columns.indexOf( prop.get().value.get() ) );

        combo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().value.set( columns.get( combo.getSelectionIndex() ) );
            }
        } );
        return contents;
    }
}
