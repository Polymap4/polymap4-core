/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;

import org.polymap.service.fs.Messages;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;

/**
 * Provides content nodes for {@link IMap} and {@link ILayer} and a 'projects' node
 * as root for this structure.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( ProjectContentProvider.class );

    public static final String      PROJECT_REPOSITORY_KEY = "projectRepository";
    

    public List<? extends IContentNode> getChildren( IPath path, IContentSite site ) {
        
        // projects root
        if (path.segmentCount() == 0) {
            // XXX user specific!
            ProjectRepository repo = ProjectRepository.globalInstance();
//            assert site.get( PROJECT_REPOSITORY_KEY ) == null;
            site.put( PROJECT_REPOSITORY_KEY, repo );
            
            String name = Messages.get( site.getLocale(), "ProjectContentProvider_projectNode" );
            return Collections.singletonList( new ProjectsFolder( name, path, this ) );
        }

        // maps
        IContentFolder parent = site.getFolder( path );
        log.info( "parent: " + parent );
        if (parent instanceof ProjectsFolder) {
            List<IContentNode> result = new ArrayList();
            ProjectRepository repo = (ProjectRepository)site.get( PROJECT_REPOSITORY_KEY );
            for (IMap map : repo.getRootMap().getMaps()) {
                result.add( new MapFolder( path, this, map ) );
            }
            return result;
        }

        // layers
        else if (parent instanceof MapFolder) {
            List<IContentNode> result = new ArrayList();
            for (ILayer layer : ((MapFolder)parent).getMap().getLayers()) {
                result.add( new LayerFolder( path, this, layer ) );
            }
            return result;
        }
        return null;
    }
    
    
    /*
     * 
     */
    public class ProjectsFolder
            extends DefaultContentFolder {

        public ProjectsFolder( String name, IPath parentPath, IContentProvider provider ) {
            super( name, parentPath, provider, null );
        }

        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält eine Auflistung <b>aller Projekte</b>, auf die Sie im Moment Zugriff haben.";
        }
        
    }

    
    /*
     * 
     */
    public class MapFolder
            extends DefaultContentFolder {

        public MapFolder( IPath parentPath, IContentProvider provider, IMap map ) {
            super( map.getLabel(), parentPath, provider, map );
        }

        public IMap getMap() {
            return (IMap)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält Daten des <b>Projektes</b> \"" + getName() + "\".";
        }
        
    }
    

    /*
     * 
     */
    public class LayerFolder
            extends DefaultContentFolder {

        public LayerFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( layer.getLabel(), parentPath, provider, layer );
        }

        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält Daten der <b>Ebene</b> \"" + getName() + "\".";
        }
        
    }
    
}
