package com.enonic.wem.core.content.serializer;


import static org.junit.Assert.*;

public class ContentJsonSerializerTest
    extends AbstractContentSerializerTest
{
    @Override
    ContentSerializer getSerializer()
    {
        ContentJsonSerializer serializerJson = new ContentJsonSerializer();
        serializerJson.prettyPrint();
        return serializerJson;
    }

    @Override
    void assertSerializedResult( final String fileNameForExpected, final String actualSerialization )
    {
        assertEquals( "Serialization not as expected", loadJsonAsString( fileNameForExpected + ".json" ), actualSerialization );
    }

}
