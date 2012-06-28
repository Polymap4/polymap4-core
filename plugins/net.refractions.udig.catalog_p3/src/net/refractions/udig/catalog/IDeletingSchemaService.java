/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

import org.geotools.data.DataAccess;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.SQLDialect;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * GeoTools's {@link DataAccess} does not provide a way to delete a given schema. This
 * works around this limitation.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IDeletingSchemaService {

    public void deleteSchema( IGeoResource geores, IProgressMonitor monitor )
    throws IOException;

    
    /**
     * Provides default implementation for {@link JDBCDataStore}s.  
     */
    public class JDBCHelper {
        
        private SQLDialect          dialect;
        
        private JDBCDataStore       ds;
        
        public JDBCHelper( JDBCDataStore ds, SQLDialect dialect ) {
            this.dialect = dialect;
            this.ds = ds;
        }

        public void deleteSchema( IGeoResource geores, IProgressMonitor monitor )
        throws IOException {
            StringBuffer sql = new StringBuffer( "DROP TABLE " ); 

            // tablename
            String typename = geores.getInfo( monitor ).getName();
            if (ds.getDatabaseSchema() != null) {
                dialect.encodeSchemaName( ds.getDatabaseSchema(), sql );
                sql.append( "." );
            }
            dialect.encodeTableName( typename, sql );

            // execute statement
            Connection cx = null;
            try {
                cx = ds.getDataSource().getConnection();
                dialect.initializeConnection( cx );

                System.out.println( "Delete schema SQL: " + sql );
                Statement st = cx.createStatement();
                st.execute( sql.toString() );
            } 
            catch (Exception e) {
                String msg = "Error occurred creating table";
                throw (IOException) new IOException(msg).initCause(e);
            } 
            finally {
                ds.closeSafe( cx );
            }
        }
    }
    
}
