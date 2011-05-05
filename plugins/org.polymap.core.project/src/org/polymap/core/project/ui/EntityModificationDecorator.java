/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.project.ui;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.internal.util.BundleUtility;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.model.Entity;
import org.polymap.core.model.event.GlobalModelChangeEvent;
import org.polymap.core.model.event.GlobalModelChangeListener;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.event.EntityChangeStatus;
import org.polymap.core.qi4j.event.ModelChangeSupport;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class EntityModificationDecorator
        implements ILightweightLabelDecorator, GlobalModelChangeListener {

    private static final Log log = LogFactory.getLog( EntityModificationDecorator.class );
    
    private static final ImageDescriptor locallyModified =  
            ImageDescriptor.createFromFile( EntityModificationDecorator.class, "icons/sample_decorator.gif");

    private IConfigurationElement       configElement;

    private ImageDescriptor             dirtyImage, pendingImage, warningImage, conflictImage;
    
    private ListenerList                listeners = new ListenerList( ListenerList.IDENTITY );

    private ProjectRepository           module;
    
    private Display                     display;
    
    
    public void dispose() {
        listeners.clear();
        if (module != null) {
            module.removeGlobalModelChangeListener( this );
            module = null;
            display = null;
        }
    }

    public void addListener( ILabelProviderListener listener ) {
        log.debug( "add: listener=" + listener );
        listeners.add( listener );
    }

    public void removeListener( ILabelProviderListener listener ) {
        log.debug( "add: listener=" + listener );
        listeners.add( listener );
    }

    protected void fireEvent() {
        LabelProviderChangedEvent ev = new LabelProviderChangedEvent( this );
        for (Object l : listeners.getListeners()) {
            ((ILabelProviderListener)l).labelProviderChanged( ev );
        }
    }
    
    
    public void decorate( Object elm, IDecoration decoration ) {
        // init
        if (dirtyImage == null) {
            URL url = BundleUtility.find( ProjectPlugin.PLUGIN_ID, "icons/ovr16/write_ovr.gif" );
            assert (url != null) : "No image found.";
            dirtyImage = ImageDescriptor.createFromURL( url );
            
            url = BundleUtility.find( ProjectPlugin.PLUGIN_ID, "icons/ovr16/changed_ovr.gif" );
            assert (url != null) : "No image found.";
            pendingImage = ImageDescriptor.createFromURL( url );
            
            url = BundleUtility.find( ProjectPlugin.PLUGIN_ID, "icons/ovr16/warning_ovr.gif" );
            assert (url != null) : "No image found.";
            warningImage = ImageDescriptor.createFromURL( url );
            
            url = BundleUtility.find( ProjectPlugin.PLUGIN_ID, "icons/ovr16/error_ovr.gif" );
            assert (url != null) : "No image found.";
            conflictImage = ImageDescriptor.createFromURL( url );
            
            display = Display.getDefault();
            
            module = ProjectRepository.instance();
            module.addGlobalModelChangeListener( this );
        }
        
        Entity entity = (Entity)elm;
        log.debug( "### Decorating: entity=" + entity.id() );
        EntityChangeStatus entityState = EntityChangeStatus.forEntity( (ModelChangeSupport)entity );
        
        boolean dirty = entityState.isLocallyChanged();
        boolean pendingChanges = entityState.isGloballyChanged();
        boolean pendingCommits = entityState.isGloballyCommitted();

        if (dirty && pendingCommits) {
            decoration.addOverlay( conflictImage, IDecoration.BOTTOM_RIGHT );
        }
        else if (dirty && pendingChanges) {
            decoration.addOverlay( warningImage, IDecoration.BOTTOM_RIGHT );
        }
        else if (dirty && !pendingChanges && !pendingCommits) {
            decoration.addOverlay( dirtyImage, IDecoration.BOTTOM_RIGHT );
        }
        else if (!dirty && (pendingChanges || pendingCommits)) {
            decoration.addOverlay( pendingImage, IDecoration.BOTTOM_RIGHT );
        }
    }

    // GlobalModelChangeListener
    
    public boolean isValid() {
        return true;
    }

    public void modelChanged( final GlobalModelChangeEvent ev ) {
        log.debug( "Global change: ev= " + ev );
        
        // make sure that display is not disposed and our session is still valid
        if (display == null) {
            return;
        }
        if (display.isDisposed()) {
            dispose();
        }
        else {
            display.asyncExec( new Runnable() {
                public void run() {
                    log.warn( "Skipping global event: " + ev );
//                    try {
//                        fireEvent();
//                    }
//                    catch (Throwable e) {
//                        log.info( "Error while modelChange(): " + e.getLocalizedMessage() );
//                    }
                }
            });
        }
    }
    

    public boolean isLabelProperty( Object element, String property ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
