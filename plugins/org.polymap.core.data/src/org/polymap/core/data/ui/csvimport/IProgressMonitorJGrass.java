package org.polymap.core.data.ui.csvimport;

/**
 * A clone of eclipse's IProgressMonitor.
 * 
 * <p>
 * This is done in order to be able to use the monitor outside of the
 * eclipse environment.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public interface IProgressMonitorJGrass {
    public final static int UNKNOWN = -1;

    public void beginTask( String name, int totalWork );

    public void done();

    public void internalWorked( double work );

    public boolean isCanceled();

    public void setCanceled( boolean value );

    public void setTaskName( String name );

    public void subTask( String name );

    public void worked( int work );
}
