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

package org.polymap.openlayers.rap.widget.internal.resources;

import org.eclipse.rwt.resources.IResource;
import org.eclipse.rwt.resources.IResourceManager.RegisterOptions;

/**
 * Resource for the OpenLayers RAP Widget
 * 
 * @author Marcus -LiGi- B&uuml;schleb< mail: ligi (at) polymap (dot) de >
 * 
 */

public class OpenLayersResource implements IResource {

	public String getCharset() {
		return "ISO-8859-1";
	}

	public ClassLoader getLoader() {
		return this.getClass().getClassLoader();
	}

	public RegisterOptions getOptions() {
		return RegisterOptions.VERSION_AND_COMPRESS;
	}

	public String getLocation() {
		return "org/polymap/openlayers/rap/widget/internal/resources/OpenLayersWrapper.js";
	}

	public boolean isJSLibrary() {
		return true;
	}

	public boolean isExternal() {
		return false;
	}
}
