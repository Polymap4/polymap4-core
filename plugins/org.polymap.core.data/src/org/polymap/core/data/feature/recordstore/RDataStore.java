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
package org.polymap.core.data.feature.recordstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.IOException;
import java.net.URI;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListenerManager;
import org.geotools.data.FeatureSource;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.IRecordStore.Updater;

/**
 * The DataStore of a {@link RSFeatureStore}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RDataStore
        implements DataAccess {

    private static Log log = LogFactory.getLog( RDataStore.class );

    static final FilterFactory2         ff = CommonFactoryFinder.getFilterFactory2( null );

    protected IRecordStore              store;
    
    protected QueryDialect              queryDialect;
    
    protected FeatureListenerManager    listeners = new FeatureListenerManager();

    private Map<Name,FeatureType>       schemas;
    
    private ServiceInfo                 info;

    private JsonSchemaCoder             schemaCoder = new JsonSchemaCoder();
    
    
    public RDataStore( IRecordStore store, QueryDialect queryDialect )
    throws Exception {
        this.store = store;
        this.queryDialect = queryDialect;
        this.queryDialect.initStore( store );
        initSchemas();
    }

    
    protected void initSchemas() throws Exception {
        ResultSet resultSet = store.find ( new SimpleQuery()
                .setMaxResults( 100 ).eq( "type", "FeatureType" ) );
        
        schemas = new HashMap( resultSet.count()*2 );
        for (IRecordState entry : resultSet) {
            FeatureType schema = schemaCoder.decode( (String)entry.get( "content" ) );
            schemas.put( schema.getName(), schema );
        }
    }

    
    public void dispose() {
        if (store != null) {
            store.close();
            store = null;
            listeners = null;
            schemas = null;
        }
    }


    protected IRecordStore getStore() {
        return store;
    }


    public List getNames() throws IOException {
        return new ArrayList( schemas.keySet() );
    }


    public FeatureSource getFeatureSource( Name name )
    throws IOException {
        FeatureType schema = getSchema( name );
        return new RFeatureStore( this, schema );
    }


    public FeatureType getSchema( Name name ) throws IOException {
        return schemas.get( name );
    }


    public void createSchema( FeatureType schema )
    throws IOException {
        Updater tx = store.prepareUpdate();
        try {
            tx.store( store.newRecord()
                    .put( "type", "FeatureType" )
                    .put( "content", schemaCoder.encode( schema ) ) );
            tx.apply();
            schemas.put( schema.getName(), schema );
        }
        catch (Exception e) {
            log.debug( "", e );
            tx.discard();
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            else {
                throw new IOException( e );
            }
        }
    }


    public void updateSchema( Name typeName, FeatureType featureType )
    throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


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
