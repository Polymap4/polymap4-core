/*
 * polymap.org 
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.feature.ConstantFilter;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

/**
 * Creates a {@link ConstantFilter} for {@link Style#visibleIf} that matches a
 * feature attribute against a literal.
 *
 * @author Steffen Stundzig
 * @author Falko Bräutigam
 */
public class AttributeLiteralFilterEditor
        extends StylePropertyEditor<ConstantFilter> {

    private static final IMessages i18n = Messages.forPrefix( "AttributeLiteralFilterEditor" );

    public static final Map<Class<? extends BinaryComparisonOperator>,String> OPS = new HashMap() {{
            put( PropertyIsEqualTo.class, "==" );
            put( PropertyIsGreaterThan.class, ">" );
            put( PropertyIsGreaterThanOrEqualTo.class, ">=" );
            put( PropertyIsLessThan.class, "<" );
            put( PropertyIsLessThanOrEqualTo.class, "<=" );
            put( PropertyIsNotEqualTo.class, "!=" ); 
    }};
    
    // instance *******************************************
    
    private String          propertyName;
    
    private Object          literal;
    
    private Class<? extends BinaryComparisonOperator>   opType;
    
    
    @Override
    public String label() {
        return i18n.get( "title" );
    }

    
    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return super.init( site ) && Filter.class.isAssignableFrom( targetType( site ) ) && site.featureType.isPresent();
    }


    @Override
    public void updateProperty() {
        prop.createValue( ConstantFilter.defaults( true ) );
    }


    protected void initValues() {
        if (prop.get().filter() instanceof BinaryComparisonOperator) {
            BinaryComparisonOperator filter = (BinaryComparisonOperator)prop.get().filter();
            opType = OPS.keySet().stream().filter( op -> op.isAssignableFrom( filter.getClass() ) ).findAny()
                    .orElseThrow( () -> new RuntimeException( "Unhandled filter operation type: " + filter.getClass() ) );
            propertyName = ((PropertyName)filter.getExpression1()).getPropertyName();
            literal = ((Literal)filter.getExpression2()).getValue();
        }
        else {
            opType = PropertyIsEqualTo.class;
            literal = "";
        }
    }
    
    
    @Override
    public Composite createContents( Composite parent ) {
        initValues();

        List<String> propertyNames = featureType().getDescriptors().stream()
                .filter( pd -> !(pd instanceof GeometryDescriptor) )
                .map( pd -> pd.getName().getLocalPart() )
                .collect( Collectors.toList() );

        Composite contents = super.createContents( parent );

        // propertyCombo
        ComboViewer propertyCombo = new ComboViewer( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        propertyCombo.setContentProvider( ArrayContentProvider.getInstance() );
        propertyCombo.setInput( propertyNames );
        if (propertyName != null) {
            propertyCombo.setSelection( new StructuredSelection( propertyName ) );
        }
        propertyCombo.addSelectionChangedListener( ev -> {
            propertyName = UIUtils.selection( ev.getSelection() ).first( String.class ).get();
            if (isValid()) {
                submit();
            }
        });

        // opCombo
        ComboViewer opCombo = new ComboViewer( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );
        opCombo.getCombo().setVisibleItemCount( OPS.size() );
        opCombo.setLabelProvider( new LabelProvider() {
            @Override public String getText( Object elm ) {
                return OPS.get( elm );
            }
        });
        opCombo.setContentProvider( ArrayContentProvider.getInstance() );
        opCombo.setInput( OPS.keySet() );
        opCombo.setSelection( new StructuredSelection( opType ) );
        opCombo.addSelectionChangedListener( ev -> {
            opType = UIUtils.selection( ev.getSelection() ).first( Class.class ).get();
            // XXX give literalText a suited validator
            if (isValid()) {
                submit();
            }
        });

        // literal
        Text literalText = new Text( contents, SWT.BORDER );
        literalText.setText( literal.toString() );
        literalText.addModifyListener( ev -> {
            literal = literalText.getText();
            if (isValid()) {
                submit();
            }
        });

        contents.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );
        FormDataFactory.on( propertyCombo.getControl() ).left( 0 ).right( 40 );
        FormDataFactory.on( opCombo.getControl() ).left( propertyCombo.getControl() ).right( 64 );
        FormDataFactory.on( literalText ).left( opCombo.getControl() ).right( 100 );
        return contents;
    }

    
    protected void submit() {
        PropertyName left = ff.property( propertyName );
        Literal right = ff.literal( literal );
        Filter filter = null;
        if (opType.equals( PropertyIsEqualTo.class )) {
            filter = ff.equals( left, right );
        }
        else if (opType.equals( PropertyIsNotEqualTo.class )) {
            filter = ff.notEqual( left, right );
        }
        else if (opType.equals( PropertyIsLessThan.class )) {
            filter = ff.less( left, right );
        }
        else if (opType.equals( PropertyIsLessThanOrEqualTo.class )) {
            filter = ff.lessOrEqual( left, right );
        }
        else if (opType.equals( PropertyIsGreaterThan.class )) {
            filter = ff.greater( left, right );
        }
        else if (opType.equals( PropertyIsGreaterThanOrEqualTo.class )) {
            filter = ff.greaterOrEqual( left, right );
        }
        else {
            throw new RuntimeException( "Unhandled op type: " + opType );
        }
        prop.createValue( ConstantFilter.defaults( filter ) );        
    }

    
    @Override
    public boolean isValid() {
        return !StringUtils.isBlank( propertyName ) 
                && opType != null 
                && literal != null && !StringUtils.isBlank( literal.toString() );
    }
    
}
