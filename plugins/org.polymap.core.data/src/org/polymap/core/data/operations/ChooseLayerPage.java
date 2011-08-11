package org.polymap.core.data.operations;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.project.ProjectTreeViewer;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.WeakListener;

/**
 * Provides a wizard page that is used by operations to choose a layer from a
 * tree of all current maps and layers.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ChooseLayerPage
        extends WizardPage
        implements IWizardPage, ISelectionChangedListener {

    public static final String          ID = "ChooseLayerPage";

    private ProjectTreeViewer           viewer;
    
    private ILayer                      result;

    private boolean                     mandatory;


    /**
     * @param title The page title.
     * @param description The page description. 
     * @param mandatory True specifies that a layer has to be chosen to complete the
     *        page.
     */
    public ChooseLayerPage( String title, String description, boolean mandatory ) {
        super( ID );
        this.mandatory = mandatory;
        setTitle( title );
        setDescription( description );
    }

    
    public ILayer getResult() {
        return result;
    }


    public void createControl( Composite parent ) {
        Composite contents = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        layout.spacing = 5;
        contents.setLayout( layout );
        setControl( contents );

        viewer = new ProjectTreeViewer( contents, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        viewer.setRootMap( ProjectRepository.instance().getRootMap() );
        viewer.getTree().setLayoutData( new SimpleFormData().fill().create() );

        viewer.addSelectionChangedListener( WeakListener.forListener( this ) );
    }

    public boolean isPageComplete() {
        return !mandatory || result != null;
    }

    public void selectionChanged( SelectionChangedEvent ev ) {
        result = null;
        ISelection sel = ev.getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            if (elm != null && elm instanceof ILayer) {
                result = (ILayer)elm;
            }
        }
        getContainer().updateButtons();
    }

}