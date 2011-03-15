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

import org.eclipse.osgi.util.NLS;
import org.eclipse.rwt.RWT;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.polymap.styler.ui.messages"; //$NON-NLS-1$
	public  String ADD_FILTER;
	public  String ADD_FTS;
	public  String ADD_LINE_SYMBOLIZER;
	public  String ADD_POINT_SYMBOLIZER;
	public  String ADD_POLYGON_SYMBOLIZER;
	public  String ADD_RULE;
	public  String ADD_TEXT_SYMBOLIZER;
	public  String ATTRIBUTE;
	public  String BOLD;
	public  String BOTTOM;
	public  String BUTT;
	public  String CENTER;
	public  String COLOR;
	public  String DASH;
	public  String DASHDOT;
	public  String DOT;
	public  String FANTASY;
	public  String FILL;
	public  String FONT;
	public  String IMPORT;
	public  String LABEL;
	public  String LEFT;
	public  String LITERAL;
	public  String LONGDASH;
	public  String LONGDASHDOT;
	public  String MAXSCALE;
	public  String MIDDLE;
	public  String MINSCALE;
	public  String NAME;
	public  String NORMAL;
	public  String OPERANT;
	public  String OPERATOR;
	public  String REMOVE;
	public  String REMOVE_FILTER;
	public  String REMOVE_FTS;
	public  String RIGHT;
	public  String ROUND;
	public  String RULE_EDITOR;
	public  String SOLID;
	public  String SQUARE;
	public  String STROKE;
	public  String TIMES;
	public  String TOP;
	public  String VERDANA;
	public  String WESTERN;
	public  String RADIUS;
	public  String OPACITY;
	public  String ROTATION;
	public  String LINESTYLE;
	public  String LINECAP;
	public  String WIDTH;
	public  String SIZE;
	public  String WEIGHT;
	public  String FAMILY;
	public  String HALO;
	public  String VERTICAL;
	public  String HORIZONTAL;
	public  String EXPERT_MODE;
	public  String DOWNLOAD;
	public  String LOGIC_FILTER;
	public  String COMPARE_FILTER;
	public  String CIRCLE;
	public  String TRIANGLE;
	public  String STAR;
	public  String CROSS;
	public  String MARK;
	public  String MARK_TYPE;
	public  String OR;
	public  String AND;
	public  String TEXT_ALIGN;
	public  String STYLE;
	public  String EDIT_STYLE;
	public  String COMMIT_STYLE;
	public  String MOVE_UP;
	public  String MOVE_DOWN;
	
	private Messages() {
	}
	
	public static Messages get() {
		Class<Messages> clazz = Messages.class;
		return ( Messages )RWT.NLS.getISO8859_1Encoded( BUNDLE_NAME, clazz );
	}
}
