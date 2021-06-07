package com.enonic.xp.archive;

import com.google.common.base.Preconditions;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.content.ContentId;

@PublicApi
public final class ArchiveContentParams
{
    private final ContentId contentId;

    private final ArchiveContentListener archiveContentListener;

    public ArchiveContentParams( Builder builder )
    {
        this.contentId = builder.contentId;
        this.archiveContentListener = builder.archiveContentListener;
    }

    public static ArchiveContentParams.Builder create()
    {
        return new ArchiveContentParams.Builder();
    }

    public ContentId getContentId()
    {
        return contentId;
    }

    public ArchiveContentListener getArchiveContentListener()
    {
        return archiveContentListener;
    }

    public void validate()
    {
        Preconditions.checkNotNull( this.contentId, "Content id cannot be null" );
    }

    public static final class Builder
    {
        private ContentId contentId;

        private ArchiveContentListener archiveContentListener;

        private Builder()
        {
        }

        public Builder contentId( ContentId contentId )
        {
            this.contentId = contentId;
            return this;
        }

        public Builder archiveContentListener( ArchiveContentListener archiveContentListener )
        {
            this.archiveContentListener = archiveContentListener;
            return this;
        }

        private void validate()
        {
            Preconditions.checkNotNull( contentId, "contentId must be set" );
        }

        public ArchiveContentParams build()
        {
            validate();
            return new ArchiveContentParams( this );
        }
    }
}
