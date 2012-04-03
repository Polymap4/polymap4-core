package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * Property page based on  {@link PropertySheetPage}. Provides the same
 * UI as the default property view of the Workbench.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public abstract class SheetPropertyPage 
        extends PropertyPage
        implements IWorkbenchPropertyPage, IPropertySourceProvider {

    private static Log log = LogFactory.getLog( SheetPropertyPage.class );
    
    
    public SheetPropertyPage() {
    }

    
    protected Control createContents( Composite parent ) {
        Composite content = new Composite( parent, SWT.NONE );
        GridData data = new GridData( SWT.FILL );
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.verticalAlignment = SWT.FILL;
        data.horizontalAlignment = SWT.FILL;
        content.setLayoutData( data );

        noDefaultAndApplyButton();
        
        content.setLayout( new FillLayout() );

        final PropertySheetPage page = new PropertySheetPage();
        page.setPropertySourceProvider( this );
        page.createControl( content );

//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
                PropertySheetEntry root = new PropertySheetEntry();
                root.setPropertySourceProvider( SheetPropertyPage.this );
                
                StructuredSelection sel = new StructuredSelection( getElement() );
                page.selectionChanged( null, sel );
//            }
//        });
        
        return content;
    }


    public boolean performCancel() {
        return super.performCancel();
    }

}