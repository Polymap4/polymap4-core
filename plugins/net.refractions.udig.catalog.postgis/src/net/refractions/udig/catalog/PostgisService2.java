/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog;

import static org.geotools.data.postgis.PostgisNGDataStoreFactory.PORT;
import static org.geotools.data.postgis.PostgisNGDataStoreFactory.SCHEMA;
import static org.geotools.data.postgis.PostgisNGDataStoreFactory.LOOSEBBOX;
import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;
import static org.geotools.jdbc.JDBCDataStoreFactory.USER;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import org.geotools.data.postgis.PostGISDialect;
import org.geotools.jdbc.JDBCDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import net.refractions.udig.catalog.internal.postgis.PostgisPlugin;
import net.refractions.udig.catalog.internal.postgis.ui.PostgisLookUpSchemaRunnable;
import net.refractions.udig.catalog.service.database.TableDescriptor;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * A postgis service that represents the database. Its children are "folders" that each resolve to a
 * PostGISDatastore.  Each folder has georesources
 * 
 * @author jesse
 * @since 1.1.0
 */
public class PostgisService2 
        extends IService 
        implements IDeletingSchemaService {

    private final URL id;
    private Map<String, Serializable> params;
    private Status status;
    private final List<IResolve> members = new ArrayList<IResolve>();
    private Lock lock = new UDIGDisplaySafeLock();
    private Throwable message;

    public PostgisService2( URL finalID, Map<String, Serializable> map ) {
        this.id = finalID;
        this.params = new HashMap<String, Serializable>(map);
        status = Status.NOTCONNECTED;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // clean up connection
        dispose(new NullProgressMonitor());
    }

    @Override
    public Map<String, Serializable> getConnectionParams() {
        if( members.isEmpty()){
            return params;
        }
        StringBuilder builder = new StringBuilder(); 
        for( IResolve member : members ) {
            if( builder.length()>0){
                builder.append(","); //$NON-NLS-1$
            }
            
            PostgisSchemaFolder folder = (PostgisSchemaFolder) member;
            
            builder.append(folder.getSchemaName());
        }
        params.put(SCHEMA.key, builder.toString());
        
        // for wkt for a moment!
        // params.put( PostgisDataStoreFactory.WKBENABLED.key, Boolean.FALSE );
        
        params.put( LOOSEBBOX.key, Boolean.TRUE );
        return params;
    }

    @Override
	protected IServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        // make sure members are loaded cause they're needed for info
        members(monitor);
        return new PostgisServiceInfo(this);
    }

    @Override
    public List<PostgisGeoResource2> resources( IProgressMonitor monitor ) throws IOException {
        List<IResolve> resolves = members(monitor);
        List<PostgisGeoResource2> resources = new ArrayList<PostgisGeoResource2>();
        
        for( IResolve resolve : resolves ) {
            List<IResolve> folderChildren = resolve.members(monitor);
            for( IResolve resolve2 : folderChildren ) {
                resources.add((PostgisGeoResource2) resolve2);
            }
        }
        return resources;
    }

    @Override
    public List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        lock.lock();
        try {
            if (members.isEmpty()) {
                String[] schemas = lookupSchemasInDB(SubMonitor.convert(monitor,
                        "looking up schemas", 1));
                if (schemas == null) {
                    // couldn't look up schema so...
                    String commaSeperated = (String) params.get(SCHEMA.key);
                    schemas = commaSeperated.split(","); //$NON-NLS-1$
                }
                createSchemaFolder(schemas);
                message = null;
                status = Status.CONNECTED;
            }
            return Collections.unmodifiableList(members);
        } finally {
            lock.unlock();
        }
    }
    
    private String[] lookupSchemasInDB(IProgressMonitor monitor) {
        String host = (String) params.get(HOST.key);
        Integer port = (Integer) params.get(PORT.key);
        String database = (String) params.get(DATABASE.key);
        String user = (String) params.get(USER.key);
        String pass = (String) params.get(PASSWD.key);
        
        PostgisLookUpSchemaRunnable runnable = new PostgisLookUpSchemaRunnable(host, port, user, pass, database);
        runnable.run(monitor);
        
        if (runnable.getError()!=null){
            message = new Exception(runnable.getError());
            status = Status.BROKEN;
            return null;
        }
        Set<TableDescriptor> tables = runnable.getSchemas();
        Set<String> schemas = new HashSet<String>();
        for( TableDescriptor schema : tables ) {
            schemas.add(schema.schema);
        }
        return schemas.toArray(new String[0]);
    }

    private void createSchemaFolder( String[] schemas ) {

        for( String string : schemas ) {
            String trimmed = string.trim();
            if( trimmed.length()==0 ){
                continue;
            }
            
            try {
                members.add(new PostgisSchemaFolder(this, trimmed));
            } catch (IOException e) {
                // bummer something went wrong
                PostgisPlugin.log("Couldn't construct PostgisSchemaFolder for "+trimmed, e); //$NON-NLS-1$
            }
        }
    }

    public URL getIdentifier() {
        return id;
    }

    public Throwable getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
    
    @Override
    public void dispose( IProgressMonitor monitor ) {
        for( IResolve folder : members ) {
            folder.dispose(monitor);
        }
    }
    
    public void deleteSchema( IGeoResource geores, IProgressMonitor monitor )
    throws IOException {
        if (geores.service( monitor ) != this) {
            throw new IllegalStateException( "Geores is not Postgis" );
        }
        PostgisSchemaFolder folder = (PostgisSchemaFolder)geores.parent( monitor );
        JDBCDataStore ds = folder.getDataStore();
        new JDBCHelper( ds, new PostGISDialect( ds ) ).deleteSchema( geores, monitor );
    }

}
