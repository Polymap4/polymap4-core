/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.report;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.LazyInit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ReportFactoryExtension {

    private static Log log = LogFactory.getLog( ReportFactoryExtension.class );
    
    public static final String              POINT_ID = "reports";
    
    public static LazyInit<List<ReportFactoryExtension>> exts = new CachedLazyInit( 1024 );
    
    
    public static List<ReportFactoryExtension> all() {
        return exts.get( new Supplier<List<ReportFactoryExtension>>() {
            public List<ReportFactoryExtension> get() {
                IExtensionRegistry reg = Platform.getExtensionRegistry();
                IConfigurationElement[] elms = reg.getConfigurationElementsFor( 
                        DataPlugin.PLUGIN_ID, POINT_ID );

                List<ReportFactoryExtension> result = new ArrayList( elms.length );
                for (int i=0; i<elms.length; i++) {
                    result.add( new ReportFactoryExtension( elms[i] ) );
                }
                return result;
            }
        });
    }

    
    public static List<IReport> reportsFor( IReportSite site ) {
        List<IReport> result = new ArrayList();
        for (ReportFactoryExtension ext : all()) {
            try {
                IReportFactory factory = ext.createReportFactory();
                result.addAll( factory.createReports( site ) );
            }
            catch (Exception e) {
                log.warn( "Error while initializing IReportFactory: ", e );
            }
        }
        return result;
    }


    // instance *******************************************
    
    private IConfigurationElement       config;
    
    
    public ReportFactoryExtension( IConfigurationElement config ) {
        this.config = config;
    }

    
    public IReportFactory createReportFactory() {
        try {
            return (IReportFactory)config.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }
    
}
