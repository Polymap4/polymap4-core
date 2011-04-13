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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;

import org.geotools.data.FeatureStore;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FormEditorInput
        implements IEditorInput {

    private static Log log = LogFactory.getLog( FormEditorInput.class );

    private FeatureStore        fs;
    
    private Feature             feature;
    
    
    public FormEditorInput( FeatureStore fs, Feature feature ) {
        super();
        assert fs != null : "fs is null!";
        assert feature != null : "feature is null!";
        this.feature = feature;
        this.fs = fs;
    }

    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof FormEditorInput) {
            return ((FormEditorInput)obj).feature.equals( feature );
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

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "tooltip";
    }

    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( feature.getClass() )) {
            return feature;
        }
        return null;
    }

}
