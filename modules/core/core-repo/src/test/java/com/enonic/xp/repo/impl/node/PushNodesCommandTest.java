package com.enonic.xp.repo.impl.node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.enonic.xp.node.CreateNodeParams;
import com.enonic.xp.node.FindNodesByQueryResult;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodeName;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeQuery;
import com.enonic.xp.node.NodeState;
import com.enonic.xp.node.PushNodesResult;
import com.enonic.xp.node.RenameNodeParams;
import com.enonic.xp.node.SetNodeStateParams;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.security.acl.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PushNodesCommandTest
    extends AbstractNodeTest
{
    @BeforeEach
    public void setUp()
        throws Exception
    {
        this.createDefaultRootNode();
        ctxOther().callWith( this::createDefaultRootNode );
    }

    @Test
    public void push_single()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        Node testWsNode = ctxOther().callWith( () -> getNodeById( node.id() ) );

        assertTrue( testWsNode == null );

        pushNodes( NodeIds.from( node.id() ), WS_OTHER );

        testWsNode = ctxOther().callWith( () -> getNodeById( node.id() ) );

        assertTrue( testWsNode != null );
    }

    @Test
    public void push_child()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        final Node child = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child" ).
            build() );

        pushNodes( NodeIds.from( node.id() ), WS_OTHER );
        pushNodes( NodeIds.from( child.id() ), WS_OTHER );

        final Node prodNode = ctxOther().callWith( () -> getNodeById( child.id() ) );

        assertTrue( prodNode != null );
    }

    @Test
    public void only_selected_node_pushed()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child" ).
            build() );

        createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child2" ).
            build() );

        final PushNodesResult result = pushNodes( NodeIds.from( node.id() ), WS_OTHER );
        assertEquals( 1, result.getSuccessful().getSize() );

        final FindNodesByQueryResult allNodesInOther = ctxOther().callWith( () -> FindNodesByQueryCommand.create().
            query( NodeQuery.create().build() ).
            indexServiceInternal( this.indexServiceInternal ).
            searchService( this.searchService ).
            storageService( this.storageService ).
            build().
            execute() );

        assertEquals( 2L, allNodesInOther.getTotalHits() );
    }

    @Test
    public void push_child_missing_permission()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        final Node child = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child" ).
            permissions( AccessControlList.create().
                add( AccessControlEntry.create().
                    allowAll().
                    deny( Permission.PUBLISH ).
                    principal( TEST_DEFAULT_USER.getKey() ).
                    build() ).build() ).
            build() );

        final PushNodesResult result = pushNodes( NodeIds.from( node.id(), child.id() ), WS_OTHER );

        assertEquals( 1, result.getSuccessful().getSize() );
        assertEquals( 1, result.getFailed().size() );
        assertEquals( PushNodesResult.Reason.ACCESS_DENIED, result.getFailed().iterator().next().getReason() );
    }

    @Test
    public void push_fail_if_node_already_exists()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        ctxOther().callWith( () -> createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() ) );

        final PushNodesResult result = pushNodes( NodeIds.from( node.id() ), WS_OTHER );

        assertEquals( 1, result.getFailed().size() );
        assertEquals( 0, result.getSuccessful().getSize() );
        assertEquals( PushNodesResult.Reason.ALREADY_EXIST, result.getFailed().iterator().next().getReason() );
    }


    @Test
    public void push_rename_push_test()
        throws Exception
    {
        PushNodesResult result;

        //Creates and pushes a content
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );
        result = pushNodes( NodeIds.from( node.id() ), WS_OTHER );
        assertEquals( 1, result.getSuccessful().getSize() );
        assertNotNull( getNodeByPath( NodePath.create( "/my-node" ).build() ) );
        assertNotNull( getNodeByPathInOther( NodePath.create( "/my-node" ).build() ) );

        //Renames the content
        renameNode( node, "my-node-renamed" );
        assertNull( getNodeByPath( NodePath.create( "/my-node" ).build() ) );
        assertNotNull( getNodeByPath( NodePath.create( "/my-node-renamed" ).build() ) );
        assertNotNull( getNodeByPathInOther( NodePath.create( "/my-node" ).build() ) );

        //Pushed the renames content
        result = pushNodes( NodeIds.from( node.id() ), WS_OTHER );
        assertEquals( 1, result.getSuccessful().getSize() );
        assertNull( getNodeByPathInOther( NodePath.create( "/my-node" ).build() ) );
        assertNotNull( getNodeByPathInOther( NodePath.create( "/my-node-renamed" ).build() ) );
    }

    @Test
    public void push_child_fail_if_parent_does_not_exists()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        final Node child = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child" ).
            build() );

        final PushNodesResult result = pushNodes( NodeIds.from( child.id() ), WS_OTHER );

        assertEquals( 1, result.getFailed().size() );
        assertEquals( PushNodesResult.Reason.PARENT_NOT_FOUND, result.getFailed().iterator().next().getReason() );
    }

    @Test
    public void ensure_order_for_publish_with_children()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        final Node child1 = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child" ).
            build() );

        final Node child2 = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child2" ).
            build() );

        final Node child1_1 = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child1_1" ).
            build() );

        final Node child2_1 = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child2_1" ).
            build() );

        final PushNodesResult result =
            pushNodes( NodeIds.from( child2_1.id(), child1_1.id(), child1.id(), child2.id(), node.id() ), WS_OTHER );

        assertTrue( result.getFailed().isEmpty() );

        ctxOther().runWith( () -> {
            assertNotNull( getNodeById( node.id() ) );
            assertNotNull( getNodeById( child1.id() ) );
            assertNotNull( getNodeById( child1_1.id() ) );
            assertNotNull( getNodeById( child2.id() ) );
            assertNotNull( getNodeById( child2_1.id() ) );
        } );
    }

    @Test
    public void moved_nodes_yields_reindex_of_children()
        throws Exception
    {
        final Node node1 =
            createNode( CreateNodeParams.create().parent( NodePath.ROOT ).name( "node1" ).setNodeId( NodeId.from( "node1" ) ).build() );

        final Node node2 =
            createNode( CreateNodeParams.create().parent( NodePath.ROOT ).name( "node2" ).setNodeId( NodeId.from( "node2" ) ).build() );

        final Node child1 =
            createNode( CreateNodeParams.create().parent( node1.path() ).name( "child1" ).setNodeId( NodeId.from( "child1" ) ).build() );

        final Node child2 =
            createNode( CreateNodeParams.create().parent( node1.path() ).name( "child2" ).setNodeId( NodeId.from( "child2" ) ).build() );

        final Node child1_1 = createNode(
            CreateNodeParams.create().parent( child1.path() ).name( "child1_1" ).setNodeId( NodeId.from( "child1_1" ) ).build() );

        final Node child1_1_1 = createNode(
            CreateNodeParams.create().parent( child1_1.path() ).name( "child1_1_1" ).setNodeId( NodeId.from( "child1_1_1" ) ).build() );

        final Node child2_1 = createNode(
            CreateNodeParams.create().parent( child2.path() ).name( "child2_1" ).setNodeId( NodeId.from( "child2_1" ) ).build() );

        final PushNodesResult result =
            pushNodes( NodeIds.from( node1.id(), node2.id(), child1.id(), child1_1.id(), child1_1_1.id(), child2.id(), child2_1.id() ),
                       WS_OTHER );

        assertNotNull( getNodeByPathInOther( NodePath.create( node1.path(), child1.name().toString() ).build() ) );

        final Node movedNode = MoveNodeCommand.create()
            .id( node1.id() )
            .newParent( node2.path() )
            .indexServiceInternal( this.indexServiceInternal )
            .storageService( this.storageService )
            .searchService( this.searchService )
            .build()
            .execute()
            .getMovedNodes()
            .get( 0 )
            .getNode();

        pushNodes( NodeIds.from( node1.id() ), WS_OTHER );

        assertNotNull( getNodeByPathInOther( NodePath.create( movedNode.path(), child1.name().toString() ).build() ) );

        assertNull( getNodeByPathInOther( NodePath.create( node1.path(), child1.name().toString() ).build() ) );

        Node child1Node = ctxOther().callWith( () -> getNodeById( child1.id() ) );
        assertNotNull( getNodeByPathInOther( NodePath.create( child1Node.path(), child1_1.name().toString() ).build() ) );
    }

    @Test
    public void push_rename_push()
        throws Exception
    {
        final Node parent = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "parent" ).
            setNodeId( NodeId.from( "parent" ) ).
            build() );

        final Node child1 = createNode( CreateNodeParams.create().
            parent( parent.path() ).
            name( "child1" ).
            setNodeId( NodeId.from( "child1" ) ).
            build() );

        final Node child1_1 = createNode( CreateNodeParams.create().
            parent( child1.path() ).
            name( "child1_1" ).
            setNodeId( NodeId.from( "child1_1" ) ).
            build() );

        pushNodes( NodeIds.from( parent.id(), child1.id() ), WS_OTHER );

        renameNode( parent, "parent-renamed" );
        renameNode( child1, "child1-renamed" );
        renameNode( child1_1, "child1_1-renamed" );

        final PushNodesResult result = pushNodes( NodeIds.from( parent.id(), child1.id() ), WS_OTHER );

        assertEquals( 2, result.getSuccessful().getSize() );
    }


    @Test
    public void rename_to_name_already_there_but_renamed_in_same_push()
        throws Exception
    {
        final Node a = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "a" ).
            setNodeId( NodeId.from( "a" ) ).
            build() );

        final Node b = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "b" ).
            setNodeId( NodeId.from( "b" ) ).
            build() );

        pushNodes( NodeIds.from( a.id() ), WS_OTHER );

        renameNode( a.id(), "a_old" );
        renameNode( b.id(), "a" );

        final PushNodesResult result = pushNodes( NodeIds.from( b.id(), a.id() ), WS_OTHER );

        assertEquals( 0, result.getFailed().size() );
        assertEquals( 2, result.getSuccessful().getSize() );
    }

    @Test
    public void push_after_rename()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "node1" ).
            setNodeId( NodeId.from( "node1" ) ).
            build() );

        final Node child1 = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "child1" ).
            setNodeId( NodeId.from( "child1" ) ).
            build() );

        final Node child1_1 = createNode( CreateNodeParams.create().
            parent( child1.path() ).
            name( "child1_1" ).
            setNodeId( NodeId.from( "child1_1" ) ).
            build() );

        final Node child1_1_1 = createNode( CreateNodeParams.create().
            parent( child1_1.path() ).
            name( "child1_1_1" ).
            setNodeId( NodeId.from( "child1_1_1" ) ).
            build() );

        final Node node2 = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "node2" ).
            setNodeId( NodeId.from( "node2" ) ).
            build() );

        final Node child2 = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "child2" ).
            setNodeId( NodeId.from( "child2" ) ).
            build() );

        final Node child2_1 = createNode( CreateNodeParams.create().
            parent( child2.path() ).
            name( "child2_1" ).
            setNodeId( NodeId.from( "child2_1" ) ).
            build() );

        pushNodes( NodeIds.from( node.id(), node2.id(), child1.id(), child1_1.id(), child1_1_1.id(), child2.id(), child2_1.id() ),
                   WS_OTHER );

        renameNode( node, "node1-renamed" );
        renameNode( child1, "child1-renamed" );
        renameNode( child1_1, "child1_1-renamed" );
        renameNode( child1_1_1, "child1_1_1-renamed" );
        renameNode( node2, "node2-renamed" );
        renameNode( child2, "child2-renamed" );
        renameNode( child2_1, "child2_1-renamed" );

        final PushNodesResult result =
            pushNodes( NodeIds.from( child1_1_1.id(), child1_1.id(), node.id(), child2_1.id(), node2.id(), child1.id(), child2.id() ),
                       WS_OTHER );

        assertEquals( 7, result.getSuccessful().getSize() );
    }

    @Test
    public void push_after_update()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "node1" ).
            setNodeId( NodeId.from( "node1" ) ).
            build() );

        updateNode( UpdateNodeParams.create().
            id( node.id() ).
            editor( toUpdate -> toUpdate.data.addString( "newString", "newValue" ) ).
            build() );

        pushNodes( NodeIds.from( node.id() ), WS_OTHER );

        final Node pushedNode = ctxOther().callWith( () -> nodeService.getById( node.id() ) );

        assertEquals( "newValue", pushedNode.data().getString( "newString" ) );
    }

    @Test
    public void push_with_capital_node_id()
        throws Exception
    {
        final Node node = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            setNodeId( NodeId.from( "MyNodeId" ) ).
            build() );

        final Node child = createNode( CreateNodeParams.create().
            parent( node.path() ).
            name( "my-child" ).
            setNodeId( NodeId.from( "MyChildId" ) ).
            build() );

        pushNodes( NodeIds.from( node.id() ), WS_OTHER );
        pushNodes( NodeIds.from( child.id() ), WS_OTHER );

        final Node prodNode = ctxOther().callWith( () -> getNodeById( child.id() ) );

        assertTrue( prodNode != null );
    }

    private void renameNode( final Node node, final String newName )
    {
        doRenameNode( node.id(), newName );
    }

    private void renameNode( final NodeId nodeId, final String newName )
    {
        doRenameNode( nodeId, newName );
    }

    private void doRenameNode( final NodeId nodeId, final String newName )
    {
        RenameNodeCommand.create().
            params( RenameNodeParams.create().
                nodeId( nodeId ).
                nodeName( NodeName.from( newName ) ).
                build() ).
            indexServiceInternal( this.indexServiceInternal ).
            searchService( this.searchService ).
            storageService( this.storageService ).
            build().
            execute();
    }

    protected void setPendingDelete( final NodeId nodeId )
    {
        SetNodeStateCommand.create().
            params( SetNodeStateParams.create().
                nodeId( nodeId ).
                nodeState( NodeState.PENDING_DELETE ).
                recursive( true ).
                build() ).
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    private Node getNodeByPathInOther( final NodePath nodePath )
    {
        return ctxOther().callWith( () -> getNodeByPath( nodePath ) );
    }


}
