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
package org.polymap.core.data.util;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;

/**
 * Provides static helper methods that work with {@link Geometry}, {@link CRS} and
 * {@link MathTransform}. Instances of CRS and MathTransform are globally cached.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Geometries {

    private static Log log = LogFactory.getLog( Geometries.class );
    
    private static Cache<String,CoordinateReferenceSystem>  crsCache = CacheConfig.defaults().initSize( 32 ).createCache();
 
    private static Cache<TransformKey,MathTransform>        transformCache = CacheConfig.defaults().initSize( 32 ).createCache();
 
    
    /**
     * Returns the result of {@link CRS#decode(String)} but caches the result.
     *
     * @see CRS#decode(String)
     * @param  code The Coordinate Reference System authority code.
     * @return The Coordinate Reference System for the provided code.
     * @throws NoSuchAuthorityCodeException If the code could not be understood.
     * @throws FactoryException if the CRS creation failed for an other reason.
     */
    public static CoordinateReferenceSystem crs( final String code ) throws Exception {
        return crsCache.get( code, key -> {
            try {
                return CRS.decode( code );
            }
            catch (Exception e) {
                return CRS.parseWKT( code );
            }
        });
    }

    public static String srs( CoordinateReferenceSystem crs ) {
        String result = CRS.toSRS( crs );
        if (!result.startsWith( "EPSG:" )) {
            result = crs.toWKT();
        }
        return result;

//      if (!crs.getIdentifiers().isEmpty()) {
//      Object next = crs.getIdentifiers().iterator().next();
//      if (next instanceof Identifier) {
//          Identifier identifier = (Identifier) next;
//          
//          crsCode().set( identifier.toString() );
//          this.crs = crs;
//          
////          if (identifier.getAuthority().getTitle().equals(
////                  "European Petroleum Survey Group")) {
////              crsCode.set( this, "EPSG:" + identifier.getCode() );
////              this.crs = crs;
////          }
//          return;
//      }
//  }
        
    }
    
    /**
     * 
     */
    protected static class TransformKey {

        CoordinateReferenceSystem   source, target;
        
        public TransformKey( CoordinateReferenceSystem source, CoordinateReferenceSystem target ) {
            assert source != null;
            assert target != null;
            this.source = source;
            this.target = target;
        }

        public int hashCode() {
            int result = 1;
            result = 31 * result + ((source == null) ? 0 : source.hashCode());
            result = 31 * result + ((target == null) ? 0 : target.hashCode());
            return result;
        }

        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            else {
                TransformKey rhs = (TransformKey)obj;
                return source.equals( rhs.source ) && target.equals( rhs.target );
            }
        }
    };

    
    /**
     * @see CRS#findMathTransform(CoordinateReferenceSystem, CoordinateReferenceSystem, boolean)
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @return The math transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException If no math transform can be created for the specified source and
     *         target CRS.
     */
    public static MathTransform transform( final CoordinateReferenceSystem source, final CoordinateReferenceSystem target )
    throws Exception {
        TransformKey key = new TransformKey( source, target );
        return transformCache.get( key, _key -> CRS.findMathTransform( source, target ) );        
    }
    
    
    /**
     * Convenience for <code>transform( crs( sourceCode ), crs( targetCode ) )</code>.
     * 
     * @see #transform(CoordinateReferenceSystem, CoordinateReferenceSystem)
     */
    public static MathTransform transform( String sourceCode, String targetCode )
    throws Exception {
        return transform( crs( sourceCode ), crs( targetCode ) );
    }
    
    
    public static <T extends Geometry> T transform( T geom, CoordinateReferenceSystem source, CoordinateReferenceSystem target ) 
    throws MismatchedDimensionException, TransformException, Exception {
        return (T)JTS.transform( geom, transform( source, target ) );
    }
    
    
    public static <T extends Geometry> T transform( T geom, String sourceCode, String targetCode ) 
    throws MismatchedDimensionException, TransformException, Exception {
        return (T)JTS.transform( geom, transform( sourceCode, targetCode ) );
    }


    public static ReferencedEnvelope transform( ReferencedEnvelope bounds, CoordinateReferenceSystem target )
    throws TransformException, Exception {
        Envelope result = JTS.transform( bounds, transform( bounds.getCoordinateReferenceSystem(), target ) );
        return new ReferencedEnvelope( result, target );
    }
    
}
