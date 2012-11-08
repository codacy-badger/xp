package com.enonic.wem.core.content.dao;


import javax.jcr.Node;

import org.junit.Test;

import com.enonic.wem.api.content.type.ContentType;
import com.enonic.wem.api.content.type.ContentTypes;
import com.enonic.wem.api.content.type.QualifiedContentTypeName;
import com.enonic.wem.api.content.type.QualifiedContentTypeNames;
import com.enonic.wem.api.module.Module;
import com.enonic.wem.itest.AbstractJcrTest;

import static org.junit.Assert.*;

public class ContentTypeDaoImplTest
    extends AbstractJcrTest
{
    private ContentTypeDao contentTypeDao;

    public void setupDao()
        throws Exception
    {
        contentTypeDao = new ContentTypeDaoImpl();
    }

    @Test
    public void createContentType()
        throws Exception
    {
        // setup
        final ContentType contentType = new ContentType();
        contentType.setModule( new Module( "myModule" ) );
        contentType.setName( "myContentType" );
        contentType.setAbstract( false );
        contentType.setDisplayName( "My content type" );

        // exercise
        contentTypeDao.createContentType( session, contentType );
        commit();

        // verify
        Node contentNode = session.getNode( "/" + ContentDaoConstants.CONTENT_TYPES_PATH + "myModule/myContentType" );
        assertNotNull( contentNode );
    }

    @Test
    public void retrieveContentType()
        throws Exception
    {
        // setup
        final ContentType contentType = new ContentType();
        contentType.setModule( new Module( "myModule" ) );
        contentType.setName( "myContentType" );
        contentType.setAbstract( true );
        contentType.setDisplayName( "My content type" );
        contentTypeDao.createContentType( session, contentType );

        // exercise
        final ContentTypes contentTypes = contentTypeDao.retrieveContentTypes( session, QualifiedContentTypeNames.from(
            "myModule:myContentType" ) );
        commit();

        // verify
        assertNotNull( contentTypes );
        assertEquals( 1, contentTypes.getSize() );
        final ContentType contentType1 = contentTypes.getFirst();
        assertEquals( "myContentType", contentType1.getName() );
        assertEquals( "myModule", contentType1.getModule().getName() );
        assertEquals( true, contentType1.isAbstract() );
        assertEquals( "My content type", contentType1.getDisplayName() );
    }

    @Test
    public void retrieveAllContentTypes()
        throws Exception
    {
        // setup
        final ContentType contentTypeCreated1 = new ContentType();
        contentTypeCreated1.setModule( new Module( "myModule" ) );
        contentTypeCreated1.setName( "myContentType" );
        contentTypeCreated1.setAbstract( true );
        contentTypeCreated1.setDisplayName( "My content type" );
        contentTypeDao.createContentType( session, contentTypeCreated1 );

        final ContentType contentTypeCreated2 = new ContentType();
        contentTypeCreated2.setModule( new Module( "otherModule" ) );
        contentTypeCreated2.setName( "someContentType" );
        contentTypeCreated2.setAbstract( false );
        contentTypeCreated2.setDisplayName( "Another content type" );
        contentTypeDao.createContentType( session, contentTypeCreated2 );

        // exercise
        final ContentTypes contentTypes = contentTypeDao.retrieveAllContentTypes( session );
        commit();

        // verify
        assertNotNull( contentTypes );
        assertEquals( 2, contentTypes.getSize() );
        final ContentType contentType1 = contentTypes.getContentType( new QualifiedContentTypeName( "myModule:myContentType" ) );
        final ContentType contentType2 = contentTypes.getContentType( new QualifiedContentTypeName( "otherModule:someContentType" ) );

        assertEquals( "myContentType", contentType1.getName() );
        assertEquals( "myModule", contentType1.getModule().getName() );
        assertEquals( true, contentType1.isAbstract() );
        assertEquals( "My content type", contentType1.getDisplayName() );

        assertEquals( "someContentType", contentType2.getName() );
        assertEquals( "otherModule", contentType2.getModule().getName() );
        assertEquals( false, contentType2.isAbstract() );
        assertEquals( "Another content type", contentType2.getDisplayName() );
    }

    @Test
    public void updateContentType()
        throws Exception
    {
        // setup
        final ContentType contentType = new ContentType();
        contentType.setModule( new Module( "myModule" ) );
        contentType.setName( "myContentType" );
        contentType.setAbstract( true );
        contentType.setDisplayName( "My content type" );
        contentTypeDao.createContentType( session, contentType );

        // exercise
        final ContentTypes contentTypesAfterCreate =
            contentTypeDao.retrieveContentTypes( session, QualifiedContentTypeNames.from( "myModule:myContentType" ) );
        assertNotNull( contentTypesAfterCreate );
        assertEquals( 1, contentTypesAfterCreate.getSize() );

        contentType.setAbstract( false );
        contentType.setDisplayName( "My content type-UPDATED" );
        contentTypeDao.updateContentType( session, contentType );
        commit();

        // verify
        final ContentTypes contentTypesAfterUpdate =
            contentTypeDao.retrieveContentTypes( session, QualifiedContentTypeNames.from( "myModule:myContentType" ) );
        assertNotNull( contentTypesAfterUpdate );
        assertEquals( 1, contentTypesAfterUpdate.getSize() );
        final ContentType contentType1 = contentTypesAfterUpdate.getFirst();
        assertEquals( "myContentType", contentType1.getName() );
        assertEquals( "myModule", contentType1.getModule().getName() );
        assertEquals( false, contentType1.isAbstract() );
        assertEquals( "My content type-UPDATED", contentType1.getDisplayName() );
    }

    @Test
    public void deleteContentType()
        throws Exception
    {
        // setup
        final ContentType contentType = new ContentType();
        contentType.setModule( new Module( "myModule" ) );
        contentType.setName( "myContentType" );
        contentType.setAbstract( true );
        contentType.setDisplayName( "My content type" );
        contentTypeDao.createContentType( session, contentType );

        // exercise
        final ContentTypes contentTypesAfterCreate =
            contentTypeDao.retrieveContentTypes( session, QualifiedContentTypeNames.from( "myModule:myContentType" ) );
        assertNotNull( contentTypesAfterCreate );
        assertEquals( 1, contentTypesAfterCreate.getSize() );

        int deleted = contentTypeDao.deleteContentType( session, QualifiedContentTypeNames.from( contentType.getQualifiedName() ) );
        commit();

        // verify
        assertEquals( 1, deleted );
        final ContentTypes contentTypesAfterDelete =
            contentTypeDao.retrieveContentTypes( session, QualifiedContentTypeNames.from( "myModule:myContentType" ) );
        assertNotNull( contentTypesAfterDelete );
        assertTrue( contentTypesAfterDelete.isEmpty() );
    }

}
