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

import org.eclipse.rwt.SessionSingletonBase;
import org.polymap.styler.actions.ToggleExpertAction;


/**
 * class to help with colors
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class StylerSessionSingleton  extends SessionSingletonBase {

	private boolean expert_mode=false;
	
	
	public boolean getExpertMode() {
		return expert_mode;
	}
	
	public void setExpertMode(boolean expert_mode) {
		ToggleExpertAction.setExpertState(expert_mode);
		this.expert_mode=expert_mode;
	}
	
	public synchronized static StylerSessionSingleton getInstance() {
		return (StylerSessionSingleton) getInstance(StylerSessionSingleton.class);
	}

	
}