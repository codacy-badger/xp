package com.enonic.xp.admin.impl.portal;

import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.inputtype.InputTypeResolver;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.controller.ControllerScript;
import com.enonic.xp.portal.controller.ControllerScriptFactory;
import com.enonic.xp.portal.handler.PortalHandlerWorker;
import com.enonic.xp.resource.ResourceKey;

final class InputTypeHandlerWorker
    extends PortalHandlerWorker<PortalRequest>
{
    ControllerScriptFactory controllerScriptFactory;

//    AdminToolDescriptorService adminToolDescriptorService;

    InputTypeResolver inputTypeResolver;

    InputTypeName inputTypeName;

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
        final ResourceKey scriptDir =
            ResourceKey.from( inputTypeName.getApplicationKey(), "admin/inputtypes/" + inputTypeName.getLocalName() );
        final ControllerScript controllerScript = this.controllerScriptFactory.fromDir( scriptDir );
        return controllerScript.execute( this.request );
    }
}
