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
package org.polymap.core.project.model.operations;

import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;

import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.Messages;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

/**
 * Returns the bounds of the layer as best estimated. The bounds will be
 * reprojected into the crs provided. If the crs parameter is null then the
 * native envelope will be returned. If the native projection is not known or if
 * a transformation is not possible then the native envelope will be returned..
 * This method may be blocking.
 * <p>
 * Result: the envelope of the layer. If the native crs is not known or if a
 * transformation is not possible then the untransformed envelope will be
 * returned.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@Mixins( SetLayerBoundsOperation.Mixin.class )
public interface SetLayerBoundsOperation
        extends IUndoableOperation, TransientComposite {

    /**
     * @param The layer to set the bounds.
     * @param crs the desired CRS for the returned envelope.
     */
    public void init( ILayer layer, CoordinateReferenceSystem crs );

    /** Implementation is provided bei {@link AbstractOperation} */ 
    public boolean equals( Object obj );
    
    public int hashCode();

    
    /**
     * Implementation. 
     */
    public static abstract class Mixin
            extends AbstractModelChangeOperation
            implements SetLayerBoundsOperation {

        private static Log log = LogFactory.getLog( SetLayerBoundsOperation.class );

        private ILayer                      layer;

        private CoordinateReferenceSystem   crs;

        public ReferencedEnvelope           result;
    

        public Mixin() {
            super( "[undefined]" );
        }

        public void init( ILayer _layer, CoordinateReferenceSystem _crs ) {
            setLabel( Messages.get( "SetLayerBoundsOperation_titlePrefix" ) + layer.getLabel() );
            this.layer = _layer;
            this.crs = _crs;
        }


        public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
        throws ExecutionException {
            try {
                //            monitor.subTask( "sleeping..." );
                //            log.debug( "sleeping..." );
                //            Thread.sleep( 10000 );
                //            monitor.worked( 1 );

                monitor.subTask( "obtaining bounds..." );
                result = Helper.obtainBoundsFromResources( layer, crs, monitor );
                monitor.worked( 1 );
            }
            catch (Exception e) {
                throw new ExecutionException( "Failure obtaining bounds", e );
            }

            if (result != null && !result.isNull()) {
                if (crs != null) {
                    try {
                        monitor.subTask( "transforming bounds..." );
                        result = result.transform( crs, true );
                        monitor.worked( 1 );
                    } 
                    catch (Exception fe) {
                        throw new ExecutionException( "failure to transform layer bounds", fe );
                    }
                }
            } 
            else {
                result = new ReferencedEnvelope( new Envelope(), null );
            }
            return Status.OK_STATUS;
        }

    }


    /**
     * Static helper that exposes the {@link #obtainBoundsFromResources(ILayer, CoordinateReferenceSystem, IProgressMonitor)}
     * to other operations.
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     * @version POLYMAP3 ($Revision$)
     * @since 3.0
     */
    public static class Helper {

        public static ReferencedEnvelope obtainBoundsFromResources( ILayer layer, CoordinateReferenceSystem crs,
                IProgressMonitor monitor )
                throws IOException {
            ReferencedEnvelope bounds = null;
            IGeoResource geores = layer.getGeoResource();
            IGeoResourceInfo info = geores.getInfo( monitor );
            Envelope tmp = (info != null) ? info.getBounds() : null;

            if (tmp == null) {
                return ReferencedEnvelope.EVERYTHING;
            }
            if (tmp instanceof ReferencedEnvelope
                    && ((ReferencedEnvelope)tmp).getCoordinateReferenceSystem() != null) {
                bounds = (ReferencedEnvelope)tmp;
            } 
            else {
                bounds = new ReferencedEnvelope(
                        tmp.getMinX(), tmp.getMaxX(), tmp.getMinY(), tmp.getMaxY(), crs );
            }
            return bounds;
        }

    }

}
