package com.enonic.xp.core.impl.content;

import java.util.stream.Collectors;

import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentIds;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentPaths;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodePaths;

class ContentNodeHelper
{
    private static final String CONTENT_ROOT_NODE_NAME = "content";

    static final NodePath CONTENT_ROOT_NODE = NodePath.create( NodePath.ROOT, CONTENT_ROOT_NODE_NAME ).build();

    public static NodePath translateContentPathToNodePath( final ContentPath contentPath )
    {
        return new NodePath( (contentPath.getAltRoot() != null
                                 ? contentPath.getAltRoot()
                                 : CONTENT_ROOT_NODE_NAME) + contentPath.asAbsolute().toString() ).asAbsolute().trimTrailingDivider();
    }

    public static NodePaths translateContentPathsToNodePaths( final ContentPaths contentPaths )
    {
        final NodePaths.Builder builder = NodePaths.create();

        builder.addNodePaths(
            contentPaths.stream().map( ContentNodeHelper::translateContentPathToNodePath ).collect( Collectors.toList() ) );

        return builder.build();
    }

    public static ContentPaths translateNodePathsToContentPaths( final NodePaths nodePaths )
    {
        return ContentPaths.from(
            nodePaths.stream().map( ContentNodeHelper::translateNodePathToContentPath ).collect( Collectors.toList() ) );
    }

    public static ContentPath translateNodePathToContentPath( final NodePath nodePath )
    {
        if ( nodePath.elementCount() == 0 )
        {
            throw new IllegalArgumentException( "Node path is not a content path: " + nodePath );
        }

        final String rootNodeName = nodePath.asAbsolute().getElementAsString( 0 );
        final String contentPathString = nodePath.asAbsolute().toString().substring( ( rootNodeName + "/" ).length() );

        if ( CONTENT_ROOT_NODE_NAME.equals( rootNodeName ) )
        {
            return ContentPath.from( contentPathString ).asAbsolute();
        }
        else if ( ContentConstants.ARCHIVE_ROOT_NAME.equals( rootNodeName ) )
        {
            return ContentPath.from( contentPathString, rootNodeName ).asAbsolute();
        }
        throw new IllegalArgumentException( "Node path is not a content path: " + nodePath );

    }

    public static NodePath translateContentParentToNodeParentPath( final ContentPath parentContentPath )
    {
        return NodePath.create(
            parentContentPath.getAltRoot() != null ? NodePath.create( ContentConstants.CONTENT_ROOT_PARENT, parentContentPath.getAltRoot() )
                .build() : ContentConstants.CONTENT_ROOT_PATH ).elements( parentContentPath.toString() ).build();
    }

    public static NodeIds toNodeIds( final ContentIds contentIds )
    {
        return NodeIds.from( contentIds.stream().map( contentId -> NodeId.from( contentId.toString() ) ).collect( Collectors.toList() ) );
    }

    public static ContentIds toContentIds( final NodeIds nodeIds )
    {
        return ContentIds.from( nodeIds.stream().map( nodeId -> ContentId.from( nodeId.toString() ) ).collect( Collectors.toList() ) );
    }

}


