package net.refractions.udig.catalog.rasterings;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IServiceInfo;

public class AbstractRasterServiceInfo extends IServiceInfo {

    private final AbstractRasterService service;

    public AbstractRasterServiceInfo( AbstractRasterService service, String... keywords ) {
        this.service = service;
        super.keywords = keywords; 
    }
    
    @Override
    public String getTitle() {
        ID id = service.getID();
        
        String title;
        if( id.isFile() ){
            title = id.toFile().getAbsolutePath();            
        }
        else {
            title = id.toString();
        }
        // _p3: files are always in workspace -> strip path
        title = title.replace("%20", " ");
        title = StringUtils.substringAfterLast(title, File.separator);

        return title;
    }

    @Override
    public String getShortTitle() {
        return service.getID().toFile().getName();
    }
    
    @Override
    public String getDescription() {
        return service.getIdentifier().toString();
    }
    
}
