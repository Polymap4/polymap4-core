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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.CatalogPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.CatalogImportDropListener;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.core.workbench.dnd.DesktopDndSupport;

/**
 * Factory and repository for the domain model artifacts.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CatalogRepository
        extends QiModule
        implements org.polymap.core.model.Module {

    private static Log log = LogFactory.getLog( CatalogRepository.class );


    /**
     * Get or create the repository for the current user session.
     */
    public static final CatalogRepository instance() {
        return Qi4jPlugin.Session.instance().module( CatalogRepository.class );
    }

//    static ListenerList<CatalogStartupListener> startupListeners = new ListenerList();
//    
//    public static void addCatalogStartupListener( CatalogStartupListener l ) {
//        startupListeners.add( l );    
//    }
//
//    public interface CatalogStartupListener {
//        void catalogStarted( ICatalog catalog );
//    }
    
    
    // instance *******************************************

    private CatalogComposite        catalog;
    
    private OperationSaveListener   operationListener;
    

    protected CatalogRepository( QiModuleAssembler assembler ) {
        super( assembler );
        
        operationListener = new OperationSaveListener();
        OperationSupport.instance().addOperationSaveListener( operationListener );

        catalog = uow.get( CatalogComposite.class, "catalog" );
        
        DesktopDndSupport.instance().addDropListener( new CatalogImportDropListener() );
    }
    
    
    protected void done() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
    }

    
    public CatalogComposite getCatalog() {
        return catalog;
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

        public void prepareSave( OperationSupport os, IProgressMonitor monitor )
        throws Exception {
            //
        }

        public void save( OperationSupport os, IProgressMonitor monitor ) {
            try {
                commitChanges();
                
                // trigger the reload of the global catalog; 
                // the udig CatalogPlugin is not aware of the domain model and
                // the model change listeners
                CatalogPlugin.getDefault().storeToPreferences( null );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( CatalogPlugin.ID, this, 
                        "Die Änderungen konnten nicht gespeichert werden.\nDie Daten sind möglicherweise in einem inkonsistenten Zustand.\nBitte verständigen Sie den Administrator.", e );
            }
        }
        
        public void rollback( OperationSupport os, IProgressMonitor monitor ) {
        }

        public void revert( OperationSupport os, IProgressMonitor monitor ) {
            log.debug( "..." );
            revertChanges();
        }

    }
    
}
