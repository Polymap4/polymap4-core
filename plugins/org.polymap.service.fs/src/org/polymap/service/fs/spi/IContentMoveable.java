/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

/**
 * Files or folders that are moveable to other folders should implement this
 * interface.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentMoveable
        extends IContentNode {
    
    /**
     * Move this resource th the given destination path and name.
     * <p/>
     * The method is responsible of calling
     * {@link IContentSite#invalidateFolder(IContentFolder)} on folders that content
     * has changed during this method.
     *
     * @param dest
     * @param newName
     * @throws IOException
     * @throws BadRequestException
     */
    void moveTo( IPath dest, String newName )
    throws IOException, BadRequestException;

}
