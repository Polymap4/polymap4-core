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

package org.polymap.core.project.qi4j.operations;

import java.security.Principal;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.referencing.CRS;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.AclPermission;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.OperationBoundsConcern;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@Concerns( OperationBoundsConcern.class )
@Mixins( NewMapOperation.Mixin.class )
public interface NewMapOperation
        extends IUndoableOperation, TransientComposite {

    public void init( IMap _parent, String _mapName, CoordinateReferenceSystem _crs );
    
    /** Implementation is provided bei {@link AbstractOperation} */ 
    public boolean equals( Object obj );
    
    public int hashCode();

    /**
     * Implementation. 
     */
    public static abstract class Mixin
            extends AbstractOperation
            implements NewMapOperation {
        
        private IMap                        parent;

        private String                      mapName;

        private CoordinateReferenceSystem   crs;


        public Mixin() {
            super( "[undefined]" );
        }


        public void init( IMap _parent, String _mapName, CoordinateReferenceSystem _crs ) {
            assert _parent != null;

            this.parent = _parent;
            this.mapName = _mapName;
            this.crs = _crs;
            setLabel( (mapName != null ? mapName : "Karte") + " anlegen" );
        }


        public IStatus execute( IProgressMonitor monitor, IAdaptable info )
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


        public IStatus redo( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }


        public IStatus undo( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

    }

}
