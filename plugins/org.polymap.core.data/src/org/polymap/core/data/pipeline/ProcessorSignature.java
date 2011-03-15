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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ProcessorSignature {

    private static Log log = LogFactory.getLog( ProcessorSignature.class );

    private List<Class<? extends ProcessorRequest>>     requestIn = new ArrayList();
    
    private List<Class<? extends ProcessorRequest>>     requestOut = new ArrayList();
    
    private List<Class<? extends ProcessorResponse>>    responseIn = new ArrayList();
    
    private List<Class<? extends ProcessorResponse>>    responseOut = new ArrayList();
    
    
    public ProcessorSignature( Class<? extends ProcessorRequest>[] requestIn,
            Class<? extends ProcessorRequest>[] requestOut,
            Class<? extends ProcessorResponse>[] responseIn,
            Class<? extends ProcessorResponse>[] responseOut ) {
        
        this.requestIn.addAll( Arrays.asList( requestIn ) );
        this.requestOut.addAll( Arrays.asList( requestOut ) );
        this.responseOut.addAll( Arrays.asList( responseOut ) );
        this.responseIn.addAll( Arrays.asList( responseIn ) );
    }
    
    
    public ProcessorSignature( PipelineProcessor processor ) {
        
    }


    /**
     * Returns true if the given processor can be chained behind the receiver.
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
    
}
