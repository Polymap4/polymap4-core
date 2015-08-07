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

import java.util.Properties;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ProcessorDescription<P extends PipelineProcessor> {

    private Class<? extends PipelineProcessor> cl;

    /** Lazily inited by {@link #signature()}. */
    private ProcessorSignature          signature;
    
    private Properties                  props;
    
    private P                           processor;


    /**
     * 
     * @param cl
     * @param props The properties that are given to the
     *        {@link PipelineProcessor#init(Properties)} method.
     * @param usecase 
     */
    public ProcessorDescription( Class<? extends PipelineProcessor> cl, Properties props ) {
        this.cl = cl;
        this.props = props;
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
        else if (obj instanceof ProcessorDescription) {
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


    public ProcessorSignature signature() throws PipelineIncubationException {
        if (signature == null) {  // no concurrent check, multi init is ok
            signature = new ProcessorSignature( processor() );
        }
        return signature;
    }

    
    public Properties getProps() {
        return props;
    }


    public P processor() throws PipelineIncubationException {
        assert cl != null : "This ProcessorDescription was initialized without a processor class - it can only be used as the start of a chain.";
        if (processor == null) {  // no concurrent check, multi init ok
            try {
                processor = (P)cl.newInstance();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new PipelineIncubationException( "", e );
            }
        }
        return processor;
    }

    
    /**
     * Checks if the {@link TerminalPipelineProcessor} is compatible with (aka can handle)
     * the given data source. 
     *
     * @param dsd The data source description to handle.
     * @throws PipelineIncubationException 
     */
    public boolean isCompatible( DataSourceDescription dsd ) throws PipelineIncubationException {
        return ((TerminalPipelineProcessor)processor()).isCompatible( dsd );
    }

    
//    /**
//     * In case this processor is an {@link ITerminalPipelineProcessor}, check
//     * if it can handle the given service.
//     * 
//     * @param service
//     * @throws IllegalArgumentException If this processor is not a terminal.
//     */
//    public boolean isCompatible( IService service ) {
//        assert cl != null : "This ProcessorDescription was initialized without a processor class - it can only be used as the start of a chain.";
//        if (! ITerminalPipelineProcessor.class.isAssignableFrom( cl )) {
//            throw new IllegalArgumentException( "Processor is not a terminal: " + cl.getName() );
//        }
//        try {
//            Method m = cl.getMethod( "isCompatible", IService.class );
//            if (Modifier.isStatic( m.getModifiers() )) {
//                return (Boolean)m.invoke( cl, service );
//            }
//            else {
//                throw new IllegalStateException( "Method isCompatible() must be static." );
//            }
//        }
//        catch (RuntimeException e) {
//            throw e;
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }

}
