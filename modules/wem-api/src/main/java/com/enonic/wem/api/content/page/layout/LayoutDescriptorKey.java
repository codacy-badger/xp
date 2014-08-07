package com.enonic.wem.api.content.page.layout;

import org.apache.commons.lang.StringUtils;

import com.enonic.wem.api.content.page.ComponentDescriptorName;
import com.enonic.wem.api.content.page.DescriptorKey;
import com.enonic.wem.api.module.ModuleKey;
import com.enonic.wem.api.resource.ModuleResourceKey;

public final class LayoutDescriptorKey
    extends DescriptorKey
{
    private LayoutDescriptorKey( final ModuleKey moduleKey, final ComponentDescriptorName descriptorName )
    {
        super( moduleKey, descriptorName, DescriptorType.LAYOUT );
    }

    public static LayoutDescriptorKey from( final ModuleKey moduleKey, final ComponentDescriptorName descriptorName )
    {
        return new LayoutDescriptorKey( moduleKey, descriptorName );
    }

    public static LayoutDescriptorKey from( final String layoutDescriptorKey )
    {
        final String moduleKey = StringUtils.substringBefore( layoutDescriptorKey, SEPARATOR );
        final String descriptorName = StringUtils.substringAfter( layoutDescriptorKey, SEPARATOR );
        return new LayoutDescriptorKey( ModuleKey.from( moduleKey ), new ComponentDescriptorName( descriptorName ) );
    }

    @Override
    public ModuleResourceKey toResourceKey()
    {
        return ModuleResourceKey.from( getModuleKey(), "component/" + getName().toString() + "/layout.xml" );
    }
}
