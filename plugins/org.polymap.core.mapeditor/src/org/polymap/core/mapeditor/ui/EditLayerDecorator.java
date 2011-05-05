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
package org.polymap.core.mapeditor.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.internal.lifecycle.RWTLifeCycle;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.model.event.SourceClassPropertyEventFilter;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.ListenerList;

/**
 * Decorate the {@link ILayer} that is currently edited.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@SuppressWarnings("restriction")
public class EditLayerDecorator
        implements ILightweightLabelDecorator, PropertyChangeListener {

    private static Log log = LogFactory.getLog( EditLayerDecorator.class );
    
    private ListenerList                listeners = new ListenerList( ListenerList.IDENTITY );

    private Font                        font;
    
    private ProjectRepository           module;

    private Display                     display;

    private Color                       color;

    
    public EditLayerDecorator() {
        display = RWTLifeCycle.getSessionDisplay();
        display.syncExec( new Runnable() {
            public void run() {
                color = display.getSystemColor( SWT.COLOR_YELLOW );

                Font systemFont = display.getSystemFont();
                FontData fd = systemFont.getFontData()[0];
                font = Graphics.getFont( fd.getName(), fd.getHeight(), SWT.BOLD );
                //font = new Font( systemFont.getDevice(), fd.getName(), fd.getHeight(), SWT.BOLD );
            }
        });

        module = ProjectRepository.instance();
        module.addPropertyChangeListener( this, new SourceClassPropertyEventFilter( ILayer.class ) );
    }
    

    public void decorate( Object elm, IDecoration deco ) {
        log.debug( "elm= " + elm );
        
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;
            if (layer.isEditable()) {
                deco.addPrefix( "* " );
                deco.setBackgroundColor( color );
                deco.setFont( font );
            }
            else if (layer.isSelectable()) {
                deco.addPrefix( "> " );
                deco.setBackgroundColor( color );
                deco.setFont( font );
            }
        }
    }

    
    public void dispose() {
        if (font != null) {
            //font.dispose();
            font = null;
        }
        if (module != null) {
            module.removePropertyChangeListener( this );
            module = null;
            display = null;
        }
    }

    public void propertyChange( final PropertyChangeEvent ev ) {
        log.debug( "propertyChange(): ev= " + ev );
        display.asyncExec( new Runnable() {
            public void run() {
                log.info( "Skipping event: " + ev + "!" );
//                try {
//                    ILayer layer = (ILayer)ev.getSource();
//                    // was layer removed?
//                    layer.id();
//                    fireEvent();
//                }
//                catch (NoSuchEntityException e) {
//                    log.info( "Entity was removed: " + e.getLocalizedMessage() );
//                }
            }
        });
    }

    public boolean isLabelProperty( Object element, String property ) {
        log.debug( "property= " + property );
        return true;
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

}
