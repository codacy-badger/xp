package com.enonic.xp.core.impl.schema.inputtype;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.core.impl.schema.SchemaLoader;
import com.enonic.xp.inputtype.CustomInputType;
import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.xml.parser.XmlInputTypeParser;

final class InputTypeLoader
    extends SchemaLoader<InputTypeName, CustomInputType>
{
    InputTypeLoader( final ResourceService resourceService )
    {
        super( resourceService, "/admin/inputtypes" );
    }

    @Override
    protected CustomInputType load( final InputTypeName name, final Resource resource )
    {
        if ( ApplicationKey.BASE.equals( name.getApplicationKey() ) )
        {
            throw new IllegalArgumentException( "base input type cannot be loaded" );
        }

        final CustomInputType.Builder builder = CustomInputType.create();
        parseXml( resource, builder );

        return builder.name( name ).build();
    }

    private void parseXml( final Resource resource, final CustomInputType.Builder builder )
    {
        final XmlInputTypeParser parser = new XmlInputTypeParser();
        parser.currentApplication( resource.getKey().getApplicationKey() );
        parser.source( resource.readString() );
        parser.builder( builder );
        parser.parse();
    }

    //    @Override
    protected InputTypeName newName( final ApplicationKey appKey, final String name )
    {
        return InputTypeName.from( appKey, name );
    }
}
