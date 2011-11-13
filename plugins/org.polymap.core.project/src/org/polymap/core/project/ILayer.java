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

package org.polymap.core.project;

import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;

import org.geotools.styling.Style;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.Entity;
import org.polymap.core.model.ModelProperty;
import org.polymap.core.model.TransientProperty;
import org.polymap.core.model.security.ACL;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.style.IStyle;

/**
 * A Layer represents an {@link IGeoResource} inside an {@link IMap}.
 * <p>
 * Setting attributes is done via Commands.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 *         <li>30.10.2009: created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface ILayer
        extends Entity, Labeled, ACL, ParentMap, PipelineHolder, IAdaptable,
                PropertyChangeSupport {

    public static final String      PROP_OPACITY = "opacity";
    public static final String      PROP_ORDERKEY = "orderkey";
    public static final String      PROP_CRSCODE = "crscode";
    public static final String      PROP_GEORESID = "georesid";
    public static final String      PROP_STYLE = "style";
    
    public static final String      PROP_VISIBLE = "visible";
    public static final String      PROP_EDITABLE = "editable";
    public static final String      PROP_SELECTABLE = "selectable";

    public static final String      PROP_LAYERSTATUS = "layerstatus";

    /** The layer type returned by {@link #getLayerType()}. */
    public static final int         LAYER_VECTOR        = 1;
    
    /** The layer type returned by {@link #getLayerType()}. */
    public static final int         LAYER_RASTER        = 2;


    /**
     * Find the spatial resource of this layer.
     * <p>
     * Do not use this method to access the feature data or type of the layer
     * directly. See {@link org.polymap.core.data.PipelineFeatureSource}
     * instead.
     * <p>
     * This method dynamically resolves the {@link IGeoResource} from the local
     * catalog. Therefore it may block for some time and/or return null. The
     * process runs {@link IUndoableOperation} that the user can cancel. In this
     * case null is returned.
     * 
     * @see OperationSupport
     * @return The {@link IGeoResource} of this layer or null if the resource is
     *         not available currently or generally.
     */
    public IGeoResource getGeoResource();
    
    @ModelProperty(PROP_GEORESID)
    public void setGeoResource( IGeoResource selectedGeoRes );


    public CoordinateReferenceSystem getCRS();

    public String getCRSCode();

    @ModelProperty(PROP_CRSCODE)
    public void setCRS( CoordinateReferenceSystem crs );
    
    
    /**
     * Gets the style of this layer. The returned instance must not be used to
     * change the style. Instead the {@link #getSymbolizerChanger()} has to be
     * used.
     * <p>
     * This method dynamically resolves the {@link Style} from the local style
     * catalog. Therefore it may block and/or return null.
     * 
     * @return The {@link Style} of this layer or null if the style is not
     *         available currently or generally.
     */
    public IStyle getStyle();

    /**
     * Signals the layer that style has changed.
     * 
     * @param style
     */
    @TransientProperty(PROP_STYLE)
    public void setStyle( IStyle style )
    throws UnsupportedOperationException, IOException;

    /**
     * The interface to change the symbolizers of the style of this layer.
     * <p>
     * XXX Irgendwie muesste man hier aber auch noch die rule identifizieren,
     * oder?
     * 
     * @see #getStyle()
     */
    public Object getSymbolizerChanger();
    
    
    public int getOrderKey();
    
    @ModelProperty(PROP_ORDERKEY)
    public int setOrderKey( int value );
    
    
    public int getOpacity();
    
    @ModelProperty(PROP_OPACITY)
    public int setOpacity( int value );
    
    
    /**
     * The type of this layer.
     * 
     * @return One of the <code>LAYER_XXX</code> constants.
     */
    public int getLayerType();
    
    
    /**
     * True, if this layer is visible.
     */
    public boolean isVisible();
    
    @TransientProperty(PROP_VISIBLE)
    public void setVisible( boolean visible );
    
    
    /**
     * True, if this layer is in edit mode.
     */
    public boolean isEditable();
    
    @TransientProperty(PROP_EDITABLE)
    public void setEditable( boolean editable );


    /**
     * True, if this layer is select mode. In select mode the objects/features
     * of this layer are 
     */
    public boolean isSelectable();

    @TransientProperty(PROP_SELECTABLE)
    public void setSelectable( boolean selectable );


    /**
     * Indication of Layer status. This is used to provide feedback for a Layers
     * status, waiting or missing resource.
     * 
     * @return {@link Status#OK_STATUS}, {@link Status#CANCEL_STATUS} or another status.
     */
    public LayerStatus getLayerStatus();
    
    @TransientProperty(PROP_LAYERSTATUS)
    public void setLayerStatus( LayerStatus status );


    /**
     * Indication of Layer status.
     * This is used to provide feedback for a Layers rendering status.
     * 
     * @return {@link Status#OK_STATUS}, {@link Status#CANCEL_STATUS} or another status.
     */
    public RenderStatus getRenderStatus();
    
    @TransientProperty("renderStatus")
    public void setRenderStatus( RenderStatus status );

}
