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
package org.polymap.core.runtime.recordstore;

/**
 * The record store API and SPI. A record store stores records of key/value pairs
 * and it provides search capabilities. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public interface IRecordStore {
    
    /**
     * Closes this store. Dispose any resources.
     */
    public void close();
    
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
     * @return Newly created record.
     */
    public IRecordState newRecord();

    /**
     * 
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
     * 
     */
    public interface ResultSet
            extends Iterable<IRecordState> {

        public IRecordState get( int index ) throws Exception;
        
        public int count();
        
    }

    
    /**
     * Starts an update of the store. The returned {@link Updater} is used to
     * add/update/delete record.
     * 
     * @return The newly created Updater. The caller is responsible of properly
     *         applying or discarding the Updater if done.
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
         * Stores the given record. If the given record is already stores then its
         * content is updated in the store.
         * 
         * @param record
         */
        void store( IRecordState record ) throws Exception;
        
        void remove( IRecordState record ) throws Exception;
        
        void discard();
        
        /**
         * Same as <code>apply(true)</code>.
         */
        void apply();


        /**
         * Commit the changes of this Updater to the underlying data store.
         *  
         * @param optimizeIndex True specifies that the underlying index or database
         *        should be optimized for memory usage. This includes expunge deleted
         *        data and/or re-organizing indices. This might be somewhat time
         *        consuming.
         */
        void apply( boolean optimizeIndex );

    }
    
}
