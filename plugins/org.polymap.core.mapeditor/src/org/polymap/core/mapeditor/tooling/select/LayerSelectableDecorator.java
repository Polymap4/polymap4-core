/* 
 * polymap.org
 * Copyright 2011-2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.select;

import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.PlatformUI;
import org.polymap.core.mapeditor.tooling.edit.BaseLayerEditorTool;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class LayerSelectableDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, PropertyChangeListener {

    private static Log log = LogFactory.getLog( LayerSelectableDecorator.class );
    
    public static final Point           DEFAULT_SIZE = new Point( 17, 16 );

    public static final int             QUADRANT = IDecoration.TOP_RIGHT;

    public static final String          image = "icons/ovr16/selectable_ovr_small.png";

    private Set<String>                 selectableLayerIds = new HashSet();
    
    
    public LayerSelectableDecorator() {
        BaseLayerEditorTool.sessionTools().addListener( this );
    }
    
    public void dispose() {
        BaseLayerEditorTool.sessionTools().removeListener( this );
        super.dispose();
    }


    @Override
    protected void finalize() throws Throwable {
        dispose();
    }


    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;

            // layer removed?
            try { layer.id(); } catch (NoSuchEntityException e) { return; }
            
            if (selectableLayerIds.contains( layer.id() )) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, image );
                decoration.addOverlay( ovr, QUADRANT );
            }
        }
    }


    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getSource() instanceof SelectionTool) {
            ILayer layer = (ILayer)ev.getNewValue();

            if (ev.getPropertyName().equals( SelectionTool.PROP_LAYER_ACTIVATED )) {
                selectableLayerIds.add( layer.id() );
            }
            else if (ev.getPropertyName().equals( SelectionTool.PROP_LAYER_DEACTIVATED )) {
                selectableLayerIds.remove( layer.id() );
            }

            Runnable runnable = new Runnable() {
                public void run() {
                    // prevent deadlock on close
                    if (!PlatformUI.getWorkbench().isClosing()) {
                        fireLabelProviderChanged( new LabelProviderChangedEvent( LayerSelectableDecorator.this ) );
                    }
                }
            };
            if (Display.getCurrent() != null) {
                runnable.run();
            } else {
                Polymap.getSessionDisplay().asyncExec( runnable );
            }
        }
    }

}
