/* 
 * polymap.org
 * Copyright 2010, 2011 Polymap GmbH. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.PlatformUI;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
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
            extends LabelProvider {
            
        public Image getImage( Object elm ) {
            if (elm instanceof ILayer) {
//                LayerIconImageDescriptor imageDescr = new LayerIconImageDescriptor();
//                Image result = RheiPlugin.getDefault().imageForDescriptor( imageDescr, "layerIcon" );
                Image result = ProjectPlugin.getDefault().imageForName( "icons/obj16/layer_disabled_obj.gif" );
                return result;
            }
            return null;
        }


        public String getText( Object elm ) {
            try {
                if (elm instanceof ILayer) {
                    return ((ILayer)elm).getLabel();
                }
                else if (elm instanceof IMap) {
                    return ((IMap)elm).getLabel();
                }
                else {
                    return elm.toString();
                }
            }
            catch (NoSuchEntityException e) {
                return "<deleted>";
            }
        }
        
    }

}
