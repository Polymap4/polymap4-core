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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.widgets.JSExecutor;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.CorePlugin;
import org.polymap.core.Messages;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 *         <li>24.06.2009: created</li>
 */
public class PolymapWorkbench
        implements IEntryPoint {

    private static final Log log = LogFactory.getLog( PolymapWorkbench.class );
    

    public PolymapWorkbench() {
    }


    /**
     * Handle the given error by opening an error dialog and logging the given
     * message to the CorePlugin log.
     * 
     * @param src
     * @param msg The error message. If null, then a standard message is used.
     * @param e The reason of the error, must not be null.
     */
    public static void handleError( String pluginId, Object src, final String msg, Throwable e ) {
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
//        if (Display.getCurrent() == display) {
//            runnable.run();
//        }
//        else {
            display.asyncExec( runnable );
//        }
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

    
    @Override
    public int createUI() {
        return createUI( new PolymapWorkbenchAdvisor() );
    }
    
    
    protected int createUI( WorkbenchAdvisor advisor ) {
//        ScopedPreferenceStore prefStore = (ScopedPreferenceStore)PrefUtil.getAPIPreferenceStore();
//        String keyPresentationId = IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID;
//        String presentationId = prefStore.getString( keyPresentationId );

        // security config / login
        Polymap.instance().login();
        
        // start workbench
        Display display = PlatformUI.createDisplay();
        try {
            return PlatformUI.createAndRunWorkbench( display, advisor );
        }
        // thread termination due to timeout or reload/F5
        catch (Error e) {
            //log.warn( e );
            throw e;
        }
        finally {
            // logout after other cleanup happened
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    Polymap.instance().logout();
                }
            });
        }
    }

    
    /**
     * Invalidates the current HTTP session. Due to bug in RAP (?) this
     * cannot be done in UIThread.
     * <p/>
     * Invalidating the HTTP session causes the UIThread to terminate.
     * Intension was to free SessionStore and ThreadLocals on the UIThread.
     * Does not(?) work as there are still ThreadLocals on Jetty threads.
     */
    public static class Terminator
            extends Job {

        private ISessionStore sessionStore = RWT.getSessionStore();
        
        public Terminator() {
            super( "Terminator" );
            setSystem( true );
            setUser( false );
        }

        protected IStatus run( IProgressMonitor monitor ) {
            sessionStore.getHttpSession().invalidate();
//            ((SessionStoreImpl)sessionStore).valueUnbound( null );

//            List names = new ArrayList( EnumerationUtils.toList( sessionStore.getAttributeNames() ) );
//            for (Object name : names) {
//                sessionStore.removeAttribute( (String)name );
//            }
//            ContextProvider.releaseContextHolder();
            return Status.OK_STATUS;
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
    
    
    @SuppressWarnings("restriction")
    public static void restart() {
        JSExecutor.executeJS( "window.location.reload();" );
        
//        StringBuilder url = new StringBuilder( URLHelper.getURLString( false ) );
//        // convert to relative URL (code is from RAP)
//        int firstSlash = url.indexOf( "/" , url.indexOf( "//" ) + 2 ); // first slash after double slash of "http://"
//        url.delete( 0, firstSlash + 1 ); // Result is sth like "/rap?custom_service_handler..."

//        Shell shell = new Shell( Display.getCurrent(), SWT.NONE );
//        Browser browser = new Browser( shell, SWT.NONE );
//        String page = "<html><head><title></title>"
//                //+ "<meta http-equiv=\"refresh\" content=\"0;url=" + url.toString() + "\";>" 
//                + "<script type=\"text/javascript\">"
//                + "    alert( window.location.href );"
//                //+ "    if (top != self) window.location.reload();"
//                //+ "    if (top != self) top.location = self.location;"
//                + "    if (top != self) window.location.href=window.location.href;"
//                + "</script></head><body></body></html>";
//        browser.setText( page );
//        shell.setMaximized( true );
//        shell.open();
    }
    
}
