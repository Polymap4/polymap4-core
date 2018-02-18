/* 
 * polymap.org
 * Copyright (C) 2015-2018, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.pipeline;

import java.util.HashMap;
import java.util.Map;

import org.polymap.core.data.pipeline.Param.ParamsHolder;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * The runtime environment of a {@link PipelineProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineProcessorSite
        extends Configurable
        implements ParamsHolder {

    @Mandatory
    @Immutable
    public Config<ProcessorSignature>   usecase;
    
    @Mandatory
    @Immutable
    public Config<DataSourceDescriptor> dsd;

    @Mandatory
    @Immutable
    public Config<String>               layerId;

    @Immutable
    public Config<PipelineBuilder>      builder;

    protected Params                    params;
    
    
    public PipelineProcessorSite( Params params ) {
        this.params = params != null ? params : Params.EMPTY;
    }

    /**
     * Init parameters of the {@link PipelineProcessor}. See {@link Param} to define
     * and access a parameter.
     */
    @Override
    public Params params() {
        return params;
    }
    
    /**
     * Map of name/value pairs used as init parameters of {@link PipelineProcessor}
     * instances. See {@link Param} to define and access.
     * 
     * @see Param
     */
    public static class Params
            extends HashMap<String,Object>
            implements ParamsHolder {

        /**
         * Immutable, empty params instance.
         */
        public static final Params  EMPTY = new Params() {
            @Override 
            public Object put( String key, Object value ) { 
                throw new RuntimeException( "Immutable!" ); }
            @Override 
            public void putAll( Map<? extends String,? extends Object> m ) { 
                throw new RuntimeException( "Immutable!" ); }
            @Override
            public Object remove( Object key ) {
                throw new RuntimeException( "Immutable!" );
            }
            @Override
            public Object putIfAbsent( String key, Object value ) {
                throw new RuntimeException( "Immutable!" );
            }
            @Override
            public boolean remove( Object key, Object value ) {
                throw new RuntimeException( "Immutable!" );
            }
        };
        
        // instance ***************************************
        
        public Params add( String key, Object value ) {
            put( key, value );
            return this;
        }

        @Override
        public Params params() {
            return this;
        }
    }
    
}
