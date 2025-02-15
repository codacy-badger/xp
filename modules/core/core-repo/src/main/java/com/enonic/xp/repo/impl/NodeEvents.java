package com.enonic.xp.repo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.event.Event;
import com.enonic.xp.node.MoveNodeResult;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeBranchEntries;
import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.Nodes;
import com.enonic.xp.node.PushNodeEntries;
import com.enonic.xp.node.PushNodeEntry;
import com.enonic.xp.repository.RepositoryId;

public class NodeEvents
{

    public static final String NODE_CREATED_EVENT = "node.created";

    public static final String NODE_DELETED_EVENT = "node.deleted";

    public static final String NODE_PUSHED_EVENT = "node.pushed";

    public static final String NODE_DUPLICATED_EVENT = "node.duplicated";

    public static final String NODE_UPDATED_EVENT = "node.updated";

    public static final String NODE_MOVED_EVENT = "node.moved";

    public static final String NODE_RENAMED_EVENT = "node.renamed";

    public static final String NODE_SORTED_EVENT = "node.sorted";

    public static final String NODE_MANUAL_ORDER_EVENT = "node.manualOrderUpdated";

    public static final String NODE_STATE_UPDATED_EVENT = "node.stateUpdated";

    public static final String NODE_PERMISSIONS_UPDATED = "node.permissionsUpdated";

    public static Event created( final Node createdNode )
    {
        return event( NODE_CREATED_EVENT, createdNode );
    }

    public static Event pushed( final PushNodeEntries pushNodeEntries )
    {
        if ( pushNodeEntries != null )
        {
            return event( NODE_PUSHED_EVENT, pushNodeEntries ).build();
        }
        return null;
    }

    public static Event stateUpdated( final Nodes updatedNodes )
    {
        if ( updatedNodes != null )
        {
            final Node firstNode = updatedNodes.first();

            if ( firstNode == null )
            {
                return null;
            }

            return event( NODE_STATE_UPDATED_EVENT, updatedNodes ).value( "state", firstNode.getNodeState().toString() ).build();
        }
        return null;
    }

    public static Event deleted( final NodeBranchEntries deletedNodes )
    {
        return deletedNodes != null ? event( NODE_DELETED_EVENT, deletedNodes ).build() : null;
    }

    public static Event duplicated( final Node duplicatedNode )
    {
        return event( NODE_DUPLICATED_EVENT, duplicatedNode );
    }

    public static Event updated( final Node updatedNode )
    {
        return event( NODE_UPDATED_EVENT, updatedNode );
    }

    public static Event permissionsUpdated( final Node updatedNode )
    {
        return event( NODE_PERMISSIONS_UPDATED, updatedNode );
    }

    public static Event moved( final MoveNodeResult result )
    {
        return Event.create( NODE_MOVED_EVENT )
            .distributed( true )
            .value( "nodes", result.getMovedNodes()
                .stream()
                .map( movedNode -> createMoved( movedNode.getPreviousPath(), movedNode.getNode() ) )
                .collect( Collectors.toList() ) )
            .build();
    }

    public static Event renamed( final NodePath previousPath, final Node targetNode )
    {
        return Event.create( NODE_RENAMED_EVENT )
            .distributed( true )
            .value( "nodes", ImmutableList.of( createMoved( previousPath, targetNode ) ) )
            .build();
    }

    public static Event sorted( final Node sortedNode )
    {
        return event( NODE_SORTED_EVENT, sortedNode );
    }

    public static Event manualOrderUpdated( final Node sortedNode )
    {
        return event( NODE_MANUAL_ORDER_EVENT, sortedNode );
    }

    private static Event event( String type, Node node )
    {
        if ( node != null )
        {
            return event( type, Nodes.from( node ) ).build();
        }
        return null;
    }

    private static Event.Builder event( String type, Nodes nodes )
    {
        return Event.create( type ).distributed( true ).value( "nodes", nodesToList( nodes ) );
    }

    private static Event.Builder event( String type, NodeBranchEntries nodes )
    {
        return Event.create( type ).distributed( true ).value( "nodes", nodesToList( nodes ) );
    }

    private static Event.Builder event( String type, PushNodeEntries nodes )
    {
        return Event.create( type ).distributed( true ).value( "nodes", nodesToList( nodes ) );
    }

    private static ImmutableList nodesToList( final Nodes nodes )
    {
        List<ImmutableMap> list = new ArrayList<>();
        nodes.stream().map( NodeEvents::nodeToMap ).forEach( list::add );

        return ImmutableList.copyOf( list );
    }

    private static ImmutableList nodesToList( final NodeBranchEntries nodes )
    {
        List<ImmutableMap> list = new ArrayList<>();
        nodes.stream().map( NodeEvents::nodeToMap ).forEach( list::add );

        return ImmutableList.copyOf( list );
    }

    private static ImmutableList nodesToList( final PushNodeEntries pushNodeEntries )
    {
        List<ImmutableMap> list = new ArrayList<>();
        pushNodeEntries.stream()
            .map( node -> NodeEvents.nodeToMap( node, pushNodeEntries.getTargetBranch(), pushNodeEntries.getTargetRepo() ) )
            .forEach( list::add );

        return ImmutableList.copyOf( list );
    }

    private static ImmutableMap nodeToMap( final NodeBranchEntry node )
    {
        return ImmutableMap.builder()
            .put( "id", node.getNodeId().toString() )
            .put( "path", node.getNodePath().toString() )
            .put( "branch", ContextAccessor.current().getBranch().getValue() )
            .put( "repo", ContextAccessor.current().getRepositoryId().toString() )
            .build();
    }

    private static ImmutableMap nodeToMap( final Node node )
    {
        return ImmutableMap.builder()
            .put( "id", node.id().toString() )
            .put( "path", node.path().toString() )
            .put( "branch", ContextAccessor.current().getBranch().getValue() )
            .put( "repo", ContextAccessor.current().getRepositoryId().toString() )
            .build();
    }

    private static ImmutableMap nodeToMap( final PushNodeEntry node, final Branch targetBranch, final RepositoryId targetRepository )
    {
        final ImmutableMap.Builder<Object, Object> nodeAsMap = ImmutableMap.builder()
            .put( "id", node.getNodeBranchEntry().getNodeId().toString() )
            .put( "path", node.getNodeBranchEntry().getNodePath().toString() )
            .put( "branch", targetBranch.getValue() )
            .put( "repo", targetRepository.toString() );
        if ( node.getCurrentTargetPath() != null )
        {
            nodeAsMap.put( "currentTargetPath", node.getCurrentTargetPath().toString() );
        }
        return nodeAsMap.build();
    }

    private static ImmutableMap<String, String> createMoved( final NodePath previousPath, final Node targetNode )
    {
        return ImmutableMap.<String, String>builder()
            .put( "id", targetNode.id().toString() )
            .put( "path", previousPath.toString() )
            .put( "branch", ContextAccessor.current().getBranch().getValue() )
            .put( "repo", ContextAccessor.current().getRepositoryId().toString() )
            .put( "newPath", targetNode.path().toString() )
            .build();
    }
}
