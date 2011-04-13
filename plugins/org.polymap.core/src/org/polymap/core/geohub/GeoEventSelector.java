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
package org.polymap.core.geohub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.polymap.core.geohub.event.GeoEvent;
import org.polymap.core.geohub.event.GeoEvent.Type;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoEventSelector {

    private List<Filter>            filters;
    
    
    public GeoEventSelector( Filter... filters ) {
        this.filters = new ArrayList( filters.length );
        for (Filter filter : filters) {
            this.filters.add( filter );
        }
    }
    
    public void addFilter( Filter filter ) {
        filters.add( filter );    
    }
    
    boolean accept( GeoEvent ev ) {
        for (Filter filter : filters) {
            if (!filter.accept( ev )) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * The abstract base of all selector filters. Filters are used to define
     * what events a subscriber is interested in. Filters provide an
     * {@link #accept(GeoEvent)} method that provides an implementation. And
     * filters provide their properties, so that external mechanisms, like JMS,
     * can create their selectors out of it.
     */
    public static abstract class Filter {
        
        protected abstract boolean accept( GeoEvent ev );
        
    }

    
    /**
     * 
     */
    public static class MapNameFilter
            extends Filter {
        
        private String          mapName;

        public MapNameFilter( String mapName ) {
            this.mapName = mapName;
        }

        protected boolean accept( GeoEvent ev ) {
            return mapName.equals( ev.getMap() );
        }
        
    }
    
    /**
     * 
     */
    public static class TypeFilter
            extends Filter {
        
        private Set<GeoEvent.Type>      types = new HashSet();

        public TypeFilter( Type... types ) {
            for (GeoEvent.Type type : types) {
                this.types.add( type );
            }
        }

        protected boolean accept( GeoEvent ev ) {
            return types.contains( ev.getType() );
        }
        
    }
    
}
