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

import java.util.Iterator;
import java.util.Map;

import java.awt.Color;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ExpressionMappedColors;
import org.polymap.core.style.ui.FeaturePropertyMappedColorsChooser.Triple;

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
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        final Button button = new Button( parent, SWT.PUSH );
        button.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                FeaturePropertyMappedColorsChooser cc = new FeaturePropertyMappedColorsChooser( prop.get().propertyName.get(),
                        prop.get().defaultValue(), initialColors(), featureStore, featureType );
                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    if (cc.propertyName() != null && !cc.triples().isEmpty()) {
                        prop.get().propertyName.set( cc.propertyName() );
                        // prop.get().setDefaultColor( cc.defaultColor() );
                        prop.get().expressions.clear();
                        prop.get().colorValues.clear();
                        for (Triple triple : cc.triples()) {
                            if (!StringUtils.isBlank( triple.label() )) {
                                prop.get().add( ff.literal( triple.label() ), triple.color() );
                            }
                            else {
                                // empty contains the default color
                                prop.get().setDefaultColor( triple.color() );
                                updateButtonColor( button, triple.color() );
                            }
                        }
                    }
                    return true;
                } );
            }
        } );
        if (prop.get().propertyName.get() != null && !prop.get().colorValues.isEmpty()) {
            button.setText( i18n.get( "rechoose", prop.get().propertyName.get(), prop.get().colorValues.size() ) );
        }
        else {
            button.setText( i18n.get( "choose" ) );
        }
        if (prop.get().defaultValue() != null) {
            updateButtonColor( button, prop.get().defaultValue() );
        }
        return contents;
    }


    protected Map<String,Color> initialColors() {
        Iterator<Expression> expressions = prop.get().expressions().iterator();
        Iterator<Color> values = prop.get().values().iterator();
        Map<String,Color> initialColors = Maps.newHashMap();
        while (expressions.hasNext()) {
            assert values.hasNext();
            Expression expression = expressions.next();
            if (expression instanceof Literal) {
                String property = ((Literal)expression).getValue().toString();
                Color value = values.next();
                initialColors.put( property, value );
            }
        }
        return initialColors;
    }


    protected void updateButtonColor( Button button, Color color ) {
        RGB rgb = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
        button.setBackground( new org.eclipse.swt.graphics.Color( button.getDisplay(), rgb ) );
        if (rgb.red * rgb.blue * rgb.green > 8000000) {
            button.setForeground( new org.eclipse.swt.graphics.Color( button.getDisplay(), 0, 0, 0 ) );
        }
        else {
            button.setForeground( new org.eclipse.swt.graphics.Color( button.getDisplay(), 255, 255, 255 ) );
        }
    }
}
