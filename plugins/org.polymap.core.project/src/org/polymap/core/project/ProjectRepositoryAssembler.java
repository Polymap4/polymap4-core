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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import org.polymap.core.model.security.AclPermission;
import org.polymap.core.project.model.LayerComposite;
import org.polymap.core.project.model.MapComposite;
import org.polymap.core.project.model.MapState;
import org.polymap.core.project.operations.NewLayerOperation;
import org.polymap.core.project.operations.NewMapOperation;
import org.polymap.core.project.operations.RemoveLayerOperation;
import org.polymap.core.project.operations.RemoveMapOperation;
import org.polymap.core.project.operations.SetProcessorConfigurationsOperation;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.entitystore.json.JsonEntityStoreInfo;
import org.polymap.core.qi4j.entitystore.json.JsonEntityStoreService;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.Authentication;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ProjectRepositoryAssembler
        extends QiModuleAssembler {

    private static Log log = LogFactory.getLog( ProjectRepositoryAssembler.class );

    private Application                 app;
    
    private UnitOfWorkFactory           uowf;
    
    private Module                      module;
    
    
    public QiModule newModule() {
        return new ProjectRepository( this );
    }


    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "adhoc-layer", "project-module" );
        this.uowf = module.unitOfWorkFactory();
    }


    public Module getModule() {
        return module;
    }


    public void assemble( ApplicationAssembly _app )
    throws Exception {
        log.info( "Assembling: org.polymap.core.project..." );
        
        // project layer / module
        LayerAssembly domainLayer = _app.layerAssembly( "adhoc-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( "project-module" );
        domainModule.addEntities( 
                MapComposite.class,
                LayerComposite.class 
        );
        domainModule.addTransients( 
                NewMapOperation.class, 
                RemoveMapOperation.class, 
                NewLayerOperation.class, 
                SetPropertyOperation.class,
                SetProcessorConfigurationsOperation.class,
                RemoveLayerOperation.class
        );
        //                domainModule.addServices( FactoryService.class )
        //                        .visibleIn( Visibility.application );

        //              domainModule.addServices( MemoryEntityStoreService.class )
        //              .instantiateOnStartup();

//        // persistence: prefs
//        Preferences prefRoot = Preferences.userRoot().node( "org/polymap/core/project" );
//        prefRoot.put( "test", "hello" );
//        domainModule.addServices( PreferencesEntityStoreService.class )
//                .setMetaInfo( new PreferencesEntityStoreInfo( prefRoot ) )
//                .instantiateOnStartup()
//                ;  //.identifiedBy( "rdf-repository" );

        // persistence: workspace/JSON
        File root = new File( Polymap.getWorkspacePath().toFile(), "data" );
        root.mkdir();
        
        File moduleRoot = new File( root, "org.polymap.core.project" );
        moduleRoot.mkdir();

        domainModule.addServices( JsonEntityStoreService.class )
                .setMetaInfo( new JsonEntityStoreInfo( moduleRoot ) )
                .instantiateOnStartup()
                ;  //.identifiedBy( "rdf-repository" );

        domainModule.addServices( HRIdentityGeneratorService.class );

        // indexer
//        RdfNativeSesameStoreAssembler rdf = new RdfNativeSesameStoreAssembler();
//        rdf.assemble( domainModule );
    }                

    
    public void createInitData() 
    throws Exception {
        
        // check/init rootMap
        UnitOfWork start_uow = uowf.newUnitOfWork();
        try {
            IMap rootMap = start_uow.get( IMap.class, "root" );
            System.out.println( "rootMap: " + rootMap.getLabel() );
            System.out.println( "rootMap: " + ((MapState)rootMap).maps().iterator().next() );
            for (IMap child : rootMap.getMaps()) {
                System.out.println( "   child: " + child.toString() );
            }
            if (rootMap == null) {
                throw new NoSuchEntityException( null );
            }
        }
        catch (Throwable e) {
            try {
                log.info( "No config or error, creating global config. (" + e + ")" );
                EntityBuilder<IMap> builder = start_uow.newEntityBuilder( IMap.class, "root" );
                //builder.instance().setLabel( "root" );
                IMap rootMap = builder.newInstance();
                rootMap.addPermission( Authentication.ALL.getName(), AclPermission.READ, AclPermission.WRITE );
                rootMap.setLabel( "root" );

                IMap map = start_uow.newEntity( IMap.class, "First Map" );
                map.addPermission( Authentication.ALL.getName(), AclPermission.ALL );
                map.setLabel( "First Map" );
                rootMap.addMap( map );

                System.out.println( "rootMap: " + ((MapState)rootMap).maps().iterator().next() );
            }
            catch (Exception e1) {
                log.error( e1.getMessage(), e1 );
                throw e1;
            }
        }
        finally {
            start_uow.complete();
            start_uow = null;
        }
    }
    
}
