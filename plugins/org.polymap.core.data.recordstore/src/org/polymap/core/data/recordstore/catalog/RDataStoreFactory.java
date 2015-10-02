package org.polymap.core.data.recordstore.catalog;

import java.util.Map;

import java.io.File;
import java.io.Serializable;

import org.geotools.data.DataAccessFactory.Param;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.Messages;
import org.polymap.core.data.recordstore.RDataStore;
import org.polymap.core.data.recordstore.lucene.LuceneQueryDialect;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.cache.CacheManager;

import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * DataStoreFacotry for {@link RDataStore} database.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class RDataStoreFactory {

    private static Log log = LogFactory.getLog( RDataStoreFactory.class );

    private static Cache<File,RDataStore>   stores = CacheManager.instance().newCache(
            CacheConfig.DEFAULT.initSize( 64 ) );
    
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
        return stores.get( dir, new CacheLoader<File,RDataStore,Exception>() {
            public RDataStore load( File key ) throws Exception {
                LuceneRecordStore store = new LuceneRecordStore( key, false );

//                Cache<Object,Document> documentCache = CacheManager.instance().newCache( 
//                        CacheConfig.DEFAULT.initSize( 10000 ) );
//                store.setDocumentCache( documentCache );
                log.info( "### NO CACHE ACTIVATED! ###" );

                return new RDataStore( store, new LuceneQueryDialect() );    
            }
            public int size() throws Exception {
                return -1;
            }
        });
    }
    
    
    public Param[] getParametersInfo() {
        throw new RuntimeException( "not yet implemented" );
    }

}
