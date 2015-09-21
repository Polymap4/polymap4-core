/* 
 * polymap.org
 * Copyright (C) 2011-2012, Polymap GmbH. All rights reserved.
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

import java.util.List;

import org.eclipse.core.runtime.IPath;

/**
 * The interface of all content providers.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentProvider {

    public void init( IContentSite site );
    
    public void dispose();
    
    public IContentSite getSite();
    
    /**
     * Creates the child nodes for the given <code>parentPath</code>.
     * <p/>
     * This method is part of the SPI. It is called by the engine. Client code that
     * needs to access content nodes should call {@link IContentSite} instead.
     * 
     * @param parentPath
     * @param site
     * @return List of newly created content nodes.
     */
    public List<? extends IContentNode> getChildren( IPath parentPath );
    
}
