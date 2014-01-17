package com.enonic.wem.core.schema.relationship;

import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.wem.api.command.Commands;
import com.enonic.wem.api.command.schema.relationship.GetRelationshipTypes;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.schema.relationship.RelationshipType;
import com.enonic.wem.api.schema.relationship.RelationshipTypeName;
import com.enonic.wem.api.schema.relationship.RelationshipTypeNames;
import com.enonic.wem.api.schema.relationship.RelationshipTypes;
import com.enonic.wem.core.command.AbstractCommandHandlerTest;
import com.enonic.wem.core.schema.relationship.dao.RelationshipTypeDao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

public class GetRelationshipTypesHandlerTest
    extends AbstractCommandHandlerTest
{
    private GetRelationshipTypesHandler handler;

    private RelationshipTypeDao relationshipTypeDao;

    @Before
    public void setUp()
        throws Exception
    {
        super.initialize();

        relationshipTypeDao = Mockito.mock( RelationshipTypeDao.class );
        handler = new GetRelationshipTypesHandler();
        handler.setContext( this.context );
        handler.setRelationshipTypeDao( relationshipTypeDao );
    }

    @Test
    public void getRelationshipTypes()
        throws Exception
    {
        // setup
        final RelationshipTypeName name1 = RelationshipTypeName.from( "like" );
        final RelationshipType relationshipType = RelationshipType.newRelationshipType().
            name( name1 ).
            fromSemantic( "likes" ).
            toSemantic( "liked by" ).
            addAllowedFromType( ContentTypeName.from( "person" ) ).
            addAllowedToType( ContentTypeName.from( "person" ) ).
            build();

        final RelationshipTypeName name2 = RelationshipTypeName.from( "hate" );
        final RelationshipType relationshipType2 = RelationshipType.newRelationshipType().
            name( name2 ).
            fromSemantic( "hates" ).
            toSemantic( "hated by" ).
            addAllowedFromType( ContentTypeName.from( "person" ) ).
            addAllowedToType( ContentTypeName.from( "person" ) ).
            build();

        final RelationshipTypeNames selectors = RelationshipTypeNames.from( name1, name2 );
        final RelationshipTypes relationshipTypes = RelationshipTypes.from( relationshipType, relationshipType2 );

        // expectation
        Mockito.when( relationshipTypeDao.select( Mockito.eq( selectors ), Mockito.any( Session.class ) ) ).thenReturn( relationshipTypes );

        // exercise
        final GetRelationshipTypes command = Commands.relationshipType().get().byNames( selectors );

        this.handler.setCommand( command );
        this.handler.handle();

        // verify
        verify( relationshipTypeDao, only() ).select( Mockito.eq( selectors ), Mockito.any( Session.class ) );
        assertEquals( 2, command.getResult().getSize() );
    }
}
