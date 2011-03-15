package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.dialogs.PropertyPage;

import org.polymap.core.project.ILayer;

/**
 * 
 * @deprecated
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LayerBasePage 
        extends PropertyPage {

    private static Log log = LogFactory.getLog( LayerBasePage.class );

	private static final String    PATH_TITLE = "Path:";
	private static final String    OWNER_TITLE = "&Owner:";
	private static final String    OWNER_PROPERTY = "OWNER";
	private static final String    DEFAULT_OWNER = "John Doe";

	private static final int       TEXT_FIELD_WIDTH = 50;

	private Text                   ownerText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public LayerBasePage() {
		super();
	}


    private void addFirstSection( Composite parent ) {
        Composite composite = createDefaultComposite( parent );

        // Label for path field
        Label labelLabel = new Label( composite, SWT.NONE );
        labelLabel.setText( PATH_TITLE );

        // Path text field
        Text labelText = new Text( composite, SWT.WRAP | SWT.READ_ONLY );
        String label = ((ILayer)getElement()).getLabel().toString();
        labelText.setText( label );
    }


    private void addSeparator( Composite parent ) {
        Label separator = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        separator.setLayoutData( gridData );
    }


    private void addSecondSection( Composite parent ) {
        Composite composite = createDefaultComposite( parent );

        // Label for owner field
        Label ownerLabel = new Label( composite, SWT.NONE );
        ownerLabel.setText( OWNER_TITLE );

        // Owner text field
        ownerText = new Text( composite, SWT.SINGLE | SWT.BORDER );
        GridData gd = new GridData();
        gd.widthHint = convertWidthInCharsToPixels( TEXT_FIELD_WIDTH );
        ownerText.setLayoutData( gd );

        // Populate owner text field
        String owner = ((ILayer)getElement()).getCRSCode();
        ownerText.setText( (owner != null) ? owner : DEFAULT_OWNER );
    }


    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );
        GridData data = new GridData( GridData.FILL );
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData( data );

        addFirstSection( composite );
        addSeparator( composite );
        addSecondSection( composite );
        return composite;
    }


    private Composite createDefaultComposite( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout( layout );

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData( data );

        return composite;
    }


    protected void performDefaults() {
        // Populate the owner text field with the default value
        ownerText.setText( DEFAULT_OWNER );
    }


    public boolean performOk() {
        log.info( "performOK()" );
//        // store the value in the owner text field
//        try {
//            ((IResource)getElement()).setPersistentProperty(
//                    new QualifiedName( "", OWNER_PROPERTY ), ownerText.getText() );
//        }
//        catch (CoreException e) {
//            return false;
//        }
        return true;
    }

}