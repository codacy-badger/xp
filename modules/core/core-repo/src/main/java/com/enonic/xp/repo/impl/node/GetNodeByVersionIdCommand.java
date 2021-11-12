package com.enonic.xp.repo.impl.node;

import com.google.common.base.Preconditions;

import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeVersionId;
import com.enonic.xp.repo.impl.InternalContext;

public class GetNodeByVersionIdCommand
    extends AbstractNodeCommand
{
    private final NodeVersionId versionId;

    private GetNodeByVersionIdCommand( final Builder builder )
    {
        super( builder );
        this.versionId = builder.versionId;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public Node execute()
    {
        final Context context = ContextAccessor.current();

        return this.nodeStorageService.getNode( versionId, InternalContext.from( context ) );
    }

    public static final class Builder
        extends AbstractNodeCommand.Builder<Builder>
    {
        private NodeVersionId versionId;

        private Builder()
        {
            super();
        }

        public Builder versionId( NodeVersionId versionId )
        {
            this.versionId = versionId;
            return this;
        }

        @Override
        void validate()
        {
            super.validate();
            Preconditions.checkNotNull( this.versionId );
        }

        public GetNodeByVersionIdCommand build()
        {
            this.validate();
            return new GetNodeByVersionIdCommand( this );
        }

    }

}
