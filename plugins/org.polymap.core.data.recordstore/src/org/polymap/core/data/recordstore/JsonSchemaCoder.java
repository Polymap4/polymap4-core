/* 
 * polymap.org
 * Copyright (C) 2012-2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.data.recordstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.util.SimpleInternationalString;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.data.util.NameImpl;

/**
 * Encode/decode {@link FeatureType} to/from JSON.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class JsonSchemaCoder {

    private static Log log = LogFactory.getLog( JsonSchemaCoder.class );


    public String encode( FeatureType schema ) throws Exception {
        return new Encoder( schema ).run();    
    }
    
    public FeatureType decode( String jsonInput ) throws Exception{
        return new Decoder( jsonInput ).run();
    }


    /**
     * 
     */
    class Encoder {

        private FeatureType     schema;
        
        
        public Encoder( FeatureType schema ) {
            this.schema = schema;
        }

        
        public String run() throws Exception {
            // encode CRS and default geom
            JSONObject json = new JSONObject();
            CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
            if (crs != null) {
                json.put( "srs", Geometries.srs( crs ) );
            }
            GeometryDescriptor geom = schema.getGeometryDescriptor();
            if (geom != null) {
                json.put( "defaultGeom", geom.getLocalName() );
            }
            json.put( "isIdentified", schema.isIdentified() );
            json.put( "isSimple", schema instanceof SimpleFeatureType );
            if (schema.getName().getNamespaceURI() != null) {
                json.put( "namespace", schema.getName().getNamespaceURI() );
            }
            encode( json, schema );
            return json.toString( 4 );
        }
        
        
        protected JSONObject encode( JSONObject parent, ComplexType type ) throws Exception {
            parent.put( "isInline", type.isInline() );
            encode( parent, (AttributeType)type );
            parent.put( "type", "ComplexType" );
            
            JSONArray descriptorsJson = new JSONArray();
            parent.put( "descriptors", descriptorsJson );
            
            for (PropertyDescriptor descriptor : type.getDescriptors()) {
                JSONObject json = new JSONObject();
                descriptorsJson.put( json );
                json.put( "isNillable", descriptor.isNillable() );
                json.put( "maxOccurs", descriptor.getMaxOccurs() );
                json.put( "minOccurs", descriptor.getMinOccurs() );
                
                if (descriptor.getType() instanceof ComplexType) {
                    encode( json, (ComplexType)descriptor.getType() );
                }
                else if (descriptor.getType() instanceof AttributeType) {
                    // defaultValue
                    Object defaultValue = ((AttributeDescriptor)descriptor).getDefaultValue();
                    if (defaultValue != null) {
                        if (defaultValue instanceof String
                                || defaultValue instanceof Boolean
                                || defaultValue instanceof Integer
                                || defaultValue instanceof Long
                                || defaultValue instanceof Float
                                || defaultValue instanceof Double) {
                            json.putOpt( "defaultValue", defaultValue );                        
                        }
                        else if (defaultValue instanceof Date) {
                            json.putOpt( "defaultValue", new Long( ((Date)defaultValue).getTime() ) );                        
                        }
                        else {
                            throw new RuntimeException( "Default value not supported for binding: " + defaultValue.getClass().toString() );                            
                        }
                    }
                    encode( json, (AttributeType)descriptor.getType() );
                }
                else {
                    throw new RuntimeException( "PropertyType not supported yet: " + descriptor.getType() );
                }
            }
            return parent;
        }

        
        protected JSONObject encode( JSONObject parent, AttributeType type )
        throws Exception {
            if (type.getRestrictions() != null && !type.getRestrictions().isEmpty()) {
                log.warn( "Restrictions are not supported yet." );
                //throw new RuntimeException( "Restrictions are not supported yet.");
            }
            String name = encode( type.getName() );
            parent.put( "name", name );
            parent.put( "type", "AttributeType" );
            parent.put( "description", type.getDescription() );
            parent.put( "binding", type.getBinding().getName() );
            parent.put( "isAbstract", type.isAbstract() );

            if (type.getSuper() != null) {
                JSONObject superJson = new JSONObject();
                parent.put( "super", superJson );
                encode( superJson, type.getSuper() );
            }
            
            if (type instanceof GeometryType) {
                CoordinateReferenceSystem crs = ((GeometryType)type).getCoordinateReferenceSystem();
                if (crs != null) {
                    parent.put( "srs", Geometries.srs( crs ) );
                }
                parent.put( "type", "GeometryType" );
                
                // FIXME for unknown reason shapefile schema seem to have something like "MultiPolygon"
                // as the geom name
                if (type.getBinding().getName().endsWith( name )) {
                    parent.put( "name", schema.getGeometryDescriptor().getLocalName() );
                }
            }
            return parent;
        }
        
        
        protected String encode( Name name ) {
            return name.getLocalPart();
//            if (!name.getSeparator().equals( NameImpl.DEFAULT_SEPARATOR )) {
//                throw new UnsupportedOperationException( "Name sparator is not default: " + name.getSeparator() );
//            }
//            return name.getURI();
        }
    }
    
    
    /**
     * 
     */
    class Decoder {

        private JSONObject          input;
        
        private ClassLoader         cl = Thread.currentThread().getContextClassLoader();
        
        private String              namespace;
        
        private FeatureTypeFactory  factory = new FeatureTypeFactoryImpl();
        
        
        public Decoder( String jsonInput ) throws Exception  {
            this.input = new JSONObject( jsonInput );
        }

        
        public FeatureType run() throws Exception {
            namespace = input.optString( "namespace" );
            namespace = namespace.length() == 0 ? null : namespace;
            //CoordinateReferenceSystem crs = CRS.decode( input.optString( "srs",  );
            //json.put( "isIdentified", schema.isIdentified() );
            String defaultGeomName = input.optString( "defaultGeom" );
            log.debug( "isSimple:" + input.optString( "isSimple" ) );
            boolean isSimple = Boolean.parseBoolean( input.optString( "isSimple", "true" ) );
            
            ComplexType complexType = decodeComplexType( input );
            
            GeometryDescriptor geom = null;
            for (PropertyDescriptor prop : complexType.getDescriptors()) {
                if (prop instanceof GeometryDescriptor
                        && prop.getName().getLocalPart().equals( defaultGeomName ))
                    geom = (GeometryDescriptor)prop;
            }
            
            if (isSimple) {
                List<AttributeDescriptor> attrs = new ArrayList( complexType.getDescriptors() );
                return factory.createSimpleFeatureType( complexType.getName(), attrs,
                        geom, complexType.isIdentified(), complexType.getRestrictions(),
                        complexType.getSuper(), complexType.getDescription() );                
            }
            else {
                return factory.createFeatureType( complexType.getName(), complexType.getDescriptors(),
                        geom, complexType.isIdentified(), complexType.getRestrictions(),
                        complexType.getSuper(), complexType.getDescription() );
            }
        }
        
        
        protected ComplexType decodeComplexType( JSONObject json ) throws Exception {
            AttributeType attType = decodeAttributeType( json );

            Collection<PropertyDescriptor> properties = new ArrayList(); 

            JSONArray descriptorsJson = json.getJSONArray( "descriptors" );
            for (int i=0; i<descriptorsJson.length(); i++) {
                JSONObject descriptorJson = descriptorsJson.getJSONObject( i );
                
                String typeStr = descriptorJson.getString( "type" );
                int minOccurs = descriptorJson.getInt( "minOccurs" );
                int maxOccurs = descriptorJson.getInt( "maxOccurs" );
                boolean isNillable = descriptorJson.getBoolean( "isNillable" );
                Object defaultValue = descriptorJson.opt( "defaultValue" );
                
                // attribute
                if (typeStr.equals( "AttributeType" )) {
                    AttributeType propType = decodeAttributeType( descriptorJson );
                    if (defaultValue != null) {
                        Class<?> binding = propType.getBinding();
                        if (Date.class.isAssignableFrom( binding ) && defaultValue instanceof Integer) {
                            defaultValue = new Date( (Integer)defaultValue );
                        }
                        else if (Date.class.isAssignableFrom( binding ) && defaultValue instanceof Long) {
                            defaultValue = new Date( (Long)defaultValue );
                        }
                        else if (Long.class.isAssignableFrom( binding ) && defaultValue instanceof Integer) {
                            defaultValue = ((Integer)defaultValue).longValue();
                        }
                        else if (Double.class.isAssignableFrom( binding ) && defaultValue instanceof Float) {
                            defaultValue = ((Float)defaultValue).doubleValue();
                        }
                        assert binding.isAssignableFrom( defaultValue.getClass() ) : "defaultValue and binding do not match: " + defaultValue.getClass() + " -> " + binding;
                    }
                    properties.add( factory.createAttributeDescriptor( propType, 
                            propType.getName(), minOccurs, maxOccurs, isNillable, defaultValue ) );
                }
                // geometry
                else if (typeStr.equals( "GeometryType" )) {
                    CoordinateReferenceSystem crs = null;
                    String srs = descriptorJson.optString( "srs", null );
                    crs =  srs != null ? Geometries.crs( srs ) : null;
                    AttributeType propType = decodeAttributeType( descriptorJson );
                    
                    GeometryType geomType = factory.createGeometryType( propType.getName(),
                            propType.getBinding(), crs, propType.isIdentified(), propType.isAbstract(),
                            propType.getRestrictions(), propType.getSuper(), propType.getDescription() );
                    
                    properties.add( factory.createGeometryDescriptor( geomType, 
                            geomType.getName(), minOccurs, maxOccurs, isNillable, defaultValue ) );
                }
                // complex
                else if (typeStr.equals( "ComplexType" )) {
                    @SuppressWarnings("unused")
                    ComplexType propType = decodeComplexType( descriptorJson );                    
                }
                else {
                    throw new RuntimeException( "PropertyType not supported yet: " + typeStr );
                }
            }
            
            return factory.createComplexType( attType.getName(), properties, 
                    attType.isIdentified(), attType.isAbstract(), attType.getRestrictions(), 
                    attType.getSuper(), attType.getDescription() );
        }
        

        protected AttributeType decodeAttributeType( JSONObject json ) throws Exception {
            Name name = decodeName( json.getString( "name" ) );
            Class binding = cl.loadClass( json.getString( "binding" ) );
            boolean isAbstract = json.getBoolean( "isAbstract" );
            List<Filter> restrictions = null;

            InternationalString description = json.has( "description" )
                    ? SimpleInternationalString.wrap( json.getString( "description" ) )
                    : null;
            
            AttributeType superType = json.has( "super" )
                    ? decodeAttributeType( json.getJSONObject( "super" ) )
                    : null;
                    
            return factory.createAttributeType( name, binding, false, isAbstract, restrictions, superType, description ); 
        }
        
        
        protected Name decodeName( String name ) {
            return namespace != null ? new NameImpl( namespace, name ) : new NameImpl( name );
        }
        
    }
    
    
//    // Test ***********************************************
//    
//    public static void main( String[] args ) 
//    throws Exception {
//        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
//        builder.setName( "Test" );
//        builder.setCRS( CRS.decode( "EPSG:4326" ) );
//        builder.add( "name", String.class );
//        builder.add( "geom", MultiLineString.class, "EPSG:4326" );
//        
//        String encoded = new JsonSchemaCoder().encode( builder.buildFeatureType() );
//        System.out.println( encoded );
//
//        FeatureType schema = new JsonSchemaCoder().decode( encoded );
//        System.out.println( "\n\n" + schema );
//    }
//    
}
