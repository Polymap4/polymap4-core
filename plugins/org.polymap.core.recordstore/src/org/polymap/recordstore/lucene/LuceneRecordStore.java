/* 
 * polymap.org
 * Copyright (C) 2011-2015 Polymap GmbH. All rights reserved.
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
package org.polymap.recordstore.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.File;
import java.io.IOException;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Constants;
import org.apache.lucene.util.DummyConcurrentLock;
import org.apache.lucene.util.Version;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultDouble;

import org.polymap.recordstore.BaseRecordStore;
import org.polymap.recordstore.IRecordState;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.QueryExpression;
import org.polymap.recordstore.RecordQuery;
import org.polymap.recordstore.ResultSet;
import org.polymap.recordstore.SimpleQuery;

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

    /** The Lucene version this store is working with. */
    public static final Version     VERSION = Version.LUCENE_36;

//    /** Default: 10% of HEAP; 32M is good for 512M RAM and merge size 16MB (Lucene 3). */
//    public static final double      MAX_RAMBUFFER_SIZE = 10d / 100d * Runtime.getRuntime().maxMemory() / 1000000;
//    
//    public static final double      MAX_MERGE_SIZE = 24;
//    
//    public static final double      MAX_DELETED_PERCENT = 10;

    
    /**
     * Returns a newly created configuration with default setting. 
     */
    public static Configuration newConfiguration() {
        return ConfigurationFactory.create( Configuration.class );
    }
    
    /**
     * 
     */
    public static class Configuration {

        public Config2<Configuration,File>             indexDir;

        @DefaultBoolean(false)
        public Config2<Configuration,Boolean>          clean;
        
        /**
         * The ExecutorService to be used for Lucene queries. If not set then a
         * default executor configuration is used.
         */
        public Config2<Configuration,ExecutorService>  executor;
        
        /** 
         * Should be around 10% of HEAP; 32M is good for 512M RAM and merge size 16MB (Lucene 3). 
         */
        @DefaultDouble(32)
        public Config2<Configuration,Double>           maxRamBufferSize;
        
        @DefaultDouble(24)
        public Config2<Configuration,Double>           maxMergeSize;
        
        @DefaultDouble(10)
        public Config2<Configuration,Double>           maxDeletedPercent;
        
        public Config2<Configuration,CacheManager>     documentCache;
        
        public LuceneRecordStore create() throws IOException {
            return new LuceneRecordStore( this );
        }
        
    }
    
    
    /**
     * The default {@link ExecutorService} used by the {@link #searcher} if no
     * executor if specified in the config.
     */
    private static ExecutorService  defaultExecutor;
    
    static {
        //int procs = Runtime.getRuntime().availableProcessors();
        ThreadFactory threadFactory = new ThreadFactory() {
            volatile int threadNumber = 0;
            public Thread newThread( Runnable r ) {
                String prefix = "Lucene-searcher-";
                Thread t = new Thread( r, prefix + threadNumber++ );
                t.setDaemon( false );
                //t.setPriority( Thread.NORM_PRIORITY - 1 );
                return t;
            }
        };
        defaultExecutor = new ThreadPoolExecutor( 0, 100,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                threadFactory );
        ((ThreadPoolExecutor)defaultExecutor).allowCoreThreadTimeOut( true );        
    }
    
    
    // instance *******************************************
    
    private Configuration           config;
    
    private Directory               directory;

    private Analyzer                analyzer = new WhitespaceAnalyzer( VERSION );
    
    private Cache<Object,Document>  cache = null;
    
    private CacheLoader<Object,Document> loader = new DocumentLoader();
    
    /** 
     * Maps docnum into record id; this helps to find a cached record
     * for a given docnum. This contains a map only if a cache is set.
     */
    private Cache<Integer,Object>   doc2id = null;

    private ExecutorService         executor = defaultExecutor;
    
    IndexSearcher                   searcher;

    IndexReader                     reader;
    
    /** Prevents {@link #reader} close/reopen by {@link LuceneUpdater} while in use. */
    ReadWriteLock                   lock;

    ValueCoders                     valueCoders = new ValueCoders( this );
    

    public LuceneRecordStore( Configuration config ) throws IOException {
        this.config = config;
        
        File indexDir = config.indexDir.get();
        if (indexDir == null) {
            directory = new RAMDirectory();
            log.info( "    RAMDirectory: " + Arrays.asList( directory.listAll() ) );            
            open( config.clean.get() );
        }
        else {
            if (!indexDir.exists()) {
                indexDir.mkdirs();
            }
            directory = null;
            // use mmap on 32bit Linux of index size < 100MB
            // more shared memory results in system stall under rare conditions
            if (Constants.LINUX && !Constants.JRE_IS_64BIT && MMapDirectory.UNMAP_SUPPORTED
                    && FileUtils.sizeOfDirectory( indexDir ) < 100*1024*1024) {
                try {
                    directory = new MMapDirectory( indexDir, null );
                    open( config.clean.get() );
                }
                catch (OutOfMemoryError e) {
                    log.info( "Unable to mmap index: falling back to default.");
                }
            }
            if (searcher == null) {
                directory = FSDirectory.open( indexDir );
                open( config.clean.get() );
            }
        }

        // init cache (if configured)
        CacheManager cacheManager = config.documentCache.get();
        if (cacheManager != null) {
            cache = cacheManager.createCache( "LuceneRecordStore-" + hashCode(), new MutableConfiguration()
                    .setReadThrough( true )
                    .setCacheLoaderFactory( () -> loader ) );
            doc2id = cacheManager.createCache( "LuceneRecordStore-doc2id-" + hashCode(), new MutableConfiguration() );
        }

        // init ExecutorService
        executor = Optional.ofNullable( config.executor.get() ).orElse( defaultExecutor );
        
        log.info( "Database: " + (indexDir != null ? indexDir.getAbsolutePath() : "RAM")
                + "\n    size: " + (indexDir != null ? FileUtils.sizeOfDirectory( indexDir ) : "-")
                + "\n    using: " + directory.getClass().getSimpleName()
                + "\n    files in directry: " + Arrays.asList( directory.listAll() )
                + "\n    cache: " + (cache != null ? cache.getClass().getSimpleName() : "none")
                + "\n    executor: " + executor.getClass().getSimpleName() );
    }


    /**
     * Creates a new store for the given filesystem directory. 
     * 
     * @param indexDir The directory to hold the store files.
     * @param clean
     * @throws IOException
     */
    public LuceneRecordStore( File indexDir, boolean clean ) throws IOException {
        this( newConfiguration()
                .indexDir.put( indexDir )
                .clean.put( clean ) );        
    }


    /**
     * Creates a new in-memory store.
     * 
     * @throws IOException
     */
    public LuceneRecordStore() throws IOException {
        config = newConfiguration();
        directory = new RAMDirectory();
        log.info( "    RAMDirectory: " + Arrays.asList( directory.listAll() ) );
        open( true );
    }

    
    protected ReadWriteLock newLock() {
        /*= new ReentrantReadWriteLock();*/
        return new IndexReaderReadWriteLock( reader );    
    }
    
    
    protected void open( boolean clean ) throws IOException {
        // create or clear index
        if (directory.listAll().length == 0 || clean) {
            IndexWriterConfig writerConfig = new IndexWriterConfig( VERSION, analyzer )
                    .setOpenMode( OpenMode.CREATE );
            IndexWriter iwriter = new IndexWriter( directory, writerConfig );
            iwriter.close();
            log.info( "    Index created." );
        }

        reader = IndexReader.open( directory );
        lock = newLock();
        searcher = new IndexSearcher( reader, executor );
    }
    
    
    @Override
    public void close() {
        Closer closer = new Closer();
        searcher = closer.close( searcher );
        reader = closer.close( reader );
        directory = closer.close( directory );
        cache = closer.close( cache );
        doc2id = closer.close( doc2id );
        lock = null;
        closer.throwAnyException();
    }

    
    /**
     * Collect Exceptions during close() 
     */
    static class Closer {
        
        private List<Throwable>     throwables = new ArrayList();
        
        public <T extends AutoCloseable> T close( T closeable ) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            }
            catch (Throwable e) {
                throwables.add( e );
            }
            return null;
        }

        public void throwAnyException() {
            if (!throwables.isEmpty()) {
                throw new RuntimeException( "Exceptions during close(): " + throwables, throwables.get( 0 ) );
            }
        }
    }
    
    
    @Override
    public boolean isClosed() {
        return directory == null;
    }


    @Override
    protected void finalize() throws Throwable {
        close();
    }

    
    public void setAnalyzer( Analyzer analyzer ) {
        this.analyzer = analyzer;
    }


    public IndexSearcher getIndexSearcher() {
        return searcher;
    }
    
    
    public long storeSizeInByte() {
        assert !isClosed() : "Store is closed already.";
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


//    public void setDocumentCache( Cache<Object,Document> cache ) {
//        this.cache = cache;
//        this.doc2id = CacheManager. instance().newCache( 
//                CacheConfig.DEFAULT.defaultElementSize( 128 ) );
//    }


    @Override
    public IRecordState newRecord() {
        assert !isClosed() : "Store is closed already.";
        LuceneRecordState result = new LuceneRecordState( this, new Document(), false );
        result.createId( null );
        return result;
    }

    
    @Override
    public IRecordState newRecord( Object id ) {
        assert reader != null : "Store is closed.";
        
        // FIXME fails for entities that are removed in this UnitOfWork
        try { assert get( id ) == null : "Id already exists: " + id; }
        catch (AssertionError e) { throw e; }
        catch (Exception e) { throw new RuntimeException( e ); }
        
        LuceneRecordState result = new LuceneRecordState( this, new Document(), false );
        result.createId( id );
        return result;
    }


    @Override
    public IRecordState get( Object id ) throws Exception {
        assert !isClosed() : "Store is closed already.";
        assert id instanceof String : "Given record identifier is not a String: " + id;
        
        Document doc = cache != null ? cache.get( id ) : loader.load( id );
                
        return doc != null ? new LuceneRecordState( LuceneRecordStore.this, doc, cache != null ) : null;
    }

    
    /**
     * Get the record for the given document index.
     * 
     * @param docnum The document index for the {@link IndexReader}.
     * @param fieldSelector The field selector, or null.
     * @return Newly created record instance. If {@link #cache} is active and
     *         the docnum is found in {@link #doc2id} then the underlying
     *         {@link Document} of the recod might be shared with other records.
     * @throws Exception
     */
    public LuceneRecordState get( int docnum, FieldSelector fieldSelector ) throws Exception {
        assert !isClosed() : "Store is closed already.";

        // if doc2id contains the docnum *and* the cache contains the id, then
        // we can create a record without accessing the underlying store
        if (doc2id != null) {
            Object id = doc2id.get( docnum );
            if (id != null) {
                Document doc = cache.get( id );
                if (doc != null) {
                    //System.out.println( "." );
                    return new LuceneRecordState( LuceneRecordStore.this, doc, true );
                }
            }
        }
        
        Document doc = null;
        lock.readLock().lock();
        try {
            doc = reader.document( docnum, fieldSelector );
        }
        finally {
            lock.readLock().unlock();
        }
        
        LuceneRecordState result = new LuceneRecordState( LuceneRecordStore.this, doc, false );
        if (cache != null) {
            doc2id.putIfAbsent( docnum, result.id() );
            //System.out.println( "-" );
            if (cache.putIfAbsent( result.id(), doc )) {
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
        
        @Override
        public Document load( Object id ) throws CacheLoaderException {
            log.trace( "LUCENE: termDocs: " + LuceneRecordState.ID_FIELD + " = " + id.toString() );

            lock.readLock().lock();
            Term term = new Term( LuceneRecordState.ID_FIELD, id.toString() );
            
            try (TermDocs termDocs = reader.termDocs( term )) {
                if (termDocs.next()) {
                    return reader.document( termDocs.doc() );
                }
                return null;
            }
            catch (IOException e) {
                throw new CacheLoaderException( e );
            }
            finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public Map<Object,Document> loadAll( Iterable<? extends Object> keys ) {
            throw new RuntimeException( "not yet implemented" );
        }
    }

    
    @Override
    public ResultSet find( RecordQuery query ) throws Exception {
        assert !isClosed() : "Store is closed already.";
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
    
    @Override
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
            assert !isClosed() : "Store is closed already.";
            try {
                // Defauls:
                //  - IndexWriterConfig#DEFAULT_RAM_BUFFER_SIZE_MB == 16MB
                //  - autCommit == false
                //  - 8 concurrent thread
                IndexWriterConfig writerConfig = new IndexWriterConfig( VERSION, analyzer )
                        .setOpenMode( OpenMode.APPEND )
                        .setRAMBufferSizeMB( config.maxRamBufferSize.get() );
                
                // limit segment size for lower pauses on interactive indexing
                LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
                mergePolicy.setMaxMergeMB( config.maxMergeSize.get() );
                writerConfig.setMergePolicy( mergePolicy );
                writerConfig.setMaxBufferedDocs( IndexWriterConfig.DISABLE_AUTO_FLUSH );
                writer = new IndexWriter( directory, writerConfig );
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
                log.trace( "STORE: " + LuceneRecordState.ID_FIELD + ": " + record.id() ); 
                for (Map.Entry<String,Object> entry : record) {
                    log.trace( "    field: " + entry.getKey() + " = " + entry.getValue() ); 
                }
            }

            Document doc = ((LuceneRecordState)record).getDocument();
            
            // add
            if (((LuceneRecordState)record).isNew()) {
                //((LuceneRecordState)record).createId();
                ((LuceneRecordState)record).setIsNew( false );
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
            assert record.id() != null : "Record is not yet stored.";

            Term idTerm = new Term( LuceneRecordState.ID_FIELD, (String)record.id() );
            writer.deleteDocuments( idTerm );

            if (cache != null) {
                cache.remove( record.id() );
            }
        }

        
        public void apply() {
            apply( false );
        }
        
        
        public void apply( boolean optimizeIndex ) {
            assert writer != null : "Updater is closed.";
            Timer timer = new Timer();
            lock.writeLock().lock();
            try {
                writer.commit();
                log.debug( "Writer commited. (" + timer.elapsedTime() + "ms)"  );
                
                double deleted = reader.numDeletedDocs();
                double total = reader.numDocs();
                double percent = 100d / total * deleted; 
                if (optimizeIndex || percent > config.maxDeletedPercent.get() ) {
                    Timer t = new Timer();
                    writer.forceMergeDeletes( true );
                    writer.forceMerge( 1, true );
                    log.debug( "Writer optimization done. (" + t.elapsedTime() + "ms)"  );
                }
                writer.close();
                writer = null;
                
                searcher.close();
                IndexReader newReader = IndexReader.openIfChanged( reader );
                if (newReader != null) {
                    lock.writeLock().unlock();
                    reader.close();
                    reader = newReader;
                    lock = newLock();
                }
                searcher = new IndexSearcher( reader, executor );
                
                if (doc2id != null) {
                    doc2id.clear();
                }
                
                log.info( "COMMIT: " + timer.elapsedTime() + "ms" );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
            finally {
//                lock.writeLock().unlock();
//                lock = newLock();
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
            catch (AlreadyClosedException e) {
            }
            catch (Exception e) {
                log.warn( "Error during discard()", e );
                //throw new RuntimeException( e );
            }
        }
    }
    

    /**
     * Use Lucene IndexReader internal mechanism to MROW lock. Maybe(?) less overhead
     * than {@link ReentrantReadWriteLock}.
     * 
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    static class IndexReaderReadWriteLock
            implements ReadWriteLock {

        private IndexReader     indexReader;
        
        /**
         * This is just a dummy. The writeLock is aquired just to call
         * {@link IndexReader#close()}. This calls decRef() itself and so
         * synchronizes with readers.
         */
        private Lock            writeLock = DummyConcurrentLock.INSTANCE;
        
        private Lock            readLock = new Lock() {
            @Override
            public void lock() {
                indexReader.incRef();
            }
            @Override
            public void unlock() {
                try {
                    indexReader.decRef();
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }
            @Override
            public void lockInterruptibly() throws InterruptedException {
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public boolean tryLock() {
                return indexReader.tryIncRef();
            }
            @Override
            public boolean tryLock( long time, TimeUnit unit ) throws InterruptedException {
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            public Condition newCondition() {
                throw new RuntimeException( "not yet implemented." );
            }
        };
        
        public IndexReaderReadWriteLock( IndexReader indexReader ) {
            this.indexReader = indexReader;
        }

        @Override
        public Lock readLock() {
            return readLock;
        }

        @Override
        public Lock writeLock() {
            return writeLock;
        }
    }
    
}
