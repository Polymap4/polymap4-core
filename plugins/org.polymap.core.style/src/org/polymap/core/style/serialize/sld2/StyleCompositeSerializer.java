/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld2;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;
import com.rits.cloning.Cloner;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.style.model.StyleComposite;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.feature.ConstantValue;
import org.polymap.core.style.model.feature.FilterMappedValues;
import org.polymap.core.style.model.feature.NoValue;
import org.polymap.core.style.model.feature.PropertyValue;
import org.polymap.core.style.model.feature.ScaleMappedNumbers;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

import org.polymap.model2.Property;

/**
 * 
 * @param <T> The type to be {@link #serialize(StyleComposite, FeatureTypeStyle)}d.
 * @param <S> The target type to be expected in {@link #set(FeatureTypeStyle, Property, Setter)}. 
 * @author Falko Bräutigam
 */
public abstract class StyleCompositeSerializer<T extends StyleComposite,S>
        extends Configurable {

    private static final Log log = LogFactory.getLog( StyleCompositeSerializer.class );
    
    public static final StyleFactory sf = SLDSerializer2.sf;

    public static final FilterFactory2 ff = SLDSerializer2.ff;

    protected Context               context;
    
    @Mandatory
    protected Config2<StyleCompositeSerializer<T,S>,SymbolizerAccessor<S>> accessor;
    
    
    public StyleCompositeSerializer( Context context ) {
        this.context = context;
    }


    public abstract void serialize( T style, FeatureTypeStyle fts );

    
    /**
     * ...
     * <p/>
     * The setter is not called if the given property is not set. 
     *
     * @param fts
     * @param prop
     * @param setter
     */
    protected <V> void set( FeatureTypeStyle fts, Property<StylePropertyValue<V>> prop, Setter<V,S> setter ) {
        FluentIterable<RuleModifier<V,S>> modifiers = FluentIterable.from( handle( prop ) )
                .filter( modifier -> !(modifier instanceof NoValueRuleModifier) );
        multiply( fts, modifiers, setter );
    }


    /**
     * The given setter is called only if the property is <b>not</b> set. The value
     * argument of the setter is always <b>null</b>.
     * 
     * @see #set(FeatureTypeStyle, Property, Setter)
     */
    protected <V> void setDefault( FeatureTypeStyle fts, Property<StylePropertyValue<V>> prop, Setter<V,S> setter ) {
        FluentIterable<RuleModifier<V,S>> modifiers = FluentIterable.from( handle( prop ) )
                .filter( modifier -> modifier instanceof NoValueRuleModifier );
        multiply( fts, modifiers, setter );        
    }


    /**
     * Do the magic of building the cross-product of the rules in the given
     * {@link FeatureTypeStyle} and the given modifiers.
     *
     * @param fts
     * @param prop
     * @param setter
     */
    protected <V> void multiply( FeatureTypeStyle fts, FluentIterable<RuleModifier<V,S>> modifiers, Setter<V,S> setter ) {
        if (modifiers.isEmpty()) {
            return;
        }
        List<Rule> newRules = new ArrayList();
        for (Rule rule : fts.rules()) {
            for (RuleModifier<V,S> modifier : modifiers) {
                Rule copy = Cloner.standard().deepCloneDontCloneInstances( rule, ff, sf );
                modifier.apply( copy, accessor.get().apply( copy ), (Setter<V,S>)setter );

                // XXX check if filter and scales are the same for rule and copy; in this
                // case symbolizer can(?) / should (?) be merged
                newRules.add( copy );
            }
        }
        fts.rules().clear();
        fts.rules().addAll( newRules );
    }


    protected <V> Iterable<RuleModifier<V,S>> handle( Property<StylePropertyValue<V>> prop ) {
        StylePropertyValue<V> propValue = prop.get();

        // no value -> nothing to modify
        if (propValue == null || propValue instanceof NoValue) {
            return singletonList( new NoValueRuleModifier() );
        }
        // ConstantValue
        else if (propValue instanceof ConstantValue) {
            return singletonList( new SimpleRuleModifier( ((ConstantValue)propValue).value() ) );
        }
        // PropertyValue
        else if (propValue instanceof PropertyValue) {
            return singletonList( new SimpleRuleModifier( ((PropertyValue)propValue).propertyName.get() ) );
        }
        // FilterMappedValues
        else if (propValue instanceof FilterMappedValues) {
            List<Filter> filters = ((FilterMappedValues)propValue).filters();
            List<V> values = ((FilterMappedValues)propValue).values();
            return IntStream.range( 0, filters.size() )
                    .mapToObj( i -> new SimpleRuleModifier( values.get( i ), filters.get( i ) ) )
                    .collect( Collectors.toList() );
        }
        // ScaleMappedValues
        else if (propValue instanceof ScaleMappedNumbers) {
            List<V> values = ((ScaleMappedNumbers)propValue).numbers();
            List<Double> scales = ((ScaleMappedNumbers)propValue).scales();
            List<RuleModifier<V,S>> result = new ArrayList( scales.size() + 1 );
            for (int i=0; i<values.size(); i++) {
                double lowerBound = scales.get( i );
                double upperBound = scales.size() > (i+1) ? scales.get( i+1 ) : Double.POSITIVE_INFINITY;
                result.add( new SimpleRuleModifier( values.get( i ), lowerBound, upperBound ) );
            }
            return result;
        }
        else {
            throw new RuntimeException( "Unhandled StylePropertyValue type: " + propValue.getClass().getSimpleName() );
        }
    }


    protected FeatureTypeStyle defaultFeatureTypeStyle( Symbolizer... symbolizers ) {
        Rule rule = sf.createRule();
        for (Symbolizer s : symbolizers) {
            rule.symbolizers().add( s );
        };
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add( rule );
        return fts;
    }


    /**
     * 
     */
    @FunctionalInterface
    public static interface SymbolizerAccessor<S>
            extends Function<Rule,S> {
    }
    
    
    /**
     * 
     * @param <V> The type of the value to set.
     * @param <S> The target type to set the value in (Symbolizer, Fill, ...).
     */
    @FunctionalInterface
    public static interface Setter<V,S> {
        public void apply( V value, S symbolizer );
    }


    /**
     * 
     */
    public static interface RuleModifier<V,S> {
        public void apply( Rule copy, S symbolizer, Setter<V,S> setter );
    }
    

    /**
     * 
     */
    public static class NoValueRuleModifier<V,S>
            implements RuleModifier<V,S> {

        @Override
        public void apply( Rule copy, S symbolizer, Setter<V,S> setter ) {
            setter.apply( null, symbolizer );
        }
    }

    
    /**
     * 
     */
    public static class SimpleRuleModifier<V,S>
            implements RuleModifier<V,S> {
        public double       minScale = -1, maxScale = -1;
        public Filter       filter;
        public V            value;
        
        public SimpleRuleModifier( V value ) {
            this( value, null );
        }

        public SimpleRuleModifier( V value, Filter filter ) {
            this.value = value;
            this.filter = filter;
        }

        public SimpleRuleModifier( V value, double minScale, double maxScale ) {
            this.value = value;
            this.minScale = minScale;
            this.maxScale = maxScale;
        }

        @Override
        public void apply( Rule rule, S symbolizer, Setter<V,S> setter ) {
            assert value != null;
            setter.apply( value, symbolizer );
            if (filter != null) {
                Filter and = rule.getFilter() != null
                        ? ff.and( rule.getFilter(), filter )
                        : filter;
                rule.setFilter( and );
            }
            if (minScale > -1) {
                assert rule.getMinScaleDenominator() == 0;
                rule.setMinScaleDenominator( minScale );
            }
            if (maxScale > -1) {
                assert rule.getMaxScaleDenominator() == Double.POSITIVE_INFINITY;
                rule.setMaxScaleDenominator( maxScale );
            }
        }
    }
    
}
