/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.catalog;

import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Our minimal interface to a metadata catalog. More or less compliant to CSW.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMetadataCatalog
        extends AutoCloseable {

    /** This query returns all entries of the catalog. Mimics CSW AnyText query. */
    public static final String      ALL_QUERY = "*";
    
    public String getTitle();
    
    public String getDescription();
    
    @Override
    public void close();

    public Optional<? extends IMetadata> entry( String identifier, IProgressMonitor monitor ) throws Exception;
    
    /**
     * 
     *
     * @param query
     *        <a href="https://lucene.apache.org/core/2_9_4/queryparsersyntax.html">
     *        Lucene</a> style fulltext query.
     * @param monitor
     * @throws Exception
     */
    public MetadataQuery query( String query, IProgressMonitor monitor ) throws Exception;
    
    /**
     * 
     *
     * @param prefix
     * @param maxResults
     * @param monitor
     * @return
     * @throws Exception
     */
    public Iterable<String> propose( String prefix, int maxResults, IProgressMonitor monitor ) throws Exception;
    
}
