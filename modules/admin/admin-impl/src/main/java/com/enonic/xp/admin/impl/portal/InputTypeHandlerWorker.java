package com.enonic.xp.admin.impl.portal;

import com.google.common.net.MediaType;

import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.inputtype.InputTypeResolver;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.handler.PortalHandlerWorker;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.util.MediaTypes;
import com.enonic.xp.web.WebException;

final class InputTypeHandlerWorker
    extends PortalHandlerWorker<PortalRequest>
{
    ResourceService resourceService;

    InputTypeResolver inputTypeResolver;

    InputTypeName inputTypeName;

    String resourceName;

    InputTypeHandlerWorker( final PortalRequest request )
    {
        super( request );
    }

    @Override
    public PortalResponse execute()
        throws Exception
    {
        //Retrieves the AdminToolDescriptor
//        final AdminToolDescriptor adminToolDescriptor = adminToolDescriptorService.getByKey( descriptorKey );
//        if ( adminToolDescriptor == null )
//        {
//            throw WebException.notFound( String.format( "Admin application [%s] not found", descriptorKey ) );
//        }

        //Checks if the access to AdminToolDescriptor is allowed
//        final PrincipalKeys principals = ContextAccessor.current().
//            getAuthInfo().
//            getPrincipals();
//        if ( !adminToolDescriptor.isAccessAllowed( principals ) )
//        {
//            throw WebException.forbidden( String.format( "You don't have permission to access [%s]", descriptorKey ) );
//        }

        //Renders the Admin application
        final ResourceKey resourceKey =
            ResourceKey.from( inputTypeName.getApplicationKey(), "admin/inputtypes/" + inputTypeName.getLocalName() + "/" + resourceName );

        final Resource resource = resolveResource( resourceKey );

        final String type = MediaTypes.instance().fromFile( resource.getKey().getName() ).toString();
        return PortalResponse.create().body( resource ).contentType( MediaType.parse( type ) ).build();

//        final ControllerScript controllerScript = this.controllerScriptFactory.fromDir( scriptDir );
//        return controllerScript.execute( this.request );
    }

    private Resource resolveResource( final ResourceKey resourceKey )
    {
        final Resource resource = resourceService.getResource( resourceKey );
        if ( !resource.exists() )
        {
            throw WebException.notFound( String.format( "Resource [%s] not found", resourceKey ) );
        }
        return resource;
    }
}
