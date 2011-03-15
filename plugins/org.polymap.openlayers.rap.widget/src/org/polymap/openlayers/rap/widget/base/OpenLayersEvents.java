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
import java.util.Map;

/**
 * Client Side OpenLayers Object Base Class holding a reference to the widget
 * and keeps track of changes to the object
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class OpenLayersEvents {

    OpenLayersObject                obj;
    /** Maps event name into {@link ListenerData}. */
    Map<String,ListenerData>        listeners = new HashMap();

    public class ListenerData {
        String                      event_name;
        OpenLayersEventListener     listener;
        Map<String,String>          payload;
        
        public ListenerData(String event_name, OpenLayersEventListener listener, Map<String,String> payload) {
            this.event_name = event_name;
            this.listener = listener;
            this.payload = payload;
        }
    }
    
	public OpenLayersEvents(OpenLayersObject obj) {
		this.obj = obj;
	}

	public Map<String,String> getPayload(String event_name) {
	    ListenerData listener_data = listeners.get(event_name);
	    return listener_data != null
	            ? listener_data.payload : null;
	}
	
	public void register(OpenLayersEventListener listener, 
	        String event_name,
			Map<String, String> payload_request) {
	    
		ListenerData listener_data = new ListenerData( event_name, listener, payload_request );
        synchronized (listeners) {
            ListenerData old = listeners.put( event_name, listener_data );
            assert old == null : "Listener already registered for event: " + event_name;
        }
	    
		String payload_code = "";
		if (payload_request != null) {
			for (Map.Entry<String,String> entry : payload_request.entrySet()) {
				payload_code += "req.addParameter( openlayersId + '.event_payload_"
						+ entry.getKey() + "' , " + entry.getValue() + "  );";
			}
		}

		obj.addObjModCode( "obj.events.register('" + event_name + "', this,"
		        + "function (event) {"
		        + "if( !org_eclipse_rap_rwt_EventUtil_suspend ) {"
		        + "var openlayersId = org.eclipse.swt.WidgetManager.getInstance().findIdByWidget( this );"
		        + "var req = org.eclipse.swt.Request.getInstance();"
		        + "req.addParameter( openlayersId + '.event_name', event.type );"
		        + "req.addParameter( openlayersId + '.event_src_obj', '" + obj.getObjRef()
		        + "' );"

		        + "" + payload_code + "req.send();" + "}" + "});" );
	}

    public void unregister(OpenLayersEventListener listener, String name) {
        synchronized (listeners) {
            listeners.remove( listener );
        }
    }

	public void process_event(String name, HashMap<String, String> payload) {
	    ListenerData listener_data = null;
	    synchronized (listeners) {
	        listener_data = listeners.get(name);
	    }
	    listener_data.listener.process_event(obj, name, payload);
	}

}
