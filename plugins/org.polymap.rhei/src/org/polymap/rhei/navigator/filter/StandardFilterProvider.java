/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.navigator.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import edu.emory.mathcs.backport.java.util.Collections;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.field.BetweenFormField;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.filter.IFilter;
import org.polymap.rhei.filter.IFilterEditorSite;
import org.polymap.rhei.filter.IFilterProvider;
import org.polymap.rhei.filter.TransientFilter;

/**
 * Provides a standard filter for all {@link SimpleFeatureType} features.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StandardFilterProvider
        implements IFilterProvider {

    private static Log log = LogFactory.getLog( StandardFilterProvider.class );
    
    protected static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );

    public static int       maxResults = 1000;
    
    private ILayer          layer;


    public StandardFilterProvider() {
    }


    public List<IFilter> addFilters( ILayer _layer )
    throws Exception {
        this.layer = _layer;
        IGeoResource geores = layer.getGeoResource();

        if (geores != null && 
                (geores.canResolve( FeatureSource.class ) ||
                 geores.getClass().getSimpleName().equals( "EntityGeoResourceImpl" ))) {
            
            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
            if (fs != null && fs.getPipeline().length() > 0) {
                return Collections.singletonList( new StandardFilter( layer, fs ) );
            }
        }
        return null;
    }
    
    
    /*
     * 
     */
    class StandardFilter
            extends TransientFilter {

        private FeatureSource           fs;
        
        private FeatureType             schema;
        
        
        public StandardFilter( ILayer layer, FeatureSource fs ) 
        throws IOException {
            super( layer.id(), layer, "Standard...", null, null, maxResults );
            this.fs = fs;
            this.schema = fs.getSchema();
        }

        
        public Composite createControl( Composite parent, IFilterEditorSite site ) {
            Composite result = site.createStandardLayout( parent );

            for (PropertyDescriptor descriptor : schema.getDescriptors()) {
                
                Class binding = descriptor.getType().getBinding();
                String propName = descriptor.getName().getLocalPart();
                
                IFormField formField = null;
                IFormFieldValidator validator = null;
                
                // String
                if (String.class.equals( binding )) {
                    formField = new StringFormField();
                    validator = null;
                }
                // Number
                else if (Number.class.isAssignableFrom( binding )) {
                    formField = new StringFormField();
                    validator = new NumberValidator( binding, Polymap.getSessionLocale() );
// FIXME: BeetweenFormField changes Number to String -> does never match                    
//                    formField = new BetweenFormField( new StringFormField(), new StringFormField() );
//                    validator = new BetweenValidator( 
//                            new NumberValidator( binding, Polymap.getSessionLocale() ) );
                }
                // Boolean
                else if (Boolean.class.isAssignableFrom( binding )) {
                    formField = new CheckboxFormField();
                    validator = null;
                }
                // Date
                else if (Number.class.isAssignableFrom( binding )) {
                    formField = new BetweenFormField( new DateTimeFormField(), new DateTimeFormField() );
                    validator = null;
                }
                // Geometry
                else if (Geometry.class.isAssignableFrom( binding )) {
                    // skip
                }
                else {
                    log.warn( "Unknown property type: " + binding );
                }
                if (formField != null) {
                    Composite field = site.newFormField( result, propName, binding, formField, validator );
                    site.addStandardLayout( field );
                }
            }
            return result;
        }

        
        public Filter createFilter( IFilterEditorSite site ) {
            List<Filter> propFilters = new ArrayList();
            
            for (PropertyDescriptor descriptor : schema.getDescriptors()) {
                String propName = descriptor.getName().getLocalPart();
                Object value = site.getFieldValue( propName );
                
                if (value != null) {
                    Class binding = descriptor.getType().getBinding();
                    // String
                    if (String.class.equals( binding )) {
                        propFilters.add( ff.like( 
                                ff.property( propName ), value.toString(), "*", "?", "\\" ) );
                    }
//                    // Number[]
//                    else if (Number.class.isAssignableFrom( binding )) {
//                        Object[] values = (Object[])value;
//                        if (value != null && values[0] != null) { 
//                            propFilters.add( ff.greaterOrEqual( 
//                                    ff.property( propName ), ff.literal( values[0] ) ) );
//                        }
//                        if (value != null && values[1] != null) { 
//                            propFilters.add( ff.lessOrEqual( 
//                                    ff.property( propName ), ff.literal( values[1] ) ) );
//                        }
//                    }
                    // Number
                    else if (Number.class.isAssignableFrom( binding )) {
                        propFilters.add( ff.equals(
                                ff.property( propName ), ff.literal( value ) ) );
                    }
                    // Boolean
                    else if (Boolean.class.isAssignableFrom( binding )) {
                        propFilters.add( ff.equals(
                                ff.property( propName ), ff.literal( value ) ) );
                    }
                    // Date
                    else if (Date.class.isAssignableFrom( binding )) {
                        Object[] values = (Object[])value;
                        if (value != null && values[0] != null) { 
                            propFilters.add( ff.greaterOrEqual( 
                                    ff.property( propName ), 
                                    ff.literal( BetweenFormField.dayStart( (Date)values[0] ) ) ) );
                        }
                        if (value != null && values[1] != null) { 
                            propFilters.add( ff.lessOrEqual( 
                                    ff.property( propName ), 
                                    ff.literal( BetweenFormField.dayEnd( (Date)values[1] ) ) ) );
                        }
                    }
                    //
                    else {
                        throw new UnsupportedOperationException( "Property type not yet implemented: " + binding );
                    }
                }
            }
            Filter result = propFilters.isEmpty() ? Filter.INCLUDE : ff.and( propFilters );
            log.debug( "Filter: " + result );
            return result;
        }

        
        public boolean hasControl() {
            return true;
        }
        
    }

}
