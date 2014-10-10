/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides info about the {@link UnitOfWorkPropagation} annotation of a given
 * class and/or method.
 * 
 * @deprecated Yet to be supported by the engine.
 * @see UnitOfWorkPropagation
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropagationInfo {

    private static Log log = LogFactory.getLog( PropagationInfo.class );

    private Class           clazz;
    
    
    public PropagationInfo( Class clazz ) {
        this.clazz = clazz;
    }

    
//    public UnitOfWorkPropagation ofClass() {
//        
//    }
//
//    
//    public UnitOfWorkPropagation ofCall() {
//        Thread t = Thread.currentThread();
//        ClassLoader cl = t.getContextClassLoader();
//        for (StackTraceElement elm : t.getStackTrace()) {
//            try {
//                Class clazz = cl.loadClass( elm.getClassName() );
//                for (Method m : clazz.getMethods()) {
//                    if (m.getName().equals( elm.getMethodName() )) {
//                        UnitOfWorkPropagation a = m.getAnnotation( UnitOfWorkPropagation.class );
//                        uow = initUow( a );
//                        break;
//                    }
//                }
//            }
//            catch (ClassNotFoundException e) {
//                // skip
//            }
//        }
//        log.error( "Keine 'UnitOfWorkPropagation' in diesem StackTrace: " );
//        Thread.dumpStack();
//        throw new IllegalStateException( "Keine 'UnitOfWorkPropagation' im StackTrace!" );
//    }

}
