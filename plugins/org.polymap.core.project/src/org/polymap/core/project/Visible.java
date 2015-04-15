/* 
 * polymap.org
 * Copyright (C) 2013-2015, Polymap GmbH. ALl rights reserved.
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
package org.polymap.core.project;

import org.polymap.model2.Composite;
import org.polymap.model2.Defaults;
import org.polymap.model2.Property;

/**
 * Provides interface and mixin to give entities a label. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public class Visible
        extends Composite {

    @Defaults
    public Property<Boolean>    visible;
    
}
