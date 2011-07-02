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
package org.polymap.core.data.operations;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;

import com.vividsolutions.jts.geom.GeometryFactory;

import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.operation.JobMonitors;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Modifies features. This operation is triggered by
 * {@link ModifyFeatureEditorAction} but might be used by other classes as well.
 * <p>
 * ...
 * 
 * @see NewFeatureOperation
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ModifyFeaturesOperation
        extends AbstractOperation
        implements IUndoableOperation {
    
    private static Log log = LogFactory.getLog( ModifyFeaturesOperation.class );

    private static final FilterFactory      ff = CommonFactoryFinder.getFilterFactory( null );

    private static final GeometryFactory    gf = new GeometryFactory();

    private ILayer                  layer;
    
    private String                  fid;
    
    private FeatureStore            fs;
    
    private AttributeDescriptor[]   types;
    
    private Object[]                values;
    
    private Filter                  filter;


    public ModifyFeaturesOperation( ILayer layer, FeatureStore fs, String fid, String property, Object value ) {
        super( Messages.get( "ModifyFeaturesOperation_labelPrefix" ) );
        this.layer = layer;
        this.fid = fid;
        this.fs = fs;
        this.types = new AttributeDescriptor[] {
                (AttributeDescriptor)fs.getSchema().getDescriptor( property ) };
        this.values = new Object[] { value };
        this.filter = ff.id( Collections.singleton( ff.featureId( fid ) ) );
    }

    public ModifyFeaturesOperation( FeatureStore fs, Filter filter,
            AttributeDescriptor[] types, Object[] values ) {
        super( Messages.get( "ModifyFeaturesOperation_labelPrefix" ) );
        this.fs = fs;
        this.types = types;
        this.values = values;
        this.filter = filter;
    }

    public FeatureStore getFeatureStore() {
        return fs;
    }
    
    public IStatus execute( IProgressMonitor _monitor, IAdaptable info )
            throws ExecutionException {
        try {
            // display and monitor
            Display display = Polymap.getSessionDisplay();
            log.debug( "### Display: " + display );
            OffThreadProgressMonitor monitor = new OffThreadProgressMonitor( _monitor );
            JobMonitors.set( monitor );
            monitor.subTask( getLabel() );
            
            // process values
            FeatureType schema = fs.getSchema();
            for (Object value : values) {
//                // MultiXXX geometries
//                if (value instanceof Geometry) {
//                    Geometry geom = (Geometry)value;
//                    String geomName = schema.getGeometryDescriptor().getType().getName().toString();
//                    log.debug( "    geometry: " + geomName );
//                    if ("MultiLineString".equals( geomName )) {
//                        log.debug( "    convert geometry: " + geom + " -> " + geomName ); 
//                        geom = gf.createMultiLineString( new LineString[] {(LineString)geom} );
//                    }
//                    else if ("MultiPolygon".equals( geomName )) {
//                        log.debug( "    convert geometry: " + geom + " -> " + geomName ); 
//                        geom = gf.createMultiPolygon( new Polygon[] {(Polygon)geom} );
//                    }
//                }
            }

            // modify feature
            fs.modifyFeatures( types, values, filter );
            log.info( "### Feature(s) modified" );

            monitor.worked( 1 );
            
            // update UI
            display.asyncExec( new Runnable() {
                public void run() {
                    try {
                        // hover event
                        LayerFeatureSelectionManager fsm = LayerFeatureSelectionManager.forLayer( layer );
                        fsm.setHovered( fid );

                        // XXX update map editor
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getMessage(), e );
                    }
                }
            });
        }
        catch (Exception e) {
            throw new ExecutionException( "Failure...", e );
        }
        finally {
            JobMonitors.remove();
        }
        return Status.OK_STATUS;
    }


    public boolean canUndo() {
        return false;
    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean canRedo() {
        return false;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

}
