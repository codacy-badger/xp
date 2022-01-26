package com.enonic.xp.admin.impl.portal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.inputtype.InputTypeResolver;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.controller.ControllerScriptFactory;
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
//    private AdminToolDescriptorService adminToolDescriptorService;

    private InputTypeResolver inputTypeResolver;

    private ControllerScriptFactory controllerScriptFactory;

    public InputTypeHandler()
    {
        super( 60 );
    }

    @Override
    protected boolean canHandle( final WebRequest webRequest )
    {
        return webRequest.getRawPath().startsWith( InputTypePortalHandler.INPUT_TYPE_START );
    }

    @Override
    protected WebResponse doHandle( final WebRequest webRequest, final WebResponse webResponse, final WebHandlerChain webHandlerChain )
        throws Exception
    {
//        WebHandlerHelper.checkAdminAccess( webRequest );

        PortalRequest portalRequest = (PortalRequest) webRequest;
        portalRequest.setContextPath( portalRequest.getBaseUri() );

        final InputTypeHandlerWorker worker = new InputTypeHandlerWorker( portalRequest );
        worker.controllerScriptFactory = this.controllerScriptFactory;
        worker.inputTypeResolver = this.inputTypeResolver;
        worker.inputTypeName = InputTypePortalHandler.getInputTypeName( webRequest );

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

//    @Reference
//    public void setAdminToolDescriptorService( final AdminToolDescriptorService adminToolDescriptorService )
//    {
//        this.adminToolDescriptorService = adminToolDescriptorService;
//    }

    @Reference
    public void setControllerScriptFactory( final ControllerScriptFactory controllerScriptFactory )
    {
        this.controllerScriptFactory = controllerScriptFactory;
    }

    @Reference
    public void setInputTypeResolver( final InputTypeResolver inputTypeResolver )
    {
        this.inputTypeResolver = inputTypeResolver;
    }
}
