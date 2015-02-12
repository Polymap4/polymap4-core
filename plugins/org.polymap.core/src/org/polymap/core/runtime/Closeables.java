/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Closeables {

    private static Log log = LogFactory.getLog( Closeables.class );
    
    
    public static <E extends Throwable> void close( AutoCloseable closeable ) throws E {
        try {
            if (closeable != null) {
                closeable.close();
            }
        }
        catch (Throwable e) {
            // XXX not quite sure what's happening here :)
            throw (E)e;
        }
    }
    
}
