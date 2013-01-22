/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    Copyright 2012, Polymap GmbH. All rights reserved.
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.recordstore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Provides basic functionality.
 * <p/>
 * Taken from Gt 2.6.4 in order to support non-simple feature types.
 * 
 * @author Jody Garnett, Refractions Research Inc
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class AbstractFeatureSource 
        implements FeatureSource {
    
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data");
    
    protected Set                   hints = Collections.EMPTY_SET;
    
    protected QueryCapabilities     queryCapabilities = new QueryCapabilities();
    
    
    public AbstractFeatureSource() {
    }
    

    /**
     * Overrides to explicitly type narrow the return type to {@link DataStore}
     */
    public abstract DataStore getDataStore();
    
    
    /**
     * Returns the same name than the feature type (ie,
     * {@code getSchema().getName()} to honor the simple feature land common
     * practice of calling the same both the Features produces and their types
     * 
     * @since 2.5
     * @see FeatureSource#getName()
     */
    public Name getName() {
        return getSchema().getName();
    }
    
    public String getTypeName() {
        return getName().getLocalPart();
    }
    
    /**
     * This constructors allows to set the supported hints 
     * @param hints
     */
    public AbstractFeatureSource(Set hints) {
        this.hints = Collections.unmodifiableSet(new HashSet(hints));
    }
    
    
    public ResourceInfo getInfo() {
        return new ResourceInfo(){
            final Set<String> words = new HashSet<String>();
            {
                words.add("features");
                words.add( AbstractFeatureSource.this.getTypeName() );
            }
            public ReferencedEnvelope getBounds() {
                try {
                    return AbstractFeatureSource.this.getBounds();
                } catch (IOException e) {
                    return null;
                }
            }
            public CoordinateReferenceSystem getCRS() {
                return AbstractFeatureSource.this.getSchema().getCoordinateReferenceSystem();
            }

            public String getDescription() {
                return null;
            }

            public Set<String> getKeywords() {
                return words;
            }

            public String getName() {
                return AbstractFeatureSource.this.getTypeName();
            }

            public URI getSchema() {
                Name name = AbstractFeatureSource.this.getSchema().getName();
                URI namespace;
                try {
                    namespace = new URI( name.getNamespaceURI() );
                    return namespace;                    
                } catch (URISyntaxException e) {
                    return null;
                }                
            }

            public String getTitle() {
                Name name = AbstractFeatureSource.this.getSchema().getName();
                return name.getLocalPart();
            }
            
        };
    }

    
    public QueryCapabilities getQueryCapabilities(){
        return queryCapabilities;
    }
    

    /**
     * Retrieve the Transaction this FeatureSource<SimpleFeatureType, SimpleFeature>
     * is operating against.
     * <p/>
     * For a plain FeatureSource<SimpleFeatureType, SimpleFeature> that cannot modify
     * this will always be Transaction.AUTO_COMMIT.
     * 
     * @return Transacstion FeatureSource<SimpleFeatureType, SimpleFeature> is
     *         operating against
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }
    
    
//    /**
//     * Provides an interface to for the Results of a Query.
//     * <p/>
//     * Various queries can be made against the results, the most basic being to
//     * retrieve Features.
//     * 
//     * @param query
//     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
//     */
//    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(Query query) throws IOException {
//        SimpleFeatureType schema = getSchema();        
//        String typeName = schema.getTypeName();
//        
//        if( query.getTypeName() == null ){ // typeName unspecified we will "any" use a default
//            DefaultQuery defaultQuery = new DefaultQuery(query);
//            defaultQuery.setTypeName( typeName );
//        }
//        else if ( !typeName.equals( query.getTypeName() ) ){
//            return new EmptyFeatureCollection( schema );
//        }
//        
//        final QueryCapabilities queryCapabilities = getQueryCapabilities();
//        if(!queryCapabilities.supportsSorting(query.getSortBy())){
//            throw new DataSourceException("DataStore cannot provide the requested sort order");
//        }
//        
//        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = new DefaultFeatureResults(this, query);
//        if( collection.getSchema().getGeometryDescriptor() == null ){
//            return collection; // no geometry no reprojection needed
//        }
//        
//        if( false ){ // we need to have our CRS forced
//            if ( query.getCoordinateSystem() != null ){
//                try {
//                    collection = new ForceCoordinateSystemFeatureResults(collection, query.getCoordinateSystem() );
//                } catch (SchemaException e) {
//                    throw new IOException( "Could not force CRS "+query.getCoordinateSystem() ); 
//                }
//            }
//        }
//        if( false ){ // we need our data reprojected
//            if ( query.getCoordinateSystemReproject() != null){
//                try {
//                    collection = new ReprojectFeatureResults(collection, query.getCoordinateSystemReproject() );
//                } catch (Exception e) {
//                    throw new IOException( "Could not reproject to "+query.getCoordinateSystemReproject() );
//                }
//            }            
//        }
//        return collection;
//    }

    
    /**
     * Retrieve all features matching the given filter.
     *
     * @param filter Indicates features to retrieve
     * @return FeatureResults indicating features matching filter
     * @throws IOException If results could not be obtained
     */
    public FeatureCollection getFeatures( Filter filter ) throws IOException {
        return getFeatures( new DefaultQuery( getTypeName(), filter ) );
    }
    
    
    /**
     * Retrieve all Features.
     * 
     * @return FeatureResults of all Features in FeatureSource
     * @throws IOException If features could not be obtained
     */
    public FeatureCollection getFeatures() throws IOException {
        return getFeatures( Filter.INCLUDE );
    }

    
    /**
     * Retrieve Bounds of all Features.
     * <p/>
     * Currently returns null, consider getFeatures().getBounds() instead.
     * <p/>
     * Subclasses may override this method to perform the appropriate optimization
     * for this result.
     * 
     * @return null representing the lack of an optimization
     * @throws IOException DOCUMENT ME!
     */
    public ReferencedEnvelope getBounds() throws IOException {
//        return getBounds(Query.ALL); // DZ should this not return just the bounds for this type?
        return getBounds( getSchema() != null
                ? new DefaultQuery( getTypeName() )
                : Query.ALL );
    }


//    /**
//     * Retrieve Bounds of Query results.
//     * <p/>
//     * Currently returns null, consider getFeatures( query ).getBounds() instead.
//     * <p/>
//     * Subclasses may override this method to perform the appropriate optimization
//     * for this result.
//     * 
//     * @param query Query we are requesting the bounds of
//     * @return null representing the lack of an optimization
//     * @throws IOException DOCUMENT ME!
//     */
//    public ReferencedEnvelope getBounds( Query query ) throws IOException {
//        if (query.getFilter() == Filter.EXCLUDE) {
//            return new ReferencedEnvelope( new Envelope(), getSchema().getCoordinateReferenceSystem() );
//        }
//
//        DataStore dataStore = getDataStore();
//        if ((dataStore == null) || !(dataStore instanceof AbstractDataStore)) {
//            // too expensive
//            return null;
//        }
//        else {
//            // ask the abstract data store
//            return ((AbstractDataStore)dataStore).getBounds( namedQuery( query ) );
//        }
//    }
    
    
    /**
     * Ensure query modified with typeName.
     * <p>
     * This method will make copy of the provided query, using
     * DefaultQuery, if query.getTypeName is not equal to
     * getSchema().getTypeName().
     * </p>
     * @param query Original query
     * @return Query with getTypeName() equal to getSchema().getTypeName()
     */
    protected Query namedQuery( Query query ){
        String typeName = getTypeName();
        if( query.getTypeName() == null ||
                !query.getTypeName().equals( typeName )){
            
            return new DefaultQuery(
                    typeName,
                    query.getFilter(),
                    query.getMaxFeatures(),
                    query.getPropertyNames(),
                    query.getHandle()
                    );
        }
        return query;
    }
    
    
//    /**
//     * Retrieve total number of Query results.
//     *
//     * <p>
//     * Currently returns -1, consider getFeatures( query ).getCount() instead.
//     * </p>
//     *
//     * <p>
//     * Subclasses may override this method to perform the appropriate optimization for this result.
//     * </p>
//     *
//     * @param query Query we are requesting the count of
//     *
//     * @return -1 representing the lack of an optimization
//     */
//    public int getCount(Query query) throws IOException {
//        if (query.getFilter() == Filter.EXCLUDE) {
//            return 0;
//        }
//        
//        DataStore dataStore = (DataStore) getDataStore();
//        if ((dataStore == null) || !(dataStore instanceof AbstractDataStore)) {
//            // too expensive
//            return -1;
//        } 
//        // ask the abstract data store
//        Transaction t = getTransaction();
//        
//        int nativeCount = ((AbstractDataStore) dataStore).getCount( namedQuery(query));
//        if(nativeCount == -1)
//            return -1;
//        
//        //State state = t.getState(dataStore);
//        int delta = 0;
//        if(t != Transaction.AUTO_COMMIT) { 
//            if(t.getState(dataStore) == null)
//                return nativeCount;
//            
//            if (!(t.getState(dataStore) instanceof TransactionStateDiff)) {
//                //we cannot proceed; abort!
//                return -1;
//            }
//            Diff diff = ((AbstractDataStore)dataStore).state(t).diff(namedQuery(query).getTypeName());
//            synchronized (diff) {
//                Iterator it = diff.added.values().iterator();
//                while(it.hasNext()){
//                    Object feature = it.next();
//                    if( query.getFilter().evaluate(feature) )
//                        delta++;
//                }
//                
//                it = diff.modified2.values().iterator();
//                while(it.hasNext()){
//                    Object feature = it.next();
//                    
//                    if(feature == TransactionStateDiff.NULL && query.getFilter().evaluate(feature)) {
//                        delta--;
//                    }
//                }
//            }
//        }
//        
//        return nativeCount + delta;
//    }
    
    /**
     * By default, no Hints are supported
     */
    public Set getSupportedHints() {
        return hints;
    }
    
}
