package com.enonic.wem.core.content.dao;


import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.stereotype.Component;

import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentIds;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.ContentPaths;
import com.enonic.wem.api.content.ContentSelector;
import com.enonic.wem.api.content.ContentSelectors;
import com.enonic.wem.api.content.Contents;
import com.enonic.wem.api.content.type.QualifiedContentTypeName;
import com.enonic.wem.api.content.versioning.ContentVersion;
import com.enonic.wem.api.content.versioning.ContentVersionId;
import com.enonic.wem.api.support.tree.Tree;


/**
 * TODO: Figure out how to handle the thrown RepositoryException from JCR.
 */
@Component
public class ContentDaoImpl
    implements ContentDao
{

    @Override
    public ContentId createContent( final Content content, final Session session )
    {
        try
        {
            return new CreateContentDaoHandler( session ).handle( content );
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void updateContent( final Content content, final boolean createNewVersion, final Session session )
    {
        try
        {
            new UpdateContentDaoHandler( session ).handle( content, createNewVersion );
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void deleteContent( final ContentSelector contentSelector, final Session session )
    {
        try
        {
            if ( contentSelector instanceof ContentPath )
            {
                final ContentPath contentPath = (ContentPath) contentSelector;
                new DeleteContentDaoHandler( session ).deleteContentByPath( contentPath );
            }
            else if ( contentSelector instanceof ContentId )
            {
                final ContentId contentId = (ContentId) contentSelector;
                new DeleteContentDaoHandler( session ).deleteContentById( contentId );
            }
            else
            {
                throw new IllegalArgumentException( "Unsupported content selector: " + contentSelector.getClass().getCanonicalName() );
            }
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void renameContent( final ContentPath contentPath, final String newName, final Session session )
    {
        try
        {
            new RenameContentDaoHandler( session ).handle( contentPath, newName );
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Content findContent( final ContentSelector contentSelector, final Session session )
    {
        try
        {
            if ( contentSelector instanceof ContentPath )
            {
                final ContentPath contentPath = (ContentPath) contentSelector;
                return new FindContentDaoHandler( session ).findContentByPath( contentPath );
            }
            else if ( contentSelector instanceof ContentId )
            {
                final ContentId contentId = (ContentId) contentSelector;
                return new FindContentDaoHandler( session ).findContentById( contentId );
            }
            else
            {
                throw new IllegalArgumentException( "Unsupported content selector: " + contentSelector.getClass().getCanonicalName() );
            }
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Contents findContents( final ContentSelectors contentSelectors, final Session session )
    {
        try
        {
            if ( contentSelectors instanceof ContentPaths )
            {
                final ContentPaths contentPaths = (ContentPaths) contentSelectors;
                return new FindContentDaoHandler( session ).findContentsByPath( contentPaths );
            }
            else if ( contentSelectors instanceof ContentIds )
            {
                final ContentIds contentIds = (ContentIds) contentSelectors;
                return new FindContentDaoHandler( session ).findContentsById( contentIds );
            }
            else
            {
                throw new IllegalArgumentException( "Unsupported content selector: " + contentSelectors.getClass().getCanonicalName() );
            }
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Contents findChildContent( final ContentPath parentPath, final Session session )
    {
        try
        {
            return new FindChildContentDaoHandler( session ).handle( parentPath );
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Tree<Content> getContentTree( final Session session )
    {
        try
        {
            return new GetContentTreeContentDaoHandler( session ).handle();
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public int countContentTypeUsage( final QualifiedContentTypeName qualifiedContentTypeName, Session session )
    {
        try
        {
            return new CountContentTypeUsageDaoHandler( session ).handle( qualifiedContentTypeName );
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public List<ContentVersion> getContentVersions( final ContentSelector contentSelector, final Session session )
    {
        try
        {
            if ( contentSelector instanceof ContentPath )
            {
                final ContentPath contentPath = (ContentPath) contentSelector;
                return new GetContentVersionHistoryDaoHandler( session ).handle( contentPath );
            }
            else if ( contentSelector instanceof ContentId )
            {
                final ContentId contentId = (ContentId) contentSelector;
                return new GetContentVersionHistoryDaoHandler( session ).handle( contentId );
            }
            else
            {
                throw new IllegalArgumentException( "Unsupported content selector: " + contentSelector.getClass().getCanonicalName() );
            }
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Content getContentVersion( final ContentSelector contentSelector, final ContentVersionId versionId, final Session session )
    {
        try
        {
            if ( contentSelector instanceof ContentPath )
            {
                final ContentPath contentPath = (ContentPath) contentSelector;
                return new GetContentVersionDaoHandler( session ).handle( contentPath, versionId );
            }
            else if ( contentSelector instanceof ContentId )
            {
                final ContentId contentId = (ContentId) contentSelector;
                return new GetContentVersionDaoHandler( session ).handle( contentId, versionId );
            }
            else
            {
                throw new IllegalArgumentException( "Unsupported content selector: " + contentSelector.getClass().getCanonicalName() );
            }
        }
        catch ( RepositoryException e )
        {
            throw new RuntimeException( e );
        }
    }
}
