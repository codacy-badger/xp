package com.enonic.xp.xml.parser;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.app.ApplicationRelativeResolver;
import com.enonic.xp.data.ValueTypes;
import com.enonic.xp.inputtype.CustomInputType;
import com.enonic.xp.xml.DomElement;

@PublicApi
public final class XmlInputTypeParser
    extends XmlModelParser<XmlInputTypeParser>
{
    private static final XmlInputTypeConfigMapper CONFIG_MAPPER = new XmlInputTypeConfigMapper();

    private ApplicationRelativeResolver resolver;

    private CustomInputType.Builder builder;


    public XmlInputTypeParser builder( final CustomInputType.Builder builder )
    {
        this.builder = builder;
        return this;
    }

    @Override
    protected void doParse( final DomElement root )
        throws Exception
    {
        this.resolver = new ApplicationRelativeResolver( this.currentApplication );
        assertTagName( root, "input-type" );

        this.builder.valueType( ValueTypes.getByName( root.getChildValueTrimmed( "value-type" ) ) );

        //TODO: process all other fields
    }
}
