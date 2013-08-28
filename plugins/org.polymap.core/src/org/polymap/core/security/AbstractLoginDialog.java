package org.polymap.core.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.Timer;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public abstract class AbstractLoginDialog
        extends TitleAreaDialog
        implements CallbackHandler {

    boolean    processCallbacks = false;

    boolean    isCancelled      = false;

    Callback[] callbackArray;


    protected final Callback[] getCallbacks() {
        return this.callbackArray;
    }


    public abstract void internalHandle();


    public boolean isCancelled() {
        return isCancelled;
    }


    protected AbstractLoginDialog( Shell parentShell ) {
        super( parentShell );
    }


    public void handle( final Callback[] callbacks )
    throws IOException {
        this.callbackArray = callbacks;
        final Display display = Display.getDefault();
        display.syncExec( new Runnable() {

            public void run() {
                isCancelled = false;
                setBlockOnOpen( false );
                open();
                final Button okButton = getButton( IDialogConstants.OK_ID );
                okButton.setText( "Login" );
                okButton.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( final SelectionEvent event ) {
                        processCallbacks = true;
                    }
                } );
                final Button cancel = getButton( IDialogConstants.CANCEL_ID );
                cancel.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( final SelectionEvent event ) {
                        isCancelled = true;
                        processCallbacks = true;
                    }
                } );
            }
        } );
        try {
            ModalContext.setAllowReadAndDispatch( true ); // Works for now.
            ModalContext.run( new IRunnableWithProgress() {

                public void run( final IProgressMonitor monitor ) {
                    Timer start = new Timer();
                    // Wait here until OK or cancel is pressed, then let it rip.
                    // The event listener is responsible for closing the dialog 
                    // (in the loginSucceeded event).
                    while (!processCallbacks) {
                        // XXX see http://polymap.org/svn-anta2/ticket/128; force restart session to
                        // prevent deadlock in UIThread
                        if (start.elapsedTime() > 60000) {
                            System.out.println( "No login. Refreshing..." );
                            new PolymapWorkbench.Terminator().schedule();
                            return;
                        }
                        try {
                            Thread.sleep( 100 );
                        }
                        catch (final Exception e) {
                            // do nothing
                        }
                    }
                    processCallbacks = false;
                    // Call the adapter to handle the callbacks
                    if (!isCancelled())
                        internalHandle();
                }
            }, true, new NullProgressMonitor(), Display.getDefault() );
        }
        catch (final Exception e) {
            final IOException ioe = new IOException();
            ioe.initCause( e );
            throw ioe;
        }
    }


    protected void configureShell( Shell shell ) {
        super.configureShell( shell );
        //shell.setText( "Login" );
    }
}
