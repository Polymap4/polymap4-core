/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.operations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.model.ModelProperty;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.event.AbstractModelChangeOperation;

/**
 * This operation allows to set a property of an entity. The property setter
 * method is specified via the {@link ModelProperty} annotation.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class SetPropertyOperation
        extends AbstractModelChangeOperation {

    private Object                  obj;

    private String                  propName;

    private Object                  newValue;

    private Class                   type;


    public SetPropertyOperation() {
        super( "[undefined]" );
    }


    /**
     * 
     * @param The type of the <code>obj</code> to be changed.
     * @param obj The entity to update.
     * @param propName The property as specified in the
     *        {@link ModelProperty} of the setter of that property.
     * @param newValue The new value of the property.
     */
    public void init( Class _type, Object _obj, String _propName, Object _newValue ) {
        this.type = _type;
        this.obj = _obj;
        this.propName = _propName;
        this.newValue = _newValue;
        setLabel( "Eigenschaft ändern: " + _newValue.toString() );
    }

    //        public final IStatus execute( IProgressMonitor monitor, IAdaptable info )
    //        throws ExecutionException {
    //            return super.execute( monitor, info );
    //        }

    public IStatus doExecute( IProgressMonitor monitor, IAdaptable info )
            throws ExecutionException {
        try {
            ProjectRepository rep = ProjectRepository.instance();
            Method[] methods = type.getMethods();
            boolean found = false;
            for (Method m : methods) {
                ModelProperty a = m.getAnnotation( ModelProperty.class );
                // FIXME fix the set+name hack; 
                // no way to get the proper annotation; getAnnotation() alway returns null :(
                if ((a != null && a.value().equals( propName ))
                        || m.getName().equalsIgnoreCase( "set" + propName )) {
                    found = true;

                    // invoke and break
                    try {
                        m.invoke( obj, new Object[] {newValue} );
                        break;
                    }
                    catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException( "No such property:" + propName);
            }
        }
        catch (Throwable e) {
            if (e instanceof ExecutionException) {
                throw (ExecutionException)e;
            } 
            else {
                throw new ExecutionException( e.getMessage(), e );
            }
        }
        return Status.OK_STATUS;
    }


    public boolean canUndo() {
        // use implementation from AbstractModelChangeOperation
        return true;
    }

}
