package org.polymap.catalog.h2;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension2;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Polymap;

import org.polymap.catalog.h2.data.H2DataStoreFactory;

/**
 * H2 ServiceExtension
 * 
 * @since 3.1
 */
public class H2ServiceExtension 
    extends AbstractDataStoreServiceExtension
    implements ServiceExtension2 {

    private static final Log log = LogFactory.getLog( H2ServiceExtension.class );

    private static H2DataStoreFactory factory;


    public static H2DataStoreFactory getFactory() {
        if (factory == null) {
            factory = new H2DataStoreFactory();
            File baseDir = new File( Polymap.getDataDir(), "h2" );
            baseDir.mkdirs();
            factory.setBaseDirectory( baseDir );
        }
        return factory;
    }


    // instance *******************************************
    
    public IService createService( URL id, Map<String, Serializable> params ) {
        if (getFactory().canProcess( params )) {
            if (id == null) {
                try {
                    URL toURL = toURL( params );
                    return new H2ServiceImpl( toURL, params );
                } 
                catch (MalformedURLException e) {
                    log.info( "Unable to construct proper service URL.", e );
                    return null;
                }
            }
            return new H2ServiceImpl( id, params );
        }
        return null;
    }

    
    public static URL toURL( Map<String, Serializable> params ) throws MalformedURLException {
        String the_host = (String)params.get( H2DataStoreFactory.HOST.key );
        String intPort = (String)params.get( H2DataStoreFactory.PORT.key );
        String the_database = (String)params.get( H2DataStoreFactory.DATABASE.key );
        String the_username = (String)params.get( H2DataStoreFactory.USER.key );
        String the_password = (String)params.get( H2DataStoreFactory.PASSWD.key );

        URL toURL = toURL( the_username, the_password, the_host, intPort, the_database );
        return toURL;
    }

    
    /**
     * Creates some  Params for mysql based off a url that is passed in
     * 
     * @see net.refractions.udig.catalog.ServiceExtension#createParams(java.net.URL)
     * @param url for the mysql database
     * @return x
     */
    public Map<String, Serializable> createParams( URL url ) {
        if (!isH2( url )) {
            return null;
        }

        ParamInfo info = parseParamInfo( url );

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put( H2DataStoreFactory.DBTYPE.key, (Serializable)H2DataStoreFactory.DBTYPE.sample );
        params.put( H2DataStoreFactory.HOST.key, info.host );
        params.put( H2DataStoreFactory.PORT.key, info.the_port.toString() );
        params.put( H2DataStoreFactory.DATABASE.key, info.the_database );
        params.put( H2DataStoreFactory.USER.key, info.username );
        params.put( H2DataStoreFactory.PASSWD.key, info.password );

        return params;
    }

    
    /**
     * Look up Param by key; used to access the correct sample
     * value for DBTYPE.
     *
     * @param key
     */
    public static Param getPram( String key ){
        for( Param param : getFactory().getParametersInfo()){
            if( key.equals( param.key )){
                return param;
            }
        }
        return null;
    }

    
    /** A couple quick checks on the url */
    public static final boolean isH2( URL url ) {
        if (url == null) {
            return false;
        }
        return url.getProtocol().toLowerCase().equals( "h2" )
                || url.getProtocol().toLowerCase().equals( "h2.jdbc" )
                || url.getProtocol().toLowerCase().equals( "jdbc.h2" );
    }

    public static URL toURL( String username, String password, String host, String database) throws MalformedURLException {
    	return toURL(username, password, host, 3306, database);
    }
    
    public static URL toURL( String username, String password, String host, Integer port, String database) throws MalformedURLException {
    	return toURL(username, password, host, port.toString(), database);
    }
    
    public static URL toURL( String username, String password, String host, String port, String database) throws MalformedURLException {
        String the_spec = "jdbc.h2:" + database;
        return toURL(the_spec);
    }

    public static URL toURL( String the_spec ) throws MalformedURLException {
        return new URL(null, the_spec, CorePlugin.RELAXED_HANDLER);
    }

    
    public String reasonForFailure( URL url ) {
        if (!isH2( url )) {
            return "Not a H2 URL: " + url;
        }
        return reasonForFailure( createParams( url ) );
    }


    protected DataStoreFactorySpi getDataStoreFactory() {
        return getFactory();
    }

}
