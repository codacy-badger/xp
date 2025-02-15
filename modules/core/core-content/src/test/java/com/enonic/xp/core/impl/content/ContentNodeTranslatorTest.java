package com.enonic.xp.core.impl.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentNotFoundException;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentPropertyNames;
import com.enonic.xp.content.Contents;
import com.enonic.xp.core.impl.content.serializer.ContentDataSerializer;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.node.Nodes;
import com.enonic.xp.node.NodesHasChildrenResult;
import com.enonic.xp.page.PageDescriptorService;
import com.enonic.xp.region.LayoutDescriptorService;
import com.enonic.xp.region.PartDescriptorService;
import com.enonic.xp.schema.content.ContentTypeName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentNodeTranslatorTest
{
    public static final NodeId ID_1 = NodeId.from( "id1" );

    public static final NodeId ID_2 = NodeId.from( "id2" );

    public static final NodeId ID_3 = NodeId.from( "id3" );

    private ContentNodeTranslator contentNodeTranslator;

    private NodeService nodeService;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        this.nodeService = Mockito.mock( NodeService.class );
        final PartDescriptorService partDescriptorService = Mockito.mock( PartDescriptorService.class );
        final LayoutDescriptorService layoutDescriptorService = Mockito.mock( LayoutDescriptorService.class );
        final PageDescriptorService pageDescriptorService = Mockito.mock( PageDescriptorService.class );

        final ContentDataSerializer contentDataSerializer = ContentDataSerializer.create()
            .layoutDescriptorService( layoutDescriptorService )
            .pageDescriptorService( pageDescriptorService )
            .partDescriptorService( partDescriptorService )
            .build();

        this.contentNodeTranslator = new ContentNodeTranslator( nodeService, contentDataSerializer );
    }

    @Test
    public void testFromNodesResolvingChildren()
        throws Exception
    {
        final Nodes nodes = createNodes();
        final NodesHasChildrenResult hasChildrenResult =
            NodesHasChildrenResult.create().add( ID_1, true ).add( ID_2, true ).add( ID_3, false ).build();
        Mockito.when( this.nodeService.hasChildren( Mockito.any( Nodes.class ) ) ).thenReturn( hasChildrenResult );

        final Contents contents = this.contentNodeTranslator.fromNodes( nodes, true );

        assertEquals( 3, contents.getSize() );
        final Content content1 = contents.getContentById( ContentId.from( ID_1.toString() ) );
        final Content content3 = contents.getContentById( ContentId.from( ID_3.toString() ) );
        final Content content2 = contents.getContentById( ContentId.from( ID_2.toString() ) );
        assertTrue( content1.hasChildren() );
        assertTrue( content2.hasChildren() );
        assertFalse( content3.hasChildren() );
    }

    @Test
    public void testFromNodesNotResolvingChildren()
        throws Exception
    {
        final Nodes nodes = createNodes();

        final Contents contents = this.contentNodeTranslator.fromNodes( nodes, false );

        assertEquals( 3, contents.getSize() );
        final Content content1 = contents.getContentById( ContentId.from( ID_1.toString() ) );
        final Content content3 = contents.getContentById( ContentId.from( ID_3.toString() ) );
        final Content content2 = contents.getContentById( ContentId.from( ID_2.toString() ) );
        assertFalse( content1.hasChildren() );
        assertFalse( content2.hasChildren() );
        assertFalse( content3.hasChildren() );
    }

    @Test
    public void testFromNodeResolvingChildren()
        throws Exception
    {
        final Node node = createNode();
        Mockito.when( this.nodeService.hasChildren( Mockito.any( Node.class ) ) ).thenReturn( true );

        final Content content = this.contentNodeTranslator.fromNode( node, true );

        assertNotNull( content );
        assertTrue( content.hasChildren() );
    }

    @Test
    public void testFromNodeNotResolvingChildren()
        throws Exception
    {
        final Node node = createNode();

        final Content content = this.contentNodeTranslator.fromNode( node, false );

        assertNotNull( content );
        assertFalse( content.hasChildren() );
    }


    @Test
    public void testNodeOutsideOfContentRoot()
        throws Exception
    {
        assertThrows( ContentNotFoundException.class, () -> this.contentNodeTranslator.fromNode( createNode( NodePath.ROOT ), false ) );
        assertThrows( ContentNotFoundException.class,
                      () -> this.contentNodeTranslator.fromNode( createNode( NodePath.create( "/non-content" ).build() ), false ) );
    }

    @Test
    public void testNodeOutsideOfContentRootAllowed()
        throws Exception
    {
        final Content content = this.contentNodeTranslator.fromNode( createNode( NodePath.ROOT ), false, true );
        assertTrue( ContentPath.ROOT.equals( content.getPath() ) );
    }

    private Node createNode()
    {
        return createNode( ContentConstants.CONTENT_ROOT_PATH );
    }

    private Node createNode( final NodePath parentPath )
    {
        final PropertyTree rootDataSet = new PropertyTree();
        rootDataSet.setString( ContentPropertyNames.TYPE, ContentTypeName.unstructured().toString() );
        rootDataSet.setSet( ContentPropertyNames.DATA, new PropertySet() );
        rootDataSet.setString( ContentPropertyNames.CREATOR, "user:myidprovider:user1" );

        return Node.create().id( ID_1 ).name( "contentRoot" ).parentPath( parentPath ).data( rootDataSet ).build();
    }

    private Nodes createNodes()
    {

        final PropertyTree rootDataSet = new PropertyTree();
        rootDataSet.setString( ContentPropertyNames.TYPE, ContentTypeName.unstructured().toString() );
        rootDataSet.setSet( ContentPropertyNames.DATA, new PropertySet() );
        rootDataSet.setString( ContentPropertyNames.CREATOR, "user:myidprovider:user1" );

        final Node node1 =
            Node.create().id( ID_1 ).name( "contentRoot" ).parentPath( ContentConstants.CONTENT_ROOT_PATH ).data( rootDataSet ).build();
        final Node node2 = Node.create().id( ID_2 ).name( "contentParent" ).parentPath( node1.path() ).data( rootDataSet ).build();
        final Node node3 = Node.create().id( ID_3 ).name( "contentChild" ).parentPath( node2.path() ).data( rootDataSet ).build();

        return Nodes.from( node1, node2, node3 );
    }

}
