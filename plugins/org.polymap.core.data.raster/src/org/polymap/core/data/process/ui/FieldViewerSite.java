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
package org.polymap.core.data.process.ui;

import org.jgrasstools.gears.libs.modules.JGTModel;

import org.polymap.core.data.process.FieldInfo;
import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.runtime.config.Mandatory;

/**
 * The site of a {@link FieldViewer} and its {@link FieldIO}s.
 *
 * @author Falko Bräutigam
 */
public class FieldViewerSite
        extends Configurable {

    @Mandatory
    @Immutable
    public Config2<FieldViewerSite,FieldInfo>   fieldInfo;
    
    @Mandatory
    @Immutable
    public Config2<FieldViewerSite,ModuleInfo>  moduleInfo;

    @Mandatory
    @Immutable
    public Config2<FieldViewerSite,JGTModel>    module;

    /**
     * The source layer of the processing.
     */
    @Immutable
    public Config2<FieldViewerSite,ILayer>      layer;

    
    /**
     * Shortcut to:
     * <code>{@link #fieldInfo}.get().{@link FieldInfo#setValue(JGTModel, Object) setValue}( {@link #module}.get(), value )</code>
     */
    public void setFieldValue( Object value ) {
        fieldInfo.get().setValue( module.get(), value );
    }

    /**
     * Shortcut to:
     * <code>{@link #fieldInfo}.get().{@link FieldInfo#getValue(JGTModel, Object) getValue}( {@link #module}.get() )</code>
     */
    public <R> R getFieldValue() {
        return fieldInfo.get().getValue( module.get() );
    }
    
}
