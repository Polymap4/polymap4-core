/*
 * polymap.org
 * Copyright (C) 2011-2014, Polymap GmbH. All rights reserved.
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
package org.polymap.core.model2.store.recordstore;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.TemplateProperty;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;
import org.polymap.core.runtime.recordstore.lucene.ValueCoders;

/**
 * Converts {@link BooleanExpression} into Lucene queries.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LuceneQueryBuilder {

    private static Log log = LogFactory.getLog( LuceneQueryBuilder.class );

    static final Query          ALL = new MatchAllDocsQuery();

    private static List<Class<? extends LuceneExpressionHandler>> handlers;

    static {
        // more frequently used first
        handlers = new ArrayList();
        handlers.add( LuceneComparisonHandler.class );
        handlers.add( LuceneJunctionHandler.class );
        handlers.add( LuceneAssociationHandler.class );
        handlers.add( LuceneIdHandler.class );
    }

    
    // instance *******************************************
    
    protected LuceneRecordStore         store;

    protected List<BooleanExpression>   postProcess = new ArrayList();

    protected ValueCoders               valueCoders;
    
    
    public LuceneQueryBuilder( LuceneRecordStore store ) {
        this.store = store;
        this.valueCoders = store.getValueCoders();
    }

    
    public List<BooleanExpression> getPostProcess() {
        return postProcess;
    }


    public RecordQuery createQuery( Class<? extends Entity> resultType, final BooleanExpression whereClause ) {
        assert postProcess.isEmpty();
        
        Query filterQuery = processExpression( whereClause, resultType );

        Query typeQuery = new TermQuery( new Term( RecordCompositeState.TYPE_KEY, resultType.getName() ) );
        Query result = null;
        if (!filterQuery.equals( ALL )) {
            result = new BooleanQuery();
            ((BooleanQuery)result).add( typeQuery, BooleanClause.Occur.MUST );
            ((BooleanQuery)result).add( filterQuery, BooleanClause.Occur.MUST );
        }
        else {
            result = typeQuery;
        }
        log.debug( "    LUCENE query: [" + result.toString() + "]" );
        return new LuceneRecordQuery( store, result );
    }


    protected Query processExpression( final BooleanExpression expression, Class<? extends Entity> resultType ) {
        if (expression == null) {
            return ALL;
        }
        for (Class<? extends LuceneExpressionHandler> handlerClass : handlers) {
            try {
                LuceneExpressionHandler handler = handlerClass.newInstance();
                handler.builder = this;
                handler.resultType = resultType;
                
                Query result = handler.handle( expression );
                
                if (result != null) {
                    return result;
                }
            }
            catch (ClassCastException e) {
                // handler's type parameter does not match
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException( e );
            }
        }
        throw new UnsupportedOperationException( "Expression " + expression + " is not supported" );
        
//        // FID
//        else if (expression instanceof SpatialPredicate.Fids) {
//            Fids fids = (Fids)expression;
//            if (fids.size() > BooleanQuery.getMaxClauseCount()) {
//                BooleanQuery.setMaxClauseCount( fids.size() + 10 );
//            }
//            BooleanQuery result = new BooleanQuery();
//            for (String fid : fids) {
//                Query fidQuery = store.getValueCoders().searchQuery( 
//                        new QueryExpression.Equal( LuceneRecordState.ID_FIELD, fid ) );
//                result.add( fidQuery, BooleanClause.Occur.SHOULD );
//            }
//            return result;
//        }
//        // INCLUDE
//        else if (expression.equals( SpatialPredicate.INCLUDE )) {
//            return ALL;
//        }
//        // EXCLUDE
//        else if (expression.equals( SpatialPredicate.EXCLUDE )) {
//            // XXX any better way to express this?
//            return new TermQuery( new Term( "__does_not_exist__", "true") );
//        }
//        // BBOX
//        else if (expression instanceof SpatialPredicate.BBOX) {
//            return processBBOX( (SpatialPredicate.BBOX)expression );
//        }
//        // Spatial
//        else if (expression instanceof SpatialPredicate) {
//            return processSpatial( (SpatialPredicate)expression );
//        }
//        // MANY Assoc
//        else if (expression instanceof ManyAssociationContainsPredicate) {
//            throw new UnsupportedOperationException( "ManyAssociationContainsPredicate" );
//        }
//        // IS NULL
//        else if (expression instanceof PropertyNullPredicate) {
//            throw new UnsupportedOperationException( "PropertyNullPredicate" );
//        }
//        // Assoc
//        else if (expression instanceof AssociationNullPredicate) {
//            throw new UnsupportedOperationException( "AssociationNullPredicate" );
//        }
//        // contains
//        else if (expression instanceof ContainsPredicate) {
//            return processContainsPredicate( (ContainsPredicate)expression, resultType );
//        }
    }

    
//    protected Query processBBOX( SpatialPredicate.BBOX bbox ) {
//        PropertyReference<Envelope> property = bbox.getPropertyReference();
//        String fieldName = property2Fieldname( property ).toString();
//        
//        Envelope envelope = (Envelope)bbox.getValueExpression().value();
//
//        return store.getValueCoders().searchQuery( 
//                new QueryExpression.BBox( fieldName, envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY() ) );
//    }
//
//    
//    protected Query processSpatial( SpatialPredicate predicate ) {
//        PropertyReference<Envelope> property = predicate.getPropertyReference();
//        String fieldName = property2Fieldname( property ).toString();
//
//        Geometry value = (Geometry)predicate.getValueExpression().value();
//        
//        Envelope bounds = value.getEnvelopeInternal();
//        
//        postProcess.add( predicate );
//        
//        return store.getValueCoders().searchQuery( 
//                new QueryExpression.BBox( fieldName, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() ) );
//    }


//    /**
//     * Handle the contains predicate.
//     * <p/>
//     * Impl. note: This needs a patch in
//     * org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl<T> to work with
//     * Qi4j 1.0.
//     */
//    protected Query processContainsPredicate( ContainsPredicate predicate, String resultType ) {
//        final ValueCoders valueCoders = store.getValueCoders();
//
//        PropertyReference property = predicate.propertyReference();
//        final String baseFieldname = property2Fieldname( property ).toString();
//        SingleValueExpression valueExpression = (SingleValueExpression)predicate.valueExpression();
//
//        //
//        int maxElements = 10;
//        try {
//            Timer timer = new Timer();
//            String lengthFieldname = baseFieldname + "__length";
//            RecordQuery query = new SimpleQuery()
//                    .eq( "type", resultType )
//                    .sort( lengthFieldname, SimpleQuery.DESC, Integer.class )
//                    .setMaxResults( 1 );
//            ResultSet lengthResult = store.find( query );
//            IRecordState biggest = lengthResult.get( 0 );
//            maxElements = biggest.get( lengthFieldname );
//            log.debug( "    LUCENE: maxLength query: result: " + maxElements + " (" + timer.elapsedTime() + "ms)" );
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//        
//        //
//        BooleanQuery result = new BooleanQuery();
//        for (int i=0; i<maxElements; i++) {
//            final BooleanQuery valueQuery = new BooleanQuery();
//
//            final ValueComposite value = (ValueComposite)valueExpression.value();
//            ValueModel valueModel = (ValueModel)ValueInstance.getValueInstance( value ).compositeModel();
//            List<PropertyType> actualTypes = valueModel.valueType().types();
//            //                    json.key( "_type" ).value( valueModel.valueType().type().name() );
//
//
//            // all properties of the value
//            final int index = i;
//            value.state().visitProperties( new StateVisitor() {
//                public void visitProperty( QualifiedName name, Object propValue ) {
//                    if (propValue == null) {
//                    }
//                    else if (propValue.toString().equals( "-1" )) {
//                        // FIXME hack to signal that this non-optional(!) value is not to be considered
//                        log.warn( "Non-optional field ommitted: " + name.name() + ", value=" + propValue );
//                    }
//                    else {
//                        String fieldname = Joiner.on( "" ).join( 
//                                baseFieldname, "[", index, "]", 
//                                LuceneEntityState.SEPARATOR_PROP, name.name() );
//                        
//                        //Property<Object> fieldProp = value.state().getProperty( name );
//
////                      // this might not be the semantics of contains predicate but it is useless
////                      // if one cannot do a search without (instead of just a strict match)
//                        Query propQuery = propValue instanceof String
//                                && !StringUtils.containsNone( (String)propValue, "*?")
//                                ? valueCoders.searchQuery( new QueryExpression.Match( fieldname, propValue ) ) 
//                                : valueCoders.searchQuery( new QueryExpression.Equal( fieldname, propValue ) ); 
//
//                        valueQuery.add( propQuery, BooleanClause.Occur.MUST );
//                    }
//                }
//            });
//
//            result.add( valueQuery, BooleanClause.Occur.SHOULD );
//        }
//        return result;
//    }


    /**
     * Recursivly build the field name for the given Property.
     */
    public static StringBuilder fieldname( TemplateProperty property ) {
        // recursion: property
        TemplateProperty traversed = property.getTraversed();
        if (traversed != null) {
            return fieldname( traversed )
                    .append( RecordCompositeState.KEY_DELIMITER )
                    .append( property.getInfo().getNameInStore() ); 
        }
        // start: simple property
        return new StringBuilder( 128 ).append( property.getInfo().getNameInStore() );
    }

}
