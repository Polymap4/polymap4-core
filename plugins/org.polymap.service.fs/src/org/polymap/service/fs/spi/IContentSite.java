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

import java.util.Locale;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.runtime.SessionContext;

/**
 * Provides the context of an {@link IContentProvider}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentSite {

    public IContentFolder getFolder( IPath path );
    
    public Iterable<IContentNode> getChildren( IPath path );
    
    public Object put( String key, Object value );
    
    public Object get( String key );
    
    public Locale getLocale();

    public String getUserName();

    /**
     * Invalidates the children of the given folder. The next time the content of the
     * folder is requested, it is re-created by asking the {@link IContentProvider}.
     */
    public void invalidateFolder( IContentFolder node );
    
    public void invalidateSession();
    
    /**
     * Returns the corresponding {@link SessionContext}.
     */
    public SessionContext getSessionContext();

}
