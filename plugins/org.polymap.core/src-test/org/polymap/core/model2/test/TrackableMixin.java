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
package org.polymap.core.model2.test;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Computed;
import org.polymap.core.model2.ComputedProperty;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TrackableMixin
        extends Composite {

    protected Property<Integer>     track;
    
    @Computed(SimpleComputedProperty.class)
    protected Property<String>      computed;
    
    
    public static class SimpleComputedProperty
            extends ComputedProperty<String> {

        public SimpleComputedProperty( PropertyInfo info, Composite composite ) {
            super( info, composite );
        }

        @Override
        public String get() {
            return "This is the computed property: " + getInfo().getName();
        }

        @Override
        public void set( String value ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
}
