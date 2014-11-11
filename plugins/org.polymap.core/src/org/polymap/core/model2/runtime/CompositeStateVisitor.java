/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Association;
import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Mixins;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyBase;

/**
 * Allows to visit the entire hierachy of properties of the given {@link Composite}
 * and all of its {@link Mixins annotated} mixins.
 * <p/>
 * <b>Example:</b>
 * 
 * <pre>
 * // visit all simple properties
 * new CompositeStateVisitor() {
 * 
 *     protected void visitProperty( Property prop ) {
 *         log.info( &quot;simple prop: &quot; + prop.getInfo().getName() );
 *     }
 * }.process( entity );
 * </pre>
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class CompositeStateVisitor {

    private static Log log = LogFactory.getLog( CompositeStateVisitor.class );
    
    private Composite           target;

    /**
     * Override this in order to visit simple {@link Property}s. 
     */
    protected void visitProperty( Property prop ) { }
    
    
    /**
     * Override this in order to visit {@link Property}s with a {@link Composite}
     * value.
     * 
     * @return False specifies that the members of the composite will not be visited.
     *         (default: true)
     */
    protected boolean visitCompositeProperty( Property prop ) { return true; }

    /**
     * Override this in order to visit {@link CollectionProperty}s with simple
     * values.
     */
    protected void visitCollectionProperty( CollectionProperty prop ) { }

    
    /**
     * Override this in order to visit {@link CollectionProperty}s with
     * {@link Composite} values.
     * 
     * @return False specifies that the members of the collection will not be
     *         visited. (default: true)
     */
    protected boolean visitCompositeCollectionProperty( CollectionProperty prop ) { return true; }

    /**
     * Override this in order to visit {@link Association}s. 
     */
    protected void visitAssociation( Association prop ) { }
    

    /**
     * 
     *
     * @param composite The {@link Composite} to visit.
     */
    public final void process( Composite composite ) {
        this.target = composite;
        
        // composite
        processComposite( composite );

        // mixins
        if (composite instanceof Entity) {
            Collection<Class<? extends Composite>> mixins = composite.info().getMixins();
            for (Class<? extends Composite> mixinClass : mixins) {
                Composite mixin = ((Entity)composite).as( mixinClass );
                processComposite( mixin );
            }
        }
    }
    
    
    /**
     * Recursivly process properties of the given Composite.
     */
    protected final void processComposite( Composite composite ) {
        Collection<PropertyInfo> props = composite.info().getProperties();
        for (PropertyInfo propInfo : props) {
            PropertyBase prop = propInfo.get( composite );
            // Property
            if (prop instanceof Property) {
                if (Composite.class.isAssignableFrom( propInfo.getType() )) {
                    if (visitCompositeProperty( (Property)prop )) {
                        Composite value = (Composite)((Property)prop).get();
                        if (value != null) {
                            processComposite( value );
                        }
                    }
                }
                else {
                    visitProperty( (Property)prop );
                }
            }
            // Collection
            else if (prop instanceof CollectionProperty) {
                if (Composite.class.isAssignableFrom( propInfo.getType() )) {
                    if (visitCompositeCollectionProperty( (CollectionProperty)prop )) {
                        for (Composite value : ((CollectionProperty<Composite>)prop)) {
                            processComposite( value );                            
                        }
                    }
                }
                else {
                    visitCollectionProperty( (CollectionProperty)prop );
                }
            }
            // Association
            else if (prop instanceof Association) {
                visitAssociation( (Association)prop );
            }
            else {
                throw new RuntimeException( "Unhandled Property type:" + prop );
            }
        }
        
    }
    
}
