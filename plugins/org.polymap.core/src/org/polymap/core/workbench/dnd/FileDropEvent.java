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
package org.polymap.core.workbench.dnd;

import java.io.IOException;
import java.io.InputStream;

/**
 * Signals that a file was dropped from another application on the
 * desktop. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FileDropEvent
        extends DesktopDropEvent {

    public abstract String getFileName();
    
    public abstract String getContentType();
    
    public abstract InputStream getInputStream() throws IOException;
}
