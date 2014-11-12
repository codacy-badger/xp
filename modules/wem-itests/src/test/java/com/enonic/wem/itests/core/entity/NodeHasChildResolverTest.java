package com.enonic.wem.itests.core.entity;

import org.junit.Before;
import org.junit.Test;

import com.enonic.wem.core.entity.CreateNodeParams;
import com.enonic.wem.core.entity.Node;
import com.enonic.wem.core.entity.NodeHasChildResolver;
import com.enonic.wem.core.entity.NodePath;
import com.enonic.wem.core.entity.Nodes;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class NodeHasChildResolverTest
    extends AbstractNodeTest
{
    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Test
    public void has_children()
        throws Exception
    {

        final Node parentNode = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        createNode( CreateNodeParams.create().
            parent( parentNode.path() ).
            name( "my-child-node" ).
            build() );

        final Node resolvedNode = NodeHasChildResolver.create().
            queryService( this.queryService ).
            build().
            resolve( parentNode );

        assertTrue( resolvedNode.getHasChildren() );
    }

    @Test
    public void nodes_has_children()
        throws Exception
    {
        final Node parentNode1 = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node-1" ).
            build() );

        final Node parentNode2 = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node-2" ).
            build() );

        final Node parentNode3 = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node-3" ).
            build() );

        createNode( CreateNodeParams.create().
            parent( parentNode1.path() ).
            name( "my-child-node-1" ).
            build() );

        createNode( CreateNodeParams.create().
            parent( parentNode2.path() ).
            name( "my-child-node-2" ).
            build() );

        final Nodes resolvedNodes = NodeHasChildResolver.create().
            queryService( this.queryService ).
            build().
            resolve( Nodes.from( parentNode1, parentNode2, parentNode3 ) );

        assertTrue( resolvedNodes.getNodeById( parentNode1.id() ).getHasChildren() );
        assertTrue( resolvedNodes.getNodeById( parentNode2.id() ).getHasChildren() );
        assertFalse( resolvedNodes.getNodeById( parentNode3.id() ).getHasChildren() );
    }

    @Test
    public void no_children()
        throws Exception
    {

        final Node parentNode = createNode( CreateNodeParams.create().
            parent( NodePath.ROOT ).
            name( "my-node" ).
            build() );

        final Node resolvedNode = NodeHasChildResolver.create().
            queryService( this.queryService ).
            build().
            resolve( parentNode );

        assertFalse( resolvedNode.getHasChildren() );
    }


}
