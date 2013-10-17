/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All righrs reserved.
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
 */
package org.polymap.core.project;

import java.net.URL;

import net.refractions.udig.catalog.memory.ActiveMemoryDataStore;
import net.refractions.udig.catalog.memory.internal.MemoryGeoResourceImpl;
import net.refractions.udig.catalog.memory.internal.MemoryServiceImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.model.MapState;
import org.polymap.core.project.model.TempLayerComposite;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
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
    public static ProjectRepository instance() {
        return Qi4jPlugin.Session.instance().module( ProjectRepository.class );
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
    
    
    protected void dispose() {
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

    
    public <T> T visit( LayerVisitor<T> visitor ) {
        for (IMap map : getRootMap().getMaps()) {
            for (ILayer layer : map.getLayers()) {
                if (!visitor.visit( layer )) {
                    return visitor.result;
                }
            }
        }
        return visitor.result;
    }
    
    
    @SuppressWarnings("restriction")
    public TempLayerComposite newTempLayer( String name, IMap parent ) {
        assert parent != null;
        
        try {
            TransientBuilderFactory factory = assembler.getModule().transientBuilderFactory();
            TempLayerComposite result = factory.newTransient( TempLayerComposite.class );
            result.setLabel( "Graph" );
            result.setOrderKey( 100 );
            result.setOpacity( 100 );
            
            parent.addLayer( result );

            MemoryServiceImpl service = new MemoryServiceImpl( new URL( "http://polymap.org/" + name ) );
            ActiveMemoryDataStore ds = service.resolve( ActiveMemoryDataStore.class, null );
            
            String typeName = name;
            MemoryGeoResourceImpl geores = new MemoryGeoResourceImpl( typeName, service );
            result.setGeoResource( geores );

            return result;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
}
