/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Static factory of utils to work with {@link ILayer}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Layers {

    /**
     * 
     * @return Newly created {@link Predicate}.
     */
    public static Predicate<ILayer> isVisible() {
        return new Predicate<ILayer>() {
            public boolean test( ILayer input ) {
                return input.visible.get();
            }
        };
    }

    /**
     * 
     * @return Newly created {@link Predicate}.
     */
    public static Predicate<ILayer> hasLabel( final String label ) {
        return new Predicate<ILayer>() {
            public boolean test( ILayer input ) {
                return input.label.equals( label );
            }
        };
    }

    public static Function<ILayer,String> asLabel() {
        return new Function<ILayer,String>() {
            public String apply( ILayer input ) {
                return input.label.get();
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
                return l1.orderKey.get() - l2.orderKey.get();
            }
        };
    }

//    /**
//     * Convenience for <code>Iterables.toArray( layers, ILayer.class )</code>. 
//     */
//    public static ILayer[] toArray( Iterable<ILayer> layers ) {
//        return layers.stream
//    }
    
}
