/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
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
 * $Id: $
 */
package org.polymap.core.data.feature.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.spatial.BBOX;

import org.geotools.data.Query;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.util.SoftValueHashMap;

/**
 * A memory backed query/feature cache implementation for
 * {@link FeatureCacheProcessor}.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class FeatureCache {

    private static final Log log = LogFactory.getLog( FeatureCacheProcessor.class );

    public static final int             DEFAULT_HARD_CACHED_FEATURES = 100000;
    
    /** The query cache, maps query keys into List of fids. */
    private Map<String,Set<String>>         queryCache = new HashMap();
    
//    private SoftValueHashMap<String,Feature> featureCache = new SoftValueHashMap( DEFAULT_HARD_CACHED_FEATURES );            
  
    private HashMap<String,Feature>         featureCache = new HashMap( DEFAULT_HARD_CACHED_FEATURES );

    /**
     * 
     */
    public class Result {
        
        Set<String>             fids;
        
        List<Feature>           features;
        
        /** True if the result contains all fids and all features for the fids. */
        boolean                 isComplete = false;
    }
    
    
    public Result getFeatures( Query query ) {
        Result result = new Result();
        
        String queryCacheKey = createQueryKey( query );
        // XXX copy the list because it might be changed be putFeatures()
        result.fids = queryCache.get( queryCacheKey );
        
        if (result.fids != null) {
            result.features = new ArrayList( result.fids.size() );
            
            result.isComplete = true;
            for (String fid : result.fids) {
                Feature feature = featureCache.get( fid );
                if (feature != null) {
                    result.features.add( feature );
                }
                else {
                    result.isComplete = false;
                }
            }
        }
        return result;
    }
    
    
    public void putFeatures( Query query, List<Feature> features ) {
        List<String> fids = new ArrayList( features.size() );
        
        // featureCache
        for (Feature feature : features) {
            String fid = feature.getIdentifier().getID();
            featureCache.put( fid, feature );
            fids.add( fid );
        }
        
        // queryCache
        String queryCacheKey = createQueryKey( query );
        synchronized (queryCache) {
            Set<String> queryFids = queryCache.get( queryCacheKey );
            if (queryFids == null) {
                queryFids = new HashSet( fids.size() * 2 );
                queryCache.put( queryCacheKey, queryFids );
            }
            queryFids.addAll( fids );
        }
        log.debug( "putFeatures(): features=" + featureCache.size() + ", queries=" + queryCache.size() );
    }
    
 
    private String createQueryKey( Query query ) {
        StringBuffer returnString = new StringBuffer( "Query:" );

        // this code is originally taken from DefaultQuery
        if (query.getHandle() != null) {
            returnString.append( " [" + query.getHandle() + "]" );
        }

        returnString.append( "\n   feature type: " + query.getTypeName() );

        Filter filter = query.getFilter();
        if (filter == null) {
        }
        else if (filter instanceof BBOX) {
            // FastBBOX does not implement toString() :(
            BBOX bbox = (BBOX)filter;
            BBOXImpl bbox2 = new BBOXImpl( null, bbox.getExpression1(), bbox.getExpression2() );
            returnString.append( "\n   filter: " + bbox2.toString() );
        }
        else {
            returnString.append( "\n   filter: " + filter.toString() );
        }

        returnString.append( "\n   [properties: " );

        String[] properties = query.getPropertyNames();
        if ((properties == null) || (properties.length == 0)) {
            returnString.append( " ALL ]" );
        }
        else {
            for (int i = 0; i < properties.length; i++) {
                returnString.append( properties[i] );

                if (i < (properties.length - 1)) {
                    returnString.append( ", " );
                }
            }
            returnString.append( "]" );
        }
        
//        if(sortBy != null && sortBy.length > 0) {
//        returnString.append("\n   [sort by: ");
//            for (int i = 0; i < sortBy.length; i++) {
//                returnString.append(sortBy[i].getPropertyName().getPropertyName());
//                returnString.append(" ");
//                returnString.append(sortBy[i].getSortOrder().name());
//
//                if (i < (sortBy.length - 1)) {
//                    returnString.append(", ");
//                }
//            }
//
//            returnString.append("]");
//        }
        
        String result = returnString.toString();
        log.debug( "createQueryString(): result= " + result );
        return result;
    }
    
}
