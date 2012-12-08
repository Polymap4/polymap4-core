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
 */
package org.polymap.core.project.ui;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.core.runtime.IConfigurationElement;
import org.polymap.core.model.event.IEventFilter;
import org.polymap.core.model.event.IModelChangeListener;
import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.ModelStoreEvent;
import org.polymap.core.model.event.ModelStoreEvent.EventType;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.event.EntityChangeStatus;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class EntityModificationDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, IModelStoreListener, IModelChangeListener {

    private static final Log log = LogFactory.getLog( EntityModificationDecorator.class );
    
    private static final String         dirtyImage = "icons/ovr16/outgo_synch3.gif";
    private static final String         pendingImage = "icons/ovr16/changed_ovr.gif";
    private static final String         warningImage = "icons/ovr16/warning_ovr.gif";
    private static final String         conflictImage = "icons/ovr16/error_ovr.gif";

    private IConfigurationElement       configElement;

    private ProjectRepository           module;
    
    private Map<String,ModelChangeSupport> decorated = new HashMap();

    private Display                     display;
    

    public EntityModificationDecorator() {
        display = Polymap.getSessionDisplay();
        module = ProjectRepository.instance();
        
        module.addModelStoreListener( this );
        
        module.addModelChangeListener( this, new IEventFilter() {
            public boolean accept( EventObject ev ) {
                if (ev.getSource() instanceof ModelChangeSupport) {
                    ModelChangeSupport entity = (ModelChangeSupport)ev.getSource();
                    return decorated.containsKey( entity.id() );
                }
                return false;
            }
        });
    }

    public void dispose() {
        if (module != null) {
            try {
                super.dispose();
                decorated = null;
                module.removeModelChangeListener( this );
                module.removeModelStoreListener( this );
            }
            finally {
                module = null;
            }
        }
    }

    public void decorate( Object elm, IDecoration decoration ) {
        try {
            ModelChangeSupport entity = (ModelChangeSupport)elm;
            EntityChangeStatus entityState = EntityChangeStatus.forEntity( entity );
            
            boolean dirty = entityState.isDirty();
            boolean pendingConflict = entityState.isConcurrentlyDirty();
            boolean conflicting = entityState.isConflicting();

            if (dirty && conflicting) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, conflictImage );
                decoration.addOverlay( ovr, IDecoration.BOTTOM_RIGHT );
                decoration.addPrefix( "# " );
            }
            else if (!dirty && conflicting) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, warningImage );
                //decoration.addOverlay( ovr, IDecoration.BOTTOM_RIGHT );
                decoration.addPrefix( "< " );
            }
            else if (dirty) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, dirtyImage );
                decoration.addOverlay( ovr, IDecoration.BOTTOM_RIGHT );
                decoration.addPrefix( "> " );
            }

            // register
            decorated.put( entity.id(), entity );
        }
        catch (NoSuchEntityException e) {
            decoration.addSuffix( " (deleted)" );
        }
    }

    
    // model listeners ************************************
    
    public void modelChanged( ModelChangeEvent ev ) {
        modelChanged( (ModelStoreEvent)null );
    }
    
    public boolean isValid() {
        return true;
    }

    public void modelChanged( final ModelStoreEvent ev ) {
        // make sure that display is not disposed and our session is still valid
        if (module == null) {
            return;
        }
        else if (display.isDisposed()) {
            dispose();
        }
        else {
            // just listen to COMMIT events; CHANGE events are handled by
            // LayerNavigator and other views directly; firing labelChange on CHANGE
            // causes a race condition and throws exception for deleted entities (layer)
            if (ev != null && ev.getEventType() == EventType.COMMIT) {
                display.asyncExec( new Runnable() {
                    public void run() {
                        try {
                            fireLabelProviderChanged( new LabelProviderChangedEvent( 
                                    EntityModificationDecorator.this ) );
                        }
                        catch (Exception e) {
                            log.error( "", e );
                        }
                    }
                });
            }
        }
    }


//    /**
//     * Overrides the default implementation in order to catch
//     * {@link NoSuchEntityException} if entity was deleted right before.
//     */
//    protected void fireLabelProviderChanged( final LabelProviderChangedEvent ev ) {
//        for (final Object l : getListeners()) {
//            try {
//                ((ILabelProviderListener)l).labelProviderChanged( ev );
//            }
//            catch (OperationCanceledException e) {
//                log.info( "Operation canceled.", e );
//            }
//            catch (NoSuchEntityException e) {
//                log.info( "Entity deleted.", e );                
//            }
//            catch (Throwable e) {
//                Policy.getLog().log( 
//                        new Status( IStatus.ERROR, Policy.JFACE, IStatus.ERROR, "Exception occurred", e ) );
//            }
//        }
//    }

}
