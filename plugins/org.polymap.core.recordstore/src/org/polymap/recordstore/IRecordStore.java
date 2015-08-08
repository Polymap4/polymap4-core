/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.recordstore;

/**
 * Provides the API (and SPI) of a record store.
 * <p/>
 * The basic idea of record store system is to provide a <b>schema-less</b> store for
 * structured data which exposes a <b>simple</b>, <b>JSON-like</b> API. Neither
 * (domain) modelling nor entity/session support is provided. The record store is
 * meant as a easy to use API (and easy to implement SPI) to build backends for
 * higher level data/domain modelling solutions, such as OGC GeoAPI and Qi4J.
 * <p/>
 * <b>Lucene</b> is used as the backend store. While it should be as easy as possible
 * to use other backend solutions as well.
 * <p/>
 * <b>Records</b> are represented by {@link IRecordState}. This interface provides a
 * schema-less, JSON-like API to access the data in the store. Updates are done via
 * an {@link Updater} which provides a 2-phase commit protocol. There is support for
 * external transaction monitors currently.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public interface IRecordStore
        extends AutoCloseable {
    
    /**
     * Closes this store. Dispose any resources.
     */
    @Override
    public void close();
    
    public boolean isClosed();
    
    /**
     * 
     * @throws UnsupportedOperationException If this store does not support this operation.
     * @return The size of the entire store in byte.
     */
    public long storeSizeInByte();

    public void setIndexFieldSelector( IRecordFieldSelector selector );
    
    public IRecordFieldSelector getIndexFieldSelector();
    
    /**
     * Creates a new record for this store. The returned record is not yet
     * stored. Actually storing the record is done via {@link Updater#store(IRecordState)}.
     * 
     * @param id The id of the newly created record.
     * @return Newly created record.
     */
    public IRecordState newRecord( Object id );

    /**
     * Creates a new record for this store. The returned record is not yet
     * stored. Actually storing the record is done via {@link Updater#store(IRecordState)}.
     * 
     * @return Newly created record with automatically created id.
     */
    public IRecordState newRecord();

    /**
     * Finds the record for the given ID.
     *
     * @param id The ID of the record to find.
     * @return The found record, or null if no such record exists.
     */
    public IRecordState get( Object id ) throws Exception;
    
    /**
     * 
     */
    public ResultSet find( RecordQuery query ) throws Exception;

    
    /**
     * Starts an update of the store. The returned {@link Updater} is used to
     * add/update/delete records.
     * 
     * @return The newly created Updater. The caller is responsible of properly
     *         applying or discarding the Updater when done.
     */
    public Updater prepareUpdate();
    
    
    /**
     * An Updater instance represents an update transaction of the store. In the
     * prepare phase the update is defined via {@link #store(IRecordState)} and
     * {@link #remove(IRecordState)} calls. If no Exception was thrown the transaction can
     * be committed via {@link #apply()} or rolled back via {@link #discard()}.
     * <p/>
     * The prepared updates are not visible to any reader before {@link #apply()}.
     * <p/>
     * It is up the different store implementations to allow or deny access from
     * multiple thread to one Updater.
     */
    public interface Updater {

        /**
         * Stores the given record. If the given record is already stored then its
         * content is updated.
         * 
         * @param record
         */
        void store( IRecordState record ) throws Exception;
        
        void remove( IRecordState record ) throws Exception;
        
        void discard();
        
        /**
         * Same as {@link #apply(boolean)} <code>false</code>.
         */
        void apply();


        /**
         * Commit the changes of this Updater to the underlying data store.
         *  
         * @param optimizeIndex True specifies that the underlying index or database
         *        should be optimized for memory usage. This includes expunge deleted
         *        data and/or re-organizing indices. This might be somewhat/horrible time
         *        consuming.
         */
        void apply( boolean optimizeIndex );

    }
    
}
