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

import java.io.File;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.security.AclPermission;
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
public class CatalogRepositoryAssembler
        extends QiModuleAssembler {

    private static Log log = LogFactory.getLog( CatalogRepositoryAssembler.class );

    private Application                 app;
    
    private UnitOfWorkFactory           uowf;
    
    private Module                      module;
    
    
    public QiModule newModule() {
        return new CatalogRepository( this );
    }


    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "adhoc-layer", "catalog-module" );
        this.uowf = module.unitOfWorkFactory();
    }


    public Module getModule() {
        return module;
    }


    public void assemble( ApplicationAssembly _app )
    throws Exception {
        log.info( "Assembling: org.polymap.core.catalog ..." );
        
        LayerAssembly domainLayer = _app.layerAssembly( "adhoc-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( "catalog-module" );
        domainModule.addEntities( 
                ServiceComposite.class,
                CatalogComposite.class
        );
//        domainModule.addTransients( 
//                NewServiceOperation.class,
//                RemoveServiceOperation.class
//        );

        // persistence: workspace/JSON
        File root = new File( Polymap.getWorkspacePath().toFile(), "data" );
        root.mkdir();
        
        File moduleRoot = new File( root, "org.polymap.core.catalog" );
        moduleRoot.mkdir();

        domainModule.addServices( JsonEntityStoreService.class )
                .setMetaInfo( new JsonEntityStoreInfo( moduleRoot ) )
                .instantiateOnStartup();
        
        domainModule.addServices( HRIdentityGeneratorService.class );
    }                

    
    public void createInitData() 
    throws Exception {
        
        // check catalog
        UnitOfWork start_uow = uowf.newUnitOfWork();
        try {
            CatalogComposite catalog = start_uow.get( CatalogComposite.class, "catalog" );
            System.out.println( "Catalog: " + catalog );
            if (catalog == null) {
                throw new NoSuchEntityException( null );
            }
        }
        // init catalog
        catch (Throwable e) {
            try {
                log.info( "No config or error, creating catalog composite. (" + e + ")" );
//                EntityBuilder<IMap> builder = start_uow.newEntityBuilder( IMap.class, "root" );
//                //builder.instance().setLabel( "root" );
//                IMap rootMap = builder.newInstance();
//                rootMap.setLabel( "root" );

                CatalogComposite catalog = start_uow.newEntity( CatalogComposite.class, "catalog" );
                catalog.addPermission( Authentication.ALL.getName(), AclPermission.ALL );
                System.out.println( "    created: " + catalog );
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
