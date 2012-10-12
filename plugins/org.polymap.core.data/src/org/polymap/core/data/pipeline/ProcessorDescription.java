/* 
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH. All rights reserved.
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

import java.util.Properties;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.refractions.udig.catalog.IService;

import org.polymap.core.project.LayerUseCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
class ProcessorDescription {

    private Class<? extends PipelineProcessor> cl;

    private ProcessorSignature      signature;
    
    private Properties              props;
    
    private LayerUseCase            usecase;


    /**
     * 
     * @param cl
     * @param props The properties that are given to the
     *        {@link PipelineProcessor#init(Properties)} method.
     * @param usecase 
     */
    public ProcessorDescription( Class<? extends PipelineProcessor> cl, 
            Properties props, LayerUseCase usecase ) {
        this.cl = cl;
        this.props = props;
        this.usecase = usecase;
    }


    public ProcessorDescription( ProcessorSignature signature ) {
        this.signature = signature;
    }


    public int hashCode() {
        int result = 1;
        result = 31 * result + ((cl == null) ? 0 : cl.getName().hashCode());
//        result = 31 * result + ((usecase == null) ? 0 : usecase.asString().hashCode());
        return result;
    }


    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProcessorDescription) {
            ProcessorDescription rhs = (ProcessorDescription)obj;
            return cl.equals( rhs.cl );
        }
        return false;
    }


    public String toString() {
        return /*getClass().getSimpleName() + */ "[" 
                + (cl != null ? cl.getSimpleName() : "null")
                + "]";
    }


    public ProcessorSignature getSignature() {
        if (signature == null) {
            try {
                Method m = cl.getMethod( "signature", LayerUseCase.class );
                if (Modifier.isStatic( m.getModifiers() )) {
                    signature = (ProcessorSignature)m.invoke( cl, usecase );
                }
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return signature;
    }

    
    public Properties getProps() {
        return props;
    }


    public PipelineProcessor newProcessor() {
        assert cl != null : "This ProcessorDescription was initialized without a processor class - it can only be used as the start of a chain.";
        try {
            PipelineProcessor result = cl.newInstance();
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    /**
     * In case this processor is an {@link ITerminalPipelineProcessor}, check
     * if it can handle the given service.
     * 
     * @param service
     * @throws IllegalArgumentException If this processor is not a terminal.
     */
    public boolean isCompatible( IService service ) {
        assert cl != null : "This ProcessorDescription was initialized without a processor class - it can only be used as the start of a chain.";
        if (! ITerminalPipelineProcessor.class.isAssignableFrom( cl )) {
            throw new IllegalArgumentException( "Processor is not a terminal: " + cl.getName() );
        }
        try {
            Method m = cl.getMethod( "isCompatible", IService.class );
            if (Modifier.isStatic( m.getModifiers() )) {
                return (Boolean)m.invoke( cl, service );
            }
            else {
                throw new IllegalStateException( "Method isCompatible() must be static." );
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

}
