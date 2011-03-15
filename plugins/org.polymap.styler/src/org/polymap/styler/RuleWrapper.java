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

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleBuilder;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;


/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class RuleWrapper {

    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    Rule                    rule;

    ILayer                  layer;

    FeatureTypeStyleWrapper fts;

    StyleBuilder            sb = new StyleBuilder();

	
	public RuleWrapper(Rule rule, ILayer layer, FeatureTypeStyleWrapper fts) {
		this.rule = rule;
		this.layer=layer;
		this.fts=fts;
	}
	
	public void dispose() {
		int index=fts.getFeatureTypeStyle().rules().indexOf(rule);
		fts.getFeatureTypeStyle().rules().remove(index);
	}
	public Double getMinScaleDenominator() {
		return rule.getMinScaleDenominator();
	}
	
	public Double getMaxScaleDenominator() {
		return rule.getMaxScaleDenominator();
	}
	
	
	public void setMinScaleDenominator(double val) {
		rule.setMinScaleDenominator(val);
	}
	
	public void setMaxScaleDenominator(double val) {
		rule.setMaxScaleDenominator(val);
	}

	public boolean hasFilter() {
		return (rule.getFilter()!=null);
	}
	
	public static boolean isObjRule(Object obj) {
		 return (obj.getClass() == org.geotools.styling.RuleImpl.class);
	}
	
	public Rule getRule() {
		return rule;
	}
	
	public void addDefaultFilter() {
		rule.setFilter(filterFactory.greater(filterFactory.literal(""),filterFactory.literal("")));
	}
	

	public String[] getFeatureAttributes() {
		String[] res = null;
		FeatureSource fs;
		try {
		    // XXX
			fs = PipelineFeatureSource.forLayer( layer, false );

			SimpleFeatureType schema = (SimpleFeatureType) fs.getSchema();
			int i = 0;
			res = new String[schema.getAttributeDescriptors().size()];
			for (AttributeDescriptor attr : schema.getAttributeDescriptors()) {
				res[i++] = "" + attr.getName();
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}
      
	public FeatureTypeStyleWrapper getFeatureTypeStyleWrapper() {
		return fts;
	}

}