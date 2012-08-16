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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.google.common.collect.MapMaker;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.recordstore.BaseRecordStore;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.IRecordStore;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
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
    
    private ExecutorService         executor = Polymap.executorService();
    
    private Cache<Object,Document>  cache = null;
    
    private CacheLoader<Object,Document,Exception> loader = new DocumentLoader();
    
    /** 
     * Maps docnum into record id; this helps to find a cached record
     * for a given docnum. This contains a map only if a cache is set.
     */
    private ConcurrentMap<Integer,Object> doc2id = null;

    IndexSearcher                   searcher;

    IndexReader                     reader;
    
    /** Prevents {@link #reader} close/reopen by {@link LuceneUpdater} while in use. */
    ReadWriteLock                   lock = new ReentrantReadWriteLock();

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

        reader = IndexReader.open( directory );
        searcher = new IndexSearcher( reader, executor );
    }
    
    
    public void close() {
        try {
            if (searcher != null) {
                searcher.close();
                searcher = null;
            }
            if (reader != null) {
                reader.close();
                reader = null;
                directory.close();
                directory = null;
            }
            
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
                
        return doc != null ? new LuceneRecordState( LuceneRecordStore.this, doc, cache != null ) : null;
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
        
        Document doc = null;
        try {
            lock.readLock().lock();
            doc = reader.document( docnum );
        }
        finally {
            lock.readLock().unlock();
        }
        
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
            implements CacheLoader<Object,Document,Exception> {
        
        public Document load( Object id ) throws Exception {
            TermDocs termDocs = null;
            try {
                log.trace( "LUCENE: termDocs: " + LuceneRecordState.ID_FIELD + " = " + id.toString() );
                lock.readLock().lock();
                termDocs = reader.termDocs( new Term( LuceneRecordState.ID_FIELD, id.toString() ) );
                if (termDocs.next()) {
                    return reader.document( termDocs.doc() );
                }
                return null;
            }
            finally {
                lock.readLock().unlock();
                if (termDocs != null) { termDocs.close(); }
            }
        }

        public int size() throws Exception {
            return Cache.ELEMENT_SIZE_UNKNOW;
        }
    }
    
    
    public ResultSet find( RecordQuery query )
    throws Exception {
        // SimpleQuery
        if (query instanceof SimpleQuery) {
            Query luceneQuery = null;
            Collection<QueryExpression> expressions = ((SimpleQuery)query).expressions();
            if (expressions.isEmpty()) {
                luceneQuery = new MatchAllDocsQuery();
            }
            else {
                luceneQuery = new BooleanQuery();
                for (QueryExpression exp : expressions) {
                    ((BooleanQuery)luceneQuery).add( 
                            valueCoders.searchQuery( exp ), BooleanClause.Occur.MUST );
                }
            }
            return new LuceneRecordQuery( this, luceneQuery )
                    .setMaxResults( query.getMaxResults() )
                    .setFirstResult( query.getFirstResult() )
                    .sort( query.getSortKey(), query.getSortOrder(), query.getSortType() )
                    .execute();

        }
        // other
        else {
            return query.execute();
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
                // Defauls:
                //  - IndexWriterConfig#DEFAULT_RAM_BUFFER_SIZE_MB == 16MB
                //  - autCommit == false
                //  - 8 concurrent thread
                IndexWriterConfig config = new IndexWriterConfig( VERSION, analyzer )
                        .setOpenMode( OpenMode.APPEND );
                // limit segment size for lower pauses on interactive indexing
                LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
                mergePolicy.setMaxMergeMB( 10 );
                config.setMergePolicy( mergePolicy );
                writer = new IndexWriter( directory, config );
            }
            catch (Exception e) {
                if (writer != null) {
                    discard();
                }
                throw new RuntimeException( e );
            }
        }

        
        public void store( IRecordState record ) throws Exception {
            if (log.isTraceEnabled()) {
                for (Map.Entry<String,Object> entry : record) {
                    log.trace( "    field: " + entry.getKey() + " = " + entry.getValue() ); 
                }
            }

            Document doc = ((LuceneRecordState)record).getDocument();
            
            // add
            if (record.id() == null) {
                ((LuceneRecordState)record).createId();
                writer.addDocument( doc );
                
                if (cache != null) {
                    cache.putIfAbsent( record.id(), doc );
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
            apply( true );
        }
        
        
        public void apply( boolean optimizeIndex ) {
            assert writer != null : "Updater is closed.";
            Timer timer = new Timer();
            try {
                writer.commit();
                if (optimizeIndex) {
                    //writer.expungeDeletes( true );
                    writer.forceMergeDeletes( true );
                }
                writer.close();
                writer = null;
                
                lock.writeLock().lock();
                searcher.close();
                IndexReader newReader = IndexReader.openIfChanged( reader );
                if (newReader != null) {
                    reader.close();
                    reader = newReader;
                }
                searcher = new IndexSearcher( reader, executor );
                
                if (doc2id != null) {
                    doc2id.clear();
                }
                
                log.debug( "COMMIT: " + timer.elapsedTime() + "ms" );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
            finally {
                lock.writeLock().unlock();
            }
        }

        
        public void discard() {
            if (writer == null) {
                log.warn( "Updater is already closed." );
                return;
            }
            try {
                writer.rollback();
                writer.close();
                writer = null;
            }
            catch (Exception e) {
                log.warn( "Error during discard()", e );
                //throw new RuntimeException( e );
            }
        }
        
    }
    
}
