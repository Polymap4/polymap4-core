/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.polymap.styler.ui;


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.polymap.core.project.ILayer;

public class StylerPropertyPage extends PropertyPage {

    private static Log log = LogFactory.getLog( StylerPropertyPage.class );

	private ILayer                 layer;
	private StyleView view;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite outer_composite=new Composite(parent,SWT.NONE);
		view=new StyleView();
	       
		GridLayout layout = new GridLayout();
	    outer_composite.setLayout( layout );
	    GridData gdata = new GridData( GridData.FILL_BOTH );
	    gdata.grabExcessHorizontalSpace = true;
	    gdata.grabExcessVerticalSpace=true;
	    outer_composite.setLayoutData( gdata );
	        
		outer_composite.setLayout(new FillLayout());
		view.createPartControl(outer_composite);
		view.setLayer(layer);
		return null;
	}

	public void performApply() {
		log.info("Apply");
		try {
			view.commit_changes();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public void setElement( IAdaptable element ) {
        log.info( "element= " + element );
        layer = (ILayer)element;
    }

}
