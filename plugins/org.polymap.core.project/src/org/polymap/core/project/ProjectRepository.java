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

package org.polymap.core.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.model.MapState;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.ISessionContextProvider;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Factory and repository for the domain model artifacts.
 * <p/>
 * XXX This depends on the Qi4j model implementation; extract the interface and move
 * impl to {@link org.polymap.core.project.model}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class ProjectRepository
        extends QiModule
        implements org.polymap.core.model.Module {

    private static final Log log = LogFactory.getLog( ProjectRepository.class );


    /**
     * Get or create the repository for the current user session.
     */
    public static final ProjectRepository instance() {
        return (ProjectRepository)Qi4jPlugin.Session.instance().module( ProjectRepository.class );
    }


    /**
     * The global instance used outside any user session.
     * 
     * @deprecated The same as {@link #instance()}. Session contexts a provided by an
     *             {@link ISessionContextProvider}.
     * @return A newly created {@link Session} instance. It is up to the caller to
     *         store and re-use if necessary.
     */
    public static final ProjectRepository globalInstance() {
        return instance();
    }
    

    // instance *******************************************

    private IMap                    rootMap;
    
    private OperationSaveListener   operationListener;
    

    protected ProjectRepository( QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "uow: " + uow.isOpen() );
        
        operationListener = new OperationSaveListener();
        OperationSupport.instance().addOperationSaveListener( operationListener );
    }
    
    
    protected void done() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
    }


    public IMap getRootMap() {
        if (rootMap == null) {
            try {
                rootMap = uow.get( IMap.class, "root" );
                
                log.debug( "ProjectRepository: rootMap: " + rootMap.getLabel() );
                AssocCollection<IMap> col = rootMap.getMaps();
                for (IMap child : col) {
                    log.debug( "   child: " + ((MapState)child).maps().count() );
//                    AssocCollection<IMap> childcol = child.getMaps();
//                    for (IMap child2 : childcol) {
//                        System.out.println( "   child2: " + child2.getLabel() );
//                    }
                }
                if (rootMap == null) {
                    throw new NoSuchEntityException( null );
                }
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this
                        , e.getMessage(), e );
            }
        }
        return rootMap;
    }

    
    public <T> T visitProjects( ProjectVisitor<T> visitor ) {
        for (IMap map : rootMap.getMaps()) {
            for (ILayer layer : map.getLayers()) {
                visitor.visit( layer );
            }
        }
        return visitor.result;
    }
    
    
    /**
     * 
     */
    public abstract static class ProjectVisitor<T> {

        protected T         result;
        
        public abstract void visit( ILayer layer );
        
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
    
    
}
