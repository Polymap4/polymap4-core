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

package org.polymap.rhei.internal.filter;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.internal.util.BundleUtility;

import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.filter.IFilter;


/**
 * 
 * @see FilterContentProvider
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class FilterLabelProvider
        extends LabelProvider
        implements ILabelProvider {

    private Log log = LogFactory.getLog( FilterLabelProvider.class );

    private Image           filtersImage, filterImage;
    
    
    public String getText( Object elm ) {
        // folder
        if (elm instanceof FiltersFolderItem) {
            return Messages.get( "FiltersFolder_name" );
        }
        // filter
        else if (elm instanceof IFilter) {
            return ((IFilter)elm).getLabel();
        }
        // unknown
        else {
            log.warn( "Unknown element type: " + elm );
            return elm.toString();
        }
    }


    public Image getImage( Object elm ) {
        // init images
        if (filtersImage == null) {
            URL url = BundleUtility.find( RheiPlugin.PLUGIN_ID, "icons/elcl16/filter_ps.gif" );
            assert (url != null) : "No image found.";
            filtersImage = ImageDescriptor.createFromURL( url ).createImage();
        }
        
        // folder
        if (elm instanceof FiltersFolderItem) {
            return filtersImage;
        }
        // unknown element type
        else {
            return null;
        }
    }


    public void dispose() {
        // factory-created resource cannot be disposed
        filtersImage = null;
    }

}
