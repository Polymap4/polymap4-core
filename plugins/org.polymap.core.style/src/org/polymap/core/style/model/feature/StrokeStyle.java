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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.StyleComposite;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.model.UIOrder;

import org.polymap.model2.Concerns;
import org.polymap.model2.Description;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Steffen Stundzig
 */
public class StrokeStyle
        extends StyleComposite {

    private static Log log = LogFactory.getLog( StrokeStyle.class );

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<StrokeStyle> defaults = new ValueInitializer<StrokeStyle>() {
        @Override
        public StrokeStyle initialize( StrokeStyle proto ) throws Exception {
            StyleComposite.defaults.initialize( proto );
            return proto;
        }
    };
    
    @Nullable
    @UIOrder(60)
    @Description("capStyle")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StrokeCapStyle>> capStyle;

    @Nullable
    @UIOrder(70)
    @Description("dashStyle")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StrokeDashStyle>> dashStyle;

    @Nullable
    @UIOrder(80)
    @Description("joinStyle")
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<StrokeJoinStyle>> joinStyle;

}
