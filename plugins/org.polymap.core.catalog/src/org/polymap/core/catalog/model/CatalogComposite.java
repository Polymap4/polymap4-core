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
package org.polymap.core.catalog.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;

import net.refractions.udig.catalog.ICatalogInfo;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolve.Status;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ITransientResolve;
import net.refractions.udig.catalog.URLUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.catalog.Messages;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.qi4j.event.MethodOperationBoundsConcern;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.qi4j.security.ACL;
import org.polymap.core.runtime.Polymap;

/**
 * ... 
 * <p/>
 * Previous version needed {@link MethodOperationBoundsConcern} to wrap every method
 * call in a single operation to meet the requirements of the operation system. This
 * is obsolet now. The downside is that just importing a new data source into the catalog
 * is no oparation: no undo/redo, no save. 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@Concerns({
// FIXME AZV does not find services with ACL check
//        ACLCheckConcern.class,
//        ACLFilterConcern.class,
        PropertyChangeSupport.Concern.class
})
@Mixins({
        CatalogComposite.Mixin.class, 
        ACL.Mixin.class, 
        PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
})
public interface CatalogComposite
        extends QiEntity, org.polymap.core.model.security.ACL, 
                PropertyChangeSupport, ModelChangeSupport, EntityComposite {

    public static final String          PROP_SERVICES = "services";

    @Optional
    @UseDefaults
    ManyAssociation<ServiceComposite>   services();

    // internal methods ***
    
    public Object findServiceEntity( IService service );
    
    public void setFacade( CatalogImpl facade );
    
    // ICatalog methods ***
    
    public <T extends IResolve> T getById(
            Class<T> type, final ID id, @Optional IProgressMonitor monitor);
    
    @ModelProperty(PROP_SERVICES)
    public void add( IService service )
            throws UnsupportedOperationException;

    @ModelProperty(PROP_SERVICES)
    public void addTransient( ITransientResolve service )
        throws UnsupportedOperationException;

    @ModelProperty(PROP_SERVICES)
    public void remove( IService service )
            throws UnsupportedOperationException;

    @ModelProperty(PROP_SERVICES)
    public void replace( ID id, IService replacement )
            throws UnsupportedOperationException;
    
    public List<IResolve> find( @Optional URL resourceId, @Optional IProgressMonitor monitor );

    public URL getIdentifier();

    public ICatalogInfo getInfo( IProgressMonitor monitor )
            throws IOException;

    public String getTitle();

    @Optional
    public List<IResolve> getMembers( @Optional IProgressMonitor monitor )
            throws IOException;

    public <T> T resolve( Class<T> adaptee, @Optional IProgressMonitor monitor );
    
    
    /**
     * Transient fields and methods. 
     */
    public static abstract class Mixin
            implements CatalogComposite {
        
        private static Log log = LogFactory.getLog( Mixin.class );

        @This CatalogComposite          composite;
        
        /** The transient services. */
        private final Set<IService>     transientServices = new CopyOnWriteArraySet<IService>();
        
        /** Information about this catalog. */
        private CatalogInfoImpl         metadata;
        
        private CatalogImpl             facade;

        
        public Mixin() {
            metadata = new CatalogInfoImpl();
            metadata.setTitle( Messages.get( "CatalogComposite_localCatalog_title" ) ); 
            try {
                metadata.setSource( new URL( "http://localhost" ) ); //$NON-NLS-1$
            } 
            catch (MalformedURLException e) {
                throw new RuntimeException( e.getLocalizedMessage(), e );
            }
        }
        
        // internal methods *******************************
        
        protected void checkInit() {
        }
        
        protected void serialize() {
        }
        
        public ServiceComposite findServiceEntity( IService service ) {
            for (ServiceComposite candidate : services()) {
                if (candidate.getService() == service) {
                    return candidate;
                }
            }
            return null;
        }
        
        public void setFacade( CatalogImpl facade ) {
            this.facade = facade;
        }

        public Iterable<IService> persistentAndTransientServices() {
            // XXX better with an inner class but catalog code needs
            // to be refactored anyway
            List<IService> result = new ArrayList();

            for (ServiceComposite serviceComposite : services()) {
                result.add( serviceComposite.getService() );
            }
            
            for (IService service : transientServices) {
                result.add( service );
            }
            return result;
        }
        
        // ICatalog methods *******************************
        
        public URL getIdentifier() {
            return metadata.getSource();
        }

        public ICatalogInfo getInfo( IProgressMonitor monitor )
                throws IOException {
            return metadata;
        }

        public String getTitle() {
            return metadata.getTitle();
        }


        public List<IResolve> getMembers( IProgressMonitor monitor2 ) {
            IProgressMonitor monitor = monitor2 != null
                    ? monitor2
                    : new NullProgressMonitor();
            monitor.beginTask( Messages.get( "CatalogCompite_finding" ), 1 );
            
            // group services into folders for each service type
            List<ServiceTypeFolder> folders = new ArrayList<ServiceTypeFolder>();
            
            for (IService service : persistentAndTransientServices()) {
                boolean found = false;
                for (ServiceTypeFolder folder : folders) {
                    if (service != null && folder.type.isAssignableFrom( service.getClass() )) {
                        folder.services.add( service );
                        found  = true;
                        break;
                    }
                }
                if (!found && service != null) {
                    folders.add( new ServiceTypeFolder( facade, service ) );
                }
            }
            monitor.done();

            //return new LinkedList<IResolve>(services);
            List<IResolve> result = new ArrayList( folders );
//          for (ServiceTypeFolder folder : folders) {
//                result.add( folder );
//            }
            return result;
        }

        
        public <T extends IResolve> T getById(Class<T> type, final ID id, IProgressMonitor monitor) {
            IProgressMonitor monitor2 = (monitor != null) ? monitor : new NullProgressMonitor();
            if (id == null) {
                // _p3: otherwise loading just does nothing because
                throw new IllegalArgumentException( "id must not be null." );
                //return null;
            }
// falko: I don't grok those millions of get/findById/child/members whatever
//            if (IService.class.isAssignableFrom( type )) {
//                monitor2.beginTask( Messages.get( "CatalogComposite_monitorTask" ), 1 );
//                IService service = getServiceById( id );
//                monitor2.done();
//                return type.cast( service );
//            }

            URL url = id.toURL();
            if (IResolve.class.isAssignableFrom( type )) {
                for (IService service : persistentAndTransientServices()) {
                    if (service != null && URLUtils.urlEquals( url, service.getIdentifier(), true )) {
                        IResolve child = findChildById( service, id, false, monitor2 );
                        if (child != null) {
                            return type.cast( child );
                        }
                    }
                }
            }
            return null;
        }
        

        public void addTransient( ITransientResolve service )
                throws UnsupportedOperationException {
            transientServices.add( (IService)service );    
            log.debug( "Transient entries: " + transientServices.size() );
        }
        
        
        public void add( final IService entry )
                throws UnsupportedOperationException {
            // execute inside operation -> enable save and undo and concerns
            try {
                CatalogAddOperation op = new CatalogAddOperation( entry );
                op.execute( null, null );
                //OperationSupport.instance().execute( op, false, false );
            }
            catch (ExecutionException e) {
                throw new RuntimeException( e.getLocalizedMessage(), e );
            }
        }

        /**
         * see http://polymap.org/polymap3/ticket/121
         */
        public class CatalogAddOperation
                extends AbstractModelChangeOperation
                implements IAdaptable {

            private IService        entry;

            protected CatalogAddOperation( IService entry ) {
                super( Messages.get( "CatalogComposite_add", entry.getID() ) );
                this.entry = entry;
            }

            public Object getAdapter( Class adapter ) {
                if (IService.class.isAssignableFrom( adapter )) {
                    return entry;
                }
                return null;
            }

            protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws Exception {
                checkInit();
                if (entry == null || entry.getIdentifier() == null) {
                    throw new NullPointerException( "Cannot have a null id" ); //$NON-NLS-1$
                }
                if (getById( IService.class, entry.getID(), new NullProgressMonitor() ) != null) {
                    throw new IllegalArgumentException( Messages.get( "CatalogComposite_entryAlreadyExists", entry.getID() ) );
                }

                // ITransientResolve
                if (entry instanceof ITransientResolve || entry.canResolve( ITransientResolve.class )) {
                    transientServices.add( entry );
                    log.debug( "Transient entries: " + transientServices.size() );
                }
                // persistent entry
                else {
                    ServiceComposite entity = CatalogRepository.instance().newEntity( ServiceComposite.class, null );
                    for (Principal principal : Polymap.instance().getPrincipals()) {
                        entity.addPermission( principal.getName(), AclPermission.ALL );
                    }
                    entity.init( entry );
                    services().add( entity );

                    serialize();
                    log.debug( "Catalog size: " + services().count() );
                }
                return org.eclipse.core.runtime.Status.OK_STATUS;
            }
        }


        public void remove( IService entry )
                throws UnsupportedOperationException {
            if (entry == null || entry.getIdentifier() == null) {
                throw new NullPointerException( "Cannot have a null id" ); //$NON-NLS-1$
            }
            ServiceComposite entity = findServiceEntity( entry );
            if (entity == null) {
                throw new IllegalArgumentException( "No such service found: " + entry );
            }
            services().remove( entity );
            CatalogRepository.instance().removeEntity( entity );

            serialize();
            log.debug( "Catalog size:" + services().count() );
        }


        public void replace( ID id, IService replacement )
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException( "Should never been called. See CatalogImpl.");
            
//            if (replacement == null || replacement.getIdentifier() == null || id == null) {
//                throw new NullPointerException( "Cannot have a null id" ); //$NON-NLS-1$
//            }
//            
//            // find service
//            final IService service = null;
//            for (IService candidate : persistentAndTransientServices()) {
//                if (candidate.getID().equals( id )) {
//                    service = candidate;
//                    break;
//                }
//            }
//
//            List<IResolveDelta> changes = new ArrayList<IResolveDelta>();
//            List<IResolveDelta> childChanges = new ArrayList<IResolveDelta>();
//            try {
//                List<? extends IGeoResource> newChildren = replacement.resources( null );
//                List<? extends IGeoResource> oldChildren = service.resources( null );
//                if (oldChildren != null)
//                    for (IGeoResource oldChild : oldChildren) {
//                        String oldName = oldChild.getIdentifier().toString();
//
//                        for (IGeoResource child : newChildren) {
//                            String name = child.getIdentifier().toString();
//                            if (oldName.equals( name )) {
//                                childChanges.add( new ResolveDelta( child, oldChild, IResolveDelta.NO_CHILDREN ) );
//                                break;
//                            }
//                        }
//                    }
//            }
//            catch (IOException ignore) {
//                // no children? Not a very good entry ..
//            }
//            changes.add( new ResolveDelta( service, replacement, childChanges ) );
//
//            IResolveDelta deltas = new ResolveDelta( this, changes );
//            IResolveChangeEvent event = new ResolveChangeEvent( this,
//                    IResolveChangeEvent.Type.PRE_DELETE, deltas );
//            fire( event );
//
//            remove( service );
//            
//            PlatformJobs.run( new IRunnableWithProgress() {
//                public void run( IProgressMonitor monitor )
//                throws InvocationTargetException, InterruptedException {
//                    try {
//                        service.dispose( monitor );
//                    }
//                    catch (Throwable e) {
//                        log.error( "Error disposing of: " + service.getIdentifier(), e ); //$NON-NLS-1$
//                    }
//                }
//            });
//            
//            add( replacement );
//            event = new ResolveChangeEvent( this, IResolveChangeEvent.Type.POST_CHANGE, deltas );
//            
//            if (!id.equals( replacement.getIdentifier() )) {
//                throw new RuntimeException( "Service has moved: not supported yet." );
////                // the service has actually moved
////                IService moved = new MovedService( id, replacement.getID() );
////                services.add( moved );
//            }
//            fire( event );
        }

        
        public List<IResolve> find( ID id, IProgressMonitor monitor ) {
            return find( id.toURL(), monitor );        
        }
        
        
        /**
         * Quick search by url match.
         * @param query
         * 
         * @see net.refractions.udig.catalog.ICatalog#search(org.opengis.filter.Filter)
         * @return List<IResolve>
         * @throws IOException
         */
        public List<IResolve> find( URL query, IProgressMonitor monitor ) {        
            Set<IResolve> found = new LinkedHashSet<IResolve>();

            ID id = new ID( query );
            
            // first pass 1.1- use urlEquals on CONNECTED service for subset
            // check
            for (IService service : persistentAndTransientServices()) {
                log.debug( "service: " + service + " - " + service.getStatus() );
                if (service == null || service.getStatus() != Status.CONNECTED) {
                    continue; // skip non connected service
                }
                URL identifier = service.getIdentifier();
                if (URLUtils.urlEquals( query, identifier, true )) {
                    if (matchedService( query, identifier )) {
                        found.add( service );
                        found.addAll( friends( service ) );
                    }
                    else {
                        IResolve res = findChildById( service, id, true, monitor );
                        if (res != null) {
                            found.add( res );
                            found.addAll( friends( res ) );
                        }
                    }
                }
            }
            // first pass 1.2 - use urlEquals on unCONNECTED service for subset
            // check
            for (IService service : persistentAndTransientServices()) {
                if (service == null) {
                    continue;
                }
                if (service.getStatus() == Status.CONNECTED) {
                    continue; // already checked in pass 1.1
                }
                URL identifier = service.getIdentifier();
                if (URLUtils.urlEquals( query, identifier, true )) {
                    if (service.getStatus() != Status.NOTCONNECTED) {
                        continue; // look into not connected service that
                                  // "match"
                    }
                    if (matchedService( query, identifier )) {
                        found.add( service );
                        found.addAll( friends( service ) );
                    }
                    else {
                        IResolve res = findChildById( service, id, true, monitor );
                        if (res != null) {
                            found.add( res );
                            found.addAll( friends( res ) );
                        }
                    }
                }
            }
            // first pass 1.3 - use urlEquals on BROKEN or RESTRICTED_ACCESS service for subset check
            // the hope here is that a "friend" will still have data! May be tough for friends
            // to negotiate a match w/ a broken services - but there is still hope... 
            for (IService service : persistentAndTransientServices()) {
                if (service == null) {
                    continue;
                }
                if( service.getStatus() == Status.CONNECTED 
                        || service.getStatus() == Status.NOTCONNECTED) {
                    continue; // already checked in pass 1.1-1.2                                    
                }
                URL identifier = service.getIdentifier();
                if (URLUtils.urlEquals( query, identifier, true )) {
                    if (matchedService( query, identifier )) {
                        found.add( service );
                        found.addAll( friends( service ) );
                    }
                    else {
                        IResolve res = findChildById( service, id, true, monitor );
                        if (res != null) {
                            found.add( res );
                            found.addAll( friends( res ) );
                        }
                    }
                }
            }        
            return new ArrayList<IResolve>( found );
        }
        
        
        /**
         * Check if the provided query is a child of identifier.
         *
         * @param query
         * @param identifier
         * @return true if query may be a child of identifier
         */
        private boolean matchedService( URL query, URL identifier ) {
            return query.getRef() == null && URLUtils.urlEquals( query, identifier, false );
        }


        /**
         * Utility method that will search in the provided handle for an ID
         * match; especially good for nested content such as folders or WMS
         * layers. <h4>Old Comment</h4> The following comment was original
         * included in the source code: we are not sure it should be believed
         * ... we will do our best to search CONNECTED services first, nut
         * NOTCONNECTED is included in our search. <quote> Although the
         * following is a 'blocking' call, we have deemed it safe based on the
         * following reasons:
         * <ul>
         * <li>This will only be called for Identifiers which are well known.
         * <li>The Services being checked have already been screened, and only a
         * limited number of services (usually 1) will be called.
         * <ol>
         * <li>The Id was acquired from the catalog ... and this is a look-up,
         * in which case the uri exists.
         * <li>The Id was persisted.
         * </ol>
         * </ul>
         * In the future this will also be free, as we plan on caching the
         * equivalent of a getCapabilities document between runs (will have to
         * be updated too as the app has time). </quote> Repeat the following
         * comment is out of date since people are using this method to look for
         * entries that have not been added to the catalog yet.
         * 
         * @param roughMatch an ID consists of a URL and other info like a
         *        typeQualifier if roughMatch is true then the extra information
         *        is ignored during search
         */
        protected IResolve findChildById( IResolve handle, final ID id, boolean roughMatch,
                IProgressMonitor monitor ) {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }

            log.debug( "findChildById: '" + new ID( id.toURL() ) + "' / '" + new ID( handle.getIdentifier() ) + "'" );
            if (roughMatch) {
                log.info( "FIXME RecordStore is different between 3.1 and 3.2 (?)" );
                String lhs = new ID( id.toURL() ).toString();
                lhs = lhs.replaceFirst( "http:.*gml", "" );
                lhs = lhs.replace( "#:", "#" );
                String rhs = new ID( handle.getIdentifier() ).toString();
                log.info( "            -> '" + lhs + "' / '" + rhs + "'" );
                if (new ID( id.toURL() ).equals( new ID( handle.getIdentifier() ) )
                        || StringUtils.equalsIgnoreCase( lhs, rhs )) {
                    log.debug( "    match!" );
                    return handle;
                }
            }
            else {
                if (id.equals( handle.getID() )) {
                    return handle;
                }
            }
            try {
                List<? extends IResolve> children = handle.members( monitor );
                if (children == null || children.isEmpty()) {
                    return null;
                }
                monitor.beginTask( Messages.get( "CatalogComposite_monitorTask2" ), children.size() );
                for (IResolve child : children) {
                    IResolve found = findChildById( child, id, roughMatch, null );
                    if (found != null) {
                        return found;
                    }
                }
            }
            catch (IOException e) {
                log.error( "Could not search children of " + handle.getIdentifier(), e ); //$NON-NLS-1$
            }
            return null;
        }

        
        /**
         * Returns a list of friendly resources working with related data.
         * <p>
         * This method is used internally to determine resource handles that
         * offer different entry points to the same information.
         * </p>
         * A friend can be found via:
         * <ul>
         * <li>Making use of a CSW2.0 association
         * <li>URL Pattern matching for well known cases like GeoServer and MapServer
         * <li>Service Metadata, for example WMS resourceURL referencing a WFS SimpleFeatureType
         * </ul>
         * All of these handles will be returned from the find( URL, monitor ) method.
         * </ul>
         * @param handle
         * @return List of frends, possibly empty
         */
        public List<IResolve> friends( final IResolve handle ) {
            final List<IResolve> friends = new ArrayList<IResolve>();
            log.warn( "friend(): no friends are checked currently." );
//            ExtensionPointUtil.process( CatalogPlugin.getDefault(),
//                    "net.refractions.udig.catalog.friendly", 
//                    new ExtensionPointProcessor() {
//
//                /**
//                 * Lets find our friends.
//                 */
//                public void process( IExtension extension, IConfigurationElement element )
//                throws Exception {
//                    try {
//                        String target = element.getAttribute( "target" ); //$NON-NLS-1$
//                        String contain = element.getAttribute( "contain" ); //$NON-NLS-1$
//                        if (target != null) {
//                            // perform target check
//                            if (target.equals( target.getClass().toString() )) {
//                                return;
//                            }
//                        }
//                        if (contain != null) {
//                            String uri = handle.getIdentifier().toExternalForm();
//                            if (!uri.contains( contain )) {
//                                return;
//                            }
//                        }
//
//                        IFriend friendly = (IFriend)element.createExecutableExtension( "class" ); //$NON-NLS-1$
//                        friends.addAll( friendly.friendly( handle, null ) );
//                    }
//                    catch (Throwable t) {
//                        log.warn( t.getLocalizedMessage(), t );
//                    }
//                }
//            });
            return friends;
        }
        
        
        public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) {
            monitor = monitor != null ? monitor : new NullProgressMonitor();
            try {
                if (adaptee == null) {
                    return null;
                }
                monitor.beginTask( Messages.get( "CatalogComposite_resolving" )
                        + adaptee.getSimpleName(), 2 );
                monitor.worked( 1 );
                
                if (adaptee.isAssignableFrom( CatalogImpl.class )) {
                    return adaptee.cast( this );
                }
                if (adaptee.isAssignableFrom( CatalogInfoImpl.class )) {
                    return adaptee.cast( metadata );
                }
                if (adaptee.isAssignableFrom( Set.class )) {
                    throw new RuntimeException( "Resolve is not supported:" + adaptee.getName() );
                    //return adaptee.cast( services );
                }
                if (adaptee.isAssignableFrom( List.class )) {
                    throw new RuntimeException( "Resolve is not supported:" + adaptee.getName() );
                    //return adaptee.cast( new LinkedList<IService>( services ) );
                }
//                if (adaptee.isAssignableFrom( catalogListeners.getClass() )) {
//                    return adaptee.cast( getListenersCopy() );
//                }
            }
            finally{
                monitor.worked(1);
                monitor.done();
            }
            return null;
        }

    }

}
