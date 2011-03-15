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

package org.polymap.core.data.pipeline;

import java.util.Set;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

import net.refractions.udig.catalog.IService;

/**
 * Provides the SPI for executor services.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>18.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface PipelineExecutor {

    public void execute( Pipeline pipeline, ProcessorRequest request, ResponseHandler handler )
            throws Exception;


    /**
     * The runtime context of one {@link PipelineProcessor}.
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     *         <li>19.10.2009: created</li>
     */
    public static interface ProcessorContext {
        
        public IMap getMap();
        
        public Set<ILayer> getLayers();

        public IService getService();


        /**
         * Sends a new request to the upstream processor.
         * 
         * @throws Exception Signals upstream exceptions. This depends on the
         *         executor used; clients should not catch/handle this.
         */
        public void sendRequest( ProcessorRequest request ) 
        throws Exception;
        
        /**
         * Send a new request the the processing chain.
         * 
         * @throws Exception Signals upstream exceptions. This depends on the
         *         executor used; clients should not catch/handle this.
         */
        public void sendResponse( ProcessorResponse response )
        throws Exception;
        
        public Object put( String key, Object data );

        public Object get( String key );
        
    }

}
