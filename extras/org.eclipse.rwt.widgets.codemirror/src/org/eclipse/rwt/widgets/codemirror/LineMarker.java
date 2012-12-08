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
package org.eclipse.rwt.widgets.codemirror;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LineMarker implements ILineMarker {

    private static Log log = LogFactory.getLog( LineMarker.class );
    
    protected String        id;
    
    private int             line = 0;
    
    private String          text = "";
    
    private int             charStart = 0, charEnd = 0;
    
    private Color           fgColor = null, bgColor = null;
    
    private Color           underlineColor = null;
    
    private Image           icon = null;


    /**
     * Construct a new instance with default values for all fields.
     */
    public LineMarker( String id ) {
        this.id = id;
    }

    public LineMarker setCharPos( int charStart, int charEnd ) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        return this;
    }
    
    public String getId() {
        return id;
    }

    public int getCharStart() {
        return charStart;
    }

    public int getCharEnd() {
        return charEnd;
    }

    public Color getBgColor() {
        return bgColor;
    }
    
    public ILineMarker setBgColor( Color bgColor ) {
        this.bgColor = bgColor;
        return this;
    }
    
    public Color getUnderlineColor() {
        return underlineColor;
    }
    
    public ILineMarker setUnderlineColor( Color underlineColor ) {
        this.underlineColor = underlineColor;
        return this;
    }

    public LineMarker setLine( int line ) {
        this.line = line;
        return this;
    }
    
    public LineMarker setText( String text ) {
        this.text = text;
        return this;
    }
    
    public ILineMarker setFgColor( Color fgColor ) {
        this.fgColor = fgColor;
        return this;
    }
    
    public ILineMarker setIcon( Image icon ) {
        this.icon = icon;
        return this;
    }

    public int getLine() {
        return line;
    }
    
    public String getText() {
        return text;
    }
    
    public Color getFgColor() {
        return fgColor;
    }

    public Image getIcon() {
        return icon;
    }

}
