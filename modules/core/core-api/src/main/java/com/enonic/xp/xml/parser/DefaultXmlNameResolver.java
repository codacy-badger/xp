package com.enonic.xp.xml.parser;

import com.google.common.base.CaseFormat;

final class DefaultXmlNameResolver
{
    public static String resolveName( final String name )
    {
        if ( name.contains( "-" ) )
        {
            return CaseFormat.LOWER_HYPHEN.to( CaseFormat.LOWER_CAMEL, name );
        }
        else
        {
            return name;
        }
    }
}
