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

import java.io.IOException;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.store.PropertyDescriptor;
import org.polymap.core.model2.store.StoreRuntimeContext;
import org.polymap.core.model2.store.StoreSPI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureStoreAdapter
        implements StoreSPI {

    private static Log log = LogFactory.getLog( FeatureStoreAdapter.class );

    private StoreRuntimeContext         context;
    
    private DataAccess                  store;


    public FeatureStoreAdapter( DataAccess store ) {
        assert store != null;
        this.store = store;
    }


    public void init( StoreRuntimeContext _context ) {
        this.context = _context;
        EntityRepository repo = context.getRepository();
    
        // check/create/update schemas
        FeatureStoreUnitOfWork uow = (FeatureStoreUnitOfWork)createUnitOfWork();
        for (Class<? extends Entity> entityClass : repo.getConfig().getEntities()) {
            SimpleFeatureType entitySchema = simpleFeatureType( entityClass );
            try {
                // check schema
                log.info( "Checking FeatureSource: " + entitySchema.getTypeName() + " ..." ); 
                FeatureSource fs = store.getFeatureSource( entitySchema.getName() );
                // update
                if (! fs.getSchema().equals( entitySchema )) {
                    try {
                        store.updateSchema( entitySchema.getName(), entitySchema );
                    }
                    catch (UnsupportedOperationException e) {
                        log.warn( "", e );
                    }
                }
            }
            catch (IOException e) {
                // create
                try {
                    log.info( "Error. Creating schema: " + entitySchema ); 
                    store.createSchema( entitySchema );
                }
                catch (IOException e1) {
                    throw new ModelRuntimeException( e1 );
                }
            }
        }
    }


    public void close() {
    }

    
    public Object stateId( Object state ) {
        return ((Feature)state).getIdentifier().getID();
    }


    public DataAccess getStore() {
        return store;
    }

    
    public Property createProperty( final PropertyDescriptor descriptor ) {                
        if (descriptor.getParent() != null) {
            throw new UnsupportedOperationException( "Complex FeatureType is not supported yet." );
        }
        return new PropertyImpl( descriptor );
    }


    public UnitOfWork createUnitOfWork() {
        return new FeatureStoreUnitOfWork( context, this );
    }
    
    
    public <T extends Entity> FeatureType featureType( Class<T> entityClass ) {
        try {
            return new FeatureTypeBuilder( entityClass ).build();
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }

    
    public <T extends Entity> SimpleFeatureType simpleFeatureType( Class<T> entityClass ) {
        try {
            return new SimpleFeatureTypeBuilder( entityClass ).build();
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }

    
    /**
     * 
     */
    protected final class PropertyImpl
            implements Property {
    
        //private final PropertyDescriptor    descriptor;
    
        @SuppressWarnings("hiding")
        private final EntityRuntimeContext  context;
    
        private final String                propName;
    
    
        protected PropertyImpl( PropertyDescriptor descriptor ) {
            //this.descriptor = descriptor;
            context = descriptor.getContext();
            propName = descriptor.getNameInStore();
        }
    
    
        public Object get() {
            Feature feature = (Feature)context.state();
            org.opengis.feature.Property prop = feature.getProperty( propName );
            assert prop != null : "No such Feature property: " + propName;
            return feature.getProperty( propName ).getValue();
        }
    
    
        public void set( Object value ) {
            Feature feature = (Feature)context.state();
            org.opengis.feature.Property underlying = feature.getProperty( propName );
            assert underlying != null : "No such Feature property: " + propName;
            underlying.setValue( value );
            
            if (context.status() != EntityStatus.CREATED) {
                ((FeatureStoreUnitOfWork)context.unitOfWork())
                        .markPropertyModified( feature, (AttributeDescriptor)underlying.getDescriptor(), value );
            }
        }
    
    
        public PropertyInfo getInfo() {
            return new PropertyInfo() {

                public Entity getEntity() {
                    throw new RuntimeException( "Not yet implemented." );
                }
                
                public String getName() {
                    return propName;
                }
            };
        }
    }
    
}
