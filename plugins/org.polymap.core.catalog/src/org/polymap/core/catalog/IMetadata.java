/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.polymap.core.catalog.resolve.IMetadataResourceResolver;

/**
 * An entry i a {@link IMetadataCatalog} (aka a record). Loosely following
 * <a href="http://dublincore.org/documents/dcmi-terms/">Dublin Core</a> model.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMetadata {

    public enum Field {
        /**
         * An entity primarily responsible for making the resource. Examples of a Creator
         * include a person, an organization, or a service.
         */
        Creator,    
        /**
         * Publisher. An entity responsible for making the resource available.
         */
        Publisher,    
        /**
         * An entity responsible for making contributions to the resource. Examples of a
         * Contributor include a person, an organization, or a service.
         */
        Contributor,
        /**
         * Information about rights held in and over the resource. Typically, rights
         * information includes a statement about various property rights associated with
         * the resource, including intellectual property rights.
         */
        Rights,    
        /**
         * Rights Holder. A person or organization owning or managing rights over the
         * resource.
         */
        RightsHolder,    
        /**
         * Information about who can access the resource or an indication of its security
         * status. Access Rights may include information regarding access or restrictions
         * based on privacy, security, or other policies.
         */
        AccessRights
    }
    
    public String getIdentifier();
    
    /**
     * A human readable name given to the resource.
     */
    public String getTitle();
    
    /**
     * Description may include but is not limited to: an abstract, a table of
     * contents, a graphical representation, or a free-text account of the resource.
     */
    public Optional<String> getDescription();    
    
    public Optional<String> getDescription( Field field );    
    
    /**
     * The nature or genre of the resource. Recommended best practice is to use a
     * controlled vocabulary such as the
     * <a href="http://dublincore.org/documents/dcmi-type-vocabulary/#H7">DCMI Type
     * Vocabulary</a>. To describe the file format, physical medium, or dimensions of
     * the resource, use the Format element.
     */
    public Optional<String> getType();
    
    /**
     * The file format, physical medium, or dimensions of the resource. Examples of
     * dimensions include size and duration. Recommended best practice is to use a
     * controlled vocabulary such as the list of
     * <a href="http://www.iana.org/assignments/media-types/">Internet Media
     * Types</a>.
     */
    public Set<String> getFormats();

    /**
     * A language of the resource. Recommended best practice is to use a controlled
     * vocabulary such as RFC 4646 [RFC4646].
     */
    public Set<String> getLanguages();
    
    /**
     * Date Modified. Date on which the resource was changed.
     */
    public Optional<Date> getModified();
    
    /**
     * Date Created. Date of creation of the resource.
     */
    public Optional<Date> getCreated();
    
    /**
     * Date Available. Date (often a range) that the resource became or will become available.
     */
    public Date[] getAvailable();
    
    /**
     * The topic of the resource. Typically, the subject will be represented using
     * keywords, key phrases, or classification codes. Recommended best practice is
     * to use a controlled vocabulary.
     */
    public Set<String> getKeywords();

    /**
     * 
     * @see IMetadataResourceResolver#CONNECTION_PARAM_TYPE
     * @see IMetadataResourceResolver#CONNECTION_PARAM_URL
     */
    public Map<String,String> getConnectionParams();
    
}
