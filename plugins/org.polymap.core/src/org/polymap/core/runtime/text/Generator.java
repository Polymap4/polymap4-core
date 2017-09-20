/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.runtime.text;

import java.io.Writer;

import org.polymap.core.runtime.text.TextBuilder.Element;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class Generator {

    public abstract void init( Writer out );
    
    public abstract void begin( Element elm, CharSequence s );
    
    public abstract void end( Element elm );
    
}
