package org.polymap.core.data.image;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.preference.IntegerFieldEditor;

import org.eclipse.ui.dialogs.PropertyPage;

import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.project.PipelineHolder;

/**
 * The configuration UI for {@link ImageGrayscaleProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageGrayscaleProcessorConfig
        extends PropertyPage
        implements ProcessorPropertyPage {

    private PipelineHolder          holder;
    
    private Properties              props;

    private IntegerFieldEditor      labelField;
    
    
    public void init( PipelineHolder _holder, Properties _props ) {
        this.holder = _holder;
        this.props = _props;
    }

    
    public void dispose() {
        super.dispose();
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().dispose();
        }
    }


    public boolean performOk() {
        return true;
    }


    protected Control createContents( Composite parent ) {
        noDefaultAndApplyButton();
        
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout( layout );

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData( data );

        Label l = new Label( composite, SWT.NONE );
        l.setText( "Nothing to configure for ImageGrayscaleProcessor." );
        
        //new Separator();
        
        Label l2 = new Label( composite, SWT.NONE );
        l2.setText( "Currently this processor does not work for WMS pipelines." );
        
        return composite;
    }

}