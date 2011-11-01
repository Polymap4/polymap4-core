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

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentPutable
        extends IContentFolder {

    /**
     * Create a new resource, or overwrite an existing one
     *
     * @param newName - the name to create within the collection. E.g. myFile.txt
     * @param inputStream - the data to populate the resource with
     * @param length - the length of the data
     * @param contentType - the content type to create
     * @return A reference to the new resource
     * @throws IOException
     * @throws NotAuthorizedException
     */
    IContentFile createNew( String newName, InputStream inputStream, Long length, String contentType )
    throws IOException, NotAuthorizedException, BadRequestException;

}
