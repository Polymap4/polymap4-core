package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.referencing.CRS;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IWorkbenchPropertyPage;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.project.ILayer;

/**
 * Basic properties of an {@link ILayer}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LayerBasePropertiesPage 
        extends FieldEditorPreferencePage
        implements IWorkbenchPropertyPage {

    private static Log log = LogFactory.getLog( LayerBasePropertiesPage.class );

	private static final int       TEXT_FIELD_WIDTH = 50;
	
	private ILayer                 layer;


    public LayerBasePropertiesPage() {
        super( FLAT );
    }


    protected void createFieldEditors() {
//        IPreferenceStore store = new PreferenceStore();
//        setPreferenceStore( store );
        
        // label
        StringFieldEditor labelField = new StringFieldEditor(
                ILayer.PROP_LABEL, "Label", getFieldEditorParent() );
        labelField.setStringValue( layer.getLabel() );

        // CRS
        final StringFieldEditor crsField = new StringFieldEditor(
                ILayer.PROP_CRSCODE, "Koordinatensystem", getFieldEditorParent());
        crsField.setStringValue( layer.getCRSCode() );
        crsField.setPropertyChangeListener( new IPropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                log.info( "event= " + ev.getNewValue() );
                setErrorMessage( "Abspeichern der Änderungen ist noch nicht möglich." );
                try {
                    CRS.decode( (String)ev.getNewValue() );
                    setErrorMessage( null );
                }
                catch (Exception e) {
                    crsField.setErrorMessage( e.getMessage() );
                    setErrorMessage( e.getMessage() );
                }
            }
        });
        
        // Opacity
        IntegerFieldEditor opacityField = new IntegerFieldEditor(
                ILayer.PROP_OPACITY, "Deckkraft", getFieldEditorParent());
        opacityField.setStringValue( String.valueOf( layer.getOpacity() ) );
        opacityField.setValidRange( 0, 100 );
//        opacityField.setPropertyChangeListener( new IPropertyChangeListener() {
//            public void propertyChange( PropertyChangeEvent ev ) {
//                log.info( "event= " + ev.getNewValue().getClass() );
//                try {
//                    int i = Integer.parseInt( (String)ev.getNewValue() );
//                    if (i < 0 || i > 100) {
//                        setMessage( "Werte müssen zwischen 0 und 100 liegen.", DialogPage.WARNING );
//                        opacityField.setV
//                    } else {
//                        //
//                    }
//                } 
//                catch (NumberFormatException e) {
//                    setMessage( "Kein Zahlenwert: " + ev.getNewValue(), DialogPage.WARNING );
//                }
//            }
//        });
    }

    
    public boolean performOk() {
        log.info( "performOK()..." );
//        try {
//            SetPropertyOperation op = ProjectRepository.instance().newOperation( SetPropertyOperation.class );
//            
//            if (id.equals( ILayer.PROP_LABEL )) {
//                op.init( ILayer.class, layer, ILayer.PROP_LABEL, value );
//                OperationSupport.instance().execute( op, false, false );
//            }
//            else if (id.equals( ILayer.PROP_ORDERKEY )) {
//                op.init( ILayer.class, layer, ILayer.PROP_ORDERKEY, new Integer( value.toString() ) );
//                OperationSupport.instance().execute( op, false, false );
//            }
//            else if (id.equals( ILayer.PROP_OPACITY )) {
//                op.init( ILayer.class, layer, ILayer.PROP_OPACITY, new Integer( value.toString() ) );
//                OperationSupport.instance().execute( op, false, false );
//            }
//            else if (id.equals( ILayer.PROP_CRSCODE )) {
//                CoordinateReferenceSystem crs = CRS.decode( (String)value );
//                op.init( ILayer.class, layer, ILayer.PROP_CRSCODE, crs );
//                OperationSupport.instance().execute( op, false, false );
//            }
//            else {
//                log.error( "Property is read-only: " + id );
//            }
//        }
//        catch (Exception e) {
//            log.error( "Error while changing property: " + id, e );
//        }

        return true;
    }


    public IAdaptable getElement() {
        return layer;
    }


    public void setElement( IAdaptable element ) {
        log.info( "element= " + element );
        layer = (ILayer)element;
    }


//    protected void performDefaults() {
//        // Populate the owner text field with the default value
//        ownerText.setText( DEFAULT_OWNER );
//    }
//
//
//    public boolean performOk() {
//        log.info( "performOK()" );
////        // store the value in the owner text field
////        try {
////            ((IResource)getElement()).setPersistentProperty(
////                    new QualifiedName( "", OWNER_PROPERTY ), ownerText.getText() );
////        }
////        catch (CoreException e) {
////            return false;
////        }
//        return true;
//    }

}