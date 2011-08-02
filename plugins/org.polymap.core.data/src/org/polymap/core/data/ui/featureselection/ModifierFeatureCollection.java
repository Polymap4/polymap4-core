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
package org.polymap.core.data.ui.featureselection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.DecoratingFeatureCollection;

import org.eclipse.swt.widgets.Item;

import org.eclipse.jface.viewers.ICellModifier;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.operations.ModifyFeaturesOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * Decorates a {@link FeatureCollection} with an {@link IAdaptable} and an
 * {@link ICellModifier} interface to support feature editing in the
 * {@link GeoSelectionView}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 * @deprecated See {@link GeoSelectionView}.
 */
class ModifierFeatureCollection<T extends FeatureType, F extends Feature>
        extends DecoratingFeatureCollection<T, F>
        implements ICellModifier, IAdaptable {

    private static Log log = LogFactory.getLog( ModifierFeatureCollection.class );

    private FeatureStore                    fs;

    private ILayer                          layer;
    

    public ModifierFeatureCollection( ILayer layer, FeatureStore fs, FeatureCollection<T, F> delegate ) {
        super( delegate );
        this.fs = fs;
        this.layer = layer;
    }

    public Object getAdapter( Class adapter ) {
        log.debug( "getAdapter(): " + adapter );
        if (ICellModifier.class.isAssignableFrom( adapter ) ) {
            return this;
        }
        return null;
    }

    public Object getValue( Object element, String property ) {
        log.debug( "getValue(): element=" + element + ", property=" + property );
        SimpleFeature feature = (SimpleFeature)element;
        Object attr = feature.getAttribute( property );
        log.debug( "    attr: " + attr.getClass() );
        return attr;
    }

    public boolean canModify( Object element, String property ) {
        log.debug( "canModify(): element=" + element + ", property=" + property );
        return true;
    }

    public void modify( Object element, String property, Object value ) {
        log.debug( "modify(): element=" + element + ", property=" + property );
        if (element instanceof Item) {
            element = ((Item)element).getData();
        }
        if (element == null) {
            return;
        }
        try {
            SimpleFeature feature = (SimpleFeature)element;
            ModifyFeaturesOperation op = new ModifyFeaturesOperation( 
                    layer, fs, feature.getID(), property, value );
            OperationSupport.instance().execute( op, true, false );
        }
        catch (Exception e) {
            log.warn( "", e );
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, null, e );
        }
        
    }

}
