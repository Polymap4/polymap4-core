/* 
 * polymap.org
 * Copyright 2011-2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

import net.refractions.udig.catalog.CatalogPluginSession;

import org.opengis.feature.type.Name;

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

import org.polymap.core.data.Messages;
import org.polymap.core.data.feature.recordstore.RDataStore;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CreateRDataStoreAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( CreateRDataStoreAction.class );


    public void init( IViewPart view ) {
    }


    public void run( IAction action ) {
        try {
            InputDialog dialog = new InputDialog( PolymapWorkbench.getShellToParentOn(),
                    i18n( "dialogTitle" ), i18n( "dialogMsg" ), 
                    i18n( "dbName", Polymap.instance().getUser().getName() ),
                    new IInputValidator() {
                        public String isValid( String newText ) {
                            return StringUtils.containsNone( newText, "/\\@" )
                                    ? null : i18n( "validationError" );
                        }
                    });
            
            if (dialog.open() == Window.OK) {
            
                Map<String,Serializable> params = new HashMap();
                params.put( RDataStoreFactory.DBTYPE.key, (Serializable)RDataStoreFactory.DBTYPE.sample );
                params.put( RDataStoreFactory.DATABASE.key, dialog.getValue() );

                RDataStoreFactory factory = RServiceExtension.factory();
                RDataStore ds = factory.createNewDataStore( params );
                try {
                    List<Name> typeNames = ds.getNames();
                    log.info( "RDataStore: " + typeNames );
                }
                catch( Exception e) {
                    throw new RuntimeException( e );
                }

                RServiceImpl service = (RServiceImpl)new RServiceExtension().createService( null, params );
                CatalogPluginSession.instance().getLocalCatalog().add( service );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public void selectionChanged( IAction action, ISelection selection ) {
    }
    
    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "CreateRDataStoreAction_" + key, args );
    }
    
}
