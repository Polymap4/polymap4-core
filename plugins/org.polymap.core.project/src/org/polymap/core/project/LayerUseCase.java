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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;

/**
 * The layer use case specifies the target a layer is used for. Example use
 * cases are "image", "image.print" or "feature". Depending on the use case (and
 * the layer configuration) the {@link PipelineIncubator} creates different
 * pipelines.
 * <p>
 * Use cases are represented as a dot separated string. The different names
 * define different levels.
 * <p>
 * Instances of this class are immutable. Changing methods produce a new
 * instance instead of changing the receiver.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LayerUseCase {

    /**
     * This use case specifies that a resource is used as an image provider.
     */
    public static final LayerUseCase        IMAGE = new LayerUseCase( "image" );

    /**
     * This use case specifies that a resource is used as an image provider.
     */
    public static final LayerUseCase        ENCODED_IMAGE = new LayerUseCase( "encoded_image" );

//    /**
//     * This use case specifies that a resource is used as an image
//     * provider for printing.
//     */
//    public static final LayerUseCase        IMAGE_PRINT = IMAGE.addLevel( "print" );

    /**
     * This static global use case specifies that a resource is used as a
     * {@link FeatureSource} provider.
     */
    public static final LayerUseCase        FEATURES = new LayerUseCase( "features" );

    /**
     * This static global use case specifies that a resource is used as a
     * {@link FeatureStore} provider.
     */
    public static final LayerUseCase        FEATURES_TRANSACTIONAL = FEATURES.addLevel( "transactional" );
    

    private List                parts = new ArrayList( 3 );
    
    private String              s;
    
    
    public LayerUseCase( String s ) {
        this.s = s;
        
        StringTokenizer tokenizer = new StringTokenizer( s, "." );
        while (tokenizer.hasMoreTokens()) {
            parts.add( tokenizer.nextToken() );
        }
    }
    
    public String toString() {
        return asString();
    }

    public String asString() {
        return s;
    }

    public LayerUseCase addLevel( String level ) {
        assert level.indexOf( '.' ) != 0;
        return new LayerUseCase( s + "." + level );
    }
    
    /**
     * Returns true if the receiver contains all levels of the given use case.
     *  
     * @param other
     */
    public boolean isCompatible( LayerUseCase other ) {
        return s.startsWith( other.s );
    }

}
