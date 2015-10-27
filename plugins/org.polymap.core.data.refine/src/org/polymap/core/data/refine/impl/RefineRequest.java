package org.polymap.core.data.refine.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
//import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;

import com.google.common.net.HttpHeaders;

public class RefineRequest
        implements HttpServletRequest {

    private Map<String,String> params;

    private File               file;

    private InputStream        stream;

    private Map<String,String> headers;


    public RefineRequest( Map<String,String> params, Map<String,String> headers ) {
        setParams( params );
        setHeaders( headers );
    }


    public RefineRequest( Map<String,String> params, Map<String,String> headers, InputStream stream,
            String fileName ) {
        setParams( params );
        setHeaders( headers );
        setFileName( fileName );
        this.stream = stream;
    }


    public RefineRequest( Map<String,String> params, HashMap<String,String> headers, File file,
            String fileName ) {
        setParams( params );
        setHeaders( headers );
        setFileName( fileName );
        this.file = file;
    }


    private void setFileName( String fileName ) {
        if (!StringUtils.isEmpty( fileName )) {
            this.params.put( "fileName", fileName );
        }
    }


    private void setHeaders( Map<String,String> headers ) {
        this.headers = new TreeMap<String,String>( String.CASE_INSENSITIVE_ORDER );
        if (headers != null) {
            this.headers.putAll( headers );
        }
    }


    private void setParams( Map<String,String> params ) {
        this.params = new TreeMap<String,String>( String.CASE_INSENSITIVE_ORDER );
        if (params != null) {
            this.params.putAll( params );
        }
    }


    @Override
    public String getQueryString() {
        StringBuffer query = new StringBuffer( "?" );
        for (String key : params.keySet()) {
            query.append( key ).append( "=" ).append( params.get( key ) ).append( "&" );
        }
        return query.toString();
    }


    @Override
    public String getParameter( String name ) {
        return params.get( name );
    }


    public void setParameter( String name, String value ) {
        params.put( name, value );
    }


    @Override
    public String getCharacterEncoding() {
        // encodings are from ava.nio.charset.Charset.availableCharsets()
        // default
        return null;// Charsets.ISO_8859_1.name();
    }


    @Override
    public String getContentType() {
        return headers.get( HttpHeaders.CONTENT_TYPE );
    }


    @Override
    public Object getAttribute( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException();

    }


    @Override
    public void setCharacterEncoding( String env ) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();

    }


    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException();

    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();

    }


    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String[] getParameterValues( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Map<String,String[]> getParameterMap() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getScheme() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getServerName() {
        throw new UnsupportedOperationException();

    }


    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException();

    }


    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException();

    }


    @Override
    public void setAttribute( String name, Object o ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public void removeAttribute( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();

    }


    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();

    }


    @Override
    public RequestDispatcher getRequestDispatcher( String path ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getRealPath( String path ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException();

    }


    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException();

    }


    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();

    }


    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();

    }


    @Override
    public AsyncContext startAsync( ServletRequest servletRequest, ServletResponse servletResponse )
            throws IllegalStateException {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException();

    }


    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();

    }


    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();

    }


    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException();

    }


    @Override
    public long getDateHeader( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getHeader( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Enumeration<String> getHeaders( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Enumeration<String> getHeaderNames() {
        throw new UnsupportedOperationException();

    }


    @Override
    public int getIntHeader( String name ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getMethod() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isUserInRole( String role ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getRequestURI() {
        throw new UnsupportedOperationException();

    }


    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();

    }


    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException();

    }


    @Override
    public HttpSession getSession( boolean create ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean authenticate( HttpServletResponse response )
            throws IOException, ServletException {
        throw new UnsupportedOperationException();

    }


    @Override
    public void login( String username, String password ) throws ServletException {
        throw new UnsupportedOperationException();

    }


    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();

    }


    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException();

    }


    @Override
    public Part getPart( String name ) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }


    // since servlet 3.1 @Override
    public long getContentLengthLong() {
        throw new UnsupportedOperationException();
    }


    // since servlet 3.1 @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }


    // since servlet 3.1 @Override
    // public <T extends HttpUpgradeHandler> T upgrade( Class<T> handlerClass ) {
    // throw new UnsupportedOperationException();
    // }

    public File file() {
        return file;
    }


    public InputStream stream() {
        return stream;
    }

}
