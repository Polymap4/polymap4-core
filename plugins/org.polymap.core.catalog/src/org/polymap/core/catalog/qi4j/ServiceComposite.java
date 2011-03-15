/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.catalog.qi4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.core.internal.CorePlugin;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.qi4j.EntityMixin;
import org.polymap.core.qi4j.ModificationConcern;
import org.polymap.core.qi4j.security.ACL;
import org.polymap.core.qi4j.security.ACLCheckConcern;
import org.polymap.core.qi4j.security.ACLFilterConcern;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@Concerns( {
        ACLCheckConcern.class, 
        ACLFilterConcern.class, 
        ModificationConcern.class
})
@Mixins( {
        ServiceComposite.Mixin.class, 
        ACL.Mixin.class, 
        EntityMixin.class
})
public interface ServiceComposite
        extends org.polymap.core.model.ACL, Entity, EntityComposite {

    public static final String          PROP_PARAMS = "params";

    /**
     * The service ID in the form: <code>typeQulifier|id</code>.
     */
    @Optional
    @UseDefaults
    Property<String>                    serviceId();
  
    /**
     * The connection params in the form: <code>key|type|value</code>. 
     */
    @Optional
    @UseDefaults
    Property<List<String>>              params();
  
    /**
     * The persistent properties in the form: <code>key|type|value</code>. 
     */
    @Optional
    @UseDefaults
    Property<List<String>>              props();
  
    @ModelProperty(PROP_PARAMS)
    public void init( IService service );
    
    public IService getService();
    

    /**
     * Transient fields and methods. 
     * <p>
     * Impl. note: property change events are handled by the
     * {@link ChangeEventSideEffect}.
     */
    public static abstract class Mixin
            implements ServiceComposite {
        
        private static Log log = LogFactory.getLog( Mixin.class );

        private static final String     COLON_ENCODING = "@col@";
        private static final String     TYPE_QUALIFIER = "@type@"; //$NON-NLS-1$
        private static final String     PROPERTIES_KEY = "_properties"; //$NON-NLS-1$
        private static final String     VALUE_ID = "value"; //$NON-NLS-1$
        private static final String     TYPE_ID = "type"; //$NON-NLS-1$
        private static final String     ENCODING = "UTF-8"; //$NON-NLS-1$

        @This ServiceComposite          composite;
        
        private IService                service;

        /** Reference directory to consider when making relative files. */
        private File                    reference;

        
        public void init( IService _service ) {
            this.service = _service;
            serialize();
        }
        
        
        public IService getService() {
            checkRestore();
            return service;
        }

        /**
         * Stores the files into the preferences node.
         * 
         * @param monitor Progress monitor
         * @param node the preferences to write to
         * @param resolves the resolves to commit
         * @throws BackingStoreException
         * @throws IOException
         */
        protected void serialize() {
            try {
//                IService service = null;
//                if (member instanceof IGeoResource) {
//                    service = ((IGeoResource)member).service( monitor );
//                }
//                else if (member instanceof IService) {
//                    service = (IService)member;
//                }
//                // its not a type that we know how to get the parameters
//                // from
//                if (service == null) {
//                    continue;
//                }
                
//                String id = null;
//                ID serviceID = service.getID();
//                if (serviceID.isFile()) {
//                    System.out.println( "ServiceParameterPersister.store(): handling isFile() commented out!" );
//                    id = URLEncoder.encode( serviceID.toString(), ENCODING );
//                    //
//                    // String path =
//                    // serviceID.toFile().getAbsolutePath();
//                    // path = path.replace( ":", COLON_ENCODING );
//                    // id = URLEncoder.encode( path, ENCODING );
//                }
//                else {
//                    id = URLEncoder.encode( serviceID.toString(), ENCODING );
//                }
//                if (serviceID.getTypeQualifier() != null) {
//                    id = id + TYPE_QUALIFIER + URLEncoder.encode( serviceID.getTypeQualifier(), ENCODING );
//                }
                
                // ID
                serviceId().set( service.getID().getTypeQualifier() + "|" + 
                        service.getID().toString() );

                // connection params
                params().set( new ArrayList() );
                for (Map.Entry<String, Serializable> entry : service.getConnectionParams().entrySet()) {
                    Serializable object = entry.getValue();

                    URL url = null;
                    if (object instanceof URL) {
                        url = (URL)object;
                    }
                    else if (object instanceof File) {
                        url = ((File)object).toURI().toURL();
                    }

                    String value = null;
                    // if reference is null then we can only encode the
                    // absolute path
                    if (reference != null && url != null) {
                        URL relativeURL = URLUtils.toRelativePath( this.reference, url );
                        value = URLUtils.urlToString( relativeURL, true );
                    }
                    else {
                        value = object != null ? object.toString() : null;
                    }

                    if (value != null) {
                        params().get().add( entry.getKey() + "|" + 
                                object.getClass().getName() + "|" + value );
                    }
                }
                
                // properties
                props().set( new ArrayList() );
                for (Map.Entry<String, Serializable> entry : service.getPersistentProperties().entrySet()) {
                    Serializable object = entry.getValue();
                    props().get().add( entry.getKey() + "|" +
                            object.getClass().getName() + "|" + object.toString() );
                }
            }
            catch (Exception e) {
                log.error( "Error storing: " + service.getIdentifier(), e );
            }
        }
        
        
        /**
         * Using the connection parameter information in the preferences node
         * restore the state of the local catalog.
         * 
         * @param node
         */
        protected void checkRestore() {
            if (service != null) {
                return;
            }
            try {
                String[] idParts = StringUtils.split( serviceId().get(), "|" );
                String qualifier = idParts[0].equals( "null" ) ? null : idParts[0];
                ID id = new ID( idParts[1], qualifier );  //toId( id );
                log.debug( "checkRestore(): ID= " + id );

                // connection params
                Map<String, Serializable> connectionParams = new HashMap();
                for (String entry : params().get()) {
                    String[] parts = StringUtils.split( entry, "|" );
                    String typeName = parts[1];
                    String value = parts[2];
                    
                    connectionParams.put( parts[0], toObject( typeName, value ) );
                }

                // properties
                Map<String, Serializable> properties = new HashMap();
                for (String entry : props().get()) {
                    String[] parts = StringUtils.split( entry, "|" );
                    String typeName = parts[1];
                    String value = parts[2];
                    
                    properties.put( parts[0], toObject( typeName, value ) );
                }

                service = locateService( id, connectionParams, properties );

//                // should we check the local catalog to see if it already
//                // has an entry for this service?
//                found = localCatalog.getById( IService.class, candidate.getID(), null );
            }
            catch (Throwable t) {
                log.error( "", t );
            }
        }
        
        
        private Serializable toObject( String type, String txt ) 
        throws ClassNotFoundException{
            Class<?> clazz = Class.forName( type );

            // reference can be null so only decode relative path if reference is not null.
            // ie assume the URL/File is absolute if reference is null
            if( reference !=null && (URL.class.isAssignableFrom(clazz) 
                    || File.class.isAssignableFrom(clazz) )){
                URL result;
                try {
                    result = URLUtils.constructURL( this.reference, txt );
                    if (URL.class.isAssignableFrom( clazz ))
                        return (Serializable)result;
                    else
                        return new File( result.getFile() );
                } 
                catch (MalformedURLException e) {
                    log.warn( type + " was not able to use as a URL so we're putting it in to the parameters as a String", null ); //$NON-NLS-1$                    
                    return txt;
                }                           
            }

            try {
                // try finding the constructor that takes a string
                Constructor<?> constructor = clazz.getConstructor( new Class[] { String.class } );
                Object object = constructor.newInstance( new Object[] { txt } );
                return (Serializable)object;
            }
            catch (Throwable t) {
                throw new RuntimeException( "Restoring via setter no supported: " + type );
//                //failed lets try a setter
//                try {
//                    Method[] methods = clazz.getMethods();
//                    Method bestMatch = findBestMatch(methods);
//
//                    if (bestMatch != null) {
//                        Object obj = clazz.newInstance();
//                        bestMatch.invoke(obj, new Object[]{txt});
//                        return (Serializable) obj;
//                    }
//                }
//                catch (Throwable t2) {
//                    CatalogPlugin.log("error that occurred when trying use construction with string: "+type+" value= "+txt, t ); //$NON-NLS-1$ //$NON-NLS-2$
//                    CatalogPlugin.log("error that occurred when use a setter: "+type+" value= "+txt, t2 );  //$NON-NLS-1$//$NON-NLS-2$
//                }
            }

//            return txt;
        }

        
        /**
         * Convert a persisted id string into a URL.
         * <p>
         * This method will decode the string based ENCODING
         * @param id Persisted id string
         * @return URL based on provided id string
         */
        private ID toId( String encodedId ) {
            ID id;
            try {
                String decodeId = URLDecoder.decode( encodedId, ENCODING );
                String[] parts = decodeId.split( TYPE_QUALIFIER );
                String qualifier = null;
                if (parts.length == 2) {
                    qualifier = parts[1];
                }
                try {
                    URL url = new URL( null, parts[0], CorePlugin.RELAXED_HANDLER );
                    id = new ID( url, qualifier );
                }
                catch (MalformedURLException e) {
                    String path = parts[0].replaceAll( COLON_ENCODING, ":" );
                    id = new ID( new File( path ), qualifier );
                }
            }
            catch (UnsupportedEncodingException e) {
                //CatalogPlugin.log( "Could not code preferences URL", e ); //$NON-NLS-1$
                throw new RuntimeException( e );
            }
            return id;
        }


        /**
         * Create an IService from the provided connection parameters and add
         * them to the provided catalog.
         * 
         * @param targetID In the event of a tie favour the provided targetID
         * @param params The connection parameters. Used to to ask the
         *        ServiceFactory for list of candidates
         */
        protected IService locateService( ID targetID, 
                Map<String,Serializable> params,
                Map<String,Serializable> properties ) {
            
//            IService found = localCatalog.getById( IService.class, targetID, null );
//
//            if (found != null) {
//                System.out.println( "    already found: " + found );
//                return;
//            }

            IServiceFactory serviceFactory = net.refractions.udig.catalog.CatalogPlugin.getDefault().getServiceFactory();

            List<IService> candidates = serviceFactory.createService( params );
            log.debug( "locateService(): candidates= " + candidates );
            
            if (candidates.isEmpty()) {
                log.warn( "Nothing was able to be loaded from saved preferences: " + params, null ); //$NON-NLS-1$
                return null;
            }
            
            for (IService candidate : candidates) {
                ID candidateID = candidate.getID();
                if (candidateID.equals( targetID )) {
                    try {
                        // restore persisted properties
                        candidate.getPersistentProperties().putAll( properties );
                        return candidate;
                    }
                    catch (Exception e) {
                        // could not restore propreties
                        log.error( "    error while restoring properties: " + e, e );
                    }
                }
                else {
                    // Service was already available
                    System.out.println( "    already found: " + candidateID );
                }
            }
            log.warn( "No service located for: " + params, null ); //$NON-NLS-1$
            return null;
        }
    }
    
}
