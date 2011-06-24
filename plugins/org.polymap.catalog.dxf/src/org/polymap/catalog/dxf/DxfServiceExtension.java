/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
 *
 */
package org.polymap.catalog.dxf;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.dxf.DXFDataStoreFactory;


/**
 * Service Extension implementation for Shapefiles.
 * 
 * @author David Zwiers, Refractions Research
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public class DxfServiceExtension 
        extends AbstractDataStoreServiceExtension 
        implements ServiceExtension {

    private static DXFDataStoreFactory      dxfDSFactory;

    
	public static DXFDataStoreFactory getDxfDSFactory(){
        if(dxfDSFactory == null) {
            dxfDSFactory = new DXFDataStoreFactory();
        }
        return dxfDSFactory;
    }
    
	
    public IService createService( URL id, Map<String,Serializable> params ) {
        if (params.containsKey( DXFDataStoreFactory.PARAM_URL.key )) {
            URL url = null;
            if (params.get( DXFDataStoreFactory.PARAM_URL.key ) instanceof URL) {
                url = (URL)params.get( DXFDataStoreFactory.PARAM_URL.key );
            }
            else {
                try {
                    url = (URL)DXFDataStoreFactory.PARAM_URL.parse( params.get(
                            DXFDataStoreFactory.PARAM_URL.key ).toString() );
                    params.put( DXFDataStoreFactory.PARAM_URL.key, url );
                }
                catch (Throwable e1) {
                    // log this?
                    e1.printStackTrace();
                    return null;
                }
            }
            String file = url.getFile();
            file = file.toLowerCase();
            if (!(file.endsWith( ".dxf" ) || file.endsWith( ".dwg" ))) {
                return null;
            }
            if (id == null) {
                return new DxfServiceImpl( url, params );
            }
            return new DxfServiceImpl( id, params );
        }
        return null;
    }


    public Map<String, Serializable> createParams( URL url ) {
        URL url2 = url;
        if (!isSupportedExtension( url )) {
            return null;
        }
        url2 = toDxfURL( url2 );
        if (url2 == null) {
            return null;
        }
        if (getDxfDSFactory().canProcess( url2 )) {
            HashMap<String, Serializable> params = new HashMap<String, Serializable>();
            params.put( DXFDataStoreFactory.PARAM_URL.key, url2 );
            params.put( DXFDataStoreFactory.PARAM_SRS.key, "EPSG:31468" );
            return params;
        }
        return null;
    }


    private boolean isSupportedExtension( URL url ) {
        String file = url.getFile();
        return file.toLowerCase().endsWith( ".dxf" ) || file.toLowerCase().endsWith( ".dwg" );
    }


    @SuppressWarnings("deprecation")
    // file.toURL is deprecated in Java 6
    private URL toDxfURL( URL url ) {
        URL url2 = url;

        String auth = url.getAuthority();
        String urlFile = url2.getPath();
        if (auth != null && auth.length() != 0) {
            urlFile = "//" + auth + urlFile; //$NON-NLS-1$
        }
        if (!urlFile.toLowerCase().endsWith( ".dxf" )) { //$NON-NLS-1$
            urlFile = urlFile.substring( 0, urlFile.lastIndexOf( '.' ) ) + ".dxf"; //$NON-NLS-1$
        }
        try {
            File file = new File( urlFile );
            url2 = file.toURL();
        }
        catch (MalformedURLException e) {
            return null;
        }
        return url2;
    }


    @Override
    protected String doOtherChecks( Map<String, Serializable> params ) {
        return null;
    }

    @Override
    protected DataStoreFactorySpi getDataStoreFactory() {
        return getDxfDSFactory();
    }


    public String reasonForFailure( URL url ) {
        if (!isSupportedExtension( url )) {
            // XXX
            return "Messages.ShpServiceExtension_badExtension";
        }
        if (toDxfURL( url ) == null) {
            // XXX
            return "Messages.ShpServiceExtension_cantCreateURL";
        }
        return reasonForFailure( createParams( url ) );
    }

}
