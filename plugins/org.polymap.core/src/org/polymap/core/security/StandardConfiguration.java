/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.session.DefaultSessionContext;
import org.polymap.core.runtime.session.RapSessionContextProvider.RapSessionContext;
import org.polymap.core.runtime.session.SessionContext;

/**
 * The standard JAAS configuration. Config file: {@value #CONFIG_FILE_NAME} in
 * data/workspace; create default from "resource/jaas_config.txt". Returns
 * {@link SecurityContext#APPLICATION_CONFIG_NAME} for RAP session context and
 * {@link SecurityContext#SERVICES_CONFIG_NAME} otherwise.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StandardConfiguration
        implements SecurityContext.Configuration {

    private static Log log = LogFactory.getLog( StandardConfiguration.class );

    public static final String          CONFIG_FILE_NAME = "jaas_config.txt";
    
    
    @Override
    public String getConfigName() {
        SessionContext session = SessionContext.current();
        if (session instanceof RapSessionContext) {
            return SecurityContext.APPLICATION_CONFIG_NAME;
        }
        else if (session instanceof DefaultSessionContext) {
            return SecurityContext.SERVICES_CONFIG_NAME;            
        }
        else {
            throw new RuntimeException( "Unknown SessionContext type: " + session );
        }
    }

    
    @Override
    public URL getConfigFile() {
        try {
            File f = new File( CorePlugin.getDataLocation( CorePlugin.instance() ), "../../" + CONFIG_FILE_NAME );
            if (!f.exists()) {
                createConfigFile( f );
            }
            return f.toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    protected void createConfigFile( File f ) {
        log.info( "Creating JAAS default config: " + f.getAbsolutePath() );

        URL defaultConfigUrl = CorePlugin.instance().getBundle().getEntry( "resource/jaas_config.txt" );
        
        try (
            OutputStream out = new FileOutputStream( f );
            InputStream in = defaultConfigUrl.openStream()
        ){
            IOUtils.copy( in, out );
        }
        catch (Exception e) {
            throw new RuntimeException( "Unable to create default jaas_config.txt in workspace.", e );
        }
    }
    
}
