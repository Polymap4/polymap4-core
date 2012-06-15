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
package org.eclipse.rwt.widgets.codemirror.internal;

import java.io.IOException;

import org.eclipse.rwt.lifecycle.JSWriter;
import org.eclipse.rwt.widgets.codemirror.CodeMirror;

/**
 * This command calls the given method of the given widget on the client side.
 */
public class SetPropertyCommand
        implements RenderCommand {

    private String          propName;
    
    private Object          value;
    
    
    public SetPropertyCommand( String propName, String value ) {
        this.propName = propName;
        this.value = value;
    }

    public SetPropertyCommand( String propName, String[] values ) {
        this.propName = propName;
        this.value = values;
    }

    public void renderChanges( CodeMirror widget, JSWriter writer ) 
    throws IOException {
        writer.set( propName, value );
    }
    
}
