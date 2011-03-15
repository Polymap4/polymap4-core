package org.polymap.core.data.image;

import java.util.Properties;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;

import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.project.PipelineHolder;

/**
 * The configuration UI for {@link ImageBufferProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageBufferProcessorConfig
        extends FieldEditorPreferencePage
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
        props.setProperty( ImageBufferProcessor.PROP_CACHESIZE, labelField.getStringValue() );
        return true;
    }


    protected void createFieldEditors() {
        noDefaultAndApplyButton();
        
        // cache size
        labelField = new IntegerFieldEditor(
                ImageBufferProcessor.PROP_CACHESIZE, "Cache Size", getFieldEditorParent() );
        labelField.setStringValue( props.getProperty( 
                ImageBufferProcessor.PROP_CACHESIZE, String.valueOf( 100 ) ) );
        
//        Composite composite = new Composite( parent, SWT.NULL );
//        GridLayout layout = new GridLayout();
//        layout.numColumns = 2;
//        composite.setLayout( layout );
//
//        GridData data = new GridData();
//        data.verticalAlignment = GridData.FILL;
//        data.horizontalAlignment = GridData.FILL;
//        composite.setLayoutData( data );
//
//        Label l = new Label( composite, SWT.NONE );
//        l.setText( "ImageBufferProcessor..." );
    }

}