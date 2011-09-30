package org.polymap.core.qi4j.entitystore.json;

import java.io.File;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;


public class JsonEntityStoreAssembler
        implements Assembler {

    private Visibility visibility;

    public JsonEntityStoreAssembler( Visibility visibility ) {
        this.visibility = visibility;
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException {
        String applicationName = module.layerAssembly().applicationAssembly().name();

        File root = null;  //new File( Polymap.getWorkspacePath().toFile(), "data" );
        root.mkdir();
        
        File moduleRoot = new File( root, applicationName );
        moduleRoot.mkdir();

        JsonEntityStoreInfo info = new JsonEntityStoreInfo( moduleRoot );
        module.addServices( JsonEntityStoreService.class )
                .setMetaInfo( info )
                .visibleIn( visibility )
                .instantiateOnStartup();
        module.addServices( UuidIdentityGeneratorService.class )
                .visibleIn( visibility );
    }

}
