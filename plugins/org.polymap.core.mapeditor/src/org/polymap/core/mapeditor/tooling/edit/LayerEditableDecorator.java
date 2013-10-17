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
package org.polymap.core.mapeditor.tooling.edit;

import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.mapeditor.tooling.select.LayerSelectableDecorator;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class LayerEditableDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, PropertyChangeListener {

    private static Log log = LogFactory.getLog( LayerEditableDecorator.class );
    
    private static final int            QUADRANT = LayerSelectableDecorator.QUADRANT;

    public static final String          imageEdit = "icons/ovr16/editable_ovr.gif";
    public static final String          imageDigitize = "icons/ovr16/digitizable_ovr.png";

    private Set<String>                 editableLayerIds = new HashSet();
    private Set<String>                 digitizableLayerIds = new HashSet();
    
    
    public LayerEditableDecorator() {
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
            
            if (editableLayerIds.contains( layer.id() )) {
                ImageDescriptor ovr = MapEditorPlugin.imageDescriptor( imageEdit );
                decoration.addOverlay( ovr, QUADRANT );
            }
            if (digitizableLayerIds.contains( layer.id() )) {
                ImageDescriptor ovr = MapEditorPlugin.imageDescriptor( imageDigitize );
                decoration.addOverlay( ovr, QUADRANT );
            }
        }
    }


    public void propertyChange( PropertyChangeEvent ev ) {
        boolean changed = false;
        
        // edit
        if (ev.getSource() instanceof EditTool) {
            ILayer layer = (ILayer)ev.getNewValue();

            if (ev.getPropertyName().equals( EditTool.PROP_LAYER_ACTIVATED )) {
                changed = editableLayerIds.add( layer.id() );
            }
            else if (ev.getPropertyName().equals( EditTool.PROP_LAYER_DEACTIVATED )) {
                changed = editableLayerIds.remove( layer.id() );
            }
        }
        // digitize
        else if (ev.getSource() instanceof DigitizeTool) {
            ILayer layer = (ILayer)ev.getNewValue();

            if (ev.getPropertyName().equals( EditTool.PROP_LAYER_ACTIVATED )) {
                changed = digitizableLayerIds.add( layer.id() );
            }
            else if (ev.getPropertyName().equals( EditTool.PROP_LAYER_DEACTIVATED )) {
                changed = digitizableLayerIds.remove( layer.id() );
            }
        }

        if (changed) {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (!PlatformUI.getWorkbench().isClosing()) {
                        fireLabelProviderChanged( new LabelProviderChangedEvent( LayerEditableDecorator.this ) );
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
