/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.catalog.h2.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.io.Serializable;
import net.refractions.udig.catalog.CatalogPluginSession;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.catalog.h2.H2ServiceExtension;
import org.polymap.catalog.h2.H2ServiceImpl;
import org.polymap.catalog.h2.data.H2DataStoreFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CreateH2Action
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( CreateH2Action.class );


    public void init( IViewPart view ) {
    }


    public void run( IAction action ) {
        try {
            InputDialog dialog = new InputDialog( PolymapWorkbench.getShellToParentOn(),
                    "Eine neue, persönliche H2-Datenbank anlegen",
                    "Wählen Sie den Namen der Datenbank.\nVerwenden Sie möglichst eine Vorsilbe mit ihrem Namen.",
                    Polymap.instance().getUser().getName() + "-datenbank",
                    new IInputValidator() {
                        public String isValid( String newText ) {
                            return StringUtils.containsNone( newText, "/\\@" )
                                    ? null : "Folgende Buchstaben sind nicht erlaubt: /\\@";
                        }
                    });
            
            if (dialog.open() == Window.OK) {
            
                Map<String,Serializable> params = new HashMap();
                params.put( H2DataStoreFactory.DBTYPE.key, "h2" );
                params.put( JDBCDataStoreFactory.NAMESPACE.key, "http://www.polymap.org/" + dialog.getValue() );
                params.put( H2DataStoreFactory.DATABASE.key, dialog.getValue() );
                params.put( H2DataStoreFactory.USER.key, "polymap" );

                org.polymap.catalog.h2.data.H2DataStoreFactory factory = H2ServiceExtension.getFactory();
                JDBCDataStore ds = factory.createDataStore( params );
                try {
                    String[] typeNames = ds.getTypeNames();
                    log.info( "H2: " + Arrays.asList( typeNames ) );
                }
                catch( Exception e) {
                    throw new RuntimeException( e );
                }

                String url = ((BasicDataSource)ds.getDataSource()).getUrl();
                log.info( "URL: " + url );
                
                H2ServiceImpl service = (H2ServiceImpl)new H2ServiceExtension().createService( null, params );
//                H2ServiceImpl service = new H2ServiceImpl( 
//                        H2ServiceExtension.toURL( url ), params );
                CatalogPluginSession.instance().getLocalCatalog().add( service );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public void selectionChanged( IAction action, ISelection selection ) {
    }
    
}
