package org.polymap.core.data.image;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.project.PipelineHolder;

/**
 * The configuration UI for {@link ImageBufferProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageTransparencyProcessorConfig
        extends FieldEditorPreferencePage
        implements ProcessorPropertyPage {

    private static final Log log = LogFactory.getLog( ImageTransparencyProcessorConfig.class );

    private PipelineHolder          holder;
    
    private Properties              props;

    private StringFieldEditor       colorField;
    
    
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
        String color = colorField.getStringValue();
        log.info( "Color: " + color );
        props.setProperty( ImageTransparencyProcessor.PROP_MARKER_COLOR, color );
        return true;
    }


    protected void createFieldEditors() {
        noDefaultAndApplyButton();
        
        // cache size
        colorField = new StringFieldEditor(
                ImageTransparencyProcessor.PROP_MARKER_COLOR, "Color", getFieldEditorParent() );
//        colorField.getColorSelector().setColorValue( new RGB( props.getProperty( 
//                ImageBufferProcessor.PROP_CACHESIZE, String.valueOf( 100 ) ) );
        
    }

}
