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

package org.polymap.openlayers.rap.widget.base;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.rwt.SessionSingletonBase;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;

/**
 * Widget Provider holding a reference to the widget and generate client side
 * object hash / id's
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class OpenLayersSessionHandler extends SessionSingletonBase {

    
    /** 
     * flag to determine if the openlayers lib ( client side js ) is loaded for this session
     * to block executing js commands before this
     */
   // public boolean lib_init_done=false;
    
    /** 
     * HashMap to hold references to created objects as value with the client side id as key
     */
    public HashMap<String,OpenLayersObject> obj_ref2obj;
    
    
    /**
     * javascript command stack 
     */
    public Vector<OpenLayersCommand> cmd_stack;

    
	private OpenLayersWidget widget;

		
	private int obj_ref = 0;

	
	private OpenLayersSessionHandler() {
		obj_ref2obj=new HashMap<String,OpenLayersObject>();
		cmd_stack = new Vector<OpenLayersCommand>();
	}
	
	public OpenLayersWidget getWidget() {
		return widget;
	}

	public void setWidget(OpenLayersWidget widget) {
	
	    this.widget = widget;
		// create the initial object space ( hash )
		addCommand( new OpenLayersCommand(
				"if ( typeof objs == 'undefined' ) objs={};"));
	}


	public synchronized static OpenLayersSessionHandler getInstance() {
		return (OpenLayersSessionHandler) getInstance(OpenLayersSessionHandler.class);
	}

	public String generateObjectReference(String prefix,OpenLayersObject src_obj) {
		obj_ref++;
		obj_ref2obj.put(prefix + obj_ref, src_obj);
		return prefix + obj_ref;
	}

	/*
	public Object[] getCommand() {
        Object[] res = cmd_stack.elementAt(0 );
        OpenLayersWidgetProvider.getInstance().cmd_stack.removeElementAt(0);
        return res;
   }*/
	
	
	

    public void addCommand(OpenLayersCommand command) {
          cmd_stack.add(command);
    }

    /*
    // with a single parameter
    public void addCommand(String cmd, String param) {
        Object[] param_arr = { param };
        addCommand(cmd, param_arr);
    }*/

    public boolean hasCommand(OpenLayersWidget for_widget) {
        if (cmd_stack.isEmpty())
            return false;
        else
           return (cmd_stack.get(0).isSuitableFor(for_widget ));
    }

    public OpenLayersCommand getCommand() {
        OpenLayersCommand res =  cmd_stack.elementAt(0);
        cmd_stack.removeElementAt(0);
        return res;
    }

}
