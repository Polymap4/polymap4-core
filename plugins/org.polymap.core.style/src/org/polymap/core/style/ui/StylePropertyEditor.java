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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.lang.reflect.ParameterizedType;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory2;

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

    public static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
    
    public static final Class<StylePropertyEditor>[] availableEditors = new Class[] {
            AlwaysTrueEditor.class,
            ConstantColorEditor.class,
            ConstantFontFamilyEditor.class,
            ConstantFontStyleEditor.class,
            ConstantFontWeightEditor.class,
            ConstantNumberEditor.class, 
            ConstantStrokeCapStyleEditor.class, 
            ConstantStrokeDashStyleEditor.class,
            ConstantStrokeJoinStyleEditor.class,
            FeaturePropertyBasedStringEditor.class,
            FeaturePropertyBasedNumberEditor.class,
            FeaturePropertyMatchingNumberEditor.class,
            FeaturePropertyMatchingStringEditor.class,
            FeaturePropertyRangeMappedColorsEditor.class,
            FeaturePropertyRangeMappedNumbersEditor.class,
            NoValueEditor.class,
            ScaleRangeEditor.class,
            FeaturePropertyMappedColorsEditor.class,
            TableBasedScaleMappedNumbersEditor.class};


    /**
     * Factory of new editor instances.
     *
     * @param spv
     */
    public static StylePropertyEditor[] forValue( StylePropertyFieldSite fieldSite ) {
        List<StylePropertyEditor> result = new ArrayList( availableEditors.length );
        for (Class<StylePropertyEditor> cl : availableEditors) {
            try {
                StylePropertyEditor editor = cl.newInstance();
                if (editor.init( fieldSite )) {
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

    protected Property<SPV> prop;

    protected FeatureStore featureStore;

    protected FeatureType featureType;


    /**
     * Initialize and check if this editor is able to handle the given property's
     * {@link StylePropertyValue}.
     * 
     * @return True if this editor is able to handle the property's
     *         {@link StylePropertyValue}.
     */
    public boolean init( StylePropertyFieldSite fieldSite) {
        try {
            this.prop = (Property<SPV>)fieldSite.prop.get();
            this.featureStore = fieldSite.featureStore.get();
            this.featureType = fieldSite.featureType.get();
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
     * @param site
     * @return
     */
    protected Class targetType( StylePropertyFieldSite site ) {
        assert StylePropertyValue.class.isAssignableFrom( site.prop.get().info().getType() );
        Optional<ParameterizedType> o = site.prop.get().info().getParameterizedType();
        ParameterizedType p = o.orElseThrow(
                () -> new RuntimeException( "StylePropertyValue has no type parameter: " + prop.toString() ) );
        return (Class)p.getActualTypeArguments()[0];
    }


    /**
     * Checks if the <b>actual</b> type of the {@link #prop} is compatible with the
     * type parameter of this editor.
     */
    public boolean canHandleCurrentValue() {
        Class<? extends StylePropertyValue> targetType = (Class)((ParameterizedType)getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        StylePropertyValue current = prop.get();
        if (current != null) {
            if (current.lastEditorHint.get() != null) {
                if (current.lastEditorHint.get().equals( hint() )) {
                    return true;
                }
            }
            return targetType.isAssignableFrom( current.getClass() );
        }
        return false;
    }


    /**
     * a simple text to find the right (last) editor for a style value.
     *
     * @return the simple class name per default
     */
    protected String hint() {
        return this.getClass().getSimpleName();
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
        Composite contents = parent; // new Composite( parent, SWT.BORDER );
        contents.setLayout( new FillLayout( SWT.HORIZONTAL ) );
        return contents;
    }


    public abstract void updateProperty();

    /**
     * Updates the property and sets also the hint for the current editor.
     */
    public void updatePropertyWithHint() {
        updateProperty();
        StylePropertyValue current = prop.get();
        if (current != null) {
            current.lastEditorHint.set( hint() );
        }
    }
}
