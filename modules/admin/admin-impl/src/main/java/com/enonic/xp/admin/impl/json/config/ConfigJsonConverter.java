package com.enonic.xp.admin.impl.json.config;

import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.enonic.xp.inputtype.InputTypeConfig;
import com.enonic.xp.inputtype.InputTypeProperty;

public final class ConfigJsonConverter
    implements Function<InputTypeConfig, ObjectNode>
{
    public static final ConfigJsonConverter DEFAULT = new ConfigJsonConverter( ConfigJsonConverter::defaultPropertyConverter );

    private final Function<InputTypeProperty, ObjectNode> propertyConverter;

    public ConfigJsonConverter( final Function<InputTypeProperty, ObjectNode> propertyConverter )
    {
        this.propertyConverter = propertyConverter;
    }

    public ObjectNode apply( final InputTypeConfig config )
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();
        for ( final String name : config.getNames() )
        {
            final ArrayNode array = JsonNodeFactory.instance.arrayNode();
            config.getProperties( name ).stream().map( propertyConverter ).forEach( array::add );
            json.set( name, array );
        }

        return json;
    }

    private static ObjectNode defaultPropertyConverter( final InputTypeProperty property )
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();

        for ( final Map.Entry<String, String> attribute : property.getAttributes().entrySet() )
        {
            json.put( "@" + attribute.getKey(), attribute.getValue() );
        }

        json.put( "value", property.getValue() );

        return json;
    }
}
