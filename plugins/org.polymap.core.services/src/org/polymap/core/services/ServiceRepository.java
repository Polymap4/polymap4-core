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
package org.polymap.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.internal.service.ContextProvider;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.services.qi4j.ServiceListComposite;

/**
 * Factory and repository for the domain model artifacts.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class ServiceRepository
        extends QiModule
        implements org.polymap.core.model.Module {

    private static Log log = LogFactory.getLog( ServiceRepository.class );


    /**
     * Get or create the repository for the current user session.
     */
    public static final ServiceRepository instance() {
        return (ServiceRepository)Qi4jPlugin.Session.instance().module( ServiceRepository.class );
    }


    /**
     * The global instance used outside any user session.
     * 
     * @return A newly created {@link Session} instance. It is up to the caller
     *         to store and re-use if necessary.
     */
    public static final ServiceRepository globalInstance() {
        return (ServiceRepository)Qi4jPlugin.Session.globalInstance().module( ServiceRepository.class );
    }
    

    // instance *******************************************

    private ServiceListComposite    serviceList;
    
    private OperationSaveListener   operationListener = new OperationSaveListener();
    

    protected ServiceRepository( QiModuleAssembler assembler ) {
        super( assembler );
        
        // for the global instance of the module (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (ContextProvider.hasContext()) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }

        serviceList = uow.get( ServiceListComposite.class, "serviceList" );
        log.debug( "ServiceList: " + serviceList );
    }
    
    
    protected void done() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
    }

    
    public IProvidedService findService( IMap map, Class cl ) {
        List<IProvidedService> services = findServices( map, cl );
        if (services.size() > 1) {
            throw new IllegalStateException( "" );
        }
        else if (services.isEmpty()) {
            return null;
        }
        else {
            return services.get( 0 );
        }
    }

    
    public List<IProvidedService> findServices( IMap map, Class cl ) {
//        try {
            List<IProvidedService> result = new ArrayList();
            for (IProvidedService service : serviceList.getServices()) {
                try {
                    System.out.println( "   service: " + service );
                    if (service.getMapId().equals( map.id() )
                            && service.getServiceType().equals( cl )) {
                        result.add( service );
                    }
                }
                catch (NoSuchEntityException e) {
                    // the IMap of the service is no longer found
                    log.info( "Map is no longer found for service: " + service.getPathSpec() );
                    // FIXME delete this entity on IMap delete
                }
            }
            return result;
//        }
//        catch (Exception e) {
//            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, e.getMessage(), e );
//        }
    }
    
    
    public void addService( IProvidedService service ) { 
        serviceList.addService( service );
    }
    
    
    public void removeService( IProvidedService service ) { 
        serviceList.removeService( service );
    }
    
    
    public Collection<IProvidedService> allServices() {
        AssocCollection<IProvidedService> services = serviceList.getServices();
        List<IProvidedService> result = new ArrayList();
        for (IProvidedService service : services) {
            // check if the IMap is still there
            try {
                service.getMap();
                result.add( service );
            }
            catch (NoSuchEntityException e) {
                // the IMap of the service is no longer found
                log.info( "Map is no longer found for service: " + service.getPathSpec() );
                // FIXME delete this entity on IMap delete
            }
        }
        return result;
    }


    public <T> T newOperation( Class<T> type ) {
        T result = assembler.getModule().transientBuilderFactory().newTransient( type );
        return result;
    }

//    public void fireModelChangedEvent( Object source, String propName, Object oldValue, Object newValue) {
//        PropertyChangeEvent event = new PropertyChangeEvent( source, propName, oldValue, newValue ); 
//        for (Object l : propChangeListeners.getListeners()) {
//            ((PropertyChangeListener)l).propertyChange( event );
//        }
//    }
    
    
    
    /**
     * 
     *
     */
    class OperationSaveListener
    implements IOperationSaveListener {

        public void prepareSave( OperationSupport os )
        throws Exception {
            //
        }

        public void save( OperationSupport os )
        throws Exception {
            log.debug( "..." );
            commitChanges();
        }
        
        public void revert( OperationSupport os ) {
            log.debug( "..." );
            discardChanges();
        }

    }
    
}
