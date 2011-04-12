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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.LegalAttributeType;

import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Defines a mapping between an attribute of the source {@link FeatureType} and
 * a new target FeatureType. Used by {@link FeatureTypeEditorProcessor}.
 * 
 * @see FeatureTypeEditorProcessor
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class AttributeMapping {

    private static final Log log = LogFactory.getLog( FeatureTypeMapping.class );

    /** The name of the target attribute. */
    String                    name;

    Class                     binding;

    CoordinateReferenceSystem crs;

    /** The name of the source attribute. */
    String                    sourceName;

    String                    constantValue;


    public AttributeMapping( String name, Class binding, CoordinateReferenceSystem crs,
            String sourceName, String constantValue ) {
        this.name = name;
        this.binding = binding;
        this.crs = crs;
        this.sourceName = sourceName;
        this.constantValue = constantValue;
    }

    public AttributeMapping( String serialized ) {
        try {
            JSONObject json = new JSONObject( serialized );
            name = json.getString( "name" );
            constantValue = decode( json.getString( "constantValue" ) );
            sourceName = decode( json.getString( "sourceName" ) );
            // binding
            List<LegalAttributeType> legalTypes = LegalAttributeType.types();
            for (LegalAttributeType type : legalTypes) {
                if (type.getType().getName().equals( json.getString( "binding" ) )) {
                    binding = type.getType();
                    break;
                }
            }
            if (binding == null) {
                log.warn( "No such legal attribute type: " + json.getString( "binding" ) );
                binding = legalTypes.get( 0 ).getType();
            }
            // crs
            if (json.has( "crs" )) {
                String crsCode = decode( json.getString( "crs" ) );
                if (crsCode != null) {
                    crs = CRS.decode( crsCode );
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }

    public String serialize() {
        try {
            JSONObject json = new JSONObject();
            json.put( "name", name );
            json.put( "binding", binding.getName() );
            json.put( "constantValue", encode( constantValue ) );
            json.put( "sourceName", encode( sourceName ) );

            if (crs == null) {
                json.put( "crs", encode( null ) );
            }
            else {
                if (!crs.getIdentifiers().isEmpty()) {
                    Object next = crs.getIdentifiers().iterator().next();
                    if (next instanceof Identifier) {
                        Identifier identifier = (Identifier) next;
                        json.put( "crs", identifier.toString() );

                        //                    if (identifier.getAuthority().getTitle().equals(
                        //                            "European Petroleum Survey Group")) {
                        //                        crsCode.set( this, "EPSG:" + identifier.getCode() );
                        //                        this.crs = crs;
                        //                    }
                    }
                }
            }

            return json.toString();
        }
        catch (JSONException e) {
            throw new RuntimeException( e.getLocalizedMessage(), e );
        }
    }

    public String toString() {
        return "AttributeMapping[" + serialize() + "]";
    }

    private Object encode( Object value ) {
        return value != null ? value : "NULL";
    }

    private String decode( String value ) {
        return (value == null || value.equals( "NULL" )) ? null : value;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName( String sourceName ) {
        this.sourceName = sourceName;
    }
    
}