/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.operations;

import java.security.Principal;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.referencing.CRS;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.security.AclPermission;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class NewMapOperation
        extends AbstractModelChangeOperation {

    private IMap                        parent;

    private String                      mapName;

    private CoordinateReferenceSystem   crs;


    public NewMapOperation() {
        super( "[undefined]" );
    }


    public void init( IMap _parent, String _mapName, CoordinateReferenceSystem _crs ) {
        assert _parent != null;

        this.parent = _parent;
        this.mapName = _mapName;
        this.crs = _crs;
        setLabel( (mapName != null ? mapName : "Karte") + " anlegen" );
    }


    public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        try {
            IMap map = ProjectRepository.instance().newEntity( IMap.class, null );

            // default ACL
            for (Principal principal : Polymap.instance().getPrincipals()) {
                map.addPermission( principal.getName(), AclPermission.ALL );
            }

            map.setLabel( mapName );
            map.setCRSCode( "EPSG:" + CRS.lookupEpsgCode( crs, true ) );
            parent.addMap( map );
            parent.setLabel( parent.getLabel() + "*" );
        }
        catch (Throwable e) {
            throw new ExecutionException( e.getMessage(), e );
        }
        return Status.OK_STATUS;
    }


//    @Override
//    public boolean canUndo() {
//        return true;
//    }
//
//
//    @Override
//    public boolean canRedo() {
//        return true;
//    }
    
}
