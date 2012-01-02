/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and individual contributors as
 * indicated by the @authors tag.
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
package org.polymap.core.data.feature.lucene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.Timer;

/**
 * Feature cache backed by Lucene.
 * <p/>
 * All cache processors for the same layer share one instance (aquired by
 * {@link #instance(ILayer)}) across all sessions!
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
@SuppressWarnings("deprecation")
public class LuceneCache {

    private static final Log log = LogFactory.getLog( LuceneCache.class );

    static final String                     FIELD_MAXX = "_maxx_";
    static final String                     FIELD_MAXY = "_maxy_";
    static final String                     FIELD_MINX = "_minx_";
    static final String                     FIELD_MINY = "_miny_";
    
    private static Map<String,LuceneCache>  instances = new HashMap();
    
    private static ReentrantReadWriteLock   instancesLock = new ReentrantReadWriteLock();
    
    
    public static synchronized LuceneCache aquire( ILayer layer, FeatureType schema )
    throws IOException {
        try {
            instancesLock.readLock().lock();
            LuceneCache cache = instances.get( layer.id() );
            
            if (cache == null) {
                instancesLock.readLock().unlock();
                instancesLock.writeLock().lock();
                
                cache = new LuceneCache( layer, schema );
                instances.put( layer.id(), cache );
            }
            return cache;
        }
        finally {
            if (instancesLock.writeLock().isHeldByCurrentThread()) {
                instancesLock.readLock().lock();
                instancesLock.writeLock().unlock();
            }
            instancesLock.readLock().unlock();
        }
    }

    
    public static void release( LuceneCache cache ) {
    }
    
    
    // instance *******************************************
    
    private Directory           directory;

    private Analyzer            analyzer = new WhitespaceAnalyzer();

    private IndexSearcher       searcher;

    /** Synchronizing access is left to Lucene. */
    private IndexReader         indexReader;
    
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    private FeatureType         schema;
    
    private GeometryJSON        jsonCoder = new GeometryJSON( 6 );
    
    private boolean             isEmpty = false;

    /**
     * The memory cache (see {@link #cacheKey(int, Query)}.
     * <p/>
     * For normal Browser settings with 6-8 concurrent requests 4 concurrent threads
     * in the cache is insufficient, but in most cases actual concurrent cache
     * request are between 1-4. The smaller the number the faster the cache reads.
     * <p/>
     * FIXME Map doc id into Feature, so that no Lucene document needs to be loaded
     * after query if the feature is in cache
     */
    private ConcurrentReferenceHashMap<Object,Feature> cache = 
            new ConcurrentReferenceHashMap( 16*1024, 0.75f, 4, 
            ConcurrentReferenceHashMap.ReferenceType.STRONG, ConcurrentReferenceHashMap.ReferenceType.SOFT, null );
    
    
    LuceneCache( ILayer layer, FeatureType schema ) 
    throws IOException {
        this.schema = schema;
        
        File cacheDir = new File( Polymap.getWorkspacePath().toFile(), "cache" );
        File luceneCacheDir = new File( cacheDir, "luceneCache_" + layer.id() );
        luceneCacheDir.mkdirs();

        directory = FSDirectory.open( luceneCacheDir );
        
        if (directory.listAll().length == 0) {
            log.info( "No index found, creating..." );
            IndexWriter iwriter = new IndexWriter( directory, analyzer, true, new IndexWriter.MaxFieldLength( 25000 ) );
            iwriter.commit();
            iwriter.close();
            isEmpty = true;
        }
        
//        log.info( "    creating index reader..." );
//        indexReader = IndexReader.open( directory, true );
//        log.info( "    creating index searcher..." );
//        searcher = new IndexSearcher( indexReader );
    }
    
    
    public void dispose() 
    throws IOException {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
        if (searcher != null) {
            searcher.close();
            searcher = null;
        }
        if (indexReader != null) {
            indexReader.close();
            indexReader = null;
        }
        directory.close();
        directory = null;
    }
    
    
    public boolean isEmpty() 
    throws IOException {
        return isEmpty;
    }

    
    public boolean supports( Filter filter ) {
        return LuceneQueryParser.supports( filter );
    }

    
    public Iterable<Feature> getFeatures( final Query query ) 
    throws IOException {
        Timer timer = new Timer();
        
        LuceneQueryParser queryParser = new LuceneQueryParser( schema, query.getFilter() );

        if (indexReader == null) {
            rwLock.writeLock().lock();
            try {
                if (indexReader == null) {
                    indexReader = IndexReader.open( directory, true );
                    searcher = new IndexSearcher( indexReader );
                    log.info( "Index reloaded." );
                }
            }
            finally {
                rwLock.writeLock().unlock();
            }
        }

//        rwLock.readLock().lock();
        
        // check schema
        if (schema == null) {
            throw new RuntimeException( "schema is null, call getFeatureType() first." );
        }

        // execute Lucene query
        TopDocs topDocs = searcher.search( queryParser.getQuery(), query.getMaxFeatures() );
        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        final int count = scoreDocs.length;
        log.info( "    results: " + count + " (" + timer.elapsedTime() + "ms)" );

        // skip unwanted properties
        final FieldSelector fieldSelector = new QueryFieldSelector( query );
        
        return new Iterable<Feature>() {
            public Iterator<Feature> iterator() {
          
                return new Iterator<Feature>() {

                    private SimpleFeatureBuilder    builder = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
                    private int                     index = 0;
                    private boolean                 unlocked = false;
                    private int                     cacheHits = 0;

                    protected void finalize()
                    throws Throwable {
                        if (unlocked == false) {
                            rwLock.readLock().unlock();
                        }
                    }

                    public boolean hasNext() {
                        boolean result = index < count;
                        if (result == false && unlocked == false) {
//                            rwLock.readLock().unlock();
                            unlocked = true;
                        }
                        if (result == false) {
                            log.info( "CACHE: gets=" + count + ", hits=" + cacheHits + ", cache=" + cache.size() );
                        }
                        return result;
                    }

                    public Feature next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException( "Query result count: " + scoreDocs.length );
                        }
                        try {
                            int docnum = scoreDocs[ index++ ].doc;
                            
                            Feature result = null;
                            
                            result = cache.get( cacheKey( docnum, query ) );
                            if (result != null) {
                                ++cacheHits;
                                return result;
                            }
                            
                            Document doc = searcher.doc( docnum, fieldSelector );
//                            result = new LuceneFeature( doc, (SimpleFeatureType)schema );
                        
                            String fid = null;
                            for (Fieldable field : doc.getFields()) {
                                if (schema == null) {
                                    throw new RuntimeException( "schema is null, call getFeatureType() first." );
                                }
                                // fid
                                if (field.name().equals( "fid" )) {
                                    fid = field.stringValue();
                                    continue;
                                }
                                        
                                PropertyDescriptor descriptor = schema.getDescriptor( field.name() );
                                if (descriptor == null) {
                                    throw new RuntimeException( "No descriptor for: " + field.name() );
                                }
                                Class valueType = descriptor.getType().getBinding();
                                // Geometry
                                if (Geometry.class.isAssignableFrom( valueType )) {
                                    Geometry geom = jsonCoder.read( new StringReader( field.stringValue() ) );
                                    builder.set( field.name(), geom );
                                }
                                // other
                                else {
                                    Object value = ValueCoder.decode( field, valueType );
                                    builder.set( field.name(), value );
                                }
                            }
                            result = builder.buildFeature( fid );

                            cache.put( cacheKey( docnum, query ), result );
                            return result;
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException( "remove()" );
                    }
                    
                };
            }
        };
    }
    
    
    public void putFeatures( List<Feature> features )
    throws CorruptIndexException, IOException, PipelineIncubationException {

        IndexWriter iwriter = null;
        try {
            long start = System.currentTimeMillis();
            rwLock.writeLock().lock();

            iwriter = new IndexWriter( directory, analyzer, false, new IndexWriter.MaxFieldLength( 25000 ) );

            int size = 0;        
            int indexed = 0;

            for (Feature feature : features) {
                Document doc = new Document();
                
                for (Property prop : feature.getProperties()) {
                    String propName = prop.getName().getLocalPart();
                    //log.debug( "        prop: " + propName );

                    // no value
                    if (prop.getValue() == null) {
                        continue;
                    }
                    // Geometry
                    else if (Geometry.class.isAssignableFrom( prop.getValue().getClass() ) ) {
                        Geometry geom = (Geometry)prop.getValue();
                        StringWriter out = new StringWriter( 1024 );
                        jsonCoder.write( geom, out );
                        doc.add( new Field( propName, out.toString(), Field.Store.YES, Field.Index.NO ) );
                        
                        Envelope envelop = geom.getEnvelopeInternal();
                        doc.add( ValueCoder.encode( propName+FIELD_MAXX, envelop.getMaxX(), 
                                Double.class, Field.Store.NO, true ) );
                        doc.add( ValueCoder.encode( propName+FIELD_MAXY, envelop.getMaxY(), 
                                Double.class, Field.Store.NO, true ) );
                        doc.add( ValueCoder.encode( propName+FIELD_MINX, envelop.getMinX(), 
                                Double.class, Field.Store.NO, true ) );
                        doc.add( ValueCoder.encode( propName+FIELD_MINY, envelop.getMinY(), 
                                Double.class, Field.Store.NO, true ) );
                    }
                    // other
                    else {
                        Class valueType = prop.getType().getBinding();
                        Fieldable field = ValueCoder.encode( propName, prop.getValue(), valueType, 
                                Field.Store.YES, true );
                        doc.add( field );
                        //log.debug( "    field: " + field );
                    }
                    indexed++;
                }

                doc.add( new Field( "fid", feature.getIdentifier().getID(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
                //log.info( "DOC: " + doc );
                iwriter.addDocument( doc );
                size++;
            }
            iwriter.commit();
            iwriter.close();
            iwriter = null;
            isEmpty = false;
            log.debug( "    document: count=" + size + " indexed=" + indexed );

            log.info( "Added features: " + features.size() + " (" + (System.currentTimeMillis()-start) + "ms)" );

            if (indexReader != null) {
                searcher.close();
                searcher = null;
                indexReader.close();
                indexReader = null;
            }
//            indexReader = IndexReader.open( directory, true );
//            searcher = new IndexSearcher( indexReader );
//            log.info( "Index reloaded." );
        }
        catch (Exception e) {
            log.error( "Fehler beim Indizieren:" + e.getLocalizedMessage() );
            log.debug( e.getLocalizedMessage(), e );
        }
        finally {
            if (iwriter != null) { iwriter.close(); }
            rwLock.writeLock().unlock();
        }
    }

    
    private Object cacheKey( int docnum, Query query ) {
        // this is not exact but I don't want to store the big joined strings
        // as keys in the cache
        long propNamesHash = 0;
        if (query.getPropertyNames() != null) {
            propNamesHash = StringUtils.join( query.getPropertyNames(), "_" ).hashCode();
        }
        return Long.valueOf( docnum | (propNamesHash << 32) );
    }
    
}
