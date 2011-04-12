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
package org.polymap.core.data.feature.typeeditor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;


/**
 * 
 * @see FeatureTypeEditorProcessor
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureTypeMapping
        implements Iterable<AttributeMapping> {

    private static final Log log = LogFactory.getLog( FeatureTypeMapping.class );

    /** The name of the target {@link FeatureType}. */
    private String                      name = "_mappedFeatureType_";

    /** Maps target attribute name into mapping. */
    private Map<String, AttributeMapping> mappings = new HashMap();

    
    /**
     * Constructs an empty FeatureTypeMapping with no feature name.
     */
    public FeatureTypeMapping() {
    }

    /**
     * 
     * @param serialized
     */
    public FeatureTypeMapping( String serialized ) {
        try {
            JSONArray array = new JSONArray( serialized );
            for (int i=0; i<array.length(); i++) {
                AttributeMapping mapping = new AttributeMapping( array.getString( i ) );
                mappings.put( mapping.name, mapping );
                log.debug( "    mapping: " + mapping );
            }
        }
        catch (JSONException e) {
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }

    public String serialize() {
        JSONArray json = new JSONArray();
        for (AttributeMapping mapping : mappings.values()) {
            json.put( mapping.serialize() );
        }
        return json.toString();
    }

    
    public String getFeatureTypeName() {
        return name;
    }
    
    public void setFeatureTypeNameName( String name ) {
        this.name = name;
    }

    public AttributeMapping get( String attributeName ) {
        return mappings.get( attributeName );
    }

    public AttributeMapping put( AttributeMapping mapping ) {
        return mappings.put( mapping.name, mapping );
    }

    public Iterator<AttributeMapping> iterator() {
        return mappings.values().iterator();
    }
    
    public String toString() {
        return "FeatureTypeMapping[" + serialize() + "]";
    }

    public SimpleFeatureType newFeatureType() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( name );
        builder.setNamespaceURI( "http://www.polymap.org/" );
        
        for (AttributeMapping mapping : mappings.values()) {
            if (mapping.crs != null) {
                assert Geometry.class.isAssignableFrom( mapping.binding );
                builder.add( mapping.name, mapping.binding, mapping.crs );
                builder.setCRS( mapping.crs );
//              builder.setDefaultGeometry( "the_geom" );
            }
            else {
                builder.add( mapping.name, mapping.binding );
            }
        }
        return builder.buildFeatureType();
    }

}