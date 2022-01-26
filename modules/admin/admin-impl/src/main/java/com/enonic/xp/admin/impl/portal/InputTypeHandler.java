package com.enonic.xp.admin.impl.portal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.inputtype.InputTypeResolver;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.trace.Trace;
import com.enonic.xp.trace.Tracer;
import com.enonic.xp.web.WebRequest;
import com.enonic.xp.web.WebResponse;
import com.enonic.xp.web.handler.BaseWebHandler;
import com.enonic.xp.web.handler.WebHandler;
import com.enonic.xp.web.handler.WebHandlerChain;

@Component(immediate = true, service = WebHandler.class)
public final class InputTypeHandler
    extends BaseWebHandler
{
    private InputTypeResolver inputTypeResolver;

    public static final String INPUT_TYPE_START = "/admin/input-type";

    public static final String INPUT_TYPE_PREFIX = INPUT_TYPE_START + "/";

    public static final Pattern PATTERN = Pattern.compile( "^([^/^_]+)/([^/^_]+)/([^/^_]+)" );

    private ResourceService resourceService;


    public InputTypeHandler()
    {
        super( 60 );
    }

    @Override
    protected boolean canHandle( final WebRequest webRequest )
    {
        return webRequest.getRawPath().startsWith( INPUT_TYPE_START );
    }

    @Override
    protected WebResponse doHandle( final WebRequest webRequest, final WebResponse webResponse, final WebHandlerChain webHandlerChain )
        throws Exception
    {
//        WebHandlerHelper.checkAdminAccess( webRequest );

        PortalRequest portalRequest = new PortalRequest( webRequest );
        portalRequest.setContextPath( portalRequest.getBaseUri() );

        final InputTypeHandlerWorker worker = buildWorker( portalRequest );

        final Trace trace = Tracer.newTrace( "portalRequest" );
        if ( trace == null )
        {
            try
            {
                return worker.execute();
            }
            catch ( Exception e )
            {
                throw e;
            }
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

    public InputTypeHandlerWorker buildWorker( final PortalRequest portalRequest )
    {
        final String path = portalRequest.getRawPath();

        if ( path.startsWith( INPUT_TYPE_PREFIX ) )
        {
            final String subPath = path.substring( INPUT_TYPE_PREFIX.length() );
            final Matcher matcher = PATTERN.matcher( subPath );

            if ( matcher.find() )
            {
                final ApplicationKey applicationKey = ApplicationKey.from( matcher.group( 1 ) );
                final String inputTypeName = matcher.group( 2 );

                final InputTypeHandlerWorker worker = new InputTypeHandlerWorker( portalRequest );

                worker.resourceName = matcher.group( 3 );
                worker.inputTypeName = InputTypeName.from( applicationKey, inputTypeName );

                worker.inputTypeResolver = this.inputTypeResolver;
                worker.resourceService = this.resourceService;

                return worker;
            }
        }
        throw new IllegalArgumentException( "inputTypeName must be set" );
    }

//    @Reference
//    public void setAdminToolDescriptorService( final AdminToolDescriptorService adminToolDescriptorService )
//    {
//        this.adminToolDescriptorService = adminToolDescriptorService;
//    }

    @Reference
    public void setInputTypeResolver( final InputTypeResolver inputTypeResolver )
    {
        this.inputTypeResolver = inputTypeResolver;
    }

    @Reference
    public void setResourceService( final ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }
}
