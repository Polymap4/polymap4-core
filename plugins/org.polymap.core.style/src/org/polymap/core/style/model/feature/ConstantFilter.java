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
package org.polymap.core.style.model.feature;

import java.io.IOException;

import org.opengis.filter.Filter;

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyChange;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Provides a constant filter as style property value. For example used by
 * {@link Style#visibleIf}.
 *
 * @author Falko Bräutigam
 */
public class ConstantFilter
        extends FilterStyleProperty {

    /**  */
    public static final ValueInitializer<ConstantFilter> defaults( boolean b ) {
        return new ValueInitializer<ConstantFilter>() {
            @Override public ConstantFilter initialize( ConstantFilter proto ) throws Exception {
                // use default in order to avoid encode for default
                if (b == false) {
                    proto.encoded.set( encode( b ? Filter.INCLUDE : Filter.EXCLUDE ) );
                }
                return proto;
            }
        };
    }

    /**  */
    public static final ValueInitializer<ConstantFilter> defaults( Filter filter ) {
        return new ValueInitializer<ConstantFilter>() {
            @Override public ConstantFilter initialize( ConstantFilter proto ) throws Exception {
                proto.encoded.set( encode( filter ) );
                return proto;
            }
        };
    }

    // instance *******************************************

    /** Null specifies {@link Filter#INCLUDE}. */
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    protected Property<String>          encoded;


    public ConstantFilter setFilter( Filter filter ) throws IOException {
        encoded.set( encode( filter ) );
        return this;
    }


    @Override
    public Filter filter() {
        return encoded.get() != null ? decode( encoded.get() ) : Filter.INCLUDE;
    }

}
