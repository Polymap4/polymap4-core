/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.workbench;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import org.polymap.core.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PolymapWorkbenchWindowAdvisor
        extends WorkbenchWindowAdvisor {

    public PolymapWorkbenchWindowAdvisor( final IWorkbenchWindowConfigurer configurer ) {
        super( configurer );
    }


    public ActionBarAdvisor createActionBarAdvisor( final IActionBarConfigurer configurer ) {
        return new PolymapActionBarAdvisor( configurer );
    }


    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize( new Point( 8000, 6000 ) );
        configurer.setTitle( Messages.get( "PolymapWorkbenchWindowAdvisor_Title" ) /*"POLYMAP3 Workbench"*/ ); //$NON-NLS-1$
        configurer.setShellStyle( SWT.TITLE /*| SWT.MAX | SWT.RESIZE*/ );

        configurer.setShowCoolBar( true );
        configurer.setShowPerspectiveBar( true );
        configurer.setShowProgressIndicator( true );
        configurer.setShowMenuBar( false );
        configurer.setShowFastViewBars( true );
        configurer.setShowStatusLine( true );
    }


    public void postWindowOpen() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        Shell shell = window.getShell();
        shell.setMaximized( true );
    }

}
