package com.enonic.xp.core.impl.content;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.enonic.xp.archive.RestoreContentException;
import com.enonic.xp.archive.RestoreContentListener;
import com.enonic.xp.archive.RestoreContentParams;
import com.enonic.xp.archive.RestoreContentsResult;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentAccessException;
import com.enonic.xp.content.ContentAlreadyExistsException;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.MoveContentListener;
import com.enonic.xp.content.MoveContentParams;
import com.enonic.xp.content.MoveContentsResult;
import com.enonic.xp.node.MoveNodeException;
import com.enonic.xp.node.NodeAccessException;
import com.enonic.xp.node.NodeAlreadyExistAtPathException;

final class RestoreContentCommand
    extends AbstractArchiveCommand
    implements MoveContentListener
{
    private final RestoreContentParams params;

    private final RestoreContentListener restoreContentListener;

    private RestoreContentCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
        this.restoreContentListener = builder.restoreContentListener;
    }

    public static Builder create( final RestoreContentParams params )
    {
        return new Builder( params );
    }

    RestoreContentsResult execute()
    {
        params.validate();

        try
        {
            return doExecute();
        }
        catch ( MoveNodeException e )
        {
            throw new RestoreContentException( e.getMessage(), ContentPath.from( e.getPath().toString() ) );
        }
        catch ( NodeAlreadyExistAtPathException e )
        {
            throw new ContentAlreadyExistsException( ContentPath.from( e.getNode().toString() ), e.getRepositoryId(), e.getBranch() );
        }
        catch ( NodeAccessException e )
        {
            throw new ContentAccessException( e );
        }
    }

    private RestoreContentsResult doExecute()
    {
        final Content contentToRestore = contentService.getById( params.getContentId() );
        final Content container = contentService.getByPath( contentToRestore.getParentPath() );

        final String oldSourceParentPath = container.getData().getString( "oldParentPath" );

        final ContentPath oldParentPath = params.getPath() != null
            ? params.getPath()
            : !Strings.nullToEmpty( oldSourceParentPath ).isBlank()
                ? ContentPath.from( oldSourceParentPath )
                :  ContentPath.ROOT ;

        final MoveContentParams moveParams =
            MoveContentParams.create().contentId(  params.getContentId() ).parentContentPath( oldParentPath ).moveContentListener( this ).build();

        final MoveContentsResult result = contentService.move( moveParams );

        return RestoreContentsResult.create().addRestored( result.getMovedContents() ).build();
    }

    @Override
    public void contentMoved( final int count )
    {
        if ( restoreContentListener != null )
        {
            restoreContentListener.contentRestored( count );
        }
    }

    @Override
    public void setTotal( final int count )
    {
        if ( restoreContentListener != null )
        {
            restoreContentListener.setTotal( count );
        }
    }

    public static class Builder
        extends AbstractArchiveCommand.Builder<Builder>
    {
        private final RestoreContentParams params;

        private RestoreContentListener restoreContentListener;

        private Builder( final RestoreContentParams params )
        {
            this.params = params;
        }

        public Builder restoreListener( final RestoreContentListener restoreContentListener )
        {
            this.restoreContentListener = restoreContentListener;
            return this;
        }

        @Override
        void validate()
        {
            super.validate();
            Preconditions.checkNotNull( params );
        }

        public RestoreContentCommand build()
        {
            validate();
            return new RestoreContentCommand( this );
        }
    }

}
