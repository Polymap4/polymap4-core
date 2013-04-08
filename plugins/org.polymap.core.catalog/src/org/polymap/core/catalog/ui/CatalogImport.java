package org.polymap.core.catalog.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.internal.ui.ConnectionPageDecorator;
import net.refractions.udig.catalog.internal.ui.ReflectionWorkflowWizardPageProvider;
import net.refractions.udig.catalog.ui.ConnectionErrorPage;
import net.refractions.udig.catalog.ui.DataSourceSelectionPage;
import net.refractions.udig.catalog.ui.UDIGConnectionFactoryDescriptor;
import net.refractions.udig.catalog.ui.workflow.ConnectionErrorState;
import net.refractions.udig.catalog.ui.workflow.ConnectionFailurePage;
import net.refractions.udig.catalog.ui.workflow.ConnectionFailureState;
import net.refractions.udig.catalog.ui.workflow.DataSourceSelectionState;
import net.refractions.udig.catalog.ui.workflow.EndConnectionState;
import net.refractions.udig.catalog.ui.workflow.IntermediateState;
import net.refractions.udig.catalog.ui.workflow.State;
import net.refractions.udig.catalog.ui.workflow.Workflow;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizard;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardAdapter;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardDialog;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPage;
import net.refractions.udig.catalog.ui.workflow.WorkflowWizardPageProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.INewWizard;

/**
 * 
 * <p/>
 * Taken from {@link net.refractions.udig.catalog.internal.ui.CatalogImport} and adapted
 * for the Polymap3 way of handling adding resources to the catalog.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogImport { 

    private WorkflowWizardDialog    dialog;
	
	private WorkflowWizard          wizard;
	
	
	public CatalogImport() {
        DataSourceSelectionState state = new DataSourceSelectionState( false );
        Workflow workflow = new Workflow( new State[] { state } );

        Map<Class<? extends State>,WorkflowWizardPageProvider> map = createPageMapping();
        wizard = createWorkflowWizard( workflow, map );
	}
	
	
    public CatalogImport( UDIGConnectionFactoryDescriptor descriptor ) {
        DataSourceSelectionState state = new DataSourceSelectionState( false );
        state.setDescriptor( descriptor );
        Workflow workflow = new Workflow( new State[] { state.next() } );

        Map<Class<? extends State>,WorkflowWizardPageProvider> map = createPageMapping();
        wizard = createWorkflowWizard( workflow, map );
    }


    protected WorkflowWizardDialog getOrCreateDialog() {
        if (dialog == null) {
            Shell parentShell = PolymapWorkbench.getShellToParentOn();

            final Shell shell = new Shell( parentShell );
            dialog = new WorkflowWizardDialog( shell, wizard );

            Point dialogSize = new Point( 450, 250 );
            dialog.setMinimumPageSize( dialogSize );
            
            // center dialog
            Rectangle shellBounds = parentShell.getBounds();
            shell.setLocation(
                    -100 + (shellBounds.width - dialogSize.x) / 2,
                    -200 + (shellBounds.height - dialogSize.y) / 2);
            
            dialog.setBlockOnOpen( true );
        }
        return dialog;
	}
	
    
	public void open() {
	    Polymap.getSessionDisplay().asyncExec( new Runnable() {
	        public void run() {
	            getOrCreateDialog().open();	
	        };
	    });
	}
	
	
	/**
	 * Runs the workflow.
	 *
	 * @param monitor the monitor for 
	 * @param context
	 * @return
	 */
    public boolean run( IProgressMonitor monitor, Object context ) {
        dialog = getOrCreateDialog();
        dialog.getWorkflowWizard().getWorkflow().setContext( context );
        String bind = "CatalogImport";  /*MessageFormat.format( Messages.CatalogImport_monitor_task,
                new Object[] { format( context ) } );*/
        monitor.beginTask( bind, IProgressMonitor.UNKNOWN );
        monitor.setTaskName( bind );
        try {
            return dialog.runHeadless( new SubProgressMonitor( monitor, 100 ) );
        }
        finally {
            monitor.done();
        }
    }

    
    private String format( Object data ) {
        if (data instanceof URL) {
            return formatURL( (URL)data );
        }
        if (data instanceof IGeoResource) {
            return ((IGeoResource)data).getIdentifier().getRef();
        }
        if (data instanceof IResolve) {
            return formatURL( ((IResolve)data).getIdentifier() );
        }
        return data.toString();
    }

    
    private String formatURL( URL url ) {
        return url.getProtocol()+"://"+url.getPath(); //$NON-NLS-1$
    }

    
	protected Map<Class<? extends State>, WorkflowWizardPageProvider> createPageMapping() {
        HashMap<Class<? extends State>, WorkflowWizardPageProvider> map = new HashMap<Class<? extends State>, WorkflowWizardPageProvider>();

        addToMap( map, DataSourceSelectionState.class, DataSourceSelectionPage.class );

        WorkflowWizardPageProvider provider = new ReflectionWorkflowWizardPageProvider( ConnectionPageDecorator.class );
        map.put( IntermediateState.class, provider );
        map.put( EndConnectionState.class, provider );

        addToMap( map, ConnectionErrorState.class, ConnectionErrorPage.class );
        addToMap( map, ConnectionFailureState.class, ConnectionFailurePage.class );
        return map;
	}

	
    private void addToMap( Map<Class< ? extends State>, WorkflowWizardPageProvider> map, Class<? extends State> key, 
            Class<? extends WorkflowWizardPage> page ) {
        WorkflowWizardPageProvider pageFactory = new ReflectionWorkflowWizardPageProvider( page );
        map.put( key, pageFactory );
    }
	

    protected WorkflowWizard createWorkflowWizard( Workflow workflow,
            Map<Class<? extends State>, WorkflowWizardPageProvider> map ) {
        return new CatalogImportWizard( workflow, map );
    }

	
    /**
     * Extends {@link WorkflowWizardAdapter} by passing the CatalogImport wizard to
     * the constructor.
     * 
     * @author jesse
     * @since 1.1.0
     */
    public static class CatalogNewWizardAdapter
            extends WorkflowWizardAdapter
            implements INewWizard {

        public CatalogNewWizardAdapter() {
            super( new CatalogImport().wizard );
        }
    }

}

