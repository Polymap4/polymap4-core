/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
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
package org.polymap.rhei.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version ($Revision$)
 */
public class DefaultFormFieldDecorator
        implements IFormFieldDecorator, IFormFieldListener {

    private static Log log = LogFactory.getLog( DefaultFormFieldDecorator.class );

    private static final Image  focusImage, dirtyImage, invalidImage;
    
    private IFormFieldSite      site;
    
    private Label               label;
    
    private boolean             dirty, focus, invalid;

    private Composite           contents;
    

    static {
        dirtyImage = ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/elcl16/field_dirty3.gif" ) ).createImage();
        focusImage = ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/elcl16/field_dirty2.gif" ) ).createImage();
        invalidImage = ImageDescriptor.createFromURL( 
                RheiPlugin.getDefault().getBundle().getResource( "icons/elcl16/field_invalid.gif" ) ).createImage();
    }
    
    public void init( IFormFieldSite _site ) {
        this.site = _site;    
    }

    public void dispose() {
        if (site != null) {
            site.removeChangeListener( this );
        }
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        contents = toolkit.createComposite( parent, SWT.NONE );
        RowLayout layout = new RowLayout( SWT.HORIZONTAL );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.spacing = 0;
        layout.pack = false;
        layout.wrap = false;
        contents.setLayout( layout );

        label = toolkit.createLabel( contents, "", SWT.NO_FOCUS );
        
        this.invalid = site.getErrorMessage() != null;
        updateUI();
        
        site.addChangeListener( this );
        contents.pack( true );
        return contents;
    }

    protected void updateUI() {
        label.setImage( null );
        if (focus) {
            label.setImage( focusImage );            
        }
        if (dirty) {
            label.setImage( dirtyImage );
            try {
                label.setToolTipText( dirty ? "Dieser Wert wurde ge�ndert. Originalwert: " + site.getFieldValue() : "" );
            }
            catch (Exception e) {
                log.warn( e );
                label.setToolTipText( dirty ? "Dieser Wert wurde ge�ndert. Originalwert kann nicht ermittelt werden." : "" );
            }
        }
        if (invalid) {
            label.setImage( invalid ? invalidImage : null );
            label.setToolTipText( invalid ? site.getErrorMessage() : "" );
        }
        contents.pack( true );
    }
    
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == VALUE_CHANGE) {
            dirty = site.isDirty();
        }
        if (ev.getEventCode() == FOCUS_GAINED) {
            focus = true;
        }
        if (ev.getEventCode() == FOCUS_LOST) {
            focus = false;
        }
        
        invalid = site.getErrorMessage() != null;
        
        log.debug( "fieldChange(): dirty= " + dirty + ", focus= " + focus + ", invalid=" + invalid );
        updateUI();
    }

}
