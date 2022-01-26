package com.enonic.xp.core.impl.schema.inputtype;

import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public final class InputTypeServiceImpl
//    implements ContentTypeService
{
//    private final ContentTypeRegistry registry;
//
//    @Activate
//    public InputTypeServiceImpl( final @Reference ResourceService resourceService, @Reference final ApplicationService applicationService )
//    {
//        this.registry = new ContentTypeRegistry( resourceService, applicationService );
//    }
//
//    @Override
//    public ContentType getByName( final GetContentTypeParams params )
//    {
//        final GetContentTypeCommand command = new GetContentTypeCommand();
//        command.registry = this.registry;
//        command.params = params;
//        return command.execute();
//    }
//
//    @Override
//    public ContentTypes getByApplication( final ApplicationKey applicationKey )
//    {
//        final GetApplicationContentTypesCommand command = new GetApplicationContentTypesCommand();
//        command.registry = this.registry;
//        command.mixinService = this.mixinService;
//        command.applicationKey = applicationKey;
//        return command.execute();
//    }
//
//    @Override
//    public ContentTypes getAll()
//    {
//        final GetAllContentTypesCommand command = new GetAllContentTypesCommand();
//        command.registry = this.registry;
//        command.mixinService = this.mixinService;
//        return command.execute();
//    }
//
//    @Override
//    public Set<String> getMimeTypes( final ContentTypeNames names )
//    {
//        return ContentTypeFromMimeTypeResolver.resolveMimeTypes( names );
//    }
//
//    @Override
//    public ContentTypeValidationResult validate( final ContentType type )
//    {
//        final ContentTypeSuperTypeValidator validator = ContentTypeSuperTypeValidator.create().contentTypeService( this ).build();
//
//        validator.validate( type.getName(), type.getSuperType() );
//        return validator.getResult();
//    }
}
