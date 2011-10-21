package org.polymap.core.data.ui.csvimport;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Wrapper for eclipse's {@link IProgressMonitor}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class EclipseProgressMonitorAdapter implements IProgressMonitorJGrass {

    private final IProgressMonitor monitor;

    public EclipseProgressMonitorAdapter( IProgressMonitor monitor ) {
        this.monitor = monitor;
    }

    public void beginTask( String name, int totalWork ) {
        monitor.beginTask(name, totalWork);
    }

    public void done() {
        monitor.done();
    }

    public void internalWorked( double work ) {
        monitor.internalWorked(work);
    }

    public boolean isCanceled() {
        return monitor.isCanceled();
    }

    public void setCanceled( boolean value ) {
        monitor.setCanceled(value);
    }

    public void setTaskName( String name ) {
        monitor.setTaskName(name);
    }

    public void subTask( String name ) {
        monitor.subTask(name);
    }

    public void worked( int work ) {
        monitor.worked(work);
    }

}
