package org.polymap.catalog.h2.ui;

import static org.geotools.data.mysql.MySQLDataStoreFactory.*;
import org.eclipse.jface.dialogs.IDialogSettings;

import org.polymap.catalog.h2.H2Plugin;
import org.polymap.catalog.h2.H2ServiceExtension;

import net.refractions.udig.catalog.service.database.DatabaseConnectionRunnable;
import net.refractions.udig.catalog.service.database.DatabaseServiceDialect;
import net.refractions.udig.catalog.service.database.DatabaseWizardLocalization;
import net.refractions.udig.catalog.service.database.LookUpSchemaRunnable;

/**
 * All the H2 specific code for working for the new wizard.
 * 
 * @since 3.1
 */
public class H2Dialect 
        extends DatabaseServiceDialect {

    public H2Dialect(  ) {
        super( SCHEMA, DATABASE, HOST, PORT, USER, PASSWD, 
                H2ServiceExtension.getPram( DBTYPE.key ), /*null,*/
                "jdbc.h2", new DatabaseWizardLocalization() );
    }

    public DatabaseConnectionRunnable createDatabaseConnectionRunnable( 
            String host, int port, String username, String password ) {
        return new H2DatabaseConnectionRunnable( host, port, username, password);
    }

    public LookUpSchemaRunnable createLookupSchemaRunnable( 
            String host, int port, String username, String password, String database ) {
        return new H2LookUpSchemaRunnable( host,port,username,password,database);
    }

    public IDialogSettings getDialogSetting() {
        return H2Plugin.getDefault().getDialogSettings();
    }

    public void log( String message, Throwable e ) {
        H2Plugin.log( message, e );
    }

}
