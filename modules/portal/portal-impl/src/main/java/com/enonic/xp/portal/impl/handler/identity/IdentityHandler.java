package com.enonic.xp.portal.impl.handler.identity;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.hash.Hashing;

import com.enonic.xp.content.ContentService;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.handler.EndpointHandler;
import com.enonic.xp.portal.idprovider.IdProviderControllerService;
import com.enonic.xp.portal.impl.ContentResolver;
import com.enonic.xp.security.IdProviderKey;
import com.enonic.xp.trace.Trace;
import com.enonic.xp.trace.Tracer;
import com.enonic.xp.web.WebException;
import com.enonic.xp.web.WebRequest;
import com.enonic.xp.web.WebResponse;
import com.enonic.xp.web.handler.WebHandler;
import com.enonic.xp.web.handler.WebHandlerChain;
import com.enonic.xp.web.vhost.VirtualHost;
import com.enonic.xp.web.vhost.VirtualHostHelper;

@Component(immediate = true, service = WebHandler.class)
public class IdentityHandler
    extends EndpointHandler
{
    private static final int ID_PROVIDER_GROUP_INDEX = 1;

    private static final Pattern PATTERN = Pattern.compile( "^([^/^?]+)(?:/(login|logout))?" );

    private ContentService contentService;

    protected IdProviderControllerService idProviderControllerService;

    public IdentityHandler()
    {
        super( "idprovider" );
    }

    @Override
    protected PortalResponse doHandle( final WebRequest webRequest, final WebResponse webResponse, final WebHandlerChain webHandlerChain )
        throws Exception
    {
        final String restPath = findRestPath( webRequest );
        final Matcher matcher = PATTERN.matcher( restPath );

        if ( !matcher.find() )
        {
            throw WebException.notFound( "Not a valid idprovider url pattern" );
        }

        final IdProviderKey idProviderKey = IdProviderKey.from( matcher.group( ID_PROVIDER_GROUP_INDEX ) );

        final VirtualHost virtualHost = VirtualHostHelper.getVirtualHost( webRequest.getRawRequest() );

        if ( !( virtualHost == null || virtualHost.getIdProviderKeys().contains( idProviderKey ) ) )
        {
            throw WebException.forbidden( String.format( "'%s' id provider is forbidden", idProviderKey ) );
        }

        String idProviderFunction = matcher.group( 2 );

        final PortalRequest portalRequest =
            webRequest instanceof PortalRequest ? (PortalRequest) webRequest : new PortalRequest( webRequest );
        portalRequest.setContextPath( findPreRestPath( portalRequest ) + "/" + matcher.group( ID_PROVIDER_GROUP_INDEX ) );

        if ( idProviderFunction != null )
        {
            checkTicket( portalRequest );
        }

        if ( idProviderFunction == null )
        {
            idProviderFunction = webRequest.getMethod().
                toString().
                toLowerCase();
        }

        final IdentityHandlerWorker worker = new IdentityHandlerWorker( portalRequest );
        worker.idProviderKey = idProviderKey;
        worker.idProviderFunction = idProviderFunction;
        worker.contentResolver =  new ContentResolver( contentService );
        worker.idProviderControllerService = this.idProviderControllerService;
        final Trace trace = Tracer.newTrace( "portalRequest" );
        if ( trace == null )
        {
            return worker.execute();
        }

        trace.put( "path", webRequest.getPath() );
        trace.put( "method", webRequest.getMethod().toString() );
        trace.put( "host", webRequest.getHost() );
        trace.put( "httpRequest", webRequest );
        trace.put( "httpResponse", webResponse );
        trace.put( "context", ContextAccessor.current() );

        return Tracer.traceEx( trace, () -> {
            final PortalResponse response = worker.execute();
            addTraceInfo( trace, response );
            return response;
        } );
    }

    private void checkTicket( final PortalRequest req )
    {
        if ( getParameter( req, "redirect" ) != null )
        {
            final String ticket = removeParameter( req, "_ticket" );
            if ( ticket == null )
            {
                throw WebException.badRequest( "Missing ticket parameter" );
            }

            final String jSessionId = getJSessionId();
            final String expectedTicket = generateTicket( jSessionId );
            if ( expectedTicket.equals( ticket ) )
            {
                req.setValidTicket( Boolean.TRUE );
            }
            else
            {
                req.setValidTicket( Boolean.FALSE );
            }
        }
    }

    private String getParameter( final PortalRequest req, final String name )
    {
        final Collection<String> values = req.getParams().get( name );
        return values.isEmpty() ? null : values.iterator().next();
    }

    private String removeParameter( final PortalRequest req, final String name )
    {
        final Collection<String> values = req.getParams().removeAll( name );
        return values.isEmpty() ? null : values.iterator().next();
    }

    private String getJSessionId()
    {
        return ContextAccessor.current().
            getLocalScope().
            getSession().
            getKey().
            toString();
    }

    private String generateTicket( final String jSessionId )
    {
        return Hashing.sha1().
            newHasher().
            putString( jSessionId, StandardCharsets.UTF_8 ).
            hash().
            toString();
    }

    @Reference
    public void setContentService( final ContentService contentService )
    {
        this.contentService = contentService;
    }

    @Reference
    public void setIdProviderControllerService( final IdProviderControllerService idProviderControllerService )
    {
        this.idProviderControllerService = idProviderControllerService;
    }
}
