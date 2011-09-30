/* 
 * polymap.org
 * Copyright 201, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag.
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
 * $Id:$
 */
package org.polymap.core.help;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.RWT;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.help.AbstractHelpUI;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IToc;

import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class HelpUI
        extends AbstractHelpUI {

    private static Log log = LogFactory.getLog( HelpUI.class );

    public static final String  HELP_SERVER_URL = "http://polymap.org/polymap3/wiki/UserGuide";
        
    private Shell               window;
    
    private Browser             browser;
    
    
    public HelpUI() {
    }


    public void displayHelp() {
        for (IToc toc : HelpSystem.getTocs()) {
            log.debug( "TOC: " + toc.getLabel() );
        }
        openHelpWindow( HELP_SERVER_URL + "/Start" );
    }


    public void displaySearch() {
        openHelpWindow( "http://polymap.org/polymap3/search" );
    }


    public void displayHelpResource( String href ) {
        log.debug( "displayHelpResource(): href= " + href );
        openHelpWindow( getBaseUrl( "/topic" + href ) );
    }


    public boolean isContextHelpDisplayed() {
        return false;
      }


    public void displayDynamicHelp() {
        super.displayDynamicHelp();
        displayHelp();
    }


    public void displayContext( IContext context, int x, int y ) {
        log.debug( "displayContext(): context= " + context );
        String text = context.getText();
        MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(), "Context Help", text );
    }


    private static String getBaseUrl( String path ) {
        String helpURL = "http://{0}:{1}" + path;
        Object[] param = new Object[] { RWT.getRequest().getServerName(),
                String.valueOf( RWT.getRequest().getServerPort() ) };
        return MessageFormat.format( helpURL, param );
    }


    private void openHelpWindow( String url ) {
        if (window == null || window.isDisposed()) {
            Shell parentShell = PolymapWorkbench.getShellToParentOn();
            window = new Shell( parentShell, SWT.CLOSE | SWT.TITLE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL );
            GridLayout layout = new GridLayout( 1, false );
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            window.setLayout( layout );
            window.setSize( 970, 750 );
            window.setLocation( 100, 50 );

            browser = new Browser( window, SWT.NONE );
            browser.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        }
        browser.setUrl( url );
        window.open();
    }


//    private Shell getActiveShell() {
//        IWorkbench workbench = PlatformUI.getWorkbench();
//        Shell parentShell = workbench.getActiveWorkbenchWindow().getShell();
//        return parentShell;
//    }

}
