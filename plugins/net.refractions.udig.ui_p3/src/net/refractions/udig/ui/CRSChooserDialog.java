package net.refractions.udig.ui;

import net.refractions.udig.ui.internal.Messages;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;

public class CRSChooserDialog extends Dialog {

    private final CRSChooser                chooser = new CRSChooser();

    private final CoordinateReferenceSystem initialValue;

    private CoordinateReferenceSystem       result;

    private String                          msg;


    public CRSChooserDialog( Shell parentShell, CoordinateReferenceSystem initialValue ) {
        super( parentShell );
        setShellStyle( SWT.RESIZE | SWT.DIALOG_TRIM );
        this.initialValue = initialValue;
    }


    public CRSChooserDialog( Shell parentShell, CoordinateReferenceSystem initialValue, String msg ) {
        this( parentShell, initialValue );
        this.msg = msg;
    }


    @Override
    protected Control createDialogArea( Composite parent ) {
        getShell().setText( Messages.get( "CRSChooserDialog_title" ) );
//        getShell().setMinimumSize( 500, 300 );
//        getShell().layout( true );

        Composite client = (Composite) super.createDialogArea(parent);
        FormLayout layout = new FormLayout();
        layout.marginWidth = layout.marginHeight = 7;
        layout.spacing = 14;
        client.setLayout( layout );
        
        Label l = null;
        if (msg != null) {
            l = new Label( client, SWT.NONE );
            l.setText( msg );
            FormData formData = new FormData();
            formData.top = new FormAttachment( 0 );
            formData.left = new FormAttachment( 0 );
            formData.right = new FormAttachment( 100 );
            l.setLayoutData( formData );
        }

        chooser.setController( new Controller() {
            public void handleClose() {
                close();
            }
            public void handleOk() {
                result = chooser.getCRS();
            }
            public void handleSelect() {
            }
        } );
        Control control = chooser.createControl( client, initialValue );
        FormData formData = new FormData();
        formData.top = l != null ? new FormAttachment( l ) : new FormAttachment( 0 );
        formData.bottom = new FormAttachment( 100 );
        formData.left = new FormAttachment( 0 );
        formData.right = new FormAttachment( 100 );
        control.setLayoutData( formData );
        chooser.setFocus();
        return control;
    }


    @Override
    public boolean close() {
        result = chooser.getCRS();
        return super.close();
    }


    public CoordinateReferenceSystem getResult() {
        return result;
    }
}