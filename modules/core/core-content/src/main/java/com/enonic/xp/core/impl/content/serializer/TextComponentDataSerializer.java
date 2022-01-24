package com.enonic.xp.core.impl.content.serializer;


import com.enonic.xp.data.PropertySet;
import com.enonic.xp.region.TextComponent;
import com.enonic.xp.region.TextComponentType;

final class TextComponentDataSerializer
    extends ComponentDataSerializer<TextComponent>
{
    private static final String VALUE = "value";

    @Override
    public void applyComponentToData( final TextComponent component, final PropertySet asData )
    {
        final PropertySet specBlock = asData.addSet( component.getType().toString() );

        if ( component.getText() != null )
        {
            specBlock.addString( VALUE, component.getText() );
        }
        if ( component.getMarkup() != null )
        {
            specBlock.addString( "markup", component.getMarkup() );
        }
    }

    @Override
    public TextComponent fromData( final PropertySet data )
    {
        final TextComponent.Builder component = TextComponent.create();

        final PropertySet specialBlockSet = data.getSet( TextComponentType.INSTANCE.toString() );

        if ( specialBlockSet != null )
        {
            if ( specialBlockSet.isNotNull( VALUE ) )
            {

                component.text( specialBlockSet.getString( VALUE ) );
            }

            if ( specialBlockSet.isNotNull( "markup" ) )
            {

                component.markup( specialBlockSet.getString( "markup" ) );
            }
        }

        return component.build();
    }
}
