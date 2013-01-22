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
package org.polymap.core.model2.test;

import java.util.Date;

import javax.annotation.Nullable;

import com.vividsolutions.jts.geom.Point;

import org.polymap.core.model2.Concerns;
import org.polymap.core.model2.DefaultValue;
import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Mixins;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.SRS;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Description("Beschreibung dieses Datentyps")
@Concerns( LogConcern.class )
@Mixins( {TrackableMixin.class} )
@SRS( "EPSG:31468" )
public abstract class Person
        extends Entity {
    
    protected Property<Point>       geom;

    @Nullable
    protected Property<String>      name;

    /** Defaults to "Ulli". Not Nullable. */
    @DefaultValue("Ulli")
    protected Property<String>      firstname;

    @Nullable
    protected Property<Date>        birthday;
 
}
