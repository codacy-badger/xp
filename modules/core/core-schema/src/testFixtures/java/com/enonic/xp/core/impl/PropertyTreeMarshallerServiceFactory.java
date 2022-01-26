package com.enonic.xp.core.impl;

import com.enonic.xp.core.impl.form.PropertyTreeMarshallerServiceImpl;
import com.enonic.xp.form.PropertyTreeMarshallerService;
import com.enonic.xp.inputtype.InputTypeResolver;
import com.enonic.xp.schema.mixin.MixinService;

public class PropertyTreeMarshallerServiceFactory
{
    private PropertyTreeMarshallerServiceFactory()
    {
    }

    public static PropertyTreeMarshallerService newInstance( final MixinService mixinService, final InputTypeResolver inputTypeResolver )
    {
        return new PropertyTreeMarshallerServiceImpl( mixinService, inputTypeResolver );
    }
}
