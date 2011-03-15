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
 */

/**
 * JavaScript Part for the OpenLayers RAP Widget
 * 
 * @author Marcus -LiGi- Bueschleb mail to ligi (at) polymap (dot) de
 * 
 */


function loadScript(url, callback, context) {
	var script = document.createElement("script");

	script.type = "text/javascript";

	if (script.readyState) { // IE
		script.onreadystatechange = function() {
			if (script.readyState == "loaded"
					|| script.readyState == "complete") {
				script.onreadystatechange = null;
				callback(context);
			}
		};
	} else { // Others
		script.onload = function() {
			callback(context);
		};
	}

	script.src = url;
	document.getElementsByTagName("head")[0].appendChild(script);
}

qx.Class.define("org.polymap.openlayers.rap.widget.OpenLayersWidget", {
	extend : qx.ui.layout.CanvasLayout,

	construct : function(id) {
		this.base(arguments);
		this.setHtmlAttribute("id", id);
		this._id = id;
		this.set(  {  backgroundColor : "white"  });
	},

	properties : {},

	members : {
		load_lib : function(lib_url) {

			loadScript(lib_url, function(context) {
				qx.ui.core.Widget.flushGlobalQueues();

					if (!org_eclipse_rap_rwt_EventUtil_suspend) {
						var openlayersId = org.eclipse.swt.WidgetManager
								.getInstance().findIdByWidget(context);
						var req = org.eclipse.swt.Request.getInstance();
						req.addParameter(openlayersId + ".load_lib_done",
								"true");

						req.send();
					}

				}
			, this); // loadScript

		},
					
		eval : function(code2eval) {
			eval(code2eval);
		}
	}

});
