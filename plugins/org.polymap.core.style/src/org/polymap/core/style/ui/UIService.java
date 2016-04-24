/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.ui;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Plugin;

import org.polymap.core.style.StylePlugin;

/**
 * Generell application level UI abstractions.
 * <p/>
 * Implementing bundle must register the implementation with its
 * {@link BundleContext} on {@link Plugin#start(BundleContext)}.
 *
 * @author Falko Bräutigam
 */
public abstract class UIService {

    /**
     * Returns the instance for the current session.    
     */
    public static UIService instance() {
        BundleContext bundleContext = StylePlugin.instance().getBundle().getBundleContext();
        ServiceReference<UIService> serviceRef = bundleContext.getServiceReference( UIService.class );
        if (serviceRef != null) {
            UIService service = bundleContext.getService( serviceRef );
            return service;
        }
        else {
            throw new IllegalStateException( "No " + UIService.class.getName() + " was registered. (See javadoc for detail)" );
        }
    }

    
    // API ************************************************
    
    /**
     * Creates and opens a 'Ok' dialog. A dialog is a short-living, probably
     * blocking, UI component that is used to gather information from the user.
     * <p/>
     * <b>Example:</b>
     * <pre>
     * UIService.instance().openDialog( "Test", 
     *         dialogParent -> {
     *             new Label( dialogParent, SWT.NONE ).setText( "Message" );
     *         }, 
     *         () -> {
     *             log.info( "Perform ok task..." );
     *             return true;
     *         } );
     * </pre>
     *
     * @param title The title of the dialog.
     * @param contents Creates the contents of the dialog. The given
     *        {@link Composite} has {@link FillLayout} set as default Layout.
     * @param okAction The task to perform when 'Ok' is pressed. The dialog is closed
     *        afterwards if this task returns {@link Boolean#TRUE} or null.
     */
    public abstract void openDialog( String title, Consumer<Composite> contents, Callable<Boolean> okAction );
    
}
