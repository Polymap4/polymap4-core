/* 
 * polymap.org
 * Copyright 2011, 2012 Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.recordstore.lucene;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.recordstore.BaseRecordStore;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.RecordModel;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.SimpleQuery;

/**
 * A record store backed by a Lucene index.
 * <p/>
 * This store supports copy-on-write caching of the underlying Lucene documents. To
 * activate caching call {@link #setDocumentCache(Cache)}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneRecordStore
        extends BaseRecordStore
        implements IRecordStore {

    private static Log log = LogFactory.getLog( LuceneRecordStore.class );

    private static final Version    VERSION = Version.LUCENE_34;
    
    private Directory               directory;

    private Analyzer                analyzer = new WhitespaceAnalyzer( VERSION );
    
    private ExecutorService         executor = null; //Polymap.executorService();
    
    private Cache<Object,Document>  cache = null;
    
    private CacheLoader<Object,Document> loader = new DocumentLoader();
    
    /** 
     * Maps docnum into record id; this helps to find a cached record
     * for a given docnum. This contains a map only if a cache is set.
     */
    private ConcurrentMap<Integer,Object> doc2id = null;

    IndexSearcher                   searcher;

    IndexReader                     reader;
    
    ValueCoders                     valueCoders = new ValueCoders( this );

    
    /**
     * Creates a new store for the given filesystem directory. 
     * 
     * @param indexDir The directory to hold the store files.
     * @param clean
     * @throws IOException
     */
    public LuceneRecordStore( File indexDir, boolean clean ) 
    throws IOException {
        if (!indexDir.exists()) {
            indexDir.mkdirs();
        }
        
        directory = FSDirectory.open( indexDir );
        log.info( "    Files in directry: " + Arrays.asList( directory.listAll() ) );
        open( clean );
    }


    /**
     * Creates a new in-memory store.
     * 
     * @throws IOException
     */
    public LuceneRecordStore() 
    throws IOException {
        directory = new RAMDirectory();
        log.info( "    RAMDirectory: " + Arrays.asList( directory.listAll() ) );
        open( true );
    }

    
    protected void open( boolean clean ) 
    throws IOException {
        // create or clear index
        if (directory.listAll().length == 0 || clean) {
            IndexWriterConfig config = new IndexWriterConfig( VERSION, analyzer )
                    .setOpenMode( OpenMode.CREATE );
            IndexWriter iwriter = new IndexWriter( directory, config );
            iwriter.close();
            log.info( "    Index created." );
        }

        reader = IndexReader.open( directory, true );
        searcher = new IndexSearcher( reader, executor );
    }
    
    
    public void close() {
        try {
            searcher.close();
            searcher = null;
            reader.close();
            reader = null;
            directory.close();
            directory = null;
            
            if (cache != null) {
                cache.dispose();
                doc2id.clear();
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public IndexSearcher getIndexSearcher() {
        return searcher;    
    }
    
    
    public long storeSizeInByte() {
        try {
            long result = 0;
            for (String name : directory.listAll()) {
                if (directory.fileExists( name )) {
                    result += directory.fileLength( name );
                }
            }
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    
    public ValueCoders getValueCoders() {
        return valueCoders;
    }


    public void setDocumentCache( Cache<Object,Document> cache ) {
        this.cache = cache;
        this.doc2id = new MapMaker().initialCapacity( 1024 ).concurrencyLevel( 8 ).makeMap();
    }


    public IRecordState newRecord() {
        assert reader != null : "Store is closed.";
        return new LuceneRecordState( this, new Document(), false );
    }


    public IRecordState get( Object id ) 
    throws Exception {
        assert reader != null : "Store is closed.";
        assert id instanceof String : "Given record identifier is not a String: " + id;
        
        Document doc = cache != null
                ? cache.get( id, loader )
                : loader.load( id );
                
        return new LuceneRecordState( LuceneRecordStore.this, doc, cache != null );
    }


    public LuceneRecordState get( int docnum ) 
    throws Exception {
        assert reader != null : "Store is closed.";

        // if doc2id contains the docnum *and* the cache contains the id, then
        // we can create a record without accessing the underlying store
        if (doc2id != null) {
            Object id = doc2id.get( docnum );
            if (id != null) {
                Document doc = cache.get( id );
                if (doc != null) {
                    //System.out.print( "." );
                    return new LuceneRecordState( LuceneRecordStore.this, doc, true );
                }
            }
        }
         
        Document doc = reader.document( docnum );
        LuceneRecordState result = new LuceneRecordState( LuceneRecordStore.this, doc, false );
        
        if (cache != null) {
            doc2id.put( docnum, result.id() );
            if (cache.putIfAbsent( result.id(), doc ) == null) {
                result.setShared( true );
            }
        }
        return result;
    }


    /**
     * Loads records triggered by the cache in {@link LuceneRecordStore#get(Object)}.
     */
    class DocumentLoader
            implements CacheLoader<Object,Document> {
        
        public Document load( Object id ) throws Exception {
            TermDocs termDocs = reader.termDocs( new Term( LuceneRecordState.ID_FIELD, id.toString() ) );
            try {
                if (termDocs.next()) {
                    return reader.document( termDocs.doc() );
                }
                return null;
            }
            finally {
                termDocs.close();
            }

        }

        public int size() throws Exception {
            return Cache.ELEMENT_SIZE_UNKNOW;
        }
    }
    
    
    public ResultSet find( RecordQuery query )
    throws Exception {
        if (query instanceof SimpleQuery) {
            return new SimpleQueryResultSet( this, (SimpleQuery)query );
        }
        else {
            throw new IllegalArgumentException( "Unsupported query type: " + query.getClass() );
        }
    }

    
    public Updater prepareUpdate() {
        return new LuceneUpdater();
    }
    
    
    /*
     * 
     */
    class LuceneUpdater
            implements Updater {

        private IndexWriter         writer;


        LuceneUpdater() {
            try {
                // IndexWriterConfig#DEFAULT_RAM_BUFFER_SIZE_MB == 16MB
                // autCommit == false
                // 8 concurrent thread
                IndexWriterConfig config = new IndexWriterConfig( VERSION, analyzer )
                        .setOpenMode( OpenMode.APPEND );
                writer = new IndexWriter( directory, config );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        
        public void store( IRecordState record ) throws Exception {
            if (log.isTraceEnabled()) {
                for (Map.Entry<String,Object> entry : record) {
                    log.debug( "    field: " + entry.getKey() + " = " + entry.getValue() ); 
                }
            }
            // add
            if (record.id() == null) {
                ((LuceneRecordState)record).createId();
                writer.addDocument( ((LuceneRecordState)record).getDocument() );
                
                if (cache != null) {
                    cache.remove( record.id() );
                }
            }
            // update
            else {
                Term idTerm = new Term( LuceneRecordState.ID_FIELD, (String)record.id() );
                writer.updateDocument( idTerm, ((LuceneRecordState)record).getDocument() );

                if (cache != null) {
                    cache.remove( record.id() );
                }
            }
        }

        
        public void remove( IRecordState record ) throws Exception {
            assert record.id() != null : "Record is not stored.";

            Term idTerm = new Term( LuceneRecordState.ID_FIELD, (String)record.id() );
            writer.deleteDocuments( idTerm );

            if (cache != null) {
                cache.remove( record.id() );
            }
        }

        
        public void apply() {
            assert writer != null : "Updater is closed.";
            Timer timer = new Timer();
            try {
                writer.commit();
                
                writer.expungeDeletes( true );
                
                writer.close();
                writer = null;
                
                searcher.close();
                reader = reader.reopen();
                searcher = new IndexSearcher( reader, executor );
                
                if (doc2id != null) {
                    doc2id.clear();
                }
                
                log.debug( "COMMIT: " + timer.elapsedTime() + "ms" );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        
        public void discard() {
            log.warn( "Updater is closed." );
            assert writer != null : "Updater is closed.";
            
            try {
                writer.rollback();
                writer.close();
                writer = null;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        
    }
    
    
    // test ***********************************************
    
    /*
     * 
     */
    public static class TestRecord
            extends RecordModel {
        
        public static final TestRecord  TYPE = type( TestRecord.class );
        
        public TestRecord( IRecordState state ) {
            super( state );
        }
        
        public Property<String>         type = new Property<String>( "type" );
        
        public Property<Integer>        count = new Property<Integer>( "count" );
        
        public Property<String>         payload = new Property<String>( "payload" );
        
    }
    

    public static void main( String[] args ) throws Exception {

        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "info" );
        //System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.alkis.recordstore.lucene", "debug" );
        
//        LuceneRecordStore store = new LuceneRecordStore( new File( "/tmp", "LuceneRecordStore" ), false );
        LuceneRecordStore store = new LuceneRecordStore();
//        store.setRecordCache( new HashMapRecordCache() );
        
        Updater updater = store.prepareUpdate();
        try {
            for (int i=0; i<100000; i++) {
                TestRecord record = new TestRecord( store.newRecord() );
                record.payload.put( "LuceneRecordStore store = new LuceneRecordStore( new File LuceneRecordStore store = new LuceneRecordStore( new File LuceneRecordStore store = new LuceneRecordStore( new File" );
                record.type.put( "2" );
                record.count.put( i /*Integer.valueOf( i )*/ );
                updater.store( record.state() );
            }
            
            updater.apply();
        }
        catch (Exception e) {
            updater.discard();
            throw e;
        }
        
        final Timer timer = new Timer();
        String dummy = null;
        
//        // ForEach loop
//        List list = ForEach
//                .in( new Producer<Integer>() {
//                    int i = 0;
//                    public Integer produce() {
//                        if (i == 100000-1) {
//                            log.info( "Time: " + timer.elapsedTime() + "ms -- " + dummy );
//                        }
//                        return i++;
//                    }
//                    public int size() {
//                        return 100000;
//                    }
//                })
//                .chunked( 10000 )
//                .doFirst( new Parallel<Integer,Integer>() {
//                    public Integer process( Integer i ) 
//                    throws Exception {
//                        SimpleQuery query = new SimpleQuery().setMaxResults( 1 );
//                        TestRecord template = new TestRecord( query );
//                        template.count.put( String.valueOf( i ) );
//                        template.type.put( "2" );
//                        
//                        ResultSet result = store.find( query );
//
//                        if (result.count() == 0) {
//                            throw new RuntimeException( "Query: " + query );
//                        }
//                        
//                        TestRecord record = new TestRecord( result.get( 0 ) );
//                        String dummy2 = record.type.get();
//                        return i;
//                    }
//                }).asList();
        
        for (int i=0; i<100000; i++) {
//            RecordQuery query = new SimpleQuery()
//                    .eq( TestRecord.TYPE.count.name(), String.valueOf( i ) )
//                    .eq( TestRecord.TYPE.type.name(), "2" )
//                    .setMaxResults( 1 );
            
            SimpleQuery query = new SimpleQuery().setMaxResults( 1 );
            TestRecord template = new TestRecord( query );
            template.count.put( i );
            template.type.put( "2" );
            
            ResultSet result = store.find( query );

            if (result.count() == 0) {
                throw new RuntimeException( "Query: " + query );
            }
            
            TestRecord record = new TestRecord( result.get( 0 ) );
            dummy = record.type.get();
            
//            for (IRecordState state : result) {
//                TestRecord record = new TestRecord( state );
//                dummy = record.count.get();
//                log.debug( "Found: type=" + record.type.get() );
//            }
        }
        log.info( "Time: " + timer.elapsedTime() + "ms -- " + dummy );
        
        store.close();
    }
    
}
