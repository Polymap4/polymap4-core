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
package org.polymap.core.model2.store.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import java.lang.reflect.Field;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.SimpleInternationalString;
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

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.engine.PropertyInfoImpl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FeatureTypeBuilder {

    private static Log log = LogFactory.getLog( FeatureTypeBuilder.class );
    
    protected FeatureTypeFactory        factory;

    protected Class<? extends Entity>   entityClass;
    
    private String                      namespace;

    private CoordinateReferenceSystem   crs;
    
    private ClassLoader                 cl;
    
    
    public FeatureTypeBuilder( Class<? extends Entity> entityClass ) 
    throws Exception {
        this.factory = new FeatureTypeFactoryImpl();
        this.cl = Thread.currentThread().getContextClassLoader();
        this.entityClass = entityClass;
        
        SRS srs = entityClass.getAnnotation( SRS.class );
        if (srs != null) {
            this.crs = CRS.decode( srs.value() );
        }
        else {
            log.warn( "No SRS annotation defined. Using EPSG:4326 for: " + entityClass );
            this.crs = CRS.decode( "EPSG:4326" );
        }
    }


    public FeatureType build() throws Exception {
        ComplexType complexType = buildComplexType( entityClass );
        
        // find geom
        GeometryDescriptor geom = null;
        for (PropertyDescriptor prop : complexType.getDescriptors()) {
            if (prop instanceof GeometryDescriptor)
                geom = (GeometryDescriptor)prop;
        }
        
        return factory.createFeatureType( complexType.getName(), complexType.getDescriptors(),
                geom, complexType.isIdentified(), complexType.getRestrictions(),
                complexType.getSuper(), complexType.getDescription() );
    }
    
    
    protected ComplexType buildComplexType( Class<? extends Composite> compositeClass ) 
    throws Exception {
        // fields -> properties
        Collection<PropertyDescriptor> properties = new ArrayList();
        Class superClass = compositeClass; 
        for (;superClass != null; superClass = superClass.getSuperclass()) {
            for (Field field : superClass.getDeclaredFields()) {
                
                // Property and CollectionProperty
                if (Property.class.isAssignableFrom( field.getType() )) {

                    PropertyInfoImpl propInfo = new PropertyInfoImpl( field );
                    Class<?> binding = propInfo.getType();

                    // attribute
                    if (binding.isPrimitive() 
                            || binding.equals( String.class )
                            || Number.class.isAssignableFrom( binding )
                            || Boolean.class.isAssignableFrom( binding )
                            || Date.class.isAssignableFrom( binding )) {

                        AttributeType propType = buildAttributeType( field, binding );
                        properties.add( factory.createAttributeDescriptor( propType, 
                                propType.getName(), 0, propInfo.getMaxOccurs(), propInfo.isNullable(), propInfo.getDefaultValue() ) );
                    }
                    // geometry
                    else if (Geometry.class.isAssignableFrom( binding )) {
                        AttributeType propType = buildAttributeType( field, binding );

                        GeometryType geomType = factory.createGeometryType( propType.getName(),
                                propType.getBinding(), crs, propType.isIdentified(), propType.isAbstract(),
                                propType.getRestrictions(), propType.getSuper(), propType.getDescription() );

                        properties.add( factory.createGeometryDescriptor( geomType, 
                                geomType.getName(), 0, 1, propInfo.isNullable(), propInfo.getDefaultValue() ) );
                    }
                    // complex
                    else if (Composite.class.isAssignableFrom( binding )) {
                        ComplexType propType = buildComplexType( (Class<? extends Composite>)binding );                    
                    }
                    else {
                        throw new RuntimeException( "Property value type is not supported: " + binding );
                    }
                }
            }
        }
        
        NameInStore nameInStore = compositeClass.getAnnotation( NameInStore.class );
        Name name = buildName( nameInStore != null ? nameInStore.value() : compositeClass.getSimpleName() );
        boolean isIdentified = false;
        boolean isAbstract = false;
        List<Filter> restrictions = null;
        AttributeType superType = null;
        Description annotation = compositeClass.getAnnotation( Description.class );
        InternationalString description = annotation != null
                ? SimpleInternationalString.wrap( annotation.value() )
                : null;
                
        return factory.createComplexType( name, properties, isIdentified, isAbstract, 
                restrictions, superType, description );
    }
    

    protected AttributeType buildAttributeType( Field field, Class<?> binding ) 
    throws Exception {
        NameInStore nameInStore = field.getAnnotation( NameInStore.class );
        Name name = buildName( nameInStore != null ? nameInStore.value() : field.getName() );
        boolean isAbstract = false;
        List<Filter> restrictions = null;

        Description annotation = field.getAnnotation( Description.class );
        InternationalString description = annotation != null
                ? SimpleInternationalString.wrap( annotation.value() )
                : null;
        
        AttributeType superType = null;
                
        return factory.createAttributeType( name, binding, false, isAbstract, restrictions, superType, description ); 
    }
    
    
    protected Name buildName( String name ) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException( "Empty name given." );
        }
        else {
            return new NameImpl( namespace, name );
        }
    }
    
}
