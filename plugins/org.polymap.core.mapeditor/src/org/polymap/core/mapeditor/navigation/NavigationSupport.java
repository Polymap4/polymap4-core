package org.polymap.core.mapeditor.navigation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.polymap.core.mapeditor.IMapEditorSupport;
import org.polymap.core.mapeditor.IMapEditorSupportListener;
import org.polymap.core.mapeditor.INavigationSupport;
import org.polymap.core.mapeditor.MapEditor;
import org.polymap.core.mapeditor.MapEditorPlugin;
import org.polymap.core.project.IMap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;

/**
 * Provides the navigation support methods for an {@link MapEditor}.
 * <p/>
 * The <code>NavigationSupport</code> also provides the undo/redo logic
 * for navigation operations. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class NavigationSupport
        implements INavigationSupport, IMapEditorSupportListener, PropertyChangeListener {

    private static Log log = LogFactory.getLog( NavigationSupport.class );

    /** The {@link MapEditor} we are working with. */
    private MapEditor                   mapEditor;

    private NavigationControl        control;
    
    private boolean                     active;
    
    private IUndoContext                context;

    private DefaultOperationHistory     history;
    
    private ReferencedEnvelope          lastMapExtent;

    
    /**
     * 
     * @param mapEditor
     */
    public NavigationSupport( MapEditor mapEditor ) {
        this.mapEditor = mapEditor;
        this.mapEditor.addSupportListener( this );

        this.context = new ObjectUndoContext( this, "Navigation Context" ); //$NON-NLS-1$
        this.history = new DefaultOperationHistory();
//        approver = new AdvancedValidationUserApprover( context );
//        history.addOperationApprover( approver );
        this.history.setLimit( context, 10 );
        
//        this.lastMapExtent = mapEditor.getMap().getExtent() != null
//                ? mapEditor.getMap().getExtent()
//                : mapEditor.getMap().getMaxExtent();
        mapEditor.getMap().addPropertyChangeListener( this );
    }


    public void dispose() {
        if (mapEditor != null) {
            mapEditor.getMap().removePropertyChangeListener( this );
        }
        
        if (history != null) {
//            history.removeOperationApprover( approver );
            history.dispose( context, true, true, true );
            history = null;
        }
        if (control != null) {
            control.deactivate();
            mapEditor.removeControl( control );
            control.destroy();
            control.dispose();
            control = null;
        }
        this.mapEditor.removeSupportListener( this );
        this.mapEditor = null;
    }

    
    public void supportStateChanged( MapEditor editor, IMapEditorSupport support, boolean activated ) {
        log.debug( "support= " + support + " activated= " + activated );
        if (support == this) {
            setActive( activated );
        }
    }

    
    protected void setActive( boolean active ) {
        log.debug( "NavigationSupport: active= " + active + " isActive= " + isActive() );
        if (isActive() == active) {
            return;
        }
        this.active = active;
        if (active) {
            if (control == null) {
                control = new NavigationControl();
                this.mapEditor.addControl( control );
            }
            control.activate();
        } 
        else {
            control.deactivate();
            //this.mapEditor.olwidget.getMap().removeControl( control );
        }
    }

    
    public boolean isActive() {
        return active;
    }
    
    
    // navigation *****************************************
    
    private boolean ignoreEvents = false;
    
    
    public void propertyChange( PropertyChangeEvent ev ) {
        String name = ev.getPropertyName();
        if (!ignoreEvents
                && mapEditor.getMap().equals( ev.getSource() )
                // XXX the extent events are often followed by an update
                // event with different extent (diskrete scales); processing just
                // the update event solves this but we are potentially loosing
                // map extent chnages
                && (IMap.PROP_EXTENT.equals( name ) || IMap.PROP_EXTENT_UPDATE.equals( name )) ) {

            try {
                ReferencedEnvelope newExtent = (ReferencedEnvelope)ev.getNewValue();
                if (lastMapExtent != null && !newExtent.equals( lastMapExtent )) {
                    NavigationOperation op = new NavigationOperation( lastMapExtent, newExtent );
                    op.addContext( context );
                    history.execute( op, new NullProgressMonitor(), null );
                    log.debug( "propertyChange(): " + newExtent + ", history= " + history.getUndoHistory( context ).length );
                }
                lastMapExtent = newExtent;
            }
            catch (ExecutionException e) {
                PolymapWorkbench.handleError( MapEditorPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
    }


    public boolean canUndo() {
        return history.canUndo( context );
    }

    
    public IStatus undo() {
        try {
            ignoreEvents = true;
            return history.undo( context, new NullProgressMonitor(), null );
        }
        catch (ExecutionException e) {
            return new Status( Status.ERROR, MapEditorPlugin.PLUGIN_ID, e.getMessage() );
        }
        finally {
            ignoreEvents = false;
        }
    }

    
    public boolean canRedo() {
        return history.canRedo( context );
    }

    
    public IStatus redo() {
        try {
            ignoreEvents = true;
            return history.redo( context, new NullProgressMonitor(), null );
        }
        catch (ExecutionException e) {
            return new Status( Status.ERROR, MapEditorPlugin.PLUGIN_ID, e.getMessage() );
        }
        finally {
            ignoreEvents = false;
        }
    }

    
    /**
     * 
     */
    class NavigationOperation
            extends AbstractOperation
            implements IUndoableOperation {

        private ReferencedEnvelope      undoMapExtent, redoMapExtent;
        
        
        protected NavigationOperation( ReferencedEnvelope undoMapExtent, ReferencedEnvelope redoMapExtent ) {
            super( "Navigation" );
            this.undoMapExtent = undoMapExtent;
            this.redoMapExtent = redoMapExtent;
        }

        public void dispose() {
            log.debug( "dispose(): " + undoMapExtent );
        }

        public IStatus execute( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            // do nothing, the map has already changed it extent/zoom
            return Status.OK_STATUS;
        }

        public IStatus undo( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            log.debug( "undo(): " + undoMapExtent );
            mapEditor.getMap().setExtent( undoMapExtent );
            return Status.OK_STATUS;
        }

        public IStatus redo( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            mapEditor.getMap().setExtent( redoMapExtent );
            return Status.OK_STATUS;
        }

    }
    
}