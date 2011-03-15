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

package org.polymap.core.qi4j.sample;

import java.util.prefs.Preferences;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreInfo;
import org.qi4j.entitystore.prefs.PreferencesEntityStoreService;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import org.polymap.core.qi4j.NestedChangeSet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class HelloMain {

    static {
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.qi4j", "debug" );
    }

    
    public static void main( String args[] )
            throws Exception {

        Energy4Java qi4j = new Energy4Java();
        ApplicationSPI application = qi4j.newApplication( new ApplicationAssembler() {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) 
                    throws AssemblyException {
                
                ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                
                // domain layer / module
                LayerAssembly domainLayer = app.layerAssembly( "domain-layer" );
                ModuleAssembly domainModule = domainLayer.moduleAssembly( "hello-module" );
                domainModule.addEntities( PersonComposite.class );
                domainModule.addTransients( HelloWorldComposite.class );
                domainModule.addServices( FactoryService.class )
                        .visibleIn( Visibility.application );
                
//              domainModule.addServices( MemoryEntityStoreService.class )
//              .instantiateOnStartup();

                Preferences prefRoot = Preferences.userRoot().node(
                        domainModule.layerAssembly().applicationAssembly().name() + "/" + "hello" );
                prefRoot.put( "test", "hello" );

                // indexer
//                RdfMemoryStoreAssembler rdf = new RdfMemoryStoreAssembler();
//                RdfNativeSesameStoreAssembler rdf = new RdfNativeSesameStoreAssembler();
//                rdf.assemble( domainModule );
                
//                domainModule.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class );
//                domainModule.addServices( NativeRepositoryService.class )
//                        .identifiedBy( "rdf-repository" )
//                        .instantiateOnStartup();
//                domainModule.addServices( RdfFactoryService.class )
//                        .visibleIn( Visibility.application )
//                        .instantiateOnStartup();
//                domainModule.addServices( RdfQueryService.class )
//                        .visibleIn( Visibility.application )
//                        .instantiateOnStartup();
                
                // persistence
                domainModule.addServices( PreferencesEntityStoreService.class )
                        .setMetaInfo( new PreferencesEntityStoreInfo( prefRoot ) )
                        .instantiateOnStartup()
                        ;  //.identifiedBy( "rdf-repository" );
                
//                // auth layer / module
//                LayerAssembly authLayer = assembly.layerAssembly( "auth2-layer" );
//                ModuleAssembly authModule = authLayer.moduleAssembly( "auth-module" );
//                authModule.addAssembler( new LdapAuthenticationAssembler() );
//                    assembly.addAssembler( new ThrinkAuthorizationAssembler() );
//                    assembly.addAssembler( new UserTrackingAuditAssembler() );
                    
                // infrastructur layer
                LayerAssembly infraLayer = app.layerAssembly( "infrastructure-layer" );
                // persistence module
                ModuleAssembly persModule = infraLayer.moduleAssembly( "persistence-module" );


                persModule.addServices( PreferencesEntityStoreService.class, UuidIdentityGeneratorService.class );

                
//                persModule.addServices( JdbmEntityStoreService.class, UuidIdentityGeneratorService.class );
//                ModuleAssembly config = persModule.layerAssembly().moduleAssembly( "persistence-module" );
//                config.addEntities( JdbmConfiguration.class ).visibleIn( Visibility.layer );
//                config.addServices( MemoryEntityStoreService.class );
                
                
//                // Indexing
//                persModule.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class);
//                persModule.addServices( NativeRepositoryService.class )
//                        .identifiedBy( "rdf-repository" ).instantiateOnStartup();
//                
//                persModule.addServices( RdfFactoryService.class)
//                        .visibleIn( Visibility.application )
//                        .instantiateOnStartup();
//
//                persModule.addServices( RdfQueryService.class)
//                        .visibleIn( Visibility.application )
//                        .instantiateOnStartup();

//                // Entity store
//                persModule.addServices( JdbmEntityStoreService.class, UuidIdentityGeneratorService.class)
//                        .visibleIn( Visibility.application )
//                        .instantiateOnStartup();
//
//                // Config
//                ModuleAssembly config = persModule.layerAssembly().moduleAssembly( "Config" );
//                config.addEntities( JdbmConfiguration.class )
//                        .visibleIn( Visibility.layer );
//                
//                Preferences jdbmPreferences = Preferences.userRoot()
//                        .node( persModule.layerAssembly().applicationAssembly().name() + "/" + "Jdbm");
//                jdbmPreferences.put( "file", "test.db" );
//                
//                config.addEntities( NativeConfiguration.class)
//                        .visibleIn( Visibility.application );
//                
//                config.addServices( PreferencesEntityStoreService.class)
//                        .setMetaInfo( new PreferencesEntityStoreInfo( jdbmPreferences ) )
//                        .instantiateOnStartup();
//
//                config.addServices( UuidIdentityGeneratorService.class );

                domainLayer.uses( infraLayer );
                return app;
            }
        } );
        // activate the application
        application.activate();
        
//        Assembler assembler = new SingletonAssembler() {
//            public void assemble( ModuleAssembly assembly )
//                    throws AssemblyException {
//                assembly.addTransients( HelloWorldComposite.class );
//                
//            }
//        };

        //assembly.addAssembler( new RdfNativeSesameStoreAssembler() )
        

        TransientBuilderFactory factory = application.findModule( "domain-layer", "hello-module" )
                .transientBuilderFactory();
        HelloWorld hello = factory.newTransient( HelloWorldComposite.class );
        
        hello.label().set( "label" );
        System.out.println( "Hello: " + hello.getLabel() );
        System.out.println( "says: " + hello.say() );
        
        // entities ****

        Module domainModule = application.findModule( "domain-layer", "hello-module" );
        final UnitOfWork uow = domainModule.unitOfWorkFactory().newUnitOfWork();
        System.out.println( "uow: " + uow );
        //uow.complete();
        
        // cs1
        NestedChangeSet cs1 = NestedChangeSet.newInstance( uow );
        
        ServiceReference<Factory> fs = domainModule.serviceFinder().findService( Factory.class );
        //Person root = fs.get().createPerson( "root", "root" );
        final Person root = uow.get( Person.class, "root" );
        System.out.println( "root: " + root.toString() );
        root.extend().set( new ReferencedEnvelope() );
        printPerson( root );
        
//        Thread t = new Thread() {
//            public void run() {
//                System.out.println( "from thread" );
//                System.out.println( "uow: " + uow );
//                printPerson( root );
//            }
//        };
//        t.start();
//        t.join();
        
//        //cs2
//        NestedChangeSet cs2 = NestedChangeSet.newInstance( uow );
//        root.setLabel( "to-be-discarded" );
//        Person child = fs.get().createPerson( "child1", "child1" );
//        root.addChild( child );
//        printPerson( root );
//
//        System.out.println( "### discarding..." );
//        cs2.discard();
//        printPerson( root );
        
//        Person child = fs.get().createPerson( "child1", "child1" );
//        root.addChild( child );
//        printPerson( root );
//        
//        System.out.println( "discarding uow..." );
//        uow2.discard();
//        printPerson( root );
//
//        System.out.println( "discarding uow..." );
//        uow.discard();
//        printPerson( root );

        //Person root = fs.get().findPersonByLabel( "child2" );
        //Person root = fs.get().findPersonById( "child2" );
        
        //Person root = uow.get( Person.class, "1" );
        //Person child = uow.get( Person.class, "2" );
        //root.addChild( p );
        
        //root.extend().set( new ReferencedEnvelope() );
        
        //printPerson( root );
        uow.complete();
    }

    private static void printPerson( Person person ) {
        System.out.println( "*** Person: " + person.getLabel() );
        for (Person child : person.allChildren()) {
            System.out.println( "   child: "
                    + "parent=" + ((Identity)child.getParent()).identity().get()
                    + ", extent=" + child.extend() 
                    + ", label=" + child.getLabel() );
        }
    }

}
