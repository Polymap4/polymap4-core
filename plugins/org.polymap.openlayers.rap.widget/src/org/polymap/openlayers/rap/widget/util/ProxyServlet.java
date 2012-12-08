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

package org.polymap.openlayers.rap.widget.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * For JavaScript security reasons ( cross domain ) we sometimes 
 * need a proxy to load data ( e.g. for WFS )
 * This servlet is suposed to be a replacement for the proxy.cgi provided
 * by OpenLayers which is written in python. This Servlet (written in Java) fits
 * better into the RAP enviroment. 
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class ProxyServlet extends HttpServlet {

	private static final long serialVersionUID = -6526120143460180643L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doProxy(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO checkme - only GET is tested right now
		doProxy(request, response);
	}

	private void doProxy(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (getServletConfig().getInitParameter("host_list") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"There is no Host-List for this Servlet");
			return;

		}

		List<String> host_list = Arrays.asList(getServletConfig()
				.getInitParameter("host_list").split(","));

		String url_param = null;
		url_param = request.getParameter("url");

		if (url_param == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"URL Parameter Missing");
			return;
		}

		try {
			URL url = new URL(url_param);
			
			/* Check if the host is in the list of allowed hosts */
			if (!host_list.contains(url.getHost())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"URL Parameter Bad - the Host " + url.getHost()
								+ " is not in my list of valid Hosts!");
				return;
			}

			URLConnection con = url.openConnection();

			if (con.getContentType() != null)
				response.setContentType(con.getContentType());
			else
				response.setContentType("text/xml");

			/* check if the client accepts gzip Encoding */
			boolean client_accepts_gzip = false; // be pessimistic

			if (request.getHeader("Accept-Encoding") != null) {
				List<String> encoding_list = Arrays.asList(request.getHeader(
						"Accept-Encoding").split(","));
				client_accepts_gzip = (encoding_list.contains("gzip"));
			}
			response.setDateHeader("Date", System.currentTimeMillis());
			
			if (client_accepts_gzip) {
				response.setHeader("Content-Encoding", "gzip");
				ByteArrayOutputStream output_to_tmp = new ByteArrayOutputStream();
				IOUtils.copy(url.openStream(), output_to_tmp);
		
				OutputStream output_to_response = new GZIPOutputStream(response
						.getOutputStream());
				output_to_response.write(output_to_tmp.toByteArray());
				output_to_response.close();

			} else { // client will not accept gzip -> dont compress
				IOUtils.copy(url.openStream(), response.getOutputStream());
			}
			
		} catch (IOException e) {
			System.out.println("Err" + e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Err:" + e);
		}

	}
}
