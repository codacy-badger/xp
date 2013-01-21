package com.enonic.wem.core.content.relation;

import javax.jcr.Session;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enonic.wem.api.command.content.relation.CreateRelationshipType;
import com.enonic.wem.api.content.relation.RelationshipType;
import com.enonic.wem.core.command.CommandContext;
import com.enonic.wem.core.command.CommandHandler;
import com.enonic.wem.core.content.relation.dao.RelationshipTypeDao;

@Component
public final class CreateRelationshipTypeHandler
    extends CommandHandler<CreateRelationshipType>
{
    private RelationshipTypeDao relationshipTypeDao;

    public CreateRelationshipTypeHandler()
    {
        super( CreateRelationshipType.class );
    }

    @Override
    public void handle( final CommandContext context, final CreateRelationshipType command )
        throws Exception
    {
        final RelationshipType.Builder builder = RelationshipType.newRelationshipType();
        builder.name( command.getName() );
        builder.module( command.getModule() );
        builder.fromSemantic( command.getFromSemantic() );
        builder.toSemantic( command.getToSemantic() );
        builder.addAllowedFromType( command.getAllowedFromTypes() );
        builder.addAllowedToType( command.getAllowedToTypes() );
        builder.createdTime( DateTime.now() );
        builder.modifiedTime( DateTime.now() );
        final RelationshipType relationshipType = builder.build();

        final Session session = context.getJcrSession();
        relationshipTypeDao.createRelationshipType( relationshipType, session );
        session.save();
        command.setResult( relationshipType.getQualifiedName() );
    }

    @Autowired
    public void setRelationshipTypeDao( final RelationshipTypeDao relationshipTypeDao )
    {
        this.relationshipTypeDao = relationshipTypeDao;
    }
}
