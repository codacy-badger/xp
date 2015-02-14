package com.enonic.xp.portal.script;

import com.enonic.xp.resource.ResourceKey;

public interface ScriptExports
{
    public ResourceKey getScript();

    public ScriptValue getValue();

    public boolean hasMethod( String name );

    public ScriptValue executeMethod( String name, Object... args );
}
