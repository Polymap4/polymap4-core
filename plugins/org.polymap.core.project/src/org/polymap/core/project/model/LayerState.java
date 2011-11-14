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

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.IOException;
import java.net.URL;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.core.internal.CorePlugin;

import org.geotools.data.FeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

import org.eclipse.ui.views.properties.IPropertySource;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.project.IGeoResourceResolver;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerStatus;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.RenderStatus;
import org.polymap.core.project.ui.properties.LayerPropertySource;
import org.polymap.core.style.IStyle;
import org.polymap.core.style.IStyleCatalog;
import org.polymap.core.style.StylePlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface LayerState
        extends ILayer {
    
    @Optional
    Property<String>                georesId();
    
    @Optional
    Property<String>                crsCode();
    
    @Optional
    Property<String>                styleId();
    
    @Optional
    @UseDefaults
    Property<Integer>               orderKey();
    
    @Optional
    @UseDefaults
    Property<Integer>               opacity();


    /**
     * Transient fields and methods.
     * <p>
     * Impl. note: property change events are handled by the
     * {@link ChangeEventSideEffect}.
     */
    public static abstract class Mixin
            implements LayerState {
        
        private static final Log log = LogFactory.getLog( Mixin.class );

        /** The cache of the {@link #georesId()} property. */
        private IGeoResource                    geores;

        private ReadWriteLock                   georesLock = new ReentrantReadWriteLock();
        
        /** The cache of the {@link #crsCode()} property. */
        private CoordinateReferenceSystem       crs;
        
        /** The cache of the {@link #styleId()} property. */
        private IStyle                          style;
        
        private LayerStatus                     layerStatus = LayerStatus.STATUS_OK;
        
        private RenderStatus                    renderStatus = RenderStatus.STATUS_OK;
        
        private boolean                         visible = false;
        
        private boolean                         editable = false;
        
        private boolean                         selectable = false;
        
        
        public Object getAdapter( Class adapter ) {
            if (IPropertySource.class.isAssignableFrom( adapter )) {
                return new LayerPropertySource( this );
            }
            else {
                return null;
            }
        }    

        public int getOrderKey() {
            Integer result = orderKey().get();
            return result != null ? result : 0;
        }

        public int setOrderKey( int value ) {
            int old = orderKey().get();
            orderKey().set( value );
            return old;
        }
        
        public int getOpacity() {
            Integer result = opacity().get();
            return result != null ? result : 100;
        }

        public int setOpacity( int value ) {
            int old = opacity().get();
            opacity().set( value );
            return old;
        }
        
        public int getLayerType() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible( boolean visible ) {
            log.info( "this.visible=" + this.visible + ", new= " + visible );
            if (this.visible != visible) {
                boolean old = this.visible;
                this.visible = visible;
            }
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable( boolean editable ) {
            log.info( "this.editable=" + this.editable + ", new= " + editable );
            if (this.editable != editable) {
                boolean old = this.editable;
                this.editable = editable;
            }
        }

        public boolean isSelectable() {
            return selectable;
        }

        public void setSelectable( boolean selectable ) {
            log.info( "this.selectable=" + this.selectable + ", new= " + selectable );
            if (this.selectable != selectable) {
                boolean old = this.selectable;
                this.selectable = selectable;
            }
        }

        public String toString() {
            return "LayerImpl[label=" + getLabel() +"]";
        }


        public LayerStatus getLayerStatus() {
            return layerStatus;
        }
        
        public void setLayerStatus( LayerStatus status ) {
            assert status != null : "status == null";
            LayerStatus old = layerStatus;
            layerStatus = status;
        }


        public RenderStatus getRenderStatus() {
            return renderStatus;
        }
        
        public void setRenderStatus( RenderStatus status ) {
            assert status != null : "status == null";
            RenderStatus old = renderStatus;
            renderStatus = status;
        }


        public IStyle getStyle() {
            // XXX check synchronization
            // XXX run in job (if catalog blocks)
            if (style == null) {
                IStyleCatalog catalog = StylePlugin.getStyleCatalog();

                try {
                    //setLayerStatus( LayerStatus.STATUS_WAITING );

                    String style_id = styleId().get();
                    if (style_id != null) {
                        URL url = new URL( null, style_id, CorePlugin.RELAXED_HANDLER );
                        style = catalog.getById( new ID( url ), null );
                        
//                        List<IResolve> canditates = catalog.find( url, null );
//                        for (IResolve resolve : canditates) {
//                            if (resolve.getStatus() == Status.BROKEN) {
//                                continue;
//                            }
//                            if (resolve instanceof IGeoResource) {
//                                results.add( (IGeoResource)resolve );
//                            }
//                        }
                    }
                    else {
                        throw new IllegalStateException( "Layer hat noch keinen Style: " + getLabel() );
                    }
                } 
                catch (Exception e) {
                    log.warn( e.getMessage() );
                    ProjectPlugin.logError( e.getMessage() ); 
                    //setLayerStatus( LayerStatus.STATUS_STYLE_MISSING );

                    try {
                        FeatureSource fs = getGeoResource().resolve( FeatureSource.class, null );
                        style = catalog.createDefaultStyle( fs );
                    }
                    catch (IOException e1) {
                        style = catalog.createDefaultStyle( null );
                    }
                }
            }
            return style;
        }
        
        public void setStyle( IStyle style )
        throws UnsupportedOperationException, IOException {
            IStyle old = this.style;
            this.style = style;
            
            if (style.getID() == null) {
                log.info( "Style is default style: adding to catalog..." );
                IStyleCatalog catalog = StylePlugin.getStyleCatalog();
                catalog.add( style );
                
                styleId().set( style.getID().toURL().toExternalForm() );
            }
            else if (style == old) {
                // make the layer "dirty" in the UI and for service to reload on save
                styleId().set( styleId().get() );
            }
            else {
                throw new IllegalStateException( "Wrong style set: " + style );
            }
            
            // store style in catalog
            style.store( new NullProgressMonitor() );
        }
        
        public Object getSymbolizerChanger() {
            throw new RuntimeException( "not yet implemented." );
        }

        
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

//        public void setCRSCode( String code )
//                throws NoSuchAuthorityCodeException, FactoryException {
//            crsCode().set( code );    
//            this.crs = CRS.decode( getCRSCode() );
//        }
        
        public void setCRS( CoordinateReferenceSystem crs ) {
            // from http://lists.wald.intevation.org/pipermail/schmitzm-commits/2009-July/000228.html
            // If we can determine the EPSG code for this, let's save it as
            // "EPSG:12345" to the file.
            if (!crs.getIdentifiers().isEmpty()) {
                Object next = crs.getIdentifiers().iterator().next();
                if (next instanceof Identifier) {
                    Identifier identifier = (Identifier) next;
                    
                    crsCode().set( identifier.toString() );
                    this.crs = crs;
                    
//                    if (identifier.getAuthority().getTitle().equals(
//                            "European Petroleum Survey Group")) {
//                        crsCode.set( this, "EPSG:" + identifier.getCode() );
//                        this.crs = crs;
//                    }
                    return;
                }
            }
            throw new IllegalArgumentException( "The given crs is not EPSG compatible: " + crs );
        }
        

        public void setGeoResource( IGeoResource geores ) {
            this.geores = geores;
            IGeoResourceResolver resolver = ProjectPlugin.geoResourceResolver( this );
            georesId().set( resolver.createIdentifier( geores ) );
        }


        /**
         * Used to find the associated service in the catalog.
         * <p/>
         * On the off chance *no* services exist an empty list is returned. All this
         * means is that the service is down, or the user has not connected to it yet
         * (perhaps they are waiting on security permissions.
         * <p/>
         * getGeoResource() is a blocking method but it must not block UI thread.
         * With this purpose the new imlementation is done to avoid UI thread
         * blocking because of synchronization.
         */
        public IGeoResource getGeoResource() {
            georesLock.readLock().lock();
            try {
                if (geores == null
                        && layerStatus != LayerStatus.STATUS_MISSING) {
                    georesLock.readLock().unlock();
                    georesLock.writeLock().lock();
                    
                    setLayerStatus( LayerStatus.STATUS_WAITING );
                    try { 
                        IGeoResourceResolver resolver = ProjectPlugin.geoResourceResolver( this );
                        List<IGeoResource> results = resolver.resolve( georesId().get() );
                        if (results.isEmpty()) {
                            setLayerStatus( LayerStatus.STATUS_MISSING );
                            geores = null;
                        } 
                        else {
                            setLayerStatus( LayerStatus.STATUS_OK );
                            geores = results.get( 0 );
                        }
                    } 
                    catch (Exception e) {
                        PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this
                                , "Layer: " + getLabel() + ": error getting GeoResource id:" + georesId().get()
                                , e );
                        setLayerStatus( new LayerStatus( IStatus.ERROR, LayerStatus.MISSING, Messages.get( "LayerStatus_connectionFailed" ), e ) ); //$NON-NLS-1$
                    }
                    finally {
                        georesLock.readLock().lock();
                        georesLock.writeLock().unlock();
                    }
                } 
                return geores;
            }
            finally {
                georesLock.readLock().unlock();
            }
        }

    }
    
}
