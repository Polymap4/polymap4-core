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
package org.polymap.core.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import static net.refractions.udig.core.internal.CorePlugin.createSafeURL;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.IPath;
import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.Polymap;
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


    public String onDrop( List<DesktopDropEvent> events ) {
        try {
            IServiceFactory factory = CatalogPlugin.getDefault().getServiceFactory();

            List<File> files = new ArrayList();
            for (DesktopDropEvent ev : events) {
                // file -> copy into workspace
                if (ev instanceof FileDropEvent) {
                    FileDropEvent fev = (FileDropEvent)ev;
                    if (FilenameUtils.wildcardMatch( fev.getFileName(), "*.zip", IOCase.INSENSITIVE )
                            || fev.getContentType().equalsIgnoreCase( "application/zip" )) {
                        files.addAll( importZip( fev ) );
                    }
                    else {
                        files.addAll( importFile( fev.getFileName(), fev.getInputStream(), true ) );
                    }
                }
                // text -> find and add service
                else if (ev instanceof TextDropEvent) {
                    URL url = createSafeURL( ((TextDropEvent)ev).getText() );
                    List<IService> services = factory.createService( url );
                    
                    for (IService service : services) {
                        if (services.size() == 1
                                || MessageDialog.openQuestion(
                                        PolymapWorkbench.getShellToParentOn(),
                                        Messages.get( "CatalogImportDropListener_toMuchServices_title" ),
                                        Messages.get( "CatalogImportDropListener_toMuchServices_msg", url.toExternalForm(), service ) )) {
                            CatalogPlugin.getDefault().getLocalCatalog().add( service );
                            
                            MessageDialog.openInformation(
                                    PolymapWorkbench.getShellToParentOn(),
                                    Messages.get( "CatalogImportDropListener_success_title" ),
                                    Messages.get( "CatalogImportDropListener_success_msg", service ) );
                        }
                    }
                }
            }
            
            // create services
            List<String> added = new ArrayList();
            for (File f : files) {
                // I still don't grok the magic of the udig catalog; shapefile
                // service handles *.shp, as well as *.dbf, *.shx -> subsequent
                // add to the catalog results in exception !?
                if (added.contains( StringUtils.substringBeforeLast( f.getName(), "." ) )) {
                    continue;
                }
                URL url = f.toURI().toURL();
                List<IService> services = factory.createService( url );
                for (IService service : services) {
                    CatalogPlugin.getDefault().getLocalCatalog().add( service );
                    
                    added.add( StringUtils.substringBeforeLast( f.getName(), "." ) );

                    MessageDialog.openInformation(
                            PolymapWorkbench.getShellToParentOn(),
                            Messages.get( "CatalogImportDropListener_success_title" ),
                            Messages.get( "CatalogImportDropListener_success_msg", service ) );
                }
            }
            return null;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( CorePlugin.PLUGIN_ID, this,
                    Messages.get( "CatalogImportDropListener_error" ), e );
            return null;
        }
    }

    
    protected List<File> importFile( String filename, InputStream in, boolean close )
    throws IOException {
        List<File> result = new ArrayList();
        
        IPath workspace = Polymap.getWorkspacePath();
        File f = new File( workspace.toFile(), filename );

        boolean ok = true;
        if (f.exists()) {
            ok = MessageDialog.openConfirm(
                    PolymapWorkbench.getShellToParentOn(),
                    Messages.get( "CatalogImportDropListener_fileExists_title", filename ),
                    Messages.get( "CatalogImportDropListener_fileExists_msg", filename ) );
        }
        if (ok) {
            OutputStream fout = null;
            try {
                fout = new BufferedOutputStream( new FileOutputStream( f ) );                
                IOUtils.copy( in, fout );
            }
            finally {
                IOUtils.closeQuietly( fout );
                if (close) {
                    IOUtils.closeQuietly( in );
                }
            }
            result.add( f );
        }
        return result;
    }

    
    protected List<File> importZip( FileDropEvent ev ) 
    throws IOException {
        List<File> result = new ArrayList();
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream( ev.getInputStream() );
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                result.addAll( importFile( entry.getName(), zip, false ) );
            }
        }
        finally {
            IOUtils.closeQuietly( zip );
        }
        return result;
    }
    
}
