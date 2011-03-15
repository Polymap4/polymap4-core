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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.IService;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

/**
 * This executor runs all processors inside the calling thread. The
 * requests/responses are passed between the processors by recursively calling
 * their processXXX() methods. The entire execution runs inside one single JVM
 * stackframe. This allows 'depth first' semantics. The first response is send
 * to the client at the first possible stage of the pipeline.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DepthFirstStackExecutor
        implements PipelineExecutor {

    private static final Log log = LogFactory.getLog( DepthFirstStackExecutor.class );

    private Pipeline                pipe;
    
    /** The handler to send the result to, or null if a non-chunked execute() call was used. */
    private ResponseHandler         handler;
    
    private ProcessorRequest        request;
    
    private List<DepthFirstContext> contexts = new ArrayList();
    
    private boolean                 isEop;

    
    public void execute( Pipeline _pipe, ProcessorRequest _request, ResponseHandler _handler )
    throws Exception {
        this.pipe = _pipe;
        this.request = _request;
        this.handler = _handler;
        this.isEop = false;

        // create contexts
        int i = 0;
        for (PipelineProcessor proc : pipe) {
            DepthFirstContext context = new DepthFirstContext( proc, i++ );
            contexts.add( context );
        }
        // recursivly call processors
        DepthFirstContext sinkContext = contexts.get( 0 );
        sinkContext.proc.processRequest( request, sinkContext );
    }

    
    protected void handleResponse( ProcessorResponse r ) {
        // FIXME this try/catch must be done somewhere else
        try {
            if (r != ProcessorResponse.EOP) {
                handler.handle( r );
            }
            else {
                isEop = true;
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * The processor context used by {@link DepthFirstStackExecutor}.
     * 
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    protected class DepthFirstContext
            implements ProcessorContext {

        int                     pipePos;
        
        PipelineProcessor       proc;
        
        /** The processor specific data. */
        Map                     procData = new HashMap();
        
        boolean                 contextEop = false;
        
        
        public DepthFirstContext( PipelineProcessor proc, int pipePos ) {
            this.pipePos = pipePos;
            this.proc = proc;
        }

        public Object put( String key, Object data ) {
            return procData.put( key, data );
        }

        public Object get( String key ) {
            return procData.get( key );
        }

        public IMap getMap() {
            return pipe.getMap();
        }

        public Set<ILayer> getLayers() {
            return pipe.getLayers();
        }
        
        public IService getService() {
            return pipe.getService();
        }

        public void sendRequest( ProcessorRequest r ) 
        throws Exception {
            DepthFirstContext upstream = contexts.get( pipePos+1 );
            upstream.proc.processRequest( r, upstream );
        }

        public void sendResponse( ProcessorResponse r )
        throws Exception {
            if (pipePos > 0) {
                DepthFirstContext downstream = contexts.get( pipePos-1 );
                if (r != ProcessorResponse.EOP) {
                    downstream.proc.processResponse( r, downstream );
                }
                else {
                    // send EOP 
                    downstream.proc.processResponse( r, downstream );
                    // close context!?
                }
            }
            else {
                handleResponse( r );
            }
        }
        
    }

}
