/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.ui.internal;

import java.util.Locale;
import java.util.ResourceBundle;

import java.lang.reflect.Field;
import java.text.MessageFormat;

import net.refractions.udig.internal.ui.Images;
import net.refractions.udig.internal.ui.UiPlugin;

import org.apache.commons.lang.StringUtils;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.service.ContextProvider;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    
	private static final String BUNDLE_NAME = "net.refractions.udig.ui.internal.messages"; //$NON-NLS-1$

    private static final Locale defaultLocale = Locale.GERMAN;
    
    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    public static String get( String key, Object... args ) {
        Locale locale = ContextProvider.hasContext() ? RWT.getLocale() : defaultLocale;
        return get( locale, key, args );
    }

    /**
     * Find the localized message for the given key. If arguments are given, then the
     * result message is formatted via {@link MessageFormat}.
     *
     * @param locale The locale to use to localize the given message.
     * @param key
     * @param args If not null, then the message is formatted via {@link MessageFormat}
     * @return The message for the given key.
     */
    public static String get( Locale locale, String key, Object... args ) {
        try {
            // getBundle() caches the bundles
            ResourceBundle bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale, Messages.class.getClassLoader() );
            if (args == null || args.length == 0) {
                return bundle.getString( key );
            }
            else {
                String msg = bundle.getString( key );
                return MessageFormat.format( msg, args );
            }
        }
        catch (Exception e) {
            return StringUtils.substringAfterLast( key, "_" );
        }
    }


	private Messages() {
	}
	
	/**
     * Initialize the given Action from a ResourceBundle.
     * <p>
     * Makes use of the following keys:
     * <ul>
     * <li>prefix.label
     * <li>prefix.tooltip
     * <li>prefix.image
     * <li>prefix.description
     * </p>
     * <p>
     * Note: The use of a single image value is mapped to images for both the enabled and distabled
     * state of the IAction. the Local toolbar (elcl16/ and dlcl16/) is assumed if a path has not
     * been provided.
     * 
     * <pre><code>
     *  add_co.gif              (prefix.image)
     *     enabled: elcl16/add_co.gif
     *    disabled: dlcl/remove_co.gif
     *  tool16/discovery_wiz.16 (prefix.image)
     *     enabled: etool16/discovery_wiz.16
     *    disabled: etool16/discovery_wiz.16
     * </code></pre>
     * 
     * </p>
     * 
     * @param a action 
     * @param id used for binding (id.label, id.tooltip, ...)
     * @deprecated not safe, using this will cause bugs.  jeichar
     */
    public static void initAction( IAction a, String id ) {
        String labelKey = "_label"; //$NON-NLS-1$
        String tooltipKey = "_tooltip"; //$NON-NLS-1$
        String imageKey = "_image"; //$NON-NLS-1$
        String descriptionKey = "_description"; //$NON-NLS-1$
        if (id != null && id.length() > 0) {
            labelKey = id + labelKey;
            tooltipKey = id + tooltipKey;
            imageKey = id + imageKey;
            descriptionKey = id + descriptionKey;
        }
        String s = bind(labelKey);
        if (s != null)
            a.setText(s);
        s = bind(tooltipKey);
        if (s != null)
            a.setToolTipText(s);
        s = bind(descriptionKey);
        if (s != null)
            a.setDescription(s);
        String relPath = bind(imageKey);
        if (relPath != null && !relPath.equals(imageKey) && relPath.trim().length() > 0) {
            String dPath;
            String ePath;
            if (relPath.indexOf("/") >= 0) { //$NON-NLS-1$
                String path = relPath.substring(1);
                dPath = 'd' + path;
                ePath = 'e' + path;
            } else {
                dPath = "dlcl16/" + relPath; //$NON-NLS-1$
                ePath = "elcl16/" + relPath; //$NON-NLS-1$
            }
            ImageDescriptor image;

            image = Images.getDescriptor(ePath);
            if (id != null) {
                a.setImageDescriptor(image);
            }
            image = Images.getDescriptor(dPath);
            if (id != null) {
                a.setDisabledImageDescriptor(image);
            }
        }
    }

	private static String bind(String fieldName) {
		Field field;
		try {
			field = Messages.class.getDeclaredField(fieldName);
			return (String) field.get(null);
		} catch (Exception e) {
			UiPlugin.log("Error loading key " + fieldName, e); //$NON-NLS-1$
		}
		return null;
	}
}
