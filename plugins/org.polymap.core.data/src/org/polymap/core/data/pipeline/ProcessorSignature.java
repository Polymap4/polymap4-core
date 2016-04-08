/* 
 * polymap.org
 * Copyright (C) 2009-15, Polymap GmbH. All rights reserved.
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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;

/**
 * Describes the signature of a {@link PipelineProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProcessorSignature {

    private static Log log = LogFactory.getLog( ProcessorSignature.class );

    protected Set<Class<? extends ProcessorRequest>>    requestIn = new HashSet();
    
    protected Set<Class<? extends ProcessorRequest>>    requestOut = new HashSet();
    
    protected Set<Class<? extends ProcessorResponse>>   responseIn = new HashSet();
    
    protected Set<Class<? extends ProcessorResponse>>   responseOut = new HashSet();
    
    protected Map<Class,Method>                         callMap = new HashMap( 32 );
    
    protected PipelineProcessor                         processor;
    
    
//    public ProcessorSignature( Class<? extends ProcessorRequest>[] requestIn,
//            Class<? extends ProcessorRequest>[] requestOut,
//            Class<? extends ProcessorResponse>[] responseIn,
//            Class<? extends ProcessorResponse>[] responseOut ) {
//        
//        this.requestIn.addAll( Arrays.asList( requestIn ) );
//        this.requestOut.addAll( Arrays.asList( requestOut ) );
//        this.responseOut.addAll( Arrays.asList( responseOut ) );
//        this.responseIn.addAll( Arrays.asList( responseIn ) );
//    }
    
    public ProcessorSignature( PipelineProcessor processor ) throws PipelineIncubationException {
        this.processor = processor;
        buildSignature( processor.getClass() );
    }
    
    
    public ProcessorSignature( Class<? extends PipelineProcessor> type ) throws PipelineIncubationException {
        buildSignature( type );
    }
    
    
    protected void buildSignature( Class<? extends PipelineProcessor> type ) throws PipelineIncubationException {
        Deque<Class> deque = new ArrayDeque();
        deque.push( type );
        
        while (!deque.isEmpty()) {
            Class cl = deque.pop();
            Optional.ofNullable( cl.getSuperclass() ).ifPresent( s -> deque.push( s ) );
            Arrays.stream( cl.getInterfaces() ).forEach( i -> deque.push( i ) );
        
            for (Method m : cl.getDeclaredMethods()) {
                Consumes consumes = m.getAnnotation( Consumes.class );
                Produces produces = m.getAnnotation( Produces.class );
                Class<? extends ProcessorProbe> param = checkMethodParam( m );
                
                if (param == null) {
                    if (consumes != null || produces != null) {
                        throw new PipelineIncubationException( "Method has annotations but not correct signature: " + m );
                    }
                    continue;
                }
                
                // param
                callMap.put( param, m );
                if (ProcessorRequest.class.isAssignableFrom( param )) {
                    requestIn.add( (Class<? extends ProcessorRequest>)param );
                }
                else {
                    responseIn.add( (Class<? extends ProcessorResponse>)param );
                }
                
                // Consumes
                if (consumes != null) {
                    for (Class c : consumes.value() ) {
                        if (ProcessorRequest.class.isAssignableFrom( c )) {
                            requestIn.add( (Class<? extends ProcessorRequest>)c );
                        }
                        else {
                            responseIn.add( (Class<? extends ProcessorResponse>)c );
                        }
                    }
                }
                // Produces
                if (produces != null) {
                    for (Class c : produces.value() ) {
                        if (ProcessorRequest.class.isAssignableFrom( c )) {
                            requestOut.add( (Class<? extends ProcessorRequest>)c );
                        }
                        else {
                            responseOut.add( (Class<? extends ProcessorResponse>)c );
                        }
                    }
                }
            }
        }
    }

    
    protected Class<? extends ProcessorProbe> checkMethodParam( Method m ) {
        Class<?>[] paramTypes = m.getParameterTypes();
        if (paramTypes.length == 2
                && ProcessorContext.class.isAssignableFrom( paramTypes[1] )
                && ProcessorProbe.class.isAssignableFrom( paramTypes[0] )) {
            return (Class<? extends ProcessorProbe>)paramTypes[0];
        }
        return null;
    }

    
    public void invoke( ProcessorProbe probe, ProcessorContext context ) throws Exception {
        assert processor != null : "This ProcessorSignature was constructed from a type, not a processor instance.";
        try {
            Method m = callMap.get( probe.getClass() );
            if (m == null) {
                throw new IllegalStateException( "No method for: " + probe.getClass().getName() );
            }
            m.invoke( processor, new Object[] {probe, context} );
        }
        catch (InvocationTargetException e) {
            throw (Exception)e.getTargetException();
        }
    }

    
    /**
     * Returns true if the given processor can be chained after the receiver.
     * 
     * @param rhs
     */
    public boolean isCompatible( ProcessorSignature rhs ) {
//        // debug
//        if (log.isDebugEnabled()) {
//            StringBuffer buf1 = new StringBuffer( 256 );
//            for (Class processorClass : requestOut) {
//                buf1.append( "\n        " ).append( processorClass.getName() );
//            }
//            StringBuffer buf2 = new StringBuffer( 256 );
//            for (Class processorClass : rhs.requestIn) {
//                buf2.append( "\n        " ).append( processorClass.getName() );
//            }
//            log.debug( "this.requestOut:" + buf1 +
//                    "\n    rhs.requestIn:" + buf2 );
//        }
        
        return rhs.requestIn.containsAll( requestOut )
                && responseIn.containsAll( rhs.responseOut );
    }


    @Override
    public String toString() {
        return "    RequestIn: " + requestIn.stream().map( cl -> cl.getSimpleName() ).collect( Collectors.toList() ) + "\n" +
                "    RequestOut: " + requestOut.stream().map( cl -> cl.getSimpleName() ).collect( Collectors.toList() ) + "\n" +
                "    ResponseIn: " + responseIn.stream().map( cl -> cl.getSimpleName() ).collect( Collectors.toList() ) + "\n" +
                "    ResponseOut: " + responseOut.stream().map( cl -> cl.getSimpleName() ).collect( Collectors.toList() );
    }

}
