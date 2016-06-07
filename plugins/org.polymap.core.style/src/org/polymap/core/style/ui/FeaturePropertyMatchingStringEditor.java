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
package org.polymap.core.style.ui;

import java.util.Collection;
import java.util.List;

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.PropertyMatchingStringFilter;
import org.polymap.core.style.model.RelationalOperator;

/**
 * Editor that creates one Property matches String Editor.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyMatchingStringEditor
        extends AbstractFeaturePropertyMatchingLiteralEditor<PropertyMatchingStringFilter> {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyMatchingString" );

    private static Log log = LogFactory.getLog( FeaturePropertyMatchingStringEditor.class );

    private final static List<RelationalOperator> content = Lists.newArrayList( RelationalOperator.eq,
            RelationalOperator.neq );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );

        Text literalText = new Text( contents, SWT.BORDER );
        literalText.setText( rightLiteral );
        literalText.addModifyListener( new ModifyListener() {

            @Override
            public void modifyText( ModifyEvent ev ) {
                updateRightLiteral( literalText.getText() );
            }

        } );

        return contents;
    }


    @Override
    protected List<RelationalOperator> allowedOperators() {
        return content;
    }


    @Override
    protected List<String> allowedProperties() {
        Collection<PropertyDescriptor> schemaDescriptors = featureType.getDescriptors();
        GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
        final List<String> columns = Lists.newArrayList();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                columns.add( descriptor.getName().getLocalPart() );
            }
        }
        return columns;
    }
}
