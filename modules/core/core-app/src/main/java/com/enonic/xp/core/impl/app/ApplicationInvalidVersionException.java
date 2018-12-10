package com.enonic.xp.core.impl.app;

import org.osgi.framework.Version;

import com.enonic.xp.app.Application;

public class ApplicationInvalidVersionException
    extends RuntimeException
{
    public ApplicationInvalidVersionException( final Application application, final Version bundleVersion )
    {
        super( String.format( "Cannot start application '%s'. Required system version range is '%s', current version is '%s'.",
                              application.getKey().getName(), application.getSystemVersion(), bundleVersion ) );
    }
}
