package org.polymap.core.data.feature.recordstore.catalog;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import java.io.File;
import java.io.Serializable;

import org.geotools.data.DataAccessFactory.Param;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import com.google.common.collect.MapMaker;

import org.polymap.core.Messages;
import org.polymap.core.data.feature.recordstore.LuceneQueryDialect;
import org.polymap.core.data.feature.recordstore.RDataStore;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheManager;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * DataStoreFacotry for {@link RDataStore} database.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
class RDataStoreFactory {

    private static Log log = LogFactory.getLog( RDataStoreFactory.class );

    private static ConcurrentMap<File,RDataStore>   stores = new MapMaker().initialCapacity( 64 ).concurrencyLevel( 4 ).weakValues().makeMap();
    
    /** parameter for database type */
    public static final Param       DBTYPE = new Param( "dbtype", String.class, "Type", true, "recordstore" );
    
    /** parameter for database name */
    public static final Param       DATABASE = new Param( "database", String.class, "Database Name (Directory)", true, "database" );
    
    /** Base location to store database files. */
    private File                    baseDirectory;

    
    public void setBaseDirectory( File baseDirectory ) {
        this.baseDirectory = baseDirectory;
    }

    
    public File getBaseDirectory() {
        return baseDirectory;
    }
    
    
    protected void setupParameters( Map parameters ) {
        parameters.put( DBTYPE.key, DBTYPE );
        parameters.put( DATABASE.key, DATABASE );
    }

    
    public String getDisplayName() {
        return Messages.get( "RDataStoreFactory_displayName" );
    }

    
    public String getDescription() {
        return "RecordStore Database";
    }

    
    protected String getDatabaseID() {
        return (String) DBTYPE.sample;
    }

    
    public RDataStore createDataStore( Map<String, Serializable> params )
    throws Exception {
        assert baseDirectory != null;
        String database = (String)DATABASE.lookUp( params );

        File dir = !new File(database).isAbsolute()
                ? new File( baseDirectory, database )    
                : new File( database );
        
        return createDataStore( dir );        
    }

    
    public RDataStore createNewDataStore( Map<String, Serializable> params )
    throws Exception {
        assert baseDirectory != null;
        String database = (String)DATABASE.lookUp( params );

        File dir = !new File(database).isAbsolute()
                ? new File( baseDirectory, database )    
                : new File( database );

        if (dir.exists() && dir.listFiles().length > 0) {
            throw new IllegalStateException( "Database directory is not empty." );
        }
        return createDataStore( dir );
    }

    
    protected RDataStore createDataStore( File dir ) 
    throws Exception {
        RDataStore result = stores.get( dir );
        if (result == null) {
            LuceneRecordStore store = new LuceneRecordStore( dir, false );

            Cache<Object,Document> documentCache = CacheManager.instance().newCache( CacheConfig.DEFAULT );
            store.setDocumentCache( documentCache );
            log.info( "### CACHE ACTIVATED! ###" );

            result = new RDataStore( store, new LuceneQueryDialect() );    

            RDataStore old = stores.put( dir, result );
            if (old != null) {
                result.dispose();
                result = old;
            }
        }
        return result;
    }
    
    
    public Param[] getParametersInfo() {
        throw new RuntimeException( "not yet implemented" );
    }

}
