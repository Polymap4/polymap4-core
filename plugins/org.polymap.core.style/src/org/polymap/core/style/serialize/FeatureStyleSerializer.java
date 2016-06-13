/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.style.model.FeatureStyle;

/**
 * 
 *
 * @author Falko Br�utigam
 */
public abstract class FeatureStyleSerializer<T> {

    public abstract T serialize( Context context );
    

    public enum OutputFormat {
        OGC, GEOSERVER
    }


    /**
     * 
     */
    public static class Context
            extends Configurable {
        
        @Mandatory
        public Config2<Context,IProgressMonitor>    monitor;
        
        @Mandatory
        public Config2<Context,FeatureStyle>        featureStyle;
        
        @Mandatory
        public Config2<Context,OutputFormat>        outputFormat;
        
        public Context() {
            monitor.set( new NullProgressMonitor() );
        }
    }

}
