package com.enonic.wem.core.schema.content;

import javax.inject.Inject;

import com.enonic.wem.api.command.Commands;
import com.enonic.wem.api.command.entity.DeleteNodeByPath;
import com.enonic.wem.api.command.entity.DeleteNodeResult;
import com.enonic.wem.api.command.schema.content.DeleteContentType;
import com.enonic.wem.api.command.schema.content.DeleteContentTypeResult;
import com.enonic.wem.api.entity.NodePath;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.core.entity.DeleteNodeByPathHandler;
import com.enonic.wem.core.index.IndexService;


public final class DeleteContentTypeHandler
    extends AbstractContentTypeHandler<DeleteContentType>
{
    private IndexService indexService;

    @Override
    public void handle()
        throws Exception
    {
        final ContentTypeName contentTypeName = command.getName();

        // TODO: Fix this, use new node API
        //if ( contentDao.countContentTypeUsage( contentTypeName, context.getJcrSession() ) > 0 )
        //{
        //    command.setResult( DeleteContentTypeResult.UNABLE_TO_DELETE );
        // }
        //else
        {
            final DeleteNodeByPath deleteNodeByPathCommand =
                Commands.node().delete().byPath( new NodePath( "/content-types/" + command.getName().toString() ) );

            final DeleteNodeResult result = deleteNode( deleteNodeByPathCommand );

            switch ( result )
            {
                case SUCCESS:
                    command.setResult( DeleteContentTypeResult.SUCCESS );
                    break;
                case NOT_FOUND:
                    command.setResult( DeleteContentTypeResult.NOT_FOUND );
                    break;
                default:
                    command.setResult( DeleteContentTypeResult.UNABLE_TO_DELETE );
            }
        }
    }

    private DeleteNodeResult deleteNode( final DeleteNodeByPath deleteNodeByPathCommand )
        throws Exception
    {
        final DeleteNodeByPathHandler deleteNodeByPathHandler = DeleteNodeByPathHandler.create().
            command( deleteNodeByPathCommand ).
            context( this.context ).
            indexService( this.indexService ).
            build();

        deleteNodeByPathHandler.handle();

        return deleteNodeByPathCommand.getResult();
    }

    @Inject
    public void setIndexService( final IndexService indexService )
    {
        this.indexService = indexService;
    }
}
