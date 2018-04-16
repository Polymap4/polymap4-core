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

import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.feature.ShadowStyle.StyleId;

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ConstantStyleId
        extends ConstantValue<StyleId> {

    public static final ValueInitializer<ConstantStyleId> defaults( Style style ) {
        return new ValueInitializer<ConstantStyleId>() {
            @Override public ConstantStyleId initialize( ConstantStyleId proto ) throws Exception {
                proto.styleId.set( style != null ? style.id.get() : null );
                return proto;
            }
        };
    }

    @Concerns(StylePropertyChange.Concern.class)
    public Property<String>     styleId;

    @Override
    public StyleId value() {
        return new StyleId( styleId.get() );
    }
    
}
