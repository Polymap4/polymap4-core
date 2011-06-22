/**
 * 
 */
package org.polymap.catalog.dxf;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

import org.polymap.core.workbench.PolymapWorkbench;

import net.refractions.udig.catalog.IServiceInfo;

class DxfServiceInfo
        extends IServiceInfo {

    private final DxfServiceImpl service;


    DxfServiceInfo( DxfServiceImpl shpServiceImpl ) {
        super();
        service = shpServiceImpl;
        keywords = new String[] { ".dxf", "DXF"/*, service.ds.getTypeNames()[0]*/ };

        try {
            schema = new URI( "shp://www.opengis.net/gml" ); //$NON-NLS-1$
        }
        catch (URISyntaxException e) {
            PolymapWorkbench.handleError( DxfPlugin.PLUGIN_ID, this, "Unable to build DxfServiceInfo.", e );
            schema = null;
        }
        title = service.getID().toString();
        title = title.replace( "%20", " " ); //$NON-NLS-1$//$NON-NLS-2$
        title = StringUtils.substringAfterLast( title, File.separator );
        System.out.println( "### DxfServiceInfo: " + title );
    }


    public String getDescription() {
        return service.getIdentifier().toString();
    }


    public String getTitle() {
        return title;
    }
    
}