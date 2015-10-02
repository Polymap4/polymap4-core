/* 
 * polymap.org
 * Copyright (C) 2012-2013, Falko Bräutigam. All rights reserved.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.IOException;
import java.net.URI;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureSource;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.recordstore.RFeatureStore.TransactionState;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.LazyInit;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.UnknownSizeCacheLoader;

import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.IRecordStore.Updater;
import org.polymap.recordstore.ResultSet;
import org.polymap.recordstore.SimpleQuery;

/**
 * The DataStore of a {@link RSFeatureStore}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RDataStore
        implements DataAccess {

    private static Log log = LogFactory.getLog( RDataStore.class );

    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );

    // FIXME this was from FeatureTypeEditor; see comments on usage
    private static final String         ORIG_NAME_KEY = "_orig_name_";

    protected IRecordStore              store;
    
    protected QueryDialect              queryDialect;
    
    protected FeatureListenerManager    listeners = new FeatureListenerManager();
    
    /** The currently running tx or null. */
    protected TransactionState          runningTx;

    /* Changed inside updater so no extra synch is needed. */
    private Cache<Name,FeatureType>     schemas = CacheConfig.DEFAULT.initSize( 64 ).createCache();
    
    private LazyInit<Set<Name>>         schemaNames;
    
    private ServiceInfo                 info;

    private JsonSchemaCoder             schemaCoder = new JsonSchemaCoder();
    
    
    public RDataStore( IRecordStore store, QueryDialect queryDialect ) throws Exception {
        this.store = store;
        this.queryDialect = queryDialect;
        this.queryDialect.initStore( store );
        
        // lazy load schemaNames
        this.schemaNames = new CachedLazyInit( () -> {
            try {
                ResultSet rs = RDataStore.this.store.find ( 
                        new SimpleQuery().setMaxResults( 10000 ).eq( "type", "FeatureType" ) );

                HashSet result = new HashSet( rs.count()*2 );

                for (IRecordState entry : rs) {
                    String namespace = entry.get( "namespace" );
                    String localpart = entry.get( "name" );
                    Name name = namespace == null ? new NameImpl( namespace, localpart ) : new NameImpl( localpart );
                    if (!result.add( name )) {
                        throw new IllegalStateException( "Name already loaded: " + name );
                    }
                }
                return result;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        });
    }

    
    public FeatureType getSchema( Name name ) {
        if (!schemaNames.get().contains( name )) {
            return null;
        }
        return schemas.get( name, new UnknownSizeCacheLoader<Name,FeatureType,RuntimeException>() {
            public FeatureType load( Name key ) {
                try {
                    // query the store
                    assert key.getLocalPart() != null;
                    SimpleQuery query = new SimpleQuery().setMaxResults( 1 )
                             .eq( "type", "FeatureType" )
                             .eq( "name", key.getLocalPart() );
                    if (key.getNamespaceURI() != null) {
                        query.eq( "namespace", key.getNamespaceURI() );
                    }
                    ResultSet rs = store.find( query );
                    assert rs.count() == 1 : "Illegal size of result set: " + rs.count();
                    IRecordState entry = rs.iterator().next();
                    
                    FeatureType schema = schemaCoder.decode( (String)entry.get( "content" ) );
                    log.debug( "Decoded schema: " + schema );
                    return schema;

//                    // check if schema is simple; build SimpleFeatureType for compatibility
//                    // This is needed as long as pipeline does not fully support complex types
//                    SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
//                    ftb.setName( schema.getName() );
//                    for (PropertyDescriptor prop : schema.getDescriptors()) {
//                        if (prop instanceof GeometryDescriptor) {
//                            ftb.add( prop.getName().getLocalPart(), prop.getType().getBinding(),
//                                    ((GeometryDescriptor)prop).getCoordinateReferenceSystem() );                    
//                        }
//                        else {
//                            ftb.add( prop.getName().getLocalPart(), prop.getType().getBinding() );
//                        }
//                    }
//                    return ftb.buildFeatureType();
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        });
    }

    
    @Override
    public void removeSchema( Name typeName ) throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void dispose() {
        if (store != null) {
            // don't close store as it might be used by others
            store = null;
            listeners = null;
            schemas = null;
        }
    }

    
    @Override
    protected void finalize() throws Throwable {
        dispose();
    }

    
    public IRecordStore getStore() {
        return store;
    }


    @Override
    public List<Name> getNames() throws IOException {
        return ImmutableList.copyOf( schemaNames.get() );
    }


    @Override
    public FeatureSource getFeatureSource( Name name ) throws IOException {
        FeatureType schema = getSchema( name );
        if (schema == null) {
            throw new IOException( "Schema does not exist: " + name );
        }
        return new RFeatureStore( this, schema );
    }


    @Override
    public void createSchema( FeatureType schema ) throws IOException {
        assert schema != null : "schema is null";
        if (!schemaNames.get().add( schema.getName() )) {
            throw new IOException( "Schema name already exists: " + schema.getName() );                
        }
        if (schemas.putIfAbsent( schema.getName(), schema ) != null) {
            throw new IOException( "Schema name already exists: " + schema.getName() );
        }
        
        Updater tx = runningTx != null ? runningTx.updater() : store.prepareUpdate();
        try {
            String schemaContent = schemaCoder.encode( schema );
            log.debug( "Created schema: " + schemaContent );
            
            IRecordState schemaRecord = store.newRecord()
                    .put( "type", "FeatureType" )
                    .put( "name", schema.getName().getLocalPart() )
                    .put( "qname", schema.getName().getURI() )
                    .put( "content", schemaContent );
            if (schema.getName().getNamespaceURI() != null) {
                schemaRecord.put( "namespace", schema.getName().getNamespaceURI() );
            }
            
//          Configuration config = new WFSConfiguration();
//          Encoder encoder = new Encoder( config );
//          encoder.setIndenting( true );
//          encoder.setIndentSize( 4 );
//          encoder.encode( schema, GML.FeatureCollectionType, System.out );
          //encoder.setEncoding(Charset.forName( global.getCharset() ));

            tx.store( schemaRecord );
            if (runningTx == null) {
                tx.apply();
            }
        }
        catch (Throwable e) {
            log.debug( "", e );
            if (runningTx == null) {
                tx.discard();
            }
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            else {
                throw new IOException( e );
            }
        }
    }


    @Override
    public void updateSchema( Name name, final FeatureType newSchema ) throws IOException {
        assert name != null && newSchema != null;
        
        final Updater tx = store.prepareUpdate();
        try {
            // check modified property names
            boolean namesModified = false;
            for (PropertyDescriptor desc : newSchema.getDescriptors()) {
                // set by FeatureTypeEditor/AttributeCellModifier
                String origName = (String)desc.getUserData().get( ORIG_NAME_KEY );
                if (origName != null) {
                    namesModified = true;
                }
            }
            
            // find deleted properties
            // XXX check complex schemas
            FeatureType schema = getSchema( name );
            final List<PropertyDescriptor> deleted = new ArrayList();
            for (PropertyDescriptor desc : schema.getDescriptors()) {
                if (newSchema.getDescriptor( desc.getName() ) == null) {
                    deleted.add( desc );
                }
            }
            
            // schema name changed or prop deleted? -> update features
            final String newName = newSchema.getName().getLocalPart();
            if (!name.getLocalPart().equals( newSchema.getName().getLocalPart() )
                    || !deleted.isEmpty() || namesModified) {
                
                FeatureSource fs = getFeatureSource( name );
                fs.getFeatures().accepts( new FeatureVisitor() {
                    public void visit( Feature feature ) {
                        try {
                            // typeName
                            ((RFeature)feature).state.put( RFeature.TYPE_KEY, newName );
                            
                            // modified attribute name
                            //List<Name> origModifiedNames = new ArrayList();
                            for (PropertyDescriptor desc : newSchema.getDescriptors()) {
                                // set by FeatureTypeEditor/AttributeCellModifier
                                String origName = (String)desc.getUserData().get( ORIG_NAME_KEY );
                                if (origName != null) {
                                    RAttribute prop = (RAttribute)feature.getProperty( origName );
                                    if (prop != null) {
                                        if (prop.getValue() != null) {
                                            ((RFeature)feature).state.put( desc.getName().getLocalPart(), prop.getValue() );
                                        }
                                        ((RFeature)feature).state.remove( prop.key.toString() );
                                    }
                                }
                            }
                            
                            // deleted attributes
                            for (PropertyDescriptor desc : deleted) {
                                // XXX check complex schemas
                                RProperty prop = (RProperty)feature.getProperty( desc.getName() );
                                ((RFeature)feature).state.remove( prop.key.toString() );
                            }
                            
                            tx.store( ((RFeature)feature).state );
                        }
                        catch (Exception e) {
                            // Designing a visitor interface without Exception is not a good idea!
                            throw new RuntimeException( "", e );
                        }
                    }
                }, null );
            }
        
            // update schema record
            ResultSet rs = store.find ( new SimpleQuery().setMaxResults( 1 )
                    .eq( "type", "FeatureType" )
                    .eq( "name", name.getLocalPart() ) );

            IRecordState record = rs.get( 0 );
            String schemaContent = schemaCoder.encode( newSchema );
            record.put( "content", schemaContent );
            record.put( "name", newName );
            tx.store( record );

            log.debug( "Current schema: " + schemaCoder.encode( schema ) );
            log.debug( "Updated schema: " + schemaContent );

            // update schemas cache
            schemas.remove( name );
            //schemas.put( newSchema.getName(), newSchema );
            
            tx.apply();
        }
        catch (Throwable e) {
            log.debug( "", e );
            tx.discard();
            throw new RuntimeException( e );
        }
    }


    public void deleteSchema( FeatureType schema, IProgressMonitor monitor ) {
        if (!schemaNames.get().remove( schema.getName() )) {
            throw new IllegalArgumentException( "Schema does not exist: " + schema.getName() );                
        }
        if (schemas.putIfAbsent( schema.getName(), schema ) != null) {
            throw new IllegalArgumentException( "Schema does not exist: " + schema.getName() );
        }

        // remove features
        try {
            RFeatureStore fs = (RFeatureStore)getFeatureSource( schema.getName() );
            fs.removeFeatures( Filter.INCLUDE );
        }
        catch (Exception e) {
            log.debug( "", e );
            throw new RuntimeException( e );
        }
        
        // remove schema
        Updater tx = store.prepareUpdate();
        try {
            ResultSet rs = store.find ( new SimpleQuery().setMaxResults( 1 )
                    .eq( "type", "FeatureType" )
                    .eq( "name", schema.getName().getLocalPart() )
                    .eq( "namespace", schema.getName().getNamespaceURI() ) );

            tx.remove( rs.get( 0 ) );
            
            schemas.remove( schema.getName() );
            tx.apply();
        }
        catch (Throwable e) {
            log.debug( "", e );
            tx.discard();
            throw new RuntimeException( e );
        }
    }


    @Override
    public ServiceInfo getInfo() {
        if (info == null) {
            info = new ServiceInfo() {
    
                public String getDescription() {
                    return "POLYMAP3 record store powered by Lucene";
                }
    
                public Set<String> getKeywords() {
                    Set<String> result = new HashSet();
                    result.add( "Lucene" );
                    result.add( "POLYMAP3" );
                    result.add( "RecordStore" );
                    return result;
                }
    
                public URI getPublisher() {
                    return URI.create( "http://polymap.org/" );
                }
    
                public URI getSchema() {
                    // XXX Auto-generated method stub
                    throw new RuntimeException( "not yet implemented." );
                }
    
                @Override
                public URI getSource() {
                    // XXX Auto-generated method stub
                    throw new RuntimeException( "not yet implemented." );
                }
    
                @Override
                public String getTitle() {
                    // XXX Auto-generated method stub
                    throw new RuntimeException( "not yet implemented." );
                }
            };
        }
        return info;
    }
    
}
