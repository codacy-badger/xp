package com.enonic.xp.admin.impl.portal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.RenderMode;
import com.enonic.xp.portal.handler.BasePortalHandler;
import com.enonic.xp.web.WebRequest;
import com.enonic.xp.web.WebResponse;
import com.enonic.xp.web.exception.ExceptionMapper;
import com.enonic.xp.web.exception.ExceptionRenderer;
import com.enonic.xp.web.handler.WebHandler;

@Component(immediate = true, service = WebHandler.class)
public class InputTypePortalHandler
    extends BasePortalHandler
{
    public static final String INPUT_TYPE_START = "/admin/input-type";

    public static final String INPUT_TYPE_PREFIX = INPUT_TYPE_START + "/";

//    public static final DescriptorKey DEFAULT_DESCRIPTOR_KEY = DescriptorKey.from( "com.enonic.xp.app.main:home" );

    public static final Pattern PATTERN = Pattern.compile( "^([^/^_]+)/([^/^_]+)" );

    public static InputTypeName getInputTypeName( final WebRequest webRequest )
    {
        final String path = webRequest.getRawPath();
        if ( path.startsWith( INPUT_TYPE_PREFIX ) )
        {
            final String subPath = path.substring( INPUT_TYPE_PREFIX.length() );
            final Matcher matcher = PATTERN.matcher( subPath );
            if ( matcher.find() )
            {
                final ApplicationKey applicationKey = ApplicationKey.from( matcher.group( 1 ) );
                final String inputTypeName = matcher.group( 2 );
                return InputTypeName.from( applicationKey, inputTypeName );
            }
        }
        throw new IllegalArgumentException( "inputTypeName must be set" );
    }

    @Override
    protected boolean canHandle( final WebRequest webRequest )
    {
        return webRequest.getRawPath().startsWith( INPUT_TYPE_START );
    }

    @Override
    protected PortalRequest createPortalRequest( final WebRequest webRequest, final WebResponse webResponse )
    {
        final PortalRequest portalRequest = new PortalRequest( webRequest );

        final InputTypeName inputTypeName = getInputTypeName( webRequest );
//        if ( descriptorKey == null )
//        {
//            portalRequest.setBaseUri( INPUT_TYPE_START );
//            portalRequest.setApplicationKey( DEFAULT_DESCRIPTOR_KEY.getApplicationKey() );
//        }
//        else
//        {
        portalRequest.setBaseUri( INPUT_TYPE_PREFIX + inputTypeName.getApplicationKey() + "/" + inputTypeName.getLocalName() );
        portalRequest.setApplicationKey( inputTypeName.getApplicationKey() );
//        }
        portalRequest.setMode( RenderMode.ADMIN );
        return portalRequest;
    }

    @Reference
    public void setWebExceptionMapper( final ExceptionMapper exceptionMapper )
    {
        this.exceptionMapper = exceptionMapper;
    }

    @Reference
    public void setExceptionRenderer( final ExceptionRenderer exceptionRenderer )
    {
        this.exceptionRenderer = exceptionRenderer;
    }
}
