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

package org.polymap.styler;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;


/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class FeatureTypeStyleWrapper {

	private FeatureTypeStyle fts;
	
	// context
	private Style style;
	
	public FeatureTypeStyleWrapper(FeatureTypeStyle fts , Style style) {
		this.fts=fts;
		this.style=style;
	}
	
	public void dispose() {
		int index= style.featureTypeStyles().indexOf(fts);
		style.featureTypeStyles().remove(index);
	}
	
	
	public void moveUp() {
		if (!canMoveUp()) 
			return; 
		
		int index= style.featureTypeStyles().indexOf(fts);
		style.featureTypeStyles().remove(index);
		style.featureTypeStyles().add(index-1,fts);
	}

	public void moveDown() {
		if (!canMoveDown()) 
			return; // can't move down
		
		int index= style.featureTypeStyles().indexOf(fts);
		style.featureTypeStyles().remove(index);
		style.featureTypeStyles().add(index+1,fts);
	}

	public boolean canMoveUp() {
		int index= style.featureTypeStyles().indexOf(fts);
		return (index!=0);
	}

	public boolean canMoveDown() {
		int index= style.featureTypeStyles().indexOf(fts);
		return (index!=(style.featureTypeStyles().size()-1)); 
	}

	

	public FeatureTypeStyle getFeatureTypeStyle() {
		
		return fts;
	}
	
	public String getName() {
		return fts.getName();
	}

	public void setName(String new_name) {
		fts.setName(new_name);
	}

	public String getAbstract() {
		if (fts.getDescription().getAbstract()==null)
			return "";
		
		return fts.getDescription().getAbstract().toString();
	}
	
	public String getTitle() {
		if (fts.getDescription().getTitle()==null)
			return "";
		
		return fts.getDescription().getTitle().toString();
	}
	
	public void setAbstract(String new_abstract) {
		fts.getDescription().setAbstract(new_abstract);
	}

	public void setTitle(String new_title) {
		fts.getDescription().setTitle(new_title);
	}
	
	
}