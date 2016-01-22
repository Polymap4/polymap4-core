/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.ui;

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Allows to use lambda function as {@link SelectionListener}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SelectionListenerAdapter
        implements SelectionListener {

    private Consumer<SelectionEvent>    task;
    
    public SelectionListenerAdapter( Consumer<SelectionEvent> task ) {
        this.task = task;
    }

    @Override
    public void widgetSelected( SelectionEvent ev ) {
        task.accept( ev );
    }

    @Override
    public void widgetDefaultSelected( SelectionEvent ev ) {
        task.accept( ev );
    }

}
