/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.edit;

import org.eclipse.swt.widgets.Control;

import org.polymap.core.mapeditor.tooling.IEditorTool;
import org.polymap.core.mapeditor.tooling.IEditorToolSite;

/**
 * A {@link IEditorTool tool} may implement this interface if it wants to use
 * a VectorLayerStyler to contribute to its interface. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface VectorLayerStylerAware {

    public IEditorToolSite getSite();

    public void layoutControl( String label, Control composite );

}
