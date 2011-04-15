/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.data.entityfeature.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.data.Messages;
import org.polymap.rhei.data.entityfeature.EntityProvider;
import org.polymap.rhei.data.entityfeature.EntitySourceProcessor;

/**
 * This service references a Qi4j entity store via ({@link EntityProvider}). The
 * {@link EntitySourceProcessor} translates the entities into features for the
 * pipeline.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public abstract class EntityServiceImpl
        extends IService {

    private static final Log log = LogFactory.getLog( EntityServiceImpl.class );

    private String                      name;
    
    private URL                         id;
    
    private Map<String,Serializable>    params;
    
    private EntityProvider[]            providers;
    
    private List<EntityGeoResourceImpl> resources; 

    
    public EntityServiceImpl( String name, String id, EntityProvider... providers ) {
        try {
            this.id = new URL( id );
        }
        catch (MalformedURLException e) {
            throw new RuntimeException( e );
        }
        this.name = name;
        
        // providers / resources
        this.providers = providers;
        this.resources = new ArrayList( providers.length );
        for (EntityProvider provider : providers) {
            resources.add( new EntityGeoResourceImpl( this, provider ) );
        }
        
        // build params
        this.params = new HashMap();
        this.params.put( EntityServiceExtensionImpl.KEY, id );
        this.params.put( EntityServiceExtensionImpl.NAME_KEY, name );
        for (int i=0; i<providers.length; i++) {
            this.params.put( 
                    EntityServiceExtensionImpl.PROVIDER_BASE_KEY + i,
                    providers[i].getClass().getName() );
            
        }
    }


//    public EntityServiceImpl( URL id2, Map<String, Serializable> params ) {
//        this.params = params;
//
//        this.id = (URL)params.get( EntityServiceExtensionImpl.KEY );
//        this.name = (String)params.get( EntityServiceExtensionImpl.NAME_KEY );
//
//        boolean noMoreProviders = false;
//        for (int i=0; !noMoreProviders; i++) {
//            String providerClassName = (String)params.get( EntityServiceExtensionImpl.PROVIDER_BASE_KEY + i );
//            if (providerClassName != null) {
//                try {
//                    EntityProvider provider = (EntityProvider)Thread.currentThread().getContextClassLoader()
//                            .loadClass( providerClassName ).newInstance();
//                }
//                catch (Exception e) {
//                    log.warn( "Unable to load EntityProvider: " + providerClassName, e );
//                }
//            }
//        }
//    }


    protected IServiceInfo createInfo( IProgressMonitor monitor )
            throws IOException {
        
        return new IServiceInfo() {

            public String getTitle() {
                return name; //Messages.get( "Qi4jServiceImpl_title" ); 
            }

            public String getDescription() {
                return Messages.get( "EntityServiceImpl_description" ); 
            }
        };
    }


    public Map<String, Serializable> getConnectionParams() {
        return params;
    }


    public List<? extends IGeoResource> resources( IProgressMonitor monitor )
            throws IOException {
        return resources;
    }


    public URL getIdentifier() {
        return id;
    }


    public Throwable getMessage() {
        return null;
    }


    public Status getStatus() {
        return Status.CONNECTED;
    }
    
}
