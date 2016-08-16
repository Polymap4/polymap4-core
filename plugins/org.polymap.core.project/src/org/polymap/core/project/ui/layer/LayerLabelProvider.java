/* 
 * polymap.org
 * Copyright (C) 2010-2015 Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui.layer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.ui.PlatformUI;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectNode;
import org.polymap.core.project.ProjectPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerLabelProvider
        extends DecoratingLabelProvider {
        //implements ILabelProvider, IFontProvider {

    private static final Log log = LogFactory.getLog( LayerLabelProvider.class );


    public LayerLabelProvider() {
        super( new BaseLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator() );
    }


    /*
     * 
     */
    static class BaseLabelProvider
            extends ColumnLabelProvider {
            
        public Image getImage( Object elm ) {
            if (elm instanceof ILayer) {
                Image result = ProjectPlugin.images().image( "icons/obj16/layer_disabled_obj.gif" );
                return result;
            }
            return null;
        }


        @Override
        public String getToolTipText( Object element ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }


        public String getText( Object elm ) {
            if (elm instanceof ProjectNode) {
                return ((ProjectNode)elm).label.get();
            }
            else {
                return elm.toString();
            }
        }
        
    }

}
