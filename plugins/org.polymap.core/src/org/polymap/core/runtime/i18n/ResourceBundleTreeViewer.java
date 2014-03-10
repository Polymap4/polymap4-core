/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.runtime.i18n;

import static org.apache.commons.lang.StringUtils.substringAfterLast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ResourceBundleTreeViewer
        extends TreeViewer {

    private static Log log = LogFactory.getLog( ResourceBundleTreeViewer.class );

    
    public ResourceBundleTreeViewer( Composite parent, int style ) {
        super( parent, style );
        setLabelProvider( new ResourceBundleLabelProvider() );
        setContentProvider( new ResourceBundleModel() );
    }

    
    @Override
    public ResourceBundleModel getContentProvider() {
        return (ResourceBundleModel)super.getContentProvider();
    }


    public String getEntryValue( String key ) {
        return getContentProvider().getEntryValue( key );
    }
    
    
    public void setEntryValue( String key, String value ) {
        getContentProvider().setEntryValue( key, value );
    }


    /**
     * 
     */
    class ResourceBundleLabelProvider
            extends ColumnLabelProvider
            implements ILabelProvider {

        @Override
        public String getText( Object elm ) {
            String key = (String)elm;
            key = key.startsWith( "@" ) ? key.substring( 1 ) : key;
            String sep = getContentProvider().getKeySeparator();
            return key.contains( sep ) ? substringAfterLast( key, sep ) : key;
        }

        @Override
        public Font getFont( Object elm ) {
            return elm != null && !((String)elm).startsWith( "@" )
                    ? JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) : null;
        }

        @Override
        public Image getImage( Object elm ) {
            String key = (String)elm;
            String imageKey = key.startsWith( "@" ) ? ISharedImages.IMG_OBJ_FOLDER : null; //ISharedImages.IMG_OBJ_ELEMENT;
            return PlatformUI.getWorkbench().getSharedImages().getImage( imageKey );
        }
    }
    
    
//    /**
//     * 
//     */
//    class NameSorter
//            extends ViewerSorter {
//
//        @Override
//        public int compare( Viewer viewer, Object elm1, Object elm2 ) {
//            if (elm1 instanceof Labeled
//                    && elm2 instanceof Labeled) {
//                return ((Labeled)elm1).getLabel().compareToIgnoreCase( ((Labeled)elm2).getLabel() );
//            }
//            return 0;
//        }
//
//        public int category( Object elm ) {
//            if (elm instanceof IMap) {
//                return 0;
//            }
//            else if (elm instanceof ILayer) {
//                return 1;
//            }
//            else {
//                return 10;
//            }
//        }
//    }

}
