/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.navigator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class LayerLabelProvider
        extends LabelProvider
        implements ILabelProvider {

    private Log log = LogFactory.getLog( LayerLabelProvider.class );


    public Image getImage( Object elm ) {
        String imageKey = elm instanceof IMap 
                ? ISharedImages.IMG_OBJ_FOLDER 
                : ISharedImages.IMG_OBJ_ELEMENT;
        return PlatformUI.getWorkbench().getSharedImages().getImage( imageKey );
    }


    public String getText( Object elm ) {
        if (elm instanceof ILayer) {
            return ((ILayer)elm).getLabel();
        }
        else if (elm instanceof IMap) {
            return ((IMap)elm).getLabel();
        }
        else {
            log.warn( "Element is not instanceof ILayer/IMap: " + elm );
            return elm.toString();
        }
    }

}
