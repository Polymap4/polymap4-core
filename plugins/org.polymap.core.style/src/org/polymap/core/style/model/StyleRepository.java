/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.geotools.styling.SLDTransformer;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat;
import org.polymap.core.style.serialize.sld.SLDSerializer;

import org.polymap.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.locking.OptimisticLocking;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.recordstore.IRecordStore;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * Local repository of {@link FeatureStyle} instances.
 * <p/>
 * Every instance runs within its own {@link UnitOfWork}. An instance can be passed
 * around, it keeps stable for its entiry lifetime. However,
 * {@link FeatureStyle#store()} might {@link ConcurrentEntityModificationException
 * fail} because of concurrent modification.
 * <p/>
 * {@link #serializedFeatureStyle(String, Class) Serialized} versions of the styles
 * are cached by the repo. There is exactly one current serialized version per style
 * format at any given time.
 *
 * @author Falko Bräutigam
 */
public class StyleRepository
        implements AutoCloseable {

    private static Log log = LogFactory.getLog( StyleRepository.class );

    private EntityRepository                    repo;

    private Cache<Triple<String,String,OutputFormat>,Optional> serialized = CacheConfig.defaults().createCache();


    /**
     * Creates a repository instance backed by a {@link LuceneRecordStore} in the
     * given dataDir.
     * 
     * @param dataDir The directory of the database or null, for testing in-memory.
     * @throws IOException
     */
    public StyleRepository( File dataDir ) throws IOException {
        IRecordStore store = LuceneRecordStore.newConfiguration().indexDir.put( dataDir ).create();

        repo = EntityRepository.newConfiguration()
                .entities.set( new Class[] {
                        ConstantBoolean.class,
                        ConstantColor.class,
                        ConstantFilter.class,
                        ConstantFontFamily.class,
                        ConstantFontStyle.class,
                        ConstantFontWeight.class,
                        ConstantNumber.class,
                        ConstantString.class,
                        ConstantStrokeCapStyle.class,
                        ConstantStrokeDashStyle.class,
                        ConstantStrokeJoinStyle.class,
                        FeatureStyle.class,
                        FilterMappedColors.class,
                        FilterMappedNumbers.class,
                        Halo.class,
                        LabelPlacement.class,
                        LineStyle.class,
                        NoValue.class,
                        // ScaleMappedNumbers.class,
                        PointStyle.class,
                        PolygonStyle.class,
                        PropertyNumber.class,
                        PropertyString.class,
                        // PropertyValue.class,
                        PropertyMatchingNumberFilter.class,
                        PropertyMatchingStringFilter.class,
                        ScaleMappedNumbers.class,
                        ScaleRangeFilter.class,
                        TextStyle.class
                } ).store.set(
                        new OptimisticLocking(
                        new RecordStoreAdapter( store ) ) )
                .create();
    }


    @Override
    public void close() throws Exception {
        if (repo != null) {
            repo.close();
        }
    }


    public FeatureStyle newFeatureStyle() {
        UnitOfWork uow = repo.newUnitOfWork();
        FeatureStyle result = uow.createEntity( FeatureStyle.class, null, FeatureStyle.defaults( this, uow ) );
        return result;
    }


    public Optional<FeatureStyle> featureStyle( String id ) {
        UnitOfWork uow = repo.newUnitOfWork();
        FeatureStyle result = uow.entity( FeatureStyle.class, id );
        if (result != null) {
            result.uow = uow;
            result.repo = this;
        }
        return Optional.ofNullable( result );
    }


    /**
     * The serialized version of the {@link FeatureStyle} with the given id. The
     * result is cached until next time the style is stored.
     * 
     * Uses OutputFormat.GEOSERVER as default format.
     * 
     * @param id The id of the {@link FeatureStyle} to serialize. The instance has to
     *        be <b>stored</b>.
     * @param targetType The target type of the serialization. Possible values are:
     *        {@link geotools.styling.Style} and {@link String}.
     * @return The serialized version, or {@link Optional#empty()} no style exists
     *         for the given id.
     * @throws RuntimeException If targetType is not supported.
     */
    public <T> Optional<T> serializedFeatureStyle( String id, Class<T> targetType ) {
        return serializedFeatureStyle( id, targetType, OutputFormat.GEOSERVER );
    }


    /**
     * The serialized version of the {@link FeatureStyle} with the given id. The
     * result is cached until next time the style is stored.
     *
     * @param id The id of the {@link FeatureStyle} to serialize. The instance has to
     *        be <b>stored</b>.
     * @param targetType The target type of the serialization. Possible values are:
     *        {@link geotools.styling.Style} and {@link String}.
     * @param outputFormat The output format of the serialization. Possible values are:
     *        {@link org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat}.
     * @return The serialized version, or {@link Optional#empty()} no style exists
     *         for the given id.
     * @throws RuntimeException If targetType is not supported.
     */
    public <T> Optional<T> serializedFeatureStyle( String id, Class<T> targetType, OutputFormat outputFormat ) {
        return serialized.get( Triple.of( id, targetType.getName(), outputFormat ), key -> {
            T result = null;
            FeatureStyle fs = featureStyle( id ).orElse( null );
            if (fs != null) {
                Context sc = new Context().featureStyle.put( fs ).outputFormat.put( outputFormat );
                
                // geotools.styling.Style
                if (org.geotools.styling.Style.class.isAssignableFrom( targetType )) {
                    result = (T)new SLDSerializer().serialize( sc );
                    // // only for easier debugging
                    // try {
                    // SLDTransformer styleTransform = new SLDTransformer();
                    // styleTransform.setIndentation( 4 );
                    // styleTransform.setOmitXMLDeclaration( false );
                    // styleTransform.transform( result, System.err );
                    // }
                    // catch (TransformerException e) {
                    // throw new RuntimeException( "Unable to transform style.", e );
                    // }
                }
                // String/SLD
                else if (String.class.isAssignableFrom( targetType )) {
                    org.geotools.styling.Style style = new SLDSerializer().serialize( sc );
                    try {
                        SLDTransformer styleTransform = new SLDTransformer();
                        styleTransform.setIndentation( 4 );
                        styleTransform.setOmitXMLDeclaration( false );
                        result = (T)styleTransform.transform( style );
                        // only for easier debugging
//                         System.err.println( result );
                    }
                    catch (TransformerException e) {
                        throw new RuntimeException( "Unable to transform style.", e );
                    }
                }
                else {
                    throw new RuntimeException( "Unhandled serialization result type: " + targetType );
                }
            }
            return Optional.ofNullable( result );
        } );
    }


    protected void updated( FeatureStyle updated ) {
        List<Triple<String,String,OutputFormat>> invalid = serialized.keySet().stream()
                .filter( key -> key.getLeft().equals( updated.id() ) )
                .collect( Collectors.toList() );

        invalid.forEach( key -> serialized.remove( key ) );
    }

}
