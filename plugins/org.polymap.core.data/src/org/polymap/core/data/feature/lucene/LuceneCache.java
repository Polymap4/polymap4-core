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
import java.util.concurrent.locks.ReadWriteLock;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

/**
 * Feature cache backed by Lucene.
 * <p>
 * All cache processors for the same layer share one instance (aquired by
 * {@link #instance(ILayer)}) across all sessions!
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
@SuppressWarnings("deprecation")
public class LuceneCache {

    private static final Log log = LogFactory.getLog( LuceneCache.class );

    private static Map<String,LuceneCache>      instances = new HashMap();
    
    
    public static synchronized LuceneCache aquire( ILayer layer, FeatureType schema )
    throws IOException {
        LuceneCache cache = instances.get( layer.id() );
        if (cache == null) {
            cache = new LuceneCache( layer, schema );
            instances.put( layer.id(), cache );
        }
        return cache;
    }
    
    public static void releade( LuceneCache cache ) {
        
    }
    
    
    // instance *******************************************
    
    private Directory           directory;

    private Analyzer            analyzer = new WhitespaceAnalyzer();

    private IndexSearcher       searcher;
    
    private IndexReader         indexReader;
    
    private ReadWriteLock       rwLock = new ReentrantReadWriteLock();

    private FeatureType         schema;
    
    private LuceneQueryParser   queryParser;
    
    
    LuceneCache( ILayer layer, FeatureType schema ) 
    throws IOException {
        this.schema = schema;
        this.queryParser = new LuceneQueryParser( schema );
        
        File cacheDir = new File( Polymap.getWorkspacePath().toFile(), "cache" );
        File luceneCacheDir = new File( cacheDir, "luceneCache_" + layer.id() );
        luceneCacheDir.mkdirs();

        directory = FSDirectory.open( luceneCacheDir );
        
        if (directory.listAll().length == 0) {
            log.info( "No index found, creating..." );
            IndexWriter iwriter = new IndexWriter( directory, analyzer, true, new IndexWriter.MaxFieldLength( 25000 ) );
            iwriter.commit();
            iwriter.close();
        }
        
        log.info( "    creating index reader..." );
        indexReader = IndexReader.open( directory, false ); // read-only=true
        log.info( "    creating index searcher..." );
        searcher = new IndexSearcher( indexReader ); // read-only=true
    }
    
    
    public void dispose() 
    throws IOException {
        searcher.close();
        searcher = null;
        indexReader.close();
        indexReader.close();
        directory.close();
        directory = null;
    }
    
    
    public boolean isEmpty() 
    throws IOException {
        return indexReader == null || indexReader.maxDoc() == 0;
    }

    
    public boolean supports( Filter filter ) {
        return queryParser.supports( filter );
    }

    
    public Iterable<Feature> getFeatures( Query query ) 
    throws IOException {
        long start = System.currentTimeMillis();
        org.apache.lucene.search.Query luceneQuery = queryParser.createQuery( null, query.getFilter() );

        rwLock.readLock().lock();
        
        // execute Lucene query
        TopDocs topDocs = searcher.search( luceneQuery, query.getMaxFeatures() );
        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        log.debug( "    results: " + scoreDocs.length + " (" + (System.currentTimeMillis()-start) + "ms)" );

        return new Iterable<Feature>() {
            public Iterator<Feature> iterator() {
          
                return new Iterator<Feature>() {

                    private SimpleFeatureBuilder    builder = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
                    private int                     index = 0;
                    private GeometryJSON            jsonDecoder = new GeometryJSON();
                    private boolean                 unlocked = false;        

                    protected void finalize()
                    throws Throwable {
                        if (unlocked == false) {
                            rwLock.readLock().unlock();
                        }
                    }

                    public boolean hasNext() {
                        boolean result = index < scoreDocs.length;
                        if (result == false && unlocked == false) {
                            rwLock.readLock().unlock();
                            unlocked = true;
                        }
                        return result;
                    }

                    public Feature next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException( "Query result count: " + scoreDocs.length );
                        }
                        try {
                            Document doc = searcher.doc( scoreDocs[index++].doc );
                            String fid = null;
                            for (Fieldable field : doc.getFields()) {
                                if (schema == null) {
                                    throw new RuntimeException( "schema is null, call getFeatureType() first." );
                                }
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
                                    Geometry geom = jsonDecoder.read( new StringReader( field.stringValue() ) );
                                    builder.set( field.name(), geom );
                                }
                                // other
                                else {
                                    String value = ValueCoder.encode( field.stringValue(), valueType );
                                    builder.set( field.name(), field.stringValue() );
                                }
                            }
                            return builder.buildFeature( fid );
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
        log.info( "Adding features: " + features.size() );

        IndexWriter iwriter = null;
        try {
            rwLock.writeLock().lock();

            iwriter = new IndexWriter( directory, analyzer, false, new IndexWriter.MaxFieldLength( 25000 ) );

            int size = 0;        
            int indexed = 0;

            GeometryJSON jsonEncoder = new GeometryJSON( 4 );
                    
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
                        jsonEncoder.write( geom, out );
                        doc.add( new Field( propName, out.toString(), Field.Store.YES, Field.Index.NO ) );
                    }
                    // other
                    else {
                        Class valueType = prop.getType().getBinding();
                        String propValue = ValueCoder.encode( prop.getValue(), valueType );
                        doc.add( new Field( propName, propValue, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
                        log.debug( "    field: " + propName + " = " + propValue );
                    }
                    indexed++;
                }

                doc.add( new Field( "fid", feature.getIdentifier().getID(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
                iwriter.addDocument( doc );
                size++;
            }
            iwriter.commit();
            iwriter.close();
            log.info( "    document: count=" + size + " indexed=" + indexed );

            //indexReader.reopen();

            // XXX hack to get index reloaded
            if (indexReader != null) {
                searcher.close();
                indexReader.close();
            }
            indexReader = IndexReader.open( directory, false ); // read-only=true
            searcher = new IndexSearcher( indexReader ); // read-only=true
            log.info( "Index reloaded." );
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

}
