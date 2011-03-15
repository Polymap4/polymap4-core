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


import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.AndImpl;
import org.geotools.filter.OrImpl;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleBuilder;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class FilterWrapper {

	Rule rule=null;
	
	Filter filter=null;
	Filter parent_filter=null;
	
	
	ILayer layer;
	StyleBuilder style_builder = new StyleBuilder();
	
	
	static FilterFactory filterFactory = CommonFactoryFinder
	.getFilterFactory(null);

	public final static String[] operators = { "=" , ">" , ">=" , "<" , "<=" , "!=" };

	public FilterWrapper(Rule rule,ILayer layer) {
		this.rule=rule;
		this.layer=layer;
	}
	
	public FilterWrapper(Filter _filter, Filter parent_filter, ILayer layer) {
		this.filter=_filter;
		this.parent_filter=parent_filter;
		this.layer=layer;
	}

	

	public Filter getFilter() {
		if (filter!=null)
			return filter;
		else
			return rule.getFilter();
	}
	
	public void setFilter(Filter newFilter) {
		
		if (parent_filter!=null)
		{
			int filter_index=((org.geotools.filter.LogicFilterImpl)parent_filter).getChildren().indexOf(filter);
			((org.geotools.filter.LogicFilterImpl)parent_filter).getChildren().set(filter_index, newFilter);
		}
		if (filter!=null)
			filter=newFilter;
		else
			rule.setFilter(newFilter);
	}
	
	
	
	public Expression getFilterExpression1() {
		if (getFilter().getClass() == org.geotools.filter.IsEqualsToImpl.class)
			return ((org.geotools.filter.IsEqualsToImpl) (getFilter()))
					.getExpression1();
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanImpl.class)
			return ((org.geotools.filter.IsGreaterThanImpl) (getFilter()))
					.getExpression1();
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanOrEqualToImpl.class)
			return ((org.geotools.filter.IsGreaterThanOrEqualToImpl) (getFilter()))
					.getExpression1();

		else if (getFilter().getClass() == org.geotools.filter.IsLessThenImpl.class)
			return ((org.geotools.filter.IsLessThenImpl) (getFilter()))
					.getExpression1();
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenOrEqualToImpl.class)
			return ((org.geotools.filter.IsLessThenOrEqualToImpl) (getFilter()))
					.getExpression1();

		else if (getFilter().getClass() == org.geotools.filter.IsNotEqualToImpl.class)
			return ((org.geotools.filter.IsNotEqualToImpl) (getFilter()))
					.getExpression1();
		return null;
	}
	
	public Expression getFilterExpression2() {
		if (getFilter().getClass() == org.geotools.filter.IsEqualsToImpl.class)
			return ((org.geotools.filter.IsEqualsToImpl) (getFilter()))
					.getExpression2();
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanImpl.class)
			return ((org.geotools.filter.IsGreaterThanImpl) (getFilter()))
					.getExpression2();
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanOrEqualToImpl.class)
			return ((org.geotools.filter.IsGreaterThanOrEqualToImpl) (getFilter()))
					.getExpression2();
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenImpl.class)
			return ((org.geotools.filter.IsLessThenImpl) (getFilter()))
					.getExpression2();
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenOrEqualToImpl.class)
			return ((org.geotools.filter.IsLessThenOrEqualToImpl) (getFilter()))
					.getExpression2();
		else if (getFilter().getClass() == org.geotools.filter.IsNotEqualToImpl.class)
			return ((org.geotools.filter.IsNotEqualToImpl) (getFilter()))
					.getExpression2();
		return null;

	}
	
	
	public Expression setFilterExpression1(String expr,boolean literal) {
		Expression expression=null;
		
		if (literal)
			expression = style_builder.literalExpression(expr);
		else
			expression = style_builder.attributeExpression(expr);
		
		if (getFilter().getClass() == org.geotools.filter.IsEqualsToImpl.class)
			 ((org.geotools.filter.IsEqualsToImpl) (getFilter()))
					.setExpression1(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanImpl.class)
			 ((org.geotools.filter.IsGreaterThanImpl) (getFilter()))
					.setExpression1(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanOrEqualToImpl.class)
			 ((org.geotools.filter.IsGreaterThanOrEqualToImpl) (getFilter()))
					.setExpression1(expression);

		else if (getFilter().getClass() == org.geotools.filter.IsLessThenImpl.class)
			((org.geotools.filter.IsLessThenImpl) (getFilter()))
					.setExpression1(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenOrEqualToImpl.class)
			 ((org.geotools.filter.IsLessThenOrEqualToImpl) (getFilter()))
					
			 .setExpression1(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsNotEqualToImpl.class)
			 ((org.geotools.filter.IsNotEqualToImpl) (getFilter()))
			.setExpression1(expression);
		return null;

	}
	
	public Expression setFilterExpression2(String expr,boolean literal) {
		Expression expression=null;
		
		if (literal)
			expression = style_builder.literalExpression(expr);
		else
			expression = style_builder.attributeExpression(expr);
		
		if (getFilter().getClass() == org.geotools.filter.IsEqualsToImpl.class)
			 ((org.geotools.filter.IsEqualsToImpl) (getFilter()))
					.setExpression2(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanImpl.class)
			 ((org.geotools.filter.IsGreaterThanImpl) (getFilter()))
					.setExpression2(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanOrEqualToImpl.class)
			 ((org.geotools.filter.IsGreaterThanOrEqualToImpl) (getFilter()))
					.setExpression2(expression);

		else if (getFilter().getClass() == org.geotools.filter.IsLessThenImpl.class)
			((org.geotools.filter.IsLessThenImpl) (getFilter()))
					.setExpression2(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenOrEqualToImpl.class)
			 ((org.geotools.filter.IsLessThenOrEqualToImpl) (getFilter()))
					
			 .setExpression2(expression);
		else if (getFilter().getClass() == org.geotools.filter.IsNotEqualToImpl.class)
			 ((org.geotools.filter.IsNotEqualToImpl) (getFilter()))
			.setExpression2(expression);
		return null;
	}
	
	public String getFilterOperatorStr() {
		
		if (getFilter().getClass() == org.geotools.filter.IsEqualsToImpl.class)
			return "=";
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanImpl.class)
			return ">";
		else if (getFilter().getClass() == org.geotools.filter.IsGreaterThanOrEqualToImpl.class)
			return ">=";
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenImpl.class)
			return "<";
		else if (getFilter().getClass() == org.geotools.filter.IsLessThenOrEqualToImpl.class)
			return "<=";
		else if (getFilter().getClass() == org.geotools.filter.IsNotEqualToImpl.class)
			return "!=";
	
		else return null;
	}
	
	
	
	public void changeFilterOperator(String operator) {
		
		
		
		Expression e1 = getFilterExpression1();
		Expression e2 = getFilterExpression2();
		if (operator.equals(">"))
			setFilter(filterFactory.greater(e1,e2));
		else if (operator.equals(">="))
			setFilter(filterFactory.greaterOrEqual(e1,e2));
		else if (operator.equals("<"))
			setFilter(filterFactory.less(e1,e2));
		else if (operator.equals("<="))
			setFilter(filterFactory.lessOrEqual(e1,e2));
		else if (operator.equals("="))
			setFilter(filterFactory.equals(e1,e2));
		else if (operator.equals("!="))
			setFilter(filterFactory.notEqual(e1,e2));
		
	}
	
	public boolean isOrFilter() {
		return (getFilter() instanceof org.geotools.filter.OrImpl);
	}


	public boolean isAndFilter() {
		return (getFilter() instanceof org.geotools.filter.AndImpl);
	}
	
	public boolean isLogicFilter() {
		return (getFilter() instanceof org.geotools.filter.LogicFilterImpl);
	}

	public boolean isCompareFilter() {
		return (getFilter() instanceof org.geotools.filter.CompareFilterImpl);
	}

	public List<Filter> getChildFilterIterator() {
		if (!isLogicFilter()) return null;
		//return((org.geotools.filter.LogicFilterImpl)getFilter()).getFilterIterator();
		return ((org.geotools.filter.LogicFilterImpl)getFilter()).getChildren();
	}
	
	public void addDefaultFilter() {
		((org.geotools.filter.LogicFilterImpl)getFilter()).addFilter(createDefaultFilter());
	}
	
	public void setToDefaultLogicFilter() {
		setToOrFilter();
	}
	
	
	public static Filter createDefaultFilter() {
		return filterFactory.greater(filterFactory.literal(""),filterFactory.literal(""));
	}
	
	public void setToDefaulCompareFilter() {
		setFilter(createDefaultFilter());
	}
	
	
	public void setToOrFilter() {
		List<Filter> l; 		

		if (isAndFilter())
			l=((AndImpl)getFilter()).getChildren();
		else
			l = new ArrayList<Filter>();			
		setFilter(filterFactory.or(l));
	}
	
	public void setToAndFilter() {
		List<Filter> l;
		
		if (isOrFilter())
			l=((OrImpl)getFilter()).getChildren();
		else 
			l = new ArrayList<Filter>();
		setFilter(filterFactory.and(l));
	}
	
	
	
	public String getFilterAsString() {
		if (isOrFilter())
			return "Or";
		else if (isAndFilter())
			return "And";
		else return getFilter().toString();	
	}
	
	public String[] getFeatureAttributes() {
		String[] res = null;
		FeatureSource fs = null;
		try {
			//fs = geo_res.resolve(FeatureSource.class, null);
		    fs = PipelineFeatureSource.forLayer( layer, false );

			SimpleFeatureType schema = (SimpleFeatureType) fs.getSchema();
			int i = 0;
			res = new String[schema.getAttributeDescriptors().size()];
			for (AttributeDescriptor attr : schema.getAttributeDescriptors()) {
				res[i++] = "" + attr.getName();
			}
		} catch (Exception e) {
		    PolymapWorkbench.handleError(StylerPlugin.PLUGIN_ID, this, "Error while initializing attributes.", e);
		}

		return res;
	}
	
	/**
	 * 
	 * remove the filter from parent rule or filter
	 * 
	 */
	public void dispose() {
		if (rule!=null)
			rule.setFilter(null);
		else if (parent_filter!=null) {
			int filter_index=((org.geotools.filter.LogicFilterImpl)parent_filter).getChildren().indexOf(filter);
			((org.geotools.filter.LogicFilterImpl)parent_filter).getChildren().remove(filter_index);
		}
		
	}
}