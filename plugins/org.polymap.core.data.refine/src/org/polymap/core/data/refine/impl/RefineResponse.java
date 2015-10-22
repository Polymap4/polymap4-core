package org.polymap.core.data.refine.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;

public class RefineResponse
        implements HttpServletResponse {

    private int                sc;

    private String             charset;

    private Map<String,String> headers = Maps.newHashMap();

    private StringWriter       writer;


    @Override
    public void setStatus( int sc ) {
        this.sc = sc;
    }


    @Override
    public int getStatus() {
        return sc;
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new StringWriter();
        }
        return new PrintWriter( writer );
    }


    @Override
    public void setCharacterEncoding( String charset ) {
        this.charset = charset;
    }


    @Override
    public void setHeader( String name, String value ) {
        headers.put( name, value );
    }


    public Object result() {
        return writer != null ? writer.getBuffer().toString() : null;
    }


    // unimplemented stuff
    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();

    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();

    }


    @Override
    public void setContentLength( int len ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public void setContentType( String type ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public void setBufferSize( int size ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();

    }


    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException();

    }


    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();

    }


    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException();

    }


    @Override
    public void reset() {
        throw new UnsupportedOperationException();

    }


    @Override
    public void setLocale( Locale loc ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addCookie( Cookie cookie ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean containsHeader( String name ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String encodeURL( String url ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String encodeRedirectURL( String url ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String encodeUrl( String url ) {
        throw new UnsupportedOperationException();

    }


    @Override
    public String encodeRedirectUrl( String url ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void sendError( int sc, String msg ) throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public void sendError( int sc ) throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public void sendRedirect( String location ) throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setDateHeader( String name, long date ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addDateHeader( String name, long date ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addHeader( String name, String value ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setIntHeader( String name, int value ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addIntHeader( String name, int value ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setStatus( int sc, String sm ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getHeader( String name ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Collection<String> getHeaders( String name ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }


    // since servlet 3.1 @Override
    public void setContentLengthLong( long len ) {
        throw new UnsupportedOperationException();
    }

}
