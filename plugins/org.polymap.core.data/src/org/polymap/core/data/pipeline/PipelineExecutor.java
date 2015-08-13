/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.pipeline;


/**
 * Provides the SPI for executor services.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PipelineExecutor {

    public void execute( Pipeline pipeline, ProcessorRequest request, ResponseHandler handler ) throws Exception;


    /**
     * The runtime context of one {@link PipelineProcessor}.
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     */
    public static interface ProcessorContext {
        
        /**
         * Sends a new request to the upstream processor.
         * 
         * @throws Exception Signals upstream exceptions. This depends on the
         *         executor used; clients should not catch/handle this.
         */
        public void sendRequest( ProcessorRequest request ) throws Exception;
        
        /**
         * Send a new request the the processing chain.
         * 
         * @throws Exception Signals upstream exceptions. This depends on the
         *         executor used; clients should not catch/handle this.
         */
        public void sendResponse( ProcessorResponse response ) throws Exception;


        /**
         * Gets an element from the context. Context elements can be used by the
         * {@link PipelineProcessor} to store its state between several handle
         * request/response calls.
         * 
         * @param key The key of the element.
         * @return The element for the given key or null.
         */
        public Object get( String key );


        /**
         * Stores the given element in the context.
         * 
         * @param key The key of the element.
         * @param data The object to be stored or null to remove a previously stored
         *        element.
         * @return The previously stored element.
         */
        public Object put( String key, Object data );
        
    }

}
