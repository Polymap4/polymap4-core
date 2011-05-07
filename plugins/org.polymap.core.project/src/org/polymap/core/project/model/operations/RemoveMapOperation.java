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

package org.polymap.core.project.model.operations;

import java.util.ArrayList;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@Mixins( RemoveMapOperation.Mixin.class )
public interface RemoveMapOperation
        extends IUndoableOperation, TransientComposite {

    public void init( IMap map );
    
    /** Implementation is provided bei {@link AbstractOperation} */ 
    public boolean equals( Object obj );
    
    public int hashCode();

    
    /**
     * Implementation. 
     */
    public static abstract class Mixin
            extends AbstractModelChangeOperation
            implements RemoveMapOperation {

        private IMap                map;
        
        public Mixin() {
            super( "[undefined]" );
        }


        public void init( IMap _map ) {
            this.map = _map;
            setLabel( '"' + map.getLabel() + "\" löschen" );
        }


        public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            try {
                ProjectRepository rep = ProjectRepository.instance();
                ArrayList<ILayer> layers = new ArrayList( map.getLayers() );
                for (ILayer layer : layers) {
                    map.removeLayer( layer );
                    rep.removeEntity( layer );
                }
                map.getMap().removeMap( map );
                rep.removeEntity( map );
            }
            catch (Throwable e) {
                throw new ExecutionException( e.getMessage(), e );
            }
            return Status.OK_STATUS;
        }

    }

}
