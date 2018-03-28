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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory2;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.ui.feature.ColorGradient2FilterEditor;
import org.polymap.core.style.ui.feature.ColorMap2FilterEditor;
import org.polymap.core.style.ui.feature.ConstantColorEditor;
import org.polymap.core.style.ui.feature.ConstantExternalGraphicEditor;
import org.polymap.core.style.ui.feature.ConstantFontFamilyEditor;
import org.polymap.core.style.ui.feature.ConstantFontStyleEditor;
import org.polymap.core.style.ui.feature.ConstantFontWeightEditor;
import org.polymap.core.style.ui.feature.ConstantMarkGraphicEditor;
import org.polymap.core.style.ui.feature.ConstantNumberEditor;
import org.polymap.core.style.ui.feature.ConstantStrokeCapStyleEditor;
import org.polymap.core.style.ui.feature.ConstantStrokeDashStyleEditor;
import org.polymap.core.style.ui.feature.ConstantStrokeJoinStyleEditor;
import org.polymap.core.style.ui.feature.FeaturePropertyBasedNumberEditor;
import org.polymap.core.style.ui.feature.FeaturePropertyBasedStringEditor;
import org.polymap.core.style.ui.feature.NumberGradient2FilterEditor;
import org.polymap.core.style.ui.feature.NumberGradient2MapScaleEditor;
import org.polymap.core.style.ui.feature.ScaleRangeEditor;
import org.polymap.core.style.ui.raster.ConstantRasterBandEditor;
import org.polymap.core.style.ui.raster.ConstantRasterColorMapTypeEditor;
import org.polymap.core.style.ui.raster.PredefinedColorMapEditor;

import org.polymap.model2.Property;

/**
 * 
 * @param <T> The target type of the {@link StylePropertyValue}.
 * @author Falko Bräutigam
 */
public abstract class StylePropertyEditor<SPV extends StylePropertyValue> {

    public static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
    
    public static final Class<StylePropertyEditor>[] availableEditors = new Class[] {
            NoValueEditor.class,
            AlwaysTrueEditor.class,
            ConstantColorEditor.class,
            ConstantFontFamilyEditor.class,
            ConstantFontStyleEditor.class,
            ConstantFontWeightEditor.class,
            ConstantMarkGraphicEditor.class,
            ConstantExternalGraphicEditor.class,
            ConstantNumberEditor.class, 
            ConstantStrokeCapStyleEditor.class, 
            ConstantStrokeDashStyleEditor.class,
            ConstantStrokeJoinStyleEditor.class,
            //
            NumberGradient2FilterEditor.class,
            ColorGradient2FilterEditor.class,
            ColorMap2FilterEditor.class,
//            FeaturePropertyMatchingNumberEditor.class,
//            FeaturePropertyMatchingStringEditor.class,
            // attribute value
            FeaturePropertyBasedStringEditor.class,
            FeaturePropertyBasedNumberEditor.class,
            // scale dependent
            ScaleRangeEditor.class,
            NumberGradient2MapScaleEditor.class,
            // raster
            ConstantRasterBandEditor.class,
            PredefinedColorMapEditor.class,
            ConstantRasterColorMapTypeEditor.class };


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

    private StylePropertyFieldSite  site;

    /** Deprecated: use {@link #site()} instead. */
    protected Property<SPV>         prop;


    /**
     * Initialize and check if this editor is able to handle the given property's
     * {@link StylePropertyValue}.
     * 
     * @return True if this editor is able to handle the property's
     *         {@link StylePropertyValue}.
     */
    public boolean init( @SuppressWarnings("hiding") StylePropertyFieldSite site ) {
        try {
            this.site = site;
            this.prop = (Property<SPV>)site.prop.get();
            return true;
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    protected StylePropertyFieldSite site() {
        return site;
    }

    /**
     * Shortcut to {@link #site()}.featureStore.get()
     * @return The featureStore of the layer.
     */
    protected FeatureStore featureStore() {
        return site.featureStore.get();
    }

    /**
     * Shortcut to {@link #site()}.featureType.get()
     * @return The schema of the {@link #featureStore}.
     */
    protected FeatureType featureType() {
        return site.featureType.get();
    }


    /**
     * Returns the <b>declared</b> type of the given property:
     * <pre>
     * Property&lt;StylePropertyValue&lt;Number&gt;&gt; -> Number
     * </pre>
     * Deprecated! Use {@link StylePropertyFieldSite#targetType} instead.
     *
     * @see StylePropertyFieldSite#targetType
     * @param site
     * @return
     */
    protected Class targetType( @SuppressWarnings("hiding") StylePropertyFieldSite site ) {
        assert site() == null || site == site();
        return site.targetType.get();
    }


    /**
     * Checks if the <b>actual</b> type of the {@link #prop} is compatible with the
     * type parameter of this editor.
     */
    public boolean canHandleCurrentValue() {
        Type targetType = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class<? extends StylePropertyValue> targetClass = null;
        if (targetType instanceof Class) {
            targetClass = (Class<? extends StylePropertyValue>)targetType;
        }
        else if (targetType instanceof ParameterizedType) {
            targetClass = (Class<? extends StylePropertyValue>)((ParameterizedType)targetType).getRawType();
        }
        else {
            throw new RuntimeException( "Target type is not a Class: " + targetType );
        }

        StylePropertyValue current = prop.get();
        if (current != null) {
            if (current.lastEditorHint.get() != null) {
                return current.lastEditorHint.get().equals( hint() );
            }
            return targetClass.isAssignableFrom( current.getClass() );
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
     * Creates a control that displays the current value of this editor and a way to
     * modify the value.
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


    public boolean isValid() {
        return true;
    }
}
