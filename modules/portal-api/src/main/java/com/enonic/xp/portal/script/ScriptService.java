package com.enonic.xp.portal.script;

import com.enonic.xp.resource.ResourceKey;

public interface ScriptService
{
    public ScriptExports execute( ResourceKey script );
}
