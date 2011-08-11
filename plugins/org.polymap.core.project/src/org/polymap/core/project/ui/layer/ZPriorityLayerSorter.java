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
package org.polymap.core.project.ui.layer;

import java.text.Collator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ZPriorityLayerSorter
        extends ViewerSorter {

    private static Log log = LogFactory.getLog( ZPriorityLayerSorter.class );


    public ZPriorityLayerSorter() {
    }


    public ZPriorityLayerSorter( Collator collator ) {
        super( collator );
    }


    public int compare( Viewer viewer, Object e1, Object e2 ) {
        ILayer layer1 = (ILayer)e1;
        ILayer layer2 = (ILayer)e2;
        return layer2.getOrderKey() - layer1.getOrderKey();
    }
    
}
