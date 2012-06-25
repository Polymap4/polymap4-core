/*
 * polymap.org Copyright 2012, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.eclipse.rwt.widgets.codemirror;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Represents a line marker with possible gutter icon and text style/colors. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ILineMarker {

    public String getId();
    
    public String getText();

    public int getLine();

    public int getCharStart();

    public int getCharEnd();

    public Image getIcon();

    public Color getBgColor();

    public Color getFgColor();

    public Color getUnderlineColor();

}