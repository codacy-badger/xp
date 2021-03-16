package com.enonic.xp.admin.impl.json.content.page;

import java.util.List;

import com.google.common.collect.ImmutableList;

import com.enonic.xp.admin.impl.rest.resource.schema.content.LocaleMessageResolver;
import com.enonic.xp.admin.impl.rest.resource.schema.mixin.InlineMixinResolver;
import com.enonic.xp.page.PageDescriptor;
import com.enonic.xp.page.PageDescriptors;

@SuppressWarnings("UnusedDeclaration")
public class PageDescriptorListJson
{
    private final List<PageDescriptorJson> pageDescriptors;

    public PageDescriptorListJson( final List<PageDescriptorJson> pageDescriptors )
    {
        this.pageDescriptors = pageDescriptors;
    }

    public PageDescriptorListJson( final PageDescriptors pageDescriptors, final LocaleMessageResolver localeMessageResolver,
                                   final InlineMixinResolver inlineMixinResolver )
    {
        final ImmutableList.Builder<PageDescriptorJson> builder = ImmutableList.builder();
        if ( pageDescriptors != null )
        {
            for ( final PageDescriptor pageDescriptor : pageDescriptors )
            {
                builder.add( new PageDescriptorJson( pageDescriptor, localeMessageResolver, inlineMixinResolver ) );
            }
        }
        this.pageDescriptors = builder.build();
    }

    public List<PageDescriptorJson> getDescriptors()
    {
        return pageDescriptors;
    }
}
