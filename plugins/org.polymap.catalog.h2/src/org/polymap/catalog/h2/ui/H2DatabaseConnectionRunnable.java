package org.polymap.catalog.h2.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import net.refractions.udig.catalog.service.database.DatabaseConnectionRunnable;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A runnable that attempts to connect to a H2 database. If it does it will get a
 * list of all the databases and store them for later access. If it does not then it
 * will store an error message.
 * 
 * @since 3.1
 */
public class H2DatabaseConnectionRunnable 
        implements DatabaseConnectionRunnable {

    private volatile boolean  ran;

    private volatile String   result;

    private final Set<String> databaseNames = new HashSet<String>();

    private final String      host;

    private final int         port;

    private final String      username;

    private final String      password;


    public H2DatabaseConnectionRunnable( String host2, int port2, String username2,
            String password2 ) {
        this.host = host2;
        this.port = port2;
        this.username = username2;
        this.password = password2;
    }

    
    public void run( IProgressMonitor monitor ) 
    throws InvocationTargetException, InterruptedException {
        databaseNames.add( "polymap" );              
        ran = true;
    }

    
    /**
     * Returns null if the run method was able to connect to the database otherwise
     * will return a message indicating what went wrong.
     * 
     * @return null if the run method was able to connect to the database otherwise
     *         will return a message indicating what went wrong.
     * @throws IllegalStateException if called before run.
     */
    public String canConnect() throws IllegalStateException {
        if (!ran) {
            throw new IllegalStateException( "run() must complete running before this method is called." );
        }
        return result;
    }

    
    /**
     * Returns the names of the databases in the database that this object connected
     * to when the run method was executed.
     * 
     * @return the names of the databases in the database that this object connected
     *         to when the run method was executed.
     */
    public String[] getDatabaseNames() {
        return databaseNames.toArray(new String[0]);
    }

}
