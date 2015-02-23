/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.operations.feature;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.ui.IDropAction;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.actions.ResetServiceAction;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.model.security.ACL;
import org.polymap.core.model.security.ACLUtils;
import org.polymap.core.model.security.AclPermission;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Copy the data of an {@link IGeoResource} into another service. This allows for
 * drag&drop data in the catalog for example. The destination schema is created if
 * missing.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CopyGeoResourceDropAction
        extends IDropAction {

    private static Log log = LogFactory.getLog( CopyGeoResourceDropAction.class );

    private static IMessages            i18n = Messages.forPrefix( "CopyGeoResourceOperation" );

    public CopyGeoResourceDropAction() {
    }


    @Override
    public boolean accept() {
        Object data = getData();
        Object dest = getDestination();
        log.debug( "Drop accept(): data=" + data + ", dest=" + dest );
        return true;
    }


    @Override
    public void perform( IProgressMonitor monitor ) {
        final IResolve data = (IResolve)getData();
        final IResolve destination = (IResolve)getDestination();
        try {
            // check ACL permission
            if (destination instanceof IAdaptable) {
                ACL acl = (ACL)((IAdaptable)destination).getAdapter( ACL.class );
                if (acl != null) {
                    if (!ACLUtils.checkPermission( acl, AclPermission.WRITE, false )) {
                        throw new RuntimeException( i18n.get( "accessErrorMsg" ) );
                    }
                }
            }

            final AtomicBoolean confirmed = new AtomicBoolean();
            final Display display = Polymap.getSessionDisplay();
            display.syncExec( new Runnable() {
                public void run() {
                    confirmed.set( MessageDialog.openConfirm( display.getActiveShell(),
                            i18n.get( "confirmTitle" ), i18n.get( "confirmMsg", data.getTitle(), destination.getTitle() ) ) );
                }
            });
            
            if (confirmed.get()) {
                final FeatureSource srcFs = data.resolve( FeatureSource.class, monitor );
                final DataAccess destDs = destination.resolve( DataAccess.class, monitor );

                if (destDs == null) {
                    throw new RuntimeException( "Wrong destination type: " + getDestination() );
                }
                
                // check schema
                Name schemaExists = Iterables.find( destDs.getNames(), new Predicate<Name>() {
                    String srcName = srcFs.getSchema().getName().getLocalPart();
                    public boolean apply( Name input ) {
                        return input.getLocalPart().equals( srcName );
                    }
                }, null );
                // create schema
                if (schemaExists == null) {
                    destDs.createSchema( srcFs.getSchema() );
                }

                // copy features
                FeatureStore destFs = (FeatureStore)destDs.getFeatureSource( srcFs.getSchema().getName() );
                List createdFids = destFs.addFeatures( srcFs.getFeatures()/*, new ProgressListenerAdaptor( monitor )*/ );                    
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, i18n.get( "errorMsg" ), e );
        }
        finally {
            // reload also on error as the schema may has been created
            IService reset = null;
            if (destination instanceof IService) {
                reset = (IService)destination;
            }
            else if (destination instanceof IResolveFolder) {
                reset = ((IResolveFolder)destination).getService( monitor );
            }
            if (reset != null) {
                ResetServiceAction.reset( Collections.singletonList( reset ), monitor );
            }
        }
    }
    
}
