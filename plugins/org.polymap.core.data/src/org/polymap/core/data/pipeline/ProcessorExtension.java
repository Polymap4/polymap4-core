/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data.pipeline;

import java.util.Properties;

import org.eclipse.jface.preference.IPreferencePage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.project.PipelineHolder;

/**
 * Provides access the data of an extension of extension point
 * <code>org.polymap.core.data.pipeline.processors</code>.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ProcessorExtension {

    public static final String          EXTENSION_POINT_NAME = "pipeline.processors";

    public static ProcessorExtension[] allExtensions() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME );
        ProcessorExtension[] result = new ProcessorExtension[ elms.length ];
        for (int i=0; i<elms.length; i++) {
            result[i] = new ProcessorExtension( elms[i] );
        }
        return result;
    }
    
    public static ProcessorExtension forExtensionId( String id ) {
        IConfigurationElement[] elms = Platform.getExtensionRegistry().getConfigurationElementsFor(
                DataPlugin.PLUGIN_ID, EXTENSION_POINT_NAME, id );
        
        if (elms.length > 1) {
            throw new IllegalStateException( "More than 1 extension: " + elms );
        }
        return elms.length > 0
                ? new ProcessorExtension( elms[0] )
                : null;
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement       ext;

    
    public ProcessorExtension( IConfigurationElement ext ) {
        super();
        this.ext = ext;
    }
    
    public String getExtensionId() {
        return ext.getDeclaringExtension().getUniqueIdentifier();
    }

    public String getName() {
        return ext.getAttribute( "name" );
    }
    
    public String getDescription() {
        return ext.getAttribute( "description" );
    }
    
    public PipelineProcessor newProcessor()
    throws CoreException {
        return (PipelineProcessor)ext.createExecutableExtension( "class" );
    }

    public ProcessorPropertyPage newPropertyPage()
    throws CoreException {
        return (ProcessorPropertyPage)ext.createExecutableExtension( "propertyPage" );
    }

    
    /**
     * The property page of a {@link ProcessorExtension}.
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    public interface ProcessorPropertyPage
            extends IPreferencePage {
        
        void init( PipelineHolder holder, Properties props );
        
    }
    
}
