/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.geohub.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.net.URI;

import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;


/**
 * A base GeoHub event.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoEvent {

    public enum Type {
        /** A feature was created. */
        FEATURE_CREATED,
        /** One or more features were modified. */
        FEATURE_MODIFIED,
        /** One or more features were deleted. */
        FEATURE_DELETED, 
        /** One or more features were un/selected. If body is null, then all features are unselected. */
        FEATURE_SELECTED,
        /** One or more features were hovered. XXX Only selected features?*/
        FEATURE_HOVERED,
        /** One ore more features were displayed. */
        FEATURE_DISPLAYED,
        /** */
        MAP_OPENED,
        MAP_ACTIVATED,
        MAP_CLOSED
    }
    

    private Type                type;
    
    /** The topic of this event. */
    private String              map;
    
    private URI                 resource;
    
    private Collection<Feature> features;
    
    private Map<String,Object>  properties;

    private Filter              filter;
    
    
    /**
     * 
     * @param map The 'topic' of this event.
     * @param layers
     */
    public GeoEvent( Type type, String map, URI resource ) {
        this.type = type;
        this.map = map;
        this.resource = resource;
    }

    public String toString() {
        return "GeoEvent [map=" + map + ", resource=" + resource + ", type=" + type + "]";
    }

    public Type getType() {
        return type;
    }
    
    public String getMap() {
        return map;
    }
    
    public URI getResource() {
        return resource;
    }

    public void setProperty( String name, Object value ) {
        if (properties == null) {
            properties = new HashMap();
        }
        properties.put( name, value );
    }
    
    public Object getProperty( String name ) {
        return properties != null ? properties.get( name ) : null;    
    }
    
    public Collection<Feature> getBody() {
        return features;    
    }
    
    public void setBody( Collection<Feature> features ) {
        this.features = features;
    }

    public void setBody( FeatureCollection fc ) {
        features = new ArrayList( 256 );
        FeatureIterator it = fc.features();
        while (it.hasNext()) {
            features.add( it.next() );
        }
        it.close();
    }
    
    public Filter getFilter() {
        return filter;
    }

    public void setFilter( Filter filter ) {
        this.filter = filter;
    }
    
}
