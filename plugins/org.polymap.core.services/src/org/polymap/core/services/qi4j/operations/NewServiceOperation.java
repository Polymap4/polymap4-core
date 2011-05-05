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
package org.polymap.core.services.qi4j.operations;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.IMap;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;
import org.polymap.core.services.ServiceRepository;
import org.polymap.core.services.qi4j.ProvidedServiceComposite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@Mixins( NewServiceOperation.Mixin.class )
public interface NewServiceOperation
        extends IUndoableOperation, TransientComposite {

    static Log log = LogFactory.getLog( NewServiceOperation.class );
    

    public void init( IMap _map, Class _type/*, String _pathSpec*/ );
    
    /** Implementation is provided bei {@link AbstractOperation} */ 
    public boolean equals( Object obj );
    
    public int hashCode();

    
    /**
     * Implementation. 
     */
    public static abstract class Mixin
            extends AbstractModelChangeOperation
            implements NewServiceOperation {

        private IMap                        map;

        private Class                       type;

        private String                      pathSpec;


        public Mixin() {
            super( "[undefined]" );
        }


        @SuppressWarnings("deprecation")
        public void init( IMap _map, Class _type/*, String _pathSpec*/ ) {
            this.map = _map;
            this.type = _type;
            try {
                this.pathSpec = "/" + URLEncoder.encode( map.getLabel(), "UTF-8" );
            }
            catch (UnsupportedEncodingException e) {
                log.warn( "", e );
                this.pathSpec = "/" + URLEncoder.encode( map.getLabel() );
            }
            setLabel( "Service anlegen" );
        }


        public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            try {
                ServiceRepository repo = ServiceRepository.instance();
                ProvidedServiceComposite service = repo.newEntity( ProvidedServiceComposite.class, null );
                service.mapId().set( map.id() );
                service.serviceType().set( type.getName() );
                service.setPathSpec( pathSpec );
                
                repo.addService( service );
            }
            catch (Throwable e) {
                throw new ExecutionException( e.getMessage(), e );
            }
            return Status.OK_STATUS;
        }

    }

}
