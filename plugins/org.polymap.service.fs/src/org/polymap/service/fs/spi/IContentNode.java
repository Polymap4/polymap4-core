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

import java.util.Date;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.runtime.cache.Cache;

/**
 * Base SPI of all content nodes.
 * <p/>
 * Content nodes are stored in a {@link Cache}. They are subject to be reclaimed
 * by the GC. Implementations should carefully return its size in memory via
 * {@link #getSizeInMemory()} in order to help the global content cache.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentNode {
    
    /**
     * Note that this name MUST be consistent with URL resolution in your
     * ResourceFactory
     * <p/>
     * If they aren't consistent Milton will generate a different href in PropFind
     * responses then what clients have request and this will cause either an error
     * or no resources to be displayed
     * 
     * @return - the name of this resource. Ie just the local name, within its folder
     */
    public String getName();

    public IPath getPath();

    /**
     * The date and time that this resource, or any part of this resource, was last
     * modified. For dynamic rendered resources this should consider everything which
     * will influence its output.
     * <p/>
     * Resources for which no such date can be calculated should return null.
     * <P/>
     * This field, if not null, is used by the font end systems to produce optimized
     * replies in case the resource has not modified since last request and/or
     * caching.
     * <p/>
     * Although nulls are explicitly allowed, certain front end systems and/or client
     * applications might require modified dates for file browsing. For example, the
     * command line client on Vista doesn't work properly with WebDAV server if this
     * is null.
     */
    public Date getModifiedDate();

    
    /**
     * How many seconds to allow the content to be cached on the client side, or null
     * if caching is not allowed.
     */
    public Long getMaxAgeSeconds();

    public IContentProvider getProvider();
    
    public Object getSource();

    public Object putData( String key, Object value );
    
    public Object getData( String key );

    /**
     * The approximate size of this node in memory. This value is used for cache
     * management. This method is called right after the node is created.
     */
    public int getSizeInMemory();
    
    public void dispose();
    
    /**
     * This method is called by the engine before this node is returned from the
     * cache.
     * 
     * @return True if this (currently cached) instance is still valid.
     */
    public boolean isValid();
    
}
