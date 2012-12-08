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
 * $Id: $
 */

package org.polymap.core.workbench;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.internal.util.URLHelper;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.CorePlugin;
import org.polymap.core.Messages;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>24.06.2009: created</li>
 * @version $Revision: $
 */
public class PolymapWorkbench
        implements IEntryPoint {

    private static final Log log = LogFactory.getLog( PolymapWorkbench.class );
    
    private static final String POLYMAP_PRESENTATION = "polymap3.presentation";

    public PolymapWorkbench() {
        log.debug( "..." );
    }


    /**
     * Handle the given error by opening an error dialog and logging the given
     * message to the CorePlugin log.
     * 
     * @param src
     * @param msg The error message. If null, then a standard message is used.
     * @param e The reason of the error, must not be null.
     */
    public static void handleError( String pluginId, Object src, final String msg, Throwable e) {
        log.error( msg, e );

        final Status status = new Status( IStatus.ERROR, pluginId, e.getLocalizedMessage(), e );
        CorePlugin.getDefault().getLog().log( status );

        final Display display = Polymap.getSessionDisplay();
        if (display == null) {
            log.error( "No display -> no error message." );
            return;
        }
        
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Shell shell = getShellToParentOn();
                    ErrorDialog dialog = new ErrorDialog(
                            shell,
                            Messages.get( "PolymapWorkbench_errorDialogTitle" ),
                            msg != null ? msg : "Fehler beim Ausführen der Operation.",
                            status,
                            IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR );
//                dialog.setBlockOnOpen( true );
                    dialog.open();
                }
                catch (Throwable ie) {
                    log.warn( ie );
                }
            }
        };
        if (Display.getCurrent() != null) {
            runnable.run();
        }
        else {
            display.asyncExec( runnable );
        }
    }

    
    /**
     * Return an appropriate shell to parent dialogs on. This will be one of the
     * workbench windows (the active one) should any exist. Otherwise
     * <code>null</code> is returned.
     * 
     * @return the shell to parent on or <code>null</code> if there is no
     *         appropriate shell
     * @since 3.3
     */
    public static Shell getShellToParentOn() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchWindow windowToParentOn = activeWindow == null ? (workbench
                .getWorkbenchWindowCount() > 0 ? workbench
                .getWorkbenchWindows()[0] : null) : activeWindow;
        return windowToParentOn == null ? null : activeWindow.getShell();
    }

    
//    public static void setStatusLine() {
//        if (layer.getStatus() == ILayer.ERROR) {
//            getViewSite().getActionBars().getStatusLineManager().setErrorMessage(
//                    layer.getStatusMessage());
//        } else {
//            getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
//            getViewSite().getActionBars().getStatusLineManager().setMessage(
//                    layer.getStatusMessage());
//
//        }
//    } else {
//        getViewSite().getActionBars().getStatusLineManager().setMessage(null);
//        getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
//
//    }
    
    
    public int createUI() {
        log.debug( "..." );
        
        // see http://www.eclipse.org/forums/index.php/m/91519/
//        UICallBack.activate( String.valueOf( this.hashCode() ) );
        
        ScopedPreferenceStore prefStore = (ScopedPreferenceStore)PrefUtil.getAPIPreferenceStore();
        String keyPresentationId = IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID;
        String presentationId = prefStore.getString( keyPresentationId );

        WorkbenchAdvisor worbenchAdvisor = new PolymapWorkbenchAdvisor();
//        if (POLYMAP_PRESENTATION.equals( presentationId )) {
//            worbenchAdvisor = new PolymapPresentationWorkbenchAdvisor();
//        }

        // security config / login
        Polymap.instance().login();
        
        // start workbench
        try {
            Display display = null;
            display = PlatformUI.createDisplay();
            return PlatformUI.createAndRunWorkbench( display, worbenchAdvisor );
        }
        catch (Exception e) {
            // 
            handleError( CorePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            return PlatformUI.RETURN_OK;
        }
    }
    
    
    protected void createExceptionUI( Throwable e ){
        try {
            Display display = PlatformUI.createDisplay();
            
            final Shell mainShell = new Shell( display, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX );
            mainShell.setLayout( new FillLayout() );
            mainShell.setText( "Exception." );
            
            Label msg = new Label( mainShell, SWT.DEFAULT );
            msg.setText( "Exception: " + e.toString() );
    
            mainShell.addShellListener( new ShellAdapter() {
                public void shellClosed( ShellEvent ev ){
                    mainShell.dispose();
                }
            });
            
            // center
            Rectangle parentSize = display.getBounds();
            Rectangle mySize = mainShell.getBounds();
            mainShell.setLocation( (parentSize.width - mySize.width)/2+parentSize.x,
                    (parentSize.height - mySize.height)/2+parentSize.y );
            mainShell.open();
            while (!mainShell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            restart(); 
        }
        catch (Exception e1) {
            e.printStackTrace();
        }
    }
    
    
    public static void restart() {
        String url = URLHelper.getURLString();
        Shell shell = new Shell( Display.getCurrent(), SWT.NONE );
        Browser browser = new Browser( shell, SWT.NONE );
        String page = "<html><head><title></title><meta http-equiv=\"refresh\" content=\"0;url="
                + url + "\";>" + "<script type=\"text/javascript\">"
                + "if (top != self) top.location = self.location;"
                + "</script></head><body></body></html>";
        browser.setText( page );
        shell.setMaximized( true );
        shell.open();
    }
    
}
