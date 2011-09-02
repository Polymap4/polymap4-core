package org.polymap.service.fs.webdav;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Filter;
import com.bradmcevoy.http.FilterChain;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import com.bradmcevoy.http.http11.auth.SecurityManagerBasicAuthHandler;
import com.bradmcevoy.http.http11.auth.SecurityManagerDigestAuthenticationHandler;

/**
 * A filter to perform authentication before resource location.
 * <p/>
 * This allows the authenticated context to be available for resource location.
 * <p/>
 * Note that this filter contains a list of AuthenticationHandler. However, these
 * handlers MUST be designed to ignore the resource variable as it will always be
 * null when used with this filter. This approach allows these handlers to be used
 * with the post-resource-location approach.
 * <p/>
 * The This is taken from
 * {@link com.bradmcevoy.http.http11.auth.PreAuthenticationFilter}. The only
 * difference is that it provides an {@link DummyResource} with the realm so that the
 * above is not true for getRealm().
 */
public class PreAuthenticationFilter 
        implements Filter {

    private static Log log = LogFactory.getLog( PreAuthenticationFilter.class );

    private static final ThreadLocal<Request> tlRequest = new ThreadLocal<Request>();

    private final Http11ResponseHandler       responseHandler;

    private final List<AuthenticationHandler> authenticationHandlers;
    
    private final SecurityManager             securityManager;


    public static Request getCurrentRequest() {
        return tlRequest.get();
    }


    public PreAuthenticationFilter( Http11ResponseHandler responseHandler, SecurityManager securityManager ) {
        this.responseHandler = responseHandler;
        this.authenticationHandlers = new ArrayList<AuthenticationHandler>();
        this.securityManager = securityManager;
        
        authenticationHandlers.add( new SecurityManagerBasicAuthHandler( securityManager ) );
        authenticationHandlers.add( new SecurityManagerDigestAuthenticationHandler( securityManager ) );
    }


    public void process( FilterChain chain, Request request, Response response ) {
        log.trace( "process" );
        try {
            tlRequest.set( request );
            Object authTag = authenticate( request );
            if (authTag != null) {
                request.getAuthorization().setTag( authTag );
                chain.process( request, response );
            }
            else {
                responseHandler.respondUnauthorised( new DummyResource(), response, request );
            }
        }
        finally {
            tlRequest.remove();
        }
    }

    
    /*
     * Fake Resource that provides the real to the AuthenticationHandler only.
     */
    class DummyResource
            implements Resource {

        public Object authenticate( String user, String password ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public boolean authorise( Request request, Method method, Auth auth ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public String checkRedirect( Request request ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public Date getModifiedDate() {
            throw new RuntimeException( "not yet implemented." );
        }

        public String getName() {
            throw new RuntimeException( "not yet implemented." );
        }

        public String getRealm() {
            return securityManager.getRealm( null );
        }

        public String getUniqueId() {
            return "DummyResource";
        }
        
    }

    
    /**
     * Looks for an AuthenticationHandler which supports the given resource and
     * authorization header, and then returns the result of that handler's
     * authenticate method.
     *
     * Returns null if no handlers support the request
     *
     * @param request
     */
    public Object authenticate( Request request ) {
        for (AuthenticationHandler h : authenticationHandlers) {
            if (h.supports( null, request )) {
                Object o = h.authenticate( null, request );
                if (o == null) {
                    log.warn( "authentication failed by AuthenticationHandler:" + h.getClass() );
                }
                return o;
            }
        }

        if (request.getAuthorization() == null) {
            // note that this is completely normal, so just TRACE
            if (log.isTraceEnabled()) {
                log.debug( "No AuthenticationHandler supports this request - no authorisation given in request" );
            }
        }
        else {
            // authorisation was present in the request, but no handlers accepted it
            // - probably a config problem
            if (log.isWarnEnabled()) {
                log.warn( "No AuthenticationHandler supports this request with scheme:"
                        + request.getAuthorization().getScheme() );
            }
        }
        return null;
    }


    /**
     * Generates a list of http authentication challenges, one for each
     * supported authentication method, to be sent to the client.
     *
     * @param request - the current request
     * @return - a list of http challenges
     */
    public List<String> getChallenges( Request request ) {
        List<String> challenges = new ArrayList<String>();

        for (AuthenticationHandler h : authenticationHandlers) {
            String ch = h.getChallenge( null, request );
            challenges.add( ch );
        }
        return challenges;
    }
    
}
