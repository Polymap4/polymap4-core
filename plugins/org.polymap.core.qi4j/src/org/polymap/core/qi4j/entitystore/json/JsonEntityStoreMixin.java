package org.polymap.core.qi4j.entitystore.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreSPI;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.StateCommitter;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleSPI;

/**
 * Implementation of EntityStore backed by plain files with JSON content.
 *
 * @see Preferences
 */
public class JsonEntityStoreMixin
    implements Activatable, EntityStore, EntityStoreSPI {

    private static Log log = LogFactory.getLog( JsonEntityStoreMixin.class );
    
    @This EntityStoreSPI            entityStoreSpi;

    private @Uses ServiceDescriptor descriptor;
    
    private @Structure Application  application;

    private File                    dir;
    
    protected String                uuid;
    
    private int                     count;

    
    public void activate()
    throws Exception {
        dir = getApplicationRoot();
        log.debug( "JSON store: " + dir.getAbsolutePath() );
        uuid = UUID.randomUUID().toString() + "-";
    }


    private File getApplicationRoot() {
        JsonEntityStoreInfo storeInfo = descriptor.metaInfo( JsonEntityStoreInfo.class );

        File result = null;
        if (storeInfo == null) {
            // Default to use system root + application name
            throw new IllegalStateException( "No dir for JsonEntityStore" );
        }
        else {
            return storeInfo.getDir();
        }
    }


    public void passivate()
    throws Exception {
    }


    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecase, Module module ) {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }


    public EntityStoreUnitOfWork visitEntityStates( EntityStateVisitor visitor,
            Module moduleInstance ) {
        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( entityStoreSpi,
                newUnitOfWorkId(), moduleInstance );

        try {
            String[] identities = dir.list();
            for (String identity : identities) {
                EntityState entityState = uow.getEntityState( EntityReference
                        .parseEntityReference( identity ) );
                visitor.visitEntityState( entityState );
            }
        }
        catch (/*BackingStore*/Exception e) {
            throw new EntityStoreException( e );
        }
        
        return uow;
    }


    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity,
            EntityDescriptor entityDescriptor ) {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork)unitOfWork, identity,
                entityDescriptor );
    }


    public EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity ) {
        try {
            DefaultEntityStoreUnitOfWork desuw = (DefaultEntityStoreUnitOfWork)unitOfWork;
            ModuleSPI module = (ModuleSPI)desuw.module();

            File f = new File( dir, identity.identity() );
            if (!f.exists()) {
                throw new NoSuchEntityException( identity );
            }

            JSONObject entityJson = new JSONObject( FileUtils.readFileToString( f, "UTF-8") );

            String type = entityJson.optString( "type", null );
            EntityStatus status = EntityStatus.LOADED;

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if (entityDescriptor == null) {
                throw new EntityTypeNotFoundException( type );
            }

            Map<QualifiedName, Object> properties = new HashMap<QualifiedName, Object>();
            if (!entityDescriptor.state().properties().isEmpty()) {
                
                JSONObject propsJson = entityJson.getJSONObject( "properties" );
                
                for (PropertyTypeDescriptor propertyDescriptor : entityDescriptor.state().<PropertyTypeDescriptor> properties()) {
                    if (propertyDescriptor.qualifiedName().name().equals( "identity" )) {
                        // Fake identity property
                        properties.put( propertyDescriptor.qualifiedName(), identity.identity() );
                        continue;
                    }

                    ValueType propertyType = propertyDescriptor.propertyType().type();
                    if (propertyType.isNumber()) {
                        if (propertyType.type().name().equals( "java.lang.Long" )) {
                            Object value = propsJson.opt( propertyDescriptor.qualifiedName().name() );
                            properties.put( propertyDescriptor.qualifiedName(),
                                    value != null ? value : (Long)propertyDescriptor.initialValue() );
                        }
                        else if (propertyType.type().name().equals( "java.lang.Integer" )) {
                            Object value = propsJson.opt( propertyDescriptor.qualifiedName().name() );
                            properties.put( propertyDescriptor.qualifiedName(),
                                    value != null ? value : (Integer)propertyDescriptor.initialValue() );
                        }
                        else if (propertyType.type().name().equals( "java.lang.Double" )) {
                            Object value = propsJson.opt( propertyDescriptor.qualifiedName().name() );
                            properties.put( propertyDescriptor.qualifiedName(),
                                    value != null ? value : (Double)propertyDescriptor.initialValue() );
                        }
                        else if (propertyType.type().name().equals( "java.lang.Float" )) {
                            Object value = propsJson.opt( propertyDescriptor.qualifiedName().name() );
                            properties.put( propertyDescriptor.qualifiedName(),
                                    value != null ? value : (Float)propertyDescriptor.initialValue() );
                        }
                        else {
                            // Load as string even though it's a number
                            throw new BackingStoreException( "Not implemented yet: writing property of type: " + propertyType.type().name() );
//                            String json = propsJson.get( propertyDescriptor.qualifiedName().name(), "null" );
//                            json = "[" + json + "]";
//                            JSONTokener tokener = new JSONTokener( json );
//                            JSONArray array = (JSONArray) tokener.nextValue();
//                            Object jsonValue = array.get( 0 );
//                            Object value;
//                            if( jsonValue == JSONObject.NULL )
//                            {
//                                value = null;
//                            }
//                            else
//                            {
//                                value = propertyDescriptor.propertyType().type().fromJSON( jsonValue, module );
//                            }
//                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
                    }
                    // Boolean
                    else if (propertyType.isBoolean()) {
                        Object value = propsJson.opt( propertyDescriptor.qualifiedName().name() );
                        properties.put( propertyDescriptor.qualifiedName(),
                                value != null ? value : (Boolean)propertyDescriptor.initialValue() );
                    }
                    // Value
                    else if (propertyType.isValue()) {
                        JSONObject valueJson = propsJson.optJSONObject( propertyDescriptor.qualifiedName().name() );
                        if (valueJson == null) {
                            properties.put( propertyDescriptor.qualifiedName(), null );
                        }
                        else {
                            Object value = propertyType.fromJSON( valueJson, module );
                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
                    }
//                    // String
//                    else if (propertyType.isString()) {
//                        String json = propsJson.optString( propertyDescriptor.qualifiedName().name(), 
//                                (String)propertyDescriptor.initialValue() );
//                        if (json == null) {
//                            properties.put( propertyDescriptor.qualifiedName(), null );
//                        }
//                        else {
//                            Object value = propertyType.fromJSON( json, module );
//                            properties.put( propertyDescriptor.qualifiedName(), value );
//                        }
//                    }
                    // Set, ...
                    else {
                        Object valueJson = propsJson.opt( propertyDescriptor.qualifiedName().name() );
                        if (valueJson == null) {
                            valueJson = propertyDescriptor.initialValue();
                            properties.put( propertyDescriptor.qualifiedName(), valueJson );
                        }
                        else {
                            Object value = propertyType.fromJSON( valueJson, module );
                            properties.put( propertyDescriptor.qualifiedName(), value );
                        }
//                        String json = propsJson.get( propertyDescriptor.qualifiedName().name(), "null" );
//                        json = "[" + json + "]";
//                        JSONTokener tokener = new JSONTokener( json );
//                        JSONArray array = (JSONArray) tokener.nextValue();
//                        Object jsonValue = array.get( 0 );
//                        Object value;
//                        if( jsonValue == JSONObject.NULL )
//                        {
//                            value = null;
//                        }
//                        else
//                        {
//                            value = propertyDescriptor.propertyType().type().fromJSON( jsonValue, module );
//                        }
//                        properties.put( propertyDescriptor.qualifiedName(), value );
                    }
                }
            }

            // Associations
            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            if (!entityDescriptor.state().associations().isEmpty()) {
                JSONObject assocs = entityJson.optJSONObject( "associations" );
                for (AssociationDescriptor associationType : entityDescriptor.state().associations()) {
                    String associatedEntity = assocs.optString( 
                            associationType.qualifiedName().name(), null );
                    
                    EntityReference value = associatedEntity == null 
                            ? null : EntityReference.parseEntityReference( associatedEntity );
                    associations.put( associationType.qualifiedName(), value );
                }
            }

            // ManyAssociations
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<QualifiedName, List<EntityReference>>();
            if (!entityDescriptor.state().manyAssociations().isEmpty()) {
                JSONObject manyAssocs = entityJson.getJSONObject( "manyassociations" );

                for (ManyAssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations()) {
                    List<EntityReference> references = new ArrayList<EntityReference>();
                    JSONArray entityReferences = manyAssocs.getJSONArray( manyAssociationType.qualifiedName().name() );

                    for (int i = 0; i < entityReferences.length(); i++) {
                        String ref = entityReferences.getString( i );
                        EntityReference value = EntityReference.parseEntityReference( ref );
                        references.add( value );
                    }
                    manyAssociations.put( manyAssociationType.qualifiedName(), references );
                }
            }

            return new DefaultEntityState( desuw,
                    entityJson.optString( "version", "" ),
                    entityJson.optLong( "modified", System.currentTimeMillis() ),
                    identity,
                    status,
                    entityDescriptor,
                    properties,
                    associations,
                    manyAssociations
            );
        }
        catch (JSONException e) {
            throw new EntityStoreException( e );
        }
        catch (IOException e) {
            throw new EntityStoreException( e );
        }
        catch (BackingStoreException e) {
            throw new EntityStoreException( e );
        }
    }


    public StateCommitter apply( final Iterable<EntityState> states, final String version ) {

        return new StateCommitter() {

            public void commit() {
                try {
                    for (EntityState entityState : states) {
                        DefaultEntityState state = (DefaultEntityState)entityState;
                        File f = new File( dir, state.identity().identity() );
                        
                        if (state.status().equals( EntityStatus.NEW )) {
                            writeEntityState( state, f, version );
                        }
                        else if (state.status().equals( EntityStatus.UPDATED )) {
                            writeEntityState( state, f, version );
                        }
                        else if (state.status().equals( EntityStatus.REMOVED )) {
                            if (f.exists() && !f.delete()) {
                                throw new BackingStoreException( "Unable to remove file: " + f.getAbsolutePath() );
                            }
                        }
                    }
                }
                catch (BackingStoreException e) {
                    throw new EntityStoreException( e );
                }
            }


            public void cancel() {
            }
        };
    }


    protected void writeEntityState( DefaultEntityState state, File f, String identity )
    throws EntityStoreException {
        try {
            JSONObject entityJson = new JSONObject();

            EntityType entityType = state.entityDescriptor().entityType();
            entityJson.put( "type", state.entityDescriptor().entityType().type().name() );
            entityJson.put( "version", identity );
            entityJson.put( "modified", state.lastModified() );

            // Properties
            JSONObject propsJson = new JSONObject();
            entityJson.put( "properties", propsJson );
            for (PropertyType propertyType : entityType.properties()) {
                if (propertyType.qualifiedName().name().equals( "identity" )) {
                    continue; // Skip Identity.identity()
                }

                Object value = state.properties().get( propertyType.qualifiedName() );
                if (value == null) {
                    continue;
                }
                
                // Number
                if (propertyType.type().isNumber()) {
                    if (propertyType.type().type().name().equals( "java.lang.Long" )) {
                        propsJson.put( propertyType.qualifiedName().name(), value );
                    }
                    else if (propertyType.type().type().name().equals( "java.lang.Integer" )) {
                        propsJson.put( propertyType.qualifiedName().name(), value );
                    }
                    else if (propertyType.type().type().name().equals( "java.lang.Double" )) {
                        propsJson.put( propertyType.qualifiedName().name(), value );
                    }
                    else if (propertyType.type().type().name().equals( "java.lang.Float" )) {
                        propsJson.put( propertyType.qualifiedName().name(), value );
                    }
                    else {
                        // Store as string even though it's a number
                        throw new BackingStoreException( "Not implemented yet: writing property of type: " + propertyType.type().type().name() );
//                        JSONStringer json = new JSONStringer();
//                        json.array();
//                        propertyType.type().toJSON( value, json );
//                        json.endArray();
//                        String jsonString = json.toString();
//                        jsonString = jsonString.substring( 1, jsonString.length() - 1 );
//                        propsJson.put( propertyType.qualifiedName().name(), jsonString );
                    }
                }
                // Boolean
                else if (propertyType.type().isBoolean()) {
                    propsJson.put( propertyType.qualifiedName().name(), 
                            value instanceof Boolean ? (Boolean)value : Boolean.valueOf( value.toString() ));
                }
                // Value
                else if (propertyType.type().isValue()) {
                    Object valueJson = propertyType.type().toJSON( value );
                    propsJson.put( propertyType.qualifiedName().name(), valueJson );
                }
                // String
                else if (propertyType.type().isString()) {
                    Object valueJson = propertyType.type().toJSON( value );
                    propsJson.put( propertyType.qualifiedName().name(), valueJson );
                }
                // Set, ...
                else {
                    Object valueJson = propertyType.type().toJSON( value );
                    propsJson.put( propertyType.qualifiedName().name(), valueJson );
                }
            }
//            }

            // Associations
            if (!entityType.associations().isEmpty()) {
                JSONObject assocsJson = new JSONObject();
                entityJson.put( "associations", assocsJson );
                
                for (AssociationType associationType : entityType.associations()) {
                    EntityReference ref = state.getAssociation( associationType.qualifiedName() );
                    if (ref == null) {
                        //assocsPrefs.remove( associationType.qualifiedName().name() );
                    }
                    else {
                        assocsJson.put( associationType.qualifiedName().name(), ref.identity() );
                    }
                }
            }

            // ManyAssociations
            if (!entityType.manyAssociations().isEmpty()) {
                JSONObject manyAssocsJson = new JSONObject();
                entityJson.put( "manyassociations", manyAssocsJson );

                for (ManyAssociationType manyAssociationType : entityType.manyAssociations()) {
                    JSONArray valueJson = new JSONArray();
                    ManyAssociationState manyAssoc = state.getManyAssociation( 
                            manyAssociationType.qualifiedName() );

                    for (EntityReference entityReference : manyAssoc) {
                        valueJson.put( entityReference.identity() );
                    }
                    manyAssocsJson.put( manyAssociationType.qualifiedName().name(), valueJson );
                }
            }
            FileUtils.writeStringToFile( f, entityJson.toString( 4 ), "UTF-8" );
        }
        catch (JSONException e) {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
        catch (BackingStoreException e) {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
        catch (IOException e) {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }


    protected String newUnitOfWorkId() {
        return uuid + Integer.toHexString( count++ );
    }

}
