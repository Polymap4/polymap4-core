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

import java.util.Map;
import java.util.Set;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IUpdateableMetadata
        extends IMetadata {

    public IUpdateableMetadata setIdentifier( String identifier );
    
    public IUpdateableMetadata setTitle( String title );
    
    public IUpdateableMetadata setDescription( String description );
    
    public IUpdateableMetadata setDescription( Field field, String description );
    
    public IUpdateableMetadata setKeywords( Set<String> keywords );
    
    public IUpdateableMetadata setType( String type );
    
    public IUpdateableMetadata setFormats( Set<String> formats );
    
    public IUpdateableMetadata setLanguages( Set<String> langs );
    
    public IUpdateableMetadata setConnectionParams( Map<String,String> params );

}
