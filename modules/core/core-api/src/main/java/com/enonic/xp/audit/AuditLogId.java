package com.enonic.xp.audit;


import java.util.Objects;

import com.enonic.xp.annotation.PublicApi;

@PublicApi
public final class AuditLogId
    extends com.enonic.xp.node.UUID
{
    public AuditLogId()
    {
        super();
    }

    private AuditLogId( final String value )
    {
        super( value );
    }

    @Override
    public boolean equals( final Object o )
    {
        return super.equals( o );
    }

    public static AuditLogId from( final String string )
    {
        return new AuditLogId( string );
    }

    public static AuditLogId from( final Object object )
    {
        return new AuditLogId( Objects.toString( object, null) );
    }
}
