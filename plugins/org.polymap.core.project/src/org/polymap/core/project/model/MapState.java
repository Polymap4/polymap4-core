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

package org.polymap.core.project.model;

import java.util.StringTokenizer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;

import org.eclipse.core.runtime.Platform;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ITempLayer;
import org.polymap.core.project.LayerVisitor;
import org.polymap.core.project.MapStatus;
import org.polymap.core.project.RenderStatus;
import org.polymap.core.qi4j.AssocCollectionImpl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface MapState
        extends IMap {

    @Optional
    Property<String>                crsCode();
  
    @Optional
    Property<String>                maxExtentStr();

    /** Bidirectional association with {@link LayerState#map()}. */
    @Optional
    @UseDefaults
    ManyAssociation<ILayer>         layers();

    /** Bidirectional association with {@link MapState#parent()}. */
    @Optional
    @UseDefaults
    ManyAssociation<IMap>           maps();


    /**
     * Transient fields and methods. 
     * <p>
     * Impl. note: property change events are handled by the
     * {@link ChangeEventSideEffect}.
     */
    public static abstract class Mixin
            implements MapState {
        
        private static Log log = LogFactory.getLog( Mixin.class );

        @This MapComposite                  composite;
        
        /** The cache of the {@link #crsCode()} property. */
        private CoordinateReferenceSystem   crs;
        
        private ReferencedEnvelope          currentExtent;
        
        /** The cache of the {@link #maxExtentStr} attribute. */
        private ReferencedEnvelope          maxExtent;

        private MapStatus                   mapStatus = MapStatus.STATUS_OK;
        
        private RenderStatus                renderStatus = RenderStatus.STATUS_OK;

        private ListManyAssociation<ITempLayer> tempLayers = new ListManyAssociation();

        /**
         * Not used, see {@link IMap}. 
         */
        public Object getAdapter( Class adapter ) {
            return Platform.getAdapterManager().getAdapter( this, adapter );
        }    

        /**
         * The children maps association.
         */
        public AssocCollection<IMap> getMaps() {
            return new AssocCollectionImpl( maps() );
        }

        public boolean addMap( IMap map ) {
            boolean result = maps().add( map );
            ((org.polymap.core.project.ParentMap)map).setParentMap( composite );
            return result;
        }
        
        public boolean removeMap( IMap map ) {
            boolean result = maps().remove( map );
            ((ParentMap)map).setParentMap( null );
            return result;
        }

        /**
         * The layers association.
         */
        public AssocCollection<ILayer> getLayers() {
            return new AssocCollectionImpl( new ManyAssociation[] {
                    layers(), tempLayers } );
        }

        public boolean addLayer( ILayer layer ) {
            boolean result = layer instanceof ITempLayer
                    ? tempLayers.add( (ITempLayer)layer )
                    : layers().add( layer );
                    
            layer.setParentMap( composite );
            return result;            
        }
        
        public boolean removeLayer( ILayer layer ) {
            boolean result = layer instanceof ITempLayer
                    ? tempLayers.remove( (ITempLayer)layer )
                    : layers().remove( layer );
                    
            layer.setParentMap( null );
            return result;
        }        

        /**
         * The CRS property and fields.
         */
        public synchronized CoordinateReferenceSystem getCRS() {
            if (crs == null) {
                try {
                    crs = CRS.decode( getCRSCode() );
                }
                catch (Exception e) {
                    // checked at set, should never happen
                    throw new RuntimeException( e.getMessage(), e );
                }
            }
            return crs;
        }

        public String getCRSCode() {
            String code = crsCode().get();
            return code != null ? code : "EPSG:4326";
        }

        public void setCRSCode( String code )
                throws NoSuchAuthorityCodeException, FactoryException, TransformException {
            crsCode().set( code );    
            this.crs = CRS.decode( getCRSCode() );
            if (currentExtent != null) {
                setExtent( currentExtent.transform( crs, true ) );
            }
            if (maxExtent != null) {
                setMaxExtent( maxExtent.transform( crs, true ) );
            }
        }

        /**
         * The current extend field.
         */
        public ReferencedEnvelope getExtent() {
            return currentExtent;    
        }
        
        public ReferencedEnvelope setExtent( ReferencedEnvelope extent ) {
            ReferencedEnvelope old = currentExtent;
            this.currentExtent = extent;
            return old;
        }

        public void updateExtent( ReferencedEnvelope extent ) {
            this.currentExtent = extent;            
        }

        /**
         * The max extend property.
         */
        public ReferencedEnvelope getMaxExtent() {
            if (maxExtent == null) {
                String str = maxExtentStr().get();
                if (str != null) {
                    // decode
                    StringTokenizer stn = new StringTokenizer( str, "_" );
                    int dimension = Integer.parseInt( stn.nextToken() );
                    maxExtent = new ReferencedEnvelope(
                            Double.parseDouble( stn.nextToken() ),
                            Double.parseDouble( stn.nextToken() ),
                            Double.parseDouble( stn.nextToken() ),
                            Double.parseDouble( stn.nextToken() ),
                            getCRS() );
                }
            }
            return maxExtent;
        }
        
        public void setMaxExtent( ReferencedEnvelope extent ) {
            assert extent != null : "extent == null";
            assert getCRS().equals( extent.getCoordinateReferenceSystem() ) : "incompatible CRS";

            ReferencedEnvelope old = maxExtent;
            this.maxExtent = extent;
            
            // encode
            final StringBuilder buf = new StringBuilder( 256 );
            final int dimension = maxExtent.getDimension();
            buf.append( dimension );
            for (int i=0; i<dimension; i++) {
                buf.append( "_" );
                buf.append( maxExtent.getMinimum( i ) );
                buf.append( "_" );
                buf.append( maxExtent.getMaximum( i ) );
            }
            log.debug( "setMaxExtent: " + buf.toString() );
            maxExtentStr().set( buf.toString() );
        }

        
        public String toString() {
            return "MapImpl[label=" + getLabel() +"]";
        }
        

        public MapStatus getMapStatus() {
            return mapStatus;
        }

        public void setMapStatus( MapStatus status ) {
            assert status != null : "status == null";
            MapStatus old = mapStatus;
            mapStatus = status;
        }

        public RenderStatus getRenderStatus() {
            return renderStatus;
        }
        
        public void setRenderStatus( RenderStatus status ) {
            assert status != null : "status == null";
            RenderStatus old = renderStatus;
            renderStatus = status;
        }
        
        public <T> T visit( LayerVisitor<T> visitor ) {
            for (ILayer layer : getLayers()) {
                if (!visitor.visit( layer )) {
                    break;
                }
            }
            return visitor.result;
        }

    }
    
}
