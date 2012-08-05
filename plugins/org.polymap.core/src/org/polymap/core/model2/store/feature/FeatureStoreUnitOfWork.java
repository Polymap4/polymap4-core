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

import static java.util.Collections.*;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import java.io.IOException;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Iterables;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.engine.UnitOfWorkImpl;
import org.polymap.core.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.core.model2.store.StoreRuntimeContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureStoreUnitOfWork
        extends UnitOfWorkImpl
        implements UnitOfWork {

    private static Log log = LogFactory.getLog( FeatureStoreUnitOfWork.class );
    
    public static final FilterFactory   ff = CommonFactoryFinder.getFilterFactory( null );
    
    private static final Transaction    TX_FAILED = new DefaultTransaction( "__failed__" );
    
    private FeatureStoreAdapter         store;

    private ConcurrentMap<FeatureId,FeatureModifications> 
                                        modifications = new ConcurrentHashMap( 1024, 0.75f, 4 );
    
    private Transaction                 tx;
    
    /** Never evicting cache of used {@link FeatureSource} instances. */
    private Cache<Class<?>,FeatureStore> featureSources;

    
    protected FeatureStoreUnitOfWork( StoreRuntimeContext context, FeatureStoreAdapter store ) {
        super( context );
        this.store = store;

        this.featureSources = CacheBuilder.newBuilder().build( new CacheLoader<Class<?>,FeatureStore>() {
            public FeatureStore load( Class<?> entityClass ) throws Exception {
                // name in store
                String typeName = entityClass.getAnnotation( NameInStore.class ) != null
                        ? entityClass.getAnnotation( NameInStore.class ).value()
                        : entityClass.getSimpleName();
                        
                NameImpl name = new NameImpl( typeName );
                return (FeatureStore)FeatureStoreUnitOfWork.this
                        .store.getStore().getFeatureSource( name );
            }
        });
    }

    
    public FeatureStore featureSource( Class<? extends Entity> entityClass ) {
        try {
            return featureSources.get( entityClass );
        }
        catch (ExecutionException e) {
            throw new RuntimeException( e );
        }
    }


    protected Object stateId( Object state ) {
        return ((Feature)state).getIdentifier().getID();
    }

    
    protected <T extends Entity> Object loadState( Object id, Class<T> entityClass ) {
        FeatureStore fs = featureSource( entityClass );
        FeatureType schema = fs.getSchema();
        FeatureIterator it = null;
        try {
            FeatureCollection features = fs.getFeatures(
                    ff.id( Collections.singleton( ff.featureId( (String)id ) ) ) );
            it = features.features();
            return it.hasNext() ? entityForState( entityClass, it.next() ) : null;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
        finally {
            if (it != null) { it.close(); }
        }
    }


    public <T extends Entity> Object newState( Object id, Class<T> entityClass ) {
        // find schema for entity
        FeatureStore fs = featureSource( entityClass );
        FeatureType schema = fs.getSchema();
        
        // create feature
        Feature feature = null;
        if (schema instanceof SimpleFeatureType) {
            feature = SimpleFeatureBuilder.build( (SimpleFeatureType)schema, 
                    ListUtils.EMPTY_LIST, (String)id );
        }
        else {
            throw new UnsupportedOperationException( "Complex FeatureType is not supported yet." );
        }
        return feature;
    }


    public void removeEntity( Entity entity ) {
        throw new RuntimeException( "not yet implemented." );
    }


    protected <T extends Entity> Collection findStates( Class<T> entityClass ) {
        try {
            // schema
            FeatureStore fs = featureSource( entityClass );
            FeatureType schema = fs.getSchema();

            // features 
            final FeatureCollection features = fs.getFeatures();
            final Iterator it = features.iterator();
            
            return new AbstractCollection<T>() {

                public Iterator iterator() {
                    return it;
                }

                public int size() {
                    return features.size();
                }

                public boolean isEmpty() {
                    return features.isEmpty();
                }

                protected void finalize() throws Throwable {
                    if (it != null) { features.close( it ); }
                }
            };
        }
        catch (IOException e) {
            throw new ModelRuntimeException( e );
        }
    }


    public void prepare()
    throws IOException, ConcurrentEntityModificationException {
        checkOpen();
        assert tx == null;
        
        tx = new DefaultTransaction( getClass().getName() + " Transaction" );
        try {
            apply();
        }
        catch (IOException e) {
            Transaction tx2 = tx;
            tx = TX_FAILED;
            tx2.rollback();
            tx2.close();
            throw e;
        }
        catch (ConcurrentEntityModificationException e) {
            Transaction tx2 = tx;
            tx = TX_FAILED;
            tx2.rollback();
            tx2.close();
            throw e;            
        }
    }


    public void commit() throws ModelRuntimeException {
        assert tx != TX_FAILED;
        try {
            // prepare if not yet done
            if (tx == null) {
                prepare();
            }
            tx.commit();
            tx.close();
            tx = null;

            for (FeatureStore fs : featureSources.asMap().values()) {
                log.debug( "Checking features: " + fs );
                fs.setTransaction( Transaction.AUTO_COMMIT );
                fs.getFeatures().accepts( new FeatureVisitor() {
                    public void visit( Feature feature ) {
                        log.debug( "FeatureId: " + feature.getIdentifier() );
                    }
                }, null );
            }

            modifications.clear();
            
            // flush entities and contexts; entities are reloaded with new status
            // XXX this also clears cache, ok?
            loaded.clear();
            featureSources.asMap().clear();
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e );
        }
    }


    public void close() {
        super.close();
        if (tx != null && tx != TX_FAILED) {
            try {
                tx.rollback();
                tx = null;
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        }
        store = null;
        featureSources = null;
        modifications = null;
    }


    protected void apply()
    throws IOException, ConcurrentEntityModificationException {
        assert tx != null;
        // find created, modified, removed
        Map<Class,FeatureCollection> created = new HashMap();
        Map<Class,Set<FeatureId>> removed = new HashMap();

        for (Entity entity : loaded.values()) {
            Feature feature = (Feature)entity.state();

            // created
            if (entity.status() == EntityStatus.CREATED) {
                FeatureCollection coll = created.get( entity.getClass() );
                if (coll == null) {
                    coll = new DefaultFeatureCollection( null, null );
                    created.put( entity.getClass(), coll );
                }
                coll.add( feature );
            }
            // removed
            else if (entity.status() == EntityStatus.REMOVED) {
                Set<FeatureId> fids = removed.get( entity.getClass() );
                if (fids == null) {
                    fids = new HashSet( 1024 );
                    removed.put( entity.getClass(), fids );
                }
                fids.add( feature.getIdentifier() );
            }
        }

        // write created
        for (Entry<Class,FeatureCollection> entry : created.entrySet()) {
            log.debug( "    Adding feature(s) of " + entry.getKey().getSimpleName() + " : " + entry.getValue().size() );
            FeatureStore fs = featureSource( entry.getKey() );
            if (tx != fs.getTransaction()) {
                fs.setTransaction( tx );
            }
            fs.addFeatures( entry.getValue() );
            
//            fs.getFeatures().accepts( new FeatureVisitor() {
//                public void visit( Feature feature ) {
//                    log.debug( "        fid: " + feature.getIdentifier() );
//                }
//            }, null );
        }

        // write removed
        for (Entry<Class,Set<FeatureId>> entry : removed.entrySet()) {
            log.debug( "    Removing feature(s) of " + entry.getKey().getSimpleName() + " : " + entry.getValue().size() );
            FeatureStore fs = featureSource( entry.getKey() );
            if (tx != fs.getTransaction()) {
                fs.setTransaction( tx );
            }
            fs.removeFeatures( ff.id( entry.getValue() ) );
        }
        
        // write modified
        for (Entry<FeatureId,FeatureModifications> entry : modifications.entrySet()) {
            FeatureModifications mods = entry.getValue();
            FeatureStore fs = (FeatureStore)store.getStore().getFeatureSource( 
                    mods.feature.getType().getName() );

            // any other than no or my tx is an error
            assert fs.getTransaction() == Transaction.AUTO_COMMIT || tx == fs.getTransaction();

            if (tx != fs.getTransaction()) {
                fs.setTransaction( tx );
            }
            AttributeDescriptor[] atts = mods.types();
            Object values[] = mods.values2();
            
            log.debug( "    Modifying feature: " + mods.feature.getIdentifier() ); 
            for (int i=0; i<atts.length; i++) {
                log.debug( "        attribute: " + atts[i].getLocalName() + " = " + values[i] );
            }
            fs.modifyFeatures( mods.types(), mods.values2(), 
                    ff.id( singleton( mods.feature.getIdentifier() ) ) );
        }
    }


    protected void markPropertyModified( Feature feature, AttributeDescriptor att, Object value) {
        FeatureModifications fm = modifications.get( feature.getIdentifier() );
        if (fm == null) {
            fm = new FeatureModifications( feature );
            FeatureModifications other = modifications.putIfAbsent( feature.getIdentifier(), fm );
            fm = other != null ? other : fm;
        }
        fm.put( att, value );
    }

    
    /**
     * 
     */
    class FeatureModifications
            extends HashMap<AttributeDescriptor,Object> {

        Feature     feature;
        
        public FeatureModifications( Feature feature ) {
            this.feature = feature;
        }
        
        public AttributeDescriptor[] types() {
            return Iterables.toArray( keySet(), AttributeDescriptor.class );
        }
        
        public Object[] values2() {
            return Iterables.toArray( values(), Object.class );            
        }
    }
    
}
