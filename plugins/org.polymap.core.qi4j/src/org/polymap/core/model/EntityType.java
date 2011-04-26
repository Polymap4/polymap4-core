/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */

package org.polymap.core.model;

import java.util.Collection;

import java.lang.reflect.Type;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Provides information about and access to properties and associations of an
 * {@link Entity} type.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public interface EntityType {

    public String getName();
    
    public Class<? extends Entity> getType();
    
    public Collection<Property> getProperties();
    
    public Property getProperty( String name );
    
    
    /**
     * Information about a property of an {@link EntityType}. 
     */
    public interface Property {
        
        public String getName();
        
        public Class getType();

        public Object getValue( Entity entity ) 
        throws Exception;
        
        public void setValue( Entity entity, Object value )
        throws Exception;

    }
    
    /**
     * Information about an association of an {@link EntityType}. 
     */
    public interface Association {
        
        public String getName();
        
        public Type getType();

        public Object getValue( Entity entity ) 
        throws Exception;
        
        public void setValue( Entity entity, Object value )
        throws Exception;

    }
    
}
