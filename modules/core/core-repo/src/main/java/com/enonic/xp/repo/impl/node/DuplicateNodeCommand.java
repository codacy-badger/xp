package com.enonic.xp.repo.impl.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.data.Property;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.data.ValueTypes;
import com.enonic.xp.node.AttachedBinary;
import com.enonic.xp.node.CreateNodeParams;
import com.enonic.xp.node.DuplicateNodeParams;
import com.enonic.xp.node.FindNodesByParentParams;
import com.enonic.xp.node.FindNodesByParentResult;
import com.enonic.xp.node.InsertManualStrategy;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeAlreadyExistAtPathException;
import com.enonic.xp.node.NodeNotFoundException;
import com.enonic.xp.node.Nodes;
import com.enonic.xp.node.OperationNotPermittedException;
import com.enonic.xp.node.RefreshMode;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.repo.impl.binary.BinaryService;
import com.enonic.xp.repo.impl.search.NodeSearchService;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.util.Reference;

public final class DuplicateNodeCommand
    extends AbstractNodeCommand
{
    private static final Logger LOG = LoggerFactory.getLogger( DuplicateNodeCommand.class );

    private final DuplicateNodeParams params;

    private final BinaryService binaryService;

    private final DuplicateNodeResult.Builder result;

    private DuplicateNodeCommand( final Builder builder )
    {
        super( builder );
        this.params = builder.params;
        this.binaryService = builder.binaryService;
        this.result = DuplicateNodeResult.create();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public DuplicateNodeResult execute()
    {
        final Node existingNode = doGetById( params.getNodeId() );

        if ( existingNode == null )
        {
            throw new NodeNotFoundException( "Cannot duplicate node with id [" + params.getNodeId() + "]" );
        }

        if ( existingNode.isRoot() )
        {
            throw new OperationNotPermittedException( "Not allowed to duplicate root-node" );
        }

        final CreateNodeParams.Builder createNodeParams = CreateNodeParams.from( existingNode );
        attachBinaries( existingNode, createNodeParams );

        Node duplicatedNode = null;
        String newNodeName = existingNode.name().toString();
        do
        {
            try
            {
                newNodeName = DuplicateValueResolver.name( newNodeName );
                final CreateNodeParams processedParams = executeProcessors( createNodeParams.name( newNodeName ).build() );

                duplicatedNode =
                    CreateNodeCommand.create( this ).params( processedParams ).binaryService( binaryService ).build().execute();

            }
            catch ( NodeAlreadyExistAtPathException e )
            {
                // try again with other name
                LOG.debug( String.format( "[%s] node with [%s] parent already exist.", newNodeName, existingNode.parentPath().toString() ),
                           e );
            }
        }
        while ( duplicatedNode == null );

        result.node( duplicatedNode );
        nodeDuplicated( 1 );

        final NodeReferenceUpdatesHolder.Builder builder = NodeReferenceUpdatesHolder.create().
            add( existingNode.id(), duplicatedNode.id() );

        if ( params.getIncludeChildren() )
        {
            storeChildNodes( existingNode, duplicatedNode, builder );
        }

        final NodeReferenceUpdatesHolder nodesToBeUpdated = builder.build();

        RefreshCommand.create().
            refreshMode( RefreshMode.SEARCH ).
            indexServiceInternal( this.indexServiceInternal ).
            build().
            execute();

        updateNodeReferences( duplicatedNode, nodesToBeUpdated );
        updateChildReferences( duplicatedNode, nodesToBeUpdated );

        return result.build();
    }

    private CreateNodeParams executeProcessors( final CreateNodeParams originalParams )
    {
        if ( params.getProcessor() != null )
        {
            params.getProcessor().process( originalParams );
        }

        if ( params.getDataProcessor() != null )
        {
            return CreateNodeParams.create( originalParams ).data( params.getDataProcessor().process( originalParams.getData() ) ).build();
        }

        return originalParams;
    }

    private void storeChildNodes( final Node originalParent, final Node newParent, final NodeReferenceUpdatesHolder.Builder builder )
    {
        final FindNodesByParentResult findNodesByParentResult = doFindNodesByParent( FindNodesByParentParams.create().
            parentPath( originalParent.path() ).
            from( 0 ).
            size( NodeSearchService.GET_ALL_SIZE_FLAG ).
            build() );

        final Nodes children = GetNodesByIdsCommand.create( this ).
            ids( findNodesByParentResult.getNodeIds() ).
            build().
            execute();

        for ( final Node node : children )
        {
            final CreateNodeParams.Builder paramsBuilder = CreateNodeParams.from( node ).parent( newParent.path() );

            decideInsertStrategy( originalParent, node, paramsBuilder );

            attachBinaries( node, paramsBuilder );

            final CreateNodeParams originalParams = paramsBuilder.build();

            final CreateNodeParams processedParams = executeProcessors( originalParams );

            final Node newChildNode =
                CreateNodeCommand.create( this ).params( processedParams ).binaryService( this.binaryService ).build().execute();

            builder.add( node.id(), newChildNode.id() );

            result.addChild( newChildNode );
            nodeDuplicated( 1 );

            storeChildNodes( node, newChildNode, builder );
        }
    }

    private void decideInsertStrategy( final Node originalParent, final Node node, final CreateNodeParams.Builder paramsBuilder )
    {
        if ( originalParent.getChildOrder().isManualOrder() )
        {
            paramsBuilder.manualOrderValue( node.getManualOrderValue() ).
                insertManualStrategy( InsertManualStrategy.MANUAL );
        }
    }

    private void attachBinaries( final Node node, final CreateNodeParams.Builder paramsBuilder )
    {
        for ( final AttachedBinary attachedBinary : node.getAttachedBinaries() )
        {
            final RepositoryId repositoryId = ContextAccessor.current().getRepositoryId();
            paramsBuilder.attachBinary( attachedBinary.getBinaryReference(), this.binaryService.get( repositoryId, attachedBinary ) );
        }
    }

    private void updateChildReferences( final Node duplicatedParent, final NodeReferenceUpdatesHolder nodeReferenceUpdatesHolder )
    {
        final FindNodesByParentResult findNodesByParentResult = doFindNodesByParent( FindNodesByParentParams.create().
            parentPath( duplicatedParent.path() ).
            from( 0 ).
            size( NodeSearchService.GET_ALL_SIZE_FLAG ).
            build() );

        final Nodes children = GetNodesByIdsCommand.create( this ).
            ids( findNodesByParentResult.getNodeIds() ).
            build().
            execute();

        for ( final Node node : children )
        {
            updateNodeReferences( node, nodeReferenceUpdatesHolder );
            updateChildReferences( node, nodeReferenceUpdatesHolder );
        }
    }

    private void updateNodeReferences( final Node node, final NodeReferenceUpdatesHolder nodeReferenceUpdatesHolder )
    {
        final PropertyTree data = node.data();

        boolean changes = false;

        for ( final Property property : node.data().getProperties( ValueTypes.REFERENCE ) )
        {
            final Reference reference = property.getReference();
            if ( reference != null && nodeReferenceUpdatesHolder.mustUpdate( reference ) )
            {
                changes = true;
                data.setReference( property.getPath(), nodeReferenceUpdatesHolder.getNewReference( reference ) );
            }
        }

        if ( changes )
        {
            UpdateNodeCommand.create( this ).
                params( UpdateNodeParams.create().
                    id( node.id() ).
                    editor( toBeEdited -> toBeEdited.data = data ).
                    build() ).
                binaryService( this.binaryService ).
                build().
                execute();
        }

        nodeReferencesUpdated( 1 );
    }

    private void nodeDuplicated( final int count )
    {
        if ( params.getDuplicateListener() != null )
        {
            params.getDuplicateListener().nodesDuplicated( count );
        }
    }

    private void nodeReferencesUpdated( final int count )
    {
        if ( params.getDuplicateListener() != null )
        {
            params.getDuplicateListener().nodesReferencesUpdated( count );
        }
    }

    public static class Builder
        extends AbstractNodeCommand.Builder<Builder>
    {
        private BinaryService binaryService;

        private DuplicateNodeParams params;

        Builder()
        {
            super();
        }

        public Builder params( final DuplicateNodeParams params )
        {
            this.params = params;
            return this;
        }

        public Builder binaryService( final BinaryService binaryService )
        {
            this.binaryService = binaryService;
            return this;
        }

        public DuplicateNodeCommand build()
        {
            validate();
            return new DuplicateNodeCommand( this );
        }

        @Override
        void validate()
        {
            super.validate();
            Preconditions.checkNotNull( this.params.getNodeId() );
            Preconditions.checkNotNull( this.binaryService );
        }
    }

}
