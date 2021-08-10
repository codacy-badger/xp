package com.enonic.xp.core.impl.content;

import com.google.common.base.Preconditions;

import com.enonic.xp.content.ContentService;

abstract class AbstractArchiveCommand
{
    final ContentService contentService;

    AbstractArchiveCommand( final Builder builder )
    {
        this.contentService = builder.contentService;
    }

    public static class Builder<B extends Builder<B>>
    {
        private ContentService contentService;

        Builder()
        {
        }

        public B contentService( final ContentService contentService )
        {
            this.contentService = contentService;
            return (B) this;
        }

        void validate()
        {
            Preconditions.checkNotNull( contentService, "contentService cannot be null" );
        }
    }
}
