package org.polymap.core.data.feature.recordstore.catalog;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension2;
import net.refractions.udig.core.internal.CorePlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.feature.recordstore.RDataStore;
import org.polymap.core.runtime.Polymap;

/**
 * {@link RDataStore} ServiceExtension
 * 
 * @since 3.1
 */
public class RServiceExtension 
    implements ServiceExtension2 {

    private static final Log log = LogFactory.getLog( RServiceExtension.class );

    private static RDataStoreFactory factory;


    public static RDataStoreFactory factory() {
        if (factory == null) {
            factory = new RDataStoreFactory();
            File baseDir = new File( Polymap.getDataDir(), "recordstore" );
            baseDir.mkdirs();
            factory.setBaseDirectory( baseDir );
        }
        return factory;
    }


    // instance *******************************************
    
    @Override
    public IService createService( URL id, Map<String, Serializable> params ) {
        if (reasonForFailure( params ) == null) {
            if (id == null) {
                try {
                    URL toURL = toURL( params );
                    return new RServiceImpl( toURL, params );
                } 
                catch (MalformedURLException e) {
                    log.info( "Unable to construct proper service URL.", e );
                    return null;
                }
            }
            else {
                return new RServiceImpl( id, params );
            }
        }
        return null;
    }

    
    @Override
    public String reasonForFailure( Map<String, Serializable> params ) {
        try {
            if (RDataStoreFactory.DBTYPE.lookUp( params ) == null) {
                return RDataStoreFactory.DBTYPE + ": param missing: " + params;                
            }
            else if (RDataStoreFactory.DBTYPE.lookUp( params ) == null) {
                return RDataStoreFactory.DATABASE + ": param missing: " + params;                
            }
            return null;
        }
        catch (IOException e) {
            return e.toString();
        }
    }


    public static URL toURL( Map<String, Serializable> params ) throws MalformedURLException {
        String database = (String)params.get( RDataStoreFactory.DATABASE.key );
        return toURL( database );
    }

    
    /**
     * 
     * @see net.refractions.udig.catalog.ServiceExtension#createParams(java.net.URL)
     * @param url for the mysql database
     * @return x
     */
    public Map<String, Serializable> createParams( URL url ) {
        if (!isValid( url )) {
            return null;
        }
        Map<String,Serializable> params = new HashMap<String, Serializable>();
        params.put( RDataStoreFactory.DBTYPE.key, (Serializable)RDataStoreFactory.DBTYPE.sample );
        params.put( RDataStoreFactory.DATABASE.key, url.getPath() );
        return params;
    }

    
    public static final boolean isValid( URL url ) {
        if (url == null) {
            return false;
        }
        else if (url.getProtocol().toLowerCase().equals( RDataStoreFactory.DBTYPE.sample )) {
            return false;
        }
        else if (url.getPath().length() == 0) {
            return false;            
        }
        return true;
    }

    
    public static URL toURL( String database) throws MalformedURLException {
        String spec = RDataStoreFactory.DBTYPE.sample + ":" + database;
        return new URL( null, spec, CorePlugin.RELAXED_HANDLER );
    }


    public String reasonForFailure( URL url ) {
        if (!isValid( url )) {
            return "Not a RecordStore URL: " + url;
        }
        return reasonForFailure( createParams( url ) );
    }


//    protected DataStoreFactorySpi getDataStoreFactory() {
//        return factory();
//    }

}
