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
package org.polymap.core.style.serialize.sld;

import java.util.ArrayList;
import java.util.List;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StyleComposite;
import org.polymap.core.style.model.StylePropertyValue;

/**
 * Serializes a particular {@link Style} into a flat list of
 * {@link SymbolizerDescriptor} instances. The resulting list represents the
 * cross-product of all complex filter and/or scale definitions specified via
 * {@link StylePropertyValue} types. Those different {@link StylePropertyValue} types
 * are handled by corresponding {@link StylePropertyValueHandler} types.
 *
 * @param <S> The input style type.
 * @param <SD> The output symbolizer descriptor.
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public abstract class StyleCompositeSerializer<S extends StyleComposite, SD extends SymbolizerDescriptor> {

    private static final Log log = LogFactory.getLog( StyleCompositeSerializer.class );

    protected List<SD> descriptors;

    private Filter defaultFilter;


    public List<SD> serialize( S style ) {
        assert descriptors == null;
        try {
            descriptors = new ArrayList();
            doSerialize( style );
            return descriptors;
        }
        finally {
            descriptors = null;
        }
    }


    protected abstract SD createDescriptor();


    protected abstract void doSerialize( S style );


    /**
     * Sets the value in all current {@link #descriptors} using the given setter.
     * <p/>
     * Here goes the magic of multiplying style descriptors :)
     *
     * @param spv
     * @param setter
     */
    protected void setValue( StylePropertyValue<? extends Object> spv,
            org.polymap.core.style.serialize.sld.StylePropertyValueHandler.Setter<SD> setter ) {

        if (descriptors.isEmpty()) {
            descriptors.add( createDescriptor() );
        }

        List<SD> updated = new ArrayList( descriptors.size() );
        for (SymbolizerDescriptor sd : descriptors) {
            updated.addAll( StylePropertyValueHandler.handle( spv, (SD)sd, setter ) );
        }
        this.descriptors = updated;
    }


    protected <V extends SymbolizerDescriptor> void setComposite( List<V> composites, Setter<SD,V> setter ) {
        if (!composites.isEmpty()) {
            if (descriptors.isEmpty()) {
                for (V composite : composites) {
                    SD descriptor = createDescriptor();
                    composite.filter.ifPresent( filter -> descriptor.filterAnd( filter ) );
                    setter.set( descriptor, composite );
                    descriptors.add( descriptor );
                }
            }
            else {
                // if its only one composite, don't clone
                if (composites.size() == 1) {
                    for (SD sd : descriptors) {
                        setter.set( sd, composites.get( 0 ) );
                        composites.get( 0 ).filter.ifPresent( filter -> {
                            sd.filterAnd( filter );
                        } );
                    }
                }
                else {
                    // otherwise create fresh clones for all composites
                    List<SD> updated = new ArrayList<SD>( descriptors.size() * composites.size() );
                    for (V composite : composites) {
                        for (SD sd : descriptors) {
                            SD clone = composites.size() == 1 ? sd : (SD)sd.clone();
                            setter.set( clone, composite );
                            composite.filter.ifPresent( filter -> {
                                clone.filterAnd( filter );
                            } );
                            updated.add( clone );
                        }
                    }
                    this.descriptors = updated;
                }
            }
        }
    }


    @FunctionalInterface
    public interface Setter<SD, V extends SymbolizerDescriptor> {

        void set( SD sd, V value );
    }
}
