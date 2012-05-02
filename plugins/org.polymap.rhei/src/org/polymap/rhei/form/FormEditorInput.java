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
package org.polymap.rhei.form;

import java.util.Collections;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 1.0
 */
public class FormEditorInput
        implements IEditorInput, IPersistableElement, IElementFactory {

    private static Log log = LogFactory.getLog( FormEditorInput.class );

    public static final String  FACTORY_ID = "org.polymap.rhei.FormEditorInputFactory";

    private FeatureStore        fs;
    
    private Feature             feature;
    

    public FormEditorInput( FeatureStore fs, Feature feature ) {
        super();
        assert fs != null : "fs is null!";
        assert feature != null : "feature is null!";
        this.feature = feature;
        this.fs = fs;
    }

    /**
     * Creates the factory instance that is used to {@link #createElement(IMemento)}.
     */
    public FormEditorInput() {
    }

    /**
     * Implements {@link IElementFactory}: initialize a new instance from settings in
     * the memento. This is called after no-args ctor.
     */
    public IAdaptable createElement( IMemento memento ) {
        final String fid = memento.getString( "fid" );
        final String layerId = memento.getString( "layerId" );
        
        if (fid != null && layerId != null) {
            FeatureIterator it = null;
            try {
                ILayer layer = ProjectRepository.instance().findEntity( ILayer.class, layerId );
                if (layer != null) {
                    PipelineFeatureSource _fs = PipelineFeatureSource.forLayer( layer, true );
                    FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
                    Id filter = ff.id( Collections.singleton( ff.featureId( fid ) ) );

                    it = _fs.getFeatures( filter ).features();
                    if (it.hasNext()) {
                        Feature _feature = it.next();
                        return new FormEditorInput( _fs, _feature );
                    }
                }
            }
            catch (NoSuchEntityException e) {
                log.warn( "Layer does no longer exists: " + layerId );
            }
            catch (Exception e) {
                log.warn( "Unable to restore FormEditorInput.", e );
            }
            finally {
                if (it != null) { it.close(); }
            }
        }
        return null;
    }
    
    public IPersistableElement getPersistable() {
        return this;
    }

    public void saveState( IMemento memento ) {
        if (feature != null && (fs instanceof PipelineFeatureSource)) {
            memento.putString( "fid", feature.getIdentifier().getID() );
            memento.putString( "layerId", ((PipelineFeatureSource)fs).getLayer().id() );
        }
    }

    public String getFactoryId() {
        return FACTORY_ID;
    }

    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj != null && obj instanceof FormEditorInput) {
            FormEditorInput rhs = (FormEditorInput)obj;
            FeatureId fid1 = rhs.getFeature().getIdentifier();
            FeatureId fid2 = getFeature().getIdentifier();
            return fid1.equals( fid2 );
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return feature.hashCode();
    }

    public FeatureStore getFeatureStore() {
        return fs;
    }

    public Feature getFeature() {
        return feature;
    }

    /**
     * The layer of the feature, or null if the FeatureStore is not an instance of
     * {@link PipelineFeatureSource}.
     */
    public ILayer getLayer() {
        return fs instanceof PipelineFeatureSource ? ((PipelineFeatureSource)fs).getLayer() : null;
    }

    public String getEditorId() {
        return FormEditor.ID;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "FormEditorInput";
    }

    public String getToolTipText() {
        return feature != null ? feature.getIdentifier().getID() : "Feature";
    }

    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( feature.getClass() )) {
            return feature;
        }
        return null;
    }

}
