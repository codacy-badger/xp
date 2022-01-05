package com.enonic.xp.node;


import java.util.Objects;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public final class NodeId
    extends UUID
{
    public NodeId()
    {
        super();
    }

    private NodeId( final String value )
    {
        super( value );
    }

    @Override
    public boolean equals( final Object o )
    {
        return super.equals( o );
    }

    public static NodeId from( final String string )
    {
        return new NodeId( string );
    }

    public static NodeId from( final Object object )
    {
        return new NodeId( Objects.toString( object, null) );
    }
}
