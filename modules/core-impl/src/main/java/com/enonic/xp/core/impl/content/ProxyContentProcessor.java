package com.enonic.xp.core.impl.content;

import com.enonic.xp.content.CreateContentParams;
import com.enonic.xp.content.UpdateContentParams;
import com.enonic.xp.content.attachment.CreateAttachments;
import com.enonic.xp.schema.content.ContentType;
import com.enonic.xp.schema.content.ContentTypeName;
import com.enonic.xp.media.MediaInfo;
import com.enonic.xp.schema.mixin.MixinService;

class ProxyContentProcessor
{
    private final ImageContentProcessor imageHandler;

    ProxyContentProcessor( final Builder builder )
    {
        imageHandler = ImageContentProcessor.create().
            mediaInfo( builder.mediaInfo ).
            contentType( builder.contentType ).
            mixinService( builder.mixinService ).build();

    }

    CreateContentParams processCreate( final CreateContentParams params )
    {
        if ( params.getType().isImageMedia() )
        {
            return imageHandler.processCreate( params );
        }
        else
        {
            return params;
        }
    }

    ProcessUpdateResult processEdit( final ContentTypeName contentTypeName, final UpdateContentParams updateContentParams,
                                     final CreateAttachments createAttachments )
    {
        if ( contentTypeName.isImageMedia() )
        {
            return imageHandler.processUpdate( updateContentParams, createAttachments );
        }
        else
        {
            return null;
        }
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder {

        private MediaInfo mediaInfo;

        private ContentType contentType;

        private MixinService mixinService;

        public Builder mediaInfo( final MediaInfo mediaInfo )
        {
            this.mediaInfo = mediaInfo;
            return this;
        }

        public Builder contentType( final ContentType contentType )
        {
            this.contentType = contentType;
            return this;
        }

        public Builder mixinService( final MixinService mixinService )
        {
            this.mixinService = mixinService;
            return this;
        }

        public ProxyContentProcessor build()
        {
            return new ProxyContentProcessor( this );
        }
    }

}
