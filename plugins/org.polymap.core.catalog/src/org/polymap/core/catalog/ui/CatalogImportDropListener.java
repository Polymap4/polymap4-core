/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.catalog.ui;

import static net.refractions.udig.core.internal.CorePlugin.createSafeURL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.File;
import java.net.URL;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import net.refractions.udig.catalog.IServiceInfo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.CorePlugin;
import org.polymap.core.catalog.Messages;
import org.polymap.core.catalog.operations.AddServiceOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.core.workbench.dnd.DesktopDropEvent;
import org.polymap.core.workbench.dnd.DesktopDropListener;
import org.polymap.core.workbench.dnd.FileDropEvent;
import org.polymap.core.workbench.dnd.TextDropEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogImportDropListener
        implements DesktopDropListener {

    private static Log log = LogFactory.getLog( CatalogImportDropListener.class );

    private static final IMessages      i18n = Messages.forPrefix( "CatalogImportDropListener" );
    

    public String onDrop( List<DesktopDropEvent> events ) {
        try {
            IServiceFactory factory = CatalogPlugin.getDefault().getServiceFactory();

            List<File> files = new ArrayList();
            for (DesktopDropEvent ev : events) {
                // file -> copy into workspace
                if (ev instanceof FileDropEvent) {
                    FileDropEvent fev = (FileDropEvent)ev;
                    files.addAll( FileImporter.run( fev.getFileName(), fev.getContentType(), fev.getInputStream() ) );
                }
                // text -> find and add service
                else if (ev instanceof TextDropEvent) {
                    URL url = createSafeURL( ((TextDropEvent)ev).getText() );
                    List<IService> services = factory.createService( url );
                    
                    for (IService service : services) {
                        if (services.size() == 1
                                || MessageDialog.openQuestion(
                                        PolymapWorkbench.getShellToParentOn(),
                                        i18n.get( "toMuchServices_title" ),
                                        i18n.get( "toMuchServices_msg", url.toExternalForm(), service ) ) ) {
                            
                            CatalogPlugin.getDefault().getLocalCatalog().add( service );
                            new MessageJob( service ).schedule();
                        }
                    }
                }
            }
            
            // create services
            Set<String> added = new HashSet();
            for (File f : files) {
                // I still don't grok the magic of the udig catalog; shapefile
                // service handles *.shp, as well as *.dbf, *.shx -> subsequent
                // add to the catalog results in exception !?
                if (added.contains( FilenameUtils.getBaseName( f.getName() ) )) {
                    log.warn( "Skipping already imported: " + f.getName() );
                    continue;
                }
                URL url = f.toURI().toURL();
                List<IService> services = factory.createService( url );
                for (IService service : services) {
                    added.add( FilenameUtils.getBaseName( f.getName() ) );
                    ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
                    AddServiceOperation op = new AddServiceOperation( catalog, service );
                    OperationSupport.instance().execute( op, false, false );

                    new MessageJob( service ).schedule();
                }
            }
            return null;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, this, i18n.get( "error" ), e );
            return null;
        }
    }

    
    /**
     * 
     */
    class MessageJob
            extends UIJob {

        private IService        service;
        
        public MessageJob( IService service ) {
            super( i18n.get( "jobTitle" ) );
            this.service = service;
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            IServiceInfo info = service.getInfo( monitor );
            final String title = info.getTitle();
            
            getDisplay().asyncExec( new Runnable() {
                public void run() {
                    MessageDialog.openInformation(
                            PolymapWorkbench.getShellToParentOn(),
                            i18n.get( "success_title" ), i18n.get( "success_msg", title ) );
                }
            });
        }
        
    }
    
}
