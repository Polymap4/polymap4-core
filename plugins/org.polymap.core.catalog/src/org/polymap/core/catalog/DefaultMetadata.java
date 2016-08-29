/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class DefaultMetadata
        implements IMetadata {

//    @Override
//    public String getTitle() {
//        return "";
//    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getDescription( Field field ) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getType() {
        return Optional.empty();
    }

    @Override
    public Set<String> getFormats() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<String> getLanguages() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Optional<Date> getModified() {
        return Optional.empty();
    }

    @Override
    public Optional<Date> getCreated() {
        return Optional.empty();
    }

    @Override
    public Date[] getAvailable() {
        return new Date[] {};
    }

    @Override
    public Set<String> getKeywords() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Map<String,String> getConnectionParams() {
        return Collections.EMPTY_MAP;
    }
    
}
