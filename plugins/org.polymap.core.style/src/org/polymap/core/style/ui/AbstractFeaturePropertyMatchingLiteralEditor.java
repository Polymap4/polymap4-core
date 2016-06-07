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

import java.util.List;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.style.model.ConstantFilter;
import org.polymap.core.style.model.RelationalOperator;

/**
 * Base Editor that creates for filters.
 *
 * @author Steffen Stundzig
 */
public abstract class AbstractFeaturePropertyMatchingLiteralEditor<T extends ConstantFilter>
        extends StylePropertyEditor<T> {

    private static Log log = LogFactory.getLog( AbstractFeaturePropertyMatchingLiteralEditor.class );

    private List<RelationalOperator> content;

    private List<String> columns;

    private String leftProperty;

    private RelationalOperator relationalOperator;

    protected String rightLiteral;


    protected abstract List<RelationalOperator> allowedOperators();


    protected abstract List<String> allowedProperties();


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        if (Filter.class.isAssignableFrom( targetType( site ) ) && site.featureType.isPresent() && super.init( site )) {
            content = allowedOperators();
            columns = allowedProperties();
            return true;
        }
        return false;
    }


    @Override
    public void updateProperty() {
//        prop.createValue( ConstantFilter.defaultTrue );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        splitFilter();
        Combo propertyCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        propertyCombo.setItems( columns.toArray( new String[columns.size()] ) );
        propertyCombo.select( columns.indexOf( leftProperty ) );
        propertyCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                updateLeftProperty( columns.get( propertyCombo.getSelectionIndex() ) );
            }
        } );

        Combo expressionCombo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        expressionCombo.setItems( content.stream().map( RelationalOperator::value ).toArray( String[]::new ) );
        expressionCombo.select( content.indexOf( relationalOperator ) );
        expressionCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                updateOperator( content.get( expressionCombo.getSelectionIndex() ) );
            }
        } );

        return contents;
    }


    private void splitFilter() {
        try {
            Filter filter = prop.get().filter();

            leftProperty = "";
            relationalOperator = RelationalOperator.eq;
            rightLiteral = "";
            if (filter != null && filter instanceof BinaryComparisonOperator) {
                BinaryComparisonOperator bco = (BinaryComparisonOperator)filter;
                leftProperty = ((PropertyName)bco.getExpression1()).getPropertyName();
                rightLiteral = (String)((Literal)bco.getExpression2()).getValue();
                relationalOperator = RelationalOperator.forFilter( bco );
            }
        }
        catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException( e );
        }
    }


    protected void updateRightLiteral( String text ) {
        rightLiteral = text;
        updateFilter();
    }


    protected void updateLeftProperty( final String newValue ) {
        leftProperty = newValue;
        updateFilter();
    }


    protected void updateOperator( final RelationalOperator newValue ) {
        relationalOperator = newValue;
        updateFilter();
    }


    private void updateFilter() {
        try {
            prop.get().setFilter(
                    relationalOperator.asFilter( ff.property( leftProperty ), ff.literal( rightLiteral ) ) );
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
