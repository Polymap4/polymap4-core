/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Provides information about a processing module. 
 *
 * @author <a href="http://mapzone.io">Falko Bräutigam</a>
 */
public interface ModuleInfo
        extends BaseInfo {

    /**
     * The type of the module.
     */
    public Class<?> type();
    
    /**
     * All input {@link #fields} with {@link FieldInfo#isInput} is true.
     */
    public List<FieldInfo> inputFields();

    /**
     * All input {@link #fields} with {@link FieldInfo#isOutput} is true.
     */
    public List<FieldInfo> outputFields();

    /**
     * 
     *
     * @return Newly created module instance.
     */
    public Object createModuleInstance();

    /**
     * Executes the given module. 
     *
     * @param module
     * @param monitor
     * @throws Exception If something went wrong while execution.
     * @throws OperationCanceledException If the module detected
     *         {@link IProgressMonitor#isCanceled()} while execution.
     */
    public void execute( Object module, IProgressMonitor monitor ) throws OperationCanceledException, Exception;
    
}
