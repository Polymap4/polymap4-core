/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This executor runs all processors inside the calling thread. The
 * requests/responses are passed between the processors by recursively calling their
 * handle methods. The entire execution runs inside one single JVM stackframe. This
 * allows for 'depth first' semantics. The first response is send to the client at
 * the first possible stage of the pipeline.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DepthFirstStackExecutor
        implements PipelineExecutor {

    private static final Log log = LogFactory.getLog( DepthFirstStackExecutor.class );

    private Pipeline                pipeline;
    
    /** The handler to send the result to, or null if a non-chunked execute() call was used. */
    private ResponseHandler         handler;
    
    private ProcessorRequest        request;
    
    private List<DepthFirstContext> contexts = new ArrayList();
    
    private boolean                 isEop;

    
    public <R extends ProcessorResponse,E extends Exception> void execute( 
            Pipeline _pipeline, ProcessorRequest _request, ResponseHandler<R,E> _handler ) throws Exception {
        this.pipeline = _pipeline;
        this.request = _request;
        this.handler = _handler;
        this.isEop = false;

        // create contexts
        int i = 0;
        for (ProcessorDescriptor desc : pipeline) {
            DepthFirstContext context = new DepthFirstContext( desc, i++ );
            contexts.add( context );
        }
        // recursivly call processors
        DepthFirstContext sinkContext = contexts.get( 0 );
        sinkContext.procDesc.invoke( request, sinkContext );
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
     */
    protected class DepthFirstContext
            implements ProcessorContext {

        int                     pipePos;
        
        ProcessorDescriptor    procDesc;
        
        /** The processor specific data. */
        Map                     procData = new HashMap();
        
        boolean                 contextEop = false;
        
        
        public DepthFirstContext( ProcessorDescriptor procDesc, int pipePos ) {
            this.pipePos = pipePos;
            this.procDesc = procDesc;
        }

        public Object put( String key, Object data ) {
            assert key != null;
            return data != null
                    ? procData.put( key, data )
                    : procData.remove( key );
        }

        public Object get( String key ) {
            assert key != null;
            return procData.get( key );
        }

        public void sendRequest( ProcessorRequest r ) throws Exception {
            DepthFirstContext upstream = contexts.get( pipePos+1 );
            upstream.procDesc.invoke( r, upstream );
        }

        public void sendResponse( ProcessorResponse r ) throws Exception {
            if (pipePos > 0) {
                DepthFirstContext downstream = contexts.get( pipePos-1 );
                if (r != ProcessorResponse.EOP) {
                    downstream.procDesc.invoke( r, downstream );
                }
                else {
                    // send EOP 
                    downstream.procDesc.invoke( r, downstream );
                    // close context!?
                }
            }
            else {
                handleResponse( r );
            }
        }
        
    }

}
