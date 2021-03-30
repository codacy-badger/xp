package com.enonic.xp.script;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationService;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.impl.async.ScriptAsyncService;
import com.enonic.xp.script.impl.executor.ScriptExecutor;
import com.enonic.xp.script.impl.service.ServiceRegistry;
import com.enonic.xp.script.impl.value.ScriptValueFactory;
import com.enonic.xp.script.runtime.ScriptRuntimeFactory;
import com.enonic.xp.script.runtime.ScriptSettings;

public interface ScriptFixturesFacade
{
    ScriptRuntimeFactory scriptRuntimeFactory( final ApplicationService applicationService, final ResourceService resourceService,
                                               final ScriptAsyncService scriptAsyncService );

    ScriptValueFactory<?> scriptValueFactory();

    ScriptExecutor createExecutor( final ScriptSettings scriptSettings, final ServiceRegistry serviceRegistry,
                                   ResourceService resourceService, Application application );

    static ScriptFixturesFacade getInstance()
    {
        return getInstance( className() );
    }

    static ScriptFixturesFacade getInstance( String facadeClassName )
    {

        try
        {
            return (ScriptFixturesFacade) Class.forName( facadeClassName ).getDeclaredConstructor().newInstance();
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( "Unsupported engine facade " + facadeClassName, e );
        }
    }

    private static String className()
    {
        final String scriptEngine = System.getProperty( "xp.scriptEngine", "GraalJS" );
        if ( scriptEngine.equalsIgnoreCase( "GraalJS" ) )
        {
            return "com.enonic.xp.script.graal.GraalJsScriptFixturesFacade";
        }
        else if ( scriptEngine.equalsIgnoreCase( "Nashorn" ) )
        {
            return "com.enonic.xp.script.impl.NashornScriptFixturesFacade";
        }
        else
        {
            throw new IllegalArgumentException( "Unsupported script engine " + scriptEngine );
        }
    }
}
