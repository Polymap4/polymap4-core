/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.project;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Static factory of utils to work with {@link ILayer}. 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class Layers {

    /**
     * 
     * @return Newly created {@link Predicate}.
     */
    public static Predicate<ILayer> isVisible() {
        return new Predicate<ILayer>() {
            public boolean apply( ILayer input ) {
                return input.isVisible();
            }
        };
    }

    /**
     * 
     * @return Newly created {@link Predicate}.
     */
    public static Predicate<ILayer> hasLabel( final String label ) {
        return new Predicate<ILayer>() {
            public boolean apply( ILayer input ) {
                return input.getLabel().equals( label );
            }
        };
    }

    public static Function<ILayer,String> asLabel() {
        return new Function<ILayer,String>() {
            public String apply( ILayer input ) {
                return input.getLabel();
            }
        };
    }
    
    /**
     * 
     *
     * @return Newly created {@link Comparator}.
     */
    public static Comparator<ILayer> zPrioComparator() {
        return new Comparator<ILayer>() {
            public int compare( ILayer l1, ILayer l2 ) {
                return l1.getOrderKey() - l2.getOrderKey();
            }
        };
    }

    /**
     * Convenience for <code>Iterables.toArray( layers, ILayer.class )</code>. 
     */
    public static ILayer[] toArray( Iterable<ILayer> layers ) {
        return Iterables.toArray( layers, ILayer.class );
    }
    
}
