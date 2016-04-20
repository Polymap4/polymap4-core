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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.lang.reflect.ParameterizedType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Property;

/**
 * 
 * @param <T> The target type of the {@link StylePropertyValue}.
 * @author Falko Bräutigam
 */
public abstract class StylePropertyEditor<SPV extends StylePropertyValue> {

    private static Log log = LogFactory.getLog( StylePropertyEditor.class );
    
    public static final Class<StylePropertyEditor>[] availableEditors = new Class[] { 
            ConstantNumberEditor.class, 
            AttributeMappedNumbersEditor.class };
    
    /**
     * Factory of new editor instances. 
     *
     * @param spv
     */
    public static StylePropertyEditor[] forValue( Property<StylePropertyValue> prop ) {
        List<StylePropertyEditor> result = new ArrayList( availableEditors.length );
        for (Class<StylePropertyEditor> cl : availableEditors) {
            try {
                StylePropertyEditor editor = cl.newInstance();
                if (editor.init( prop )) {
                    result.add( editor );
                }
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return (StylePropertyEditor[])result.toArray( new StylePropertyEditor[result.size()] );
    }

    
    // instance *******************************************
    
    protected Property<SPV>             prop;
    
    
    /**
     * Initialize and check if this editor is able to handle the given property's
     * {@link StylePropertyValue}.
     * 
     * @return True if this editor is able to handle the property's
     *         {@link StylePropertyValue}.
     */
    public boolean init( @SuppressWarnings("hiding") Property<SPV> prop ) {
        try {
            this.prop = prop;
            return true;
        }
        catch (ClassCastException e) {
            return false;
        }
    }
    
    
    /**
     * Returns the <b>declared</b> type of the given property:
     * <pre>
     * Property&lt;StylePropertyValue&lt;Number&gt;&gt; -> Number
     * </pre>
     *
     * @param _prop
     * @return
     */
    protected Class targetType( Property _prop ) {
        assert StylePropertyValue.class.isAssignableFrom( _prop.info().getType() );
        Optional<ParameterizedType> o = _prop.info().getParameterizedType();
        ParameterizedType p = o.orElseThrow( () -> new RuntimeException( "StylePropertyValue has no type parameter: " + prop.toString() ) );
        return (Class)p.getActualTypeArguments()[0];    
    }

    
    /**
     * Checks if the <b>actual</b> type of the {@link #prop} is compatible with
     * the type parameter of this editor.
     */
    public boolean canHandleCurrentValue() {
        Class<? extends StylePropertyValue> targetType = (Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        StylePropertyValue current = prop.get();
        return current != null && targetType.isAssignableFrom( current.getClass() );
    }
    
    
    /**
     * The human readable name of this editor. Usually displayed in the UI to
     * select/indentify this editor.
     */
    public abstract String label();
    
    
    /**
     * Creates a control that displays the current value of this editor and a way for
     * the user to modify the value.
     */
    public Composite createContents( Composite parent ) {
        Composite contents = parent; //new Composite( parent, SWT.BORDER );
        contents.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        return contents;
    }
    
}
