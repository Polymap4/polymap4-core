/**
 * 
 */
package net.refractions.udig.ui;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.ui.internal.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;

public class CRSChooserDialog extends Dialog {
	private final CRSChooser chooser=new CRSChooser();
	private final CoordinateReferenceSystem initialValue;
	private CoordinateReferenceSystem result;
	private String msg;

    public CRSChooserDialog(Shell parentShell, CoordinateReferenceSystem initialValue) {
        super(parentShell);
        this.initialValue=initialValue;
    }

    public CRSChooserDialog(Shell parentShell, CoordinateReferenceSystem initialValue, String msg) {
        this(parentShell, initialValue);
        this.msg = msg;
    }

	@Override
	protected Control createDialogArea( Composite parent ) {
		getShell().setText(Messages.CRSChooserDialog_title);

		if (msg != null) {
		    Label l = new Label( parent, SWT.NONE );
		    l.setText( msg );
		}
		
		chooser.setController(new Controller(){
	        public void handleClose() {
	            close();
	        }
	        public void handleOk() {
	            result=chooser.getCRS();
	        }
	        public void handleSelect() {
	        }
	    });
	    Control control = chooser.createControl(parent, initialValue);
	    chooser.setFocus();
		return control;
	}

	@Override
	public boolean close() {
	    result=chooser.getCRS();
	    return super.close();
	}
	
	public CoordinateReferenceSystem getResult() {
		return result;
	}
}