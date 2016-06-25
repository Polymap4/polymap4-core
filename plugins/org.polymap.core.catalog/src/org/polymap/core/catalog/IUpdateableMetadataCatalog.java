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

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A metadata catalog that allows modifications to irs contents.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IUpdateableMetadataCatalog
        extends IMetadataCatalog {

    public Updater prepareUpdate();
    

    /**
     * 
     */
    public interface Updater
            extends AutoCloseable {

        /**
         * Creates a new entry in the catalog.
         * <p/>
         * The given initializer may create an identifier (using {@link UUID} or
         * another source). If no identifier is given then the catalog will create a
         * valid one.
         */
        void newEntry( Consumer<IUpdateableMetadata> initializer );

        /**
         * Updates the entry in the catalog identified by the given identifier.
         * <p/>
         * The given updater must set *all* fields, including all unmodified fields.
         *
         * @param identifier
         * @param updater
         */
        void updateEntry( String identifier, Consumer<IUpdateableMetadata> updater );

        void removeEntry( String identifier );

        void commit() throws Exception;
        
        @Override
        void close();
    }
    
}
