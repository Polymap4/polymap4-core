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
package org.polymap.core.style.model.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class MappedValues<K,V>
        extends StylePropertyValue<V> {

    /**
     * XXX Collections do not support StylePropertyChange.Concern yet. We use this tp
     * force firing the event.
     */
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    protected Property<String>          fake;

    
    public MappedValues<K,V> add( K key, V value ) {
        fake.set( "changed at: " + System.currentTimeMillis() );
        return this;
    }

    /**
     * 
     * @see #values(Iterable, Iterable, BiFunction)
     */
    public abstract List<Mapped<K,V>> values();
    
    public void clear() {
        fake.set( "changed at: " + System.currentTimeMillis() );
    }
    

    /**
     * Converts the given {@link Iterable}s into result list for {@link #values()}.
     */
    public static <KI,VI, KO, VO> List<Mapped<KO,VO>> values( 
            Iterable<KI> keys,
            Iterable<VI> values,
            BiFunction<KI,VI,Mapped<KO,VO>> supplier ) {
        
        List<Mapped<KO,VO>> result = new ArrayList( 30 );
        Iterator<KI> kit = keys.iterator();
        Iterator<VI> vit = values.iterator();
        while (vit.hasNext() && kit.hasNext()) {
            result.add( supplier.apply( kit.next(), vit.next() ) );
        }
        return result;
    }
    
    
    /**
     * 
     */
    public static class Mapped<K,V> {
        
        private K key;
        
        private V value;

        public Mapped( K key, V value ) {
            this.key = key;
            this.value = value;
        }
        
        public K key() {
            return key;
        }
        
        public V value() {
            return value;
        }
    }
    
}
