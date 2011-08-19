/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.spi;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Default implementation of an content node. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultContentNode
        implements IContentNode {

    private static Log log = LogFactory.getLog( DefaultContentNode.class );

    private String                  name;
    
    private IPath                   parentPath;
    
    private IContentProvider        provider;
    
    private Object                  source;

    private Map<String,Object>      data = new HashMap();
    

    public DefaultContentNode( String name, IPath parentPath, IContentProvider provider, Object source ) {
        this.name = name;
        this.parentPath = parentPath;
        this.provider = provider;
        this.source = source;
    }

    
    public String getName() {
        return name;
    }

    
    public IPath getParentPath() {
        return parentPath;
    }

    
    public IPath getPath() {
        return parentPath != null ? parentPath.append( getName() ) : new Path( getName() );
    }


    public IContentProvider getProvider() {
        return provider;
    }


    public Object getSource() {
        return source;
    }


    public Object getData( String key ) {
        return data.get( key );
    }


    public Object putData( String key, Object value ) {
        return data.put( key, value );
    }
    
}
