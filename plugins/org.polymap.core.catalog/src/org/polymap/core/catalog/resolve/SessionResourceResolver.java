/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.catalog.resolve;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.session.SessionSingleton;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class SessionResourceResolver
        extends SessionSingleton
        implements IMetadataResourceResolver {

    private static Log log = LogFactory.getLog( SessionResourceResolver.class );

    
    public static SessionResourceResolver instance() {
        return instance( SessionResourceResolver.class );
    }

    // instance *******************************************
    
    private IMetadataResourceResolver       delegate;
    

    public SessionResourceResolver( IMetadataResourceResolver delegate ) {
        this.delegate = delegate;
    }


    @Override
    public boolean canResolve( Map<String,String> params ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public IResolvableInfo resolve( Map<String,String> params, IProgressMonitor monitor ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Map<String,String> createParams( Object service ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
