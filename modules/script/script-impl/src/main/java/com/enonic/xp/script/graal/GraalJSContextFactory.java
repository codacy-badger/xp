package com.enonic.xp.script.graal;

import java.util.List;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public final class GraalJSContextFactory
{
    private final ClassLoader classLoader;

    private final Engine sharedEngine;

    public GraalJSContextFactory()
    {
        this( null, null );
    }

    public GraalJSContextFactory( final ClassLoader classLoader, final Engine sharedEngine )
    {
        this.classLoader = classLoader;
        this.sharedEngine = sharedEngine;
    }

    private static final HostAccess HOST_ACCESS = HostAccess.newBuilder( HostAccess.ALL )
        .targetTypeMapping( Value.class, Object.class, Value::hasArrayElements, value -> value.as( List.class ) )
        .build();

    public Context create()
    {
        final Context.Builder contextBuilder = Context.newBuilder( "js" )
            .allowHostAccess( HOST_ACCESS )
            .allowHostClassLookup( className -> true )
            .allowHostClassLoading( true );

        if ( Boolean.getBoolean( "polyglot.js.nashorn-compat" ) )
        {
            contextBuilder.allowExperimentalOptions( true );
        }

        if ( sharedEngine != null )
        {
            contextBuilder.engine( sharedEngine );
        }
        if ( classLoader != null )
        {
            contextBuilder.hostClassLoader( classLoader );
        }

        return contextBuilder.build();
    }
}
