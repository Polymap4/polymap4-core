package org.polymap.rhei.data.entitystore.lucene;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class LuceneEntityStoreAssembler
        implements Assembler {

    private Visibility visibility;

    public LuceneEntityStoreAssembler( Visibility visibility ) {
        this.visibility = visibility;
    }

    public void assemble( ModuleAssembly module )
            throws AssemblyException {
//        String applicationName = module.layerAssembly().applicationAssembly().name();
//
//        File root = null;  //new File( Polymap.getWorkspacePath().toFile(), "data" );
//        root.mkdir();
//        
//        File moduleRoot = new File( root, applicationName );
//        moduleRoot.mkdir();
//
//        LuceneEntityStoreInfo info = new LuceneEntityStoreInfo( moduleRoot );
//        module.addServices( LuceneEntityStoreService.class )
//                .setMetaInfo( info )
//                .visibleIn( visibility )
//                .instantiateOnStartup();
//        module.addServices( UuidIdentityGeneratorService.class )
//                .visibleIn( visibility );
    }

}
