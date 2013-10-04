package com.enonic.wem.core.relationship;

import javax.inject.Inject;
import javax.jcr.Session;

import org.joda.time.DateTime;

import com.enonic.wem.api.account.AccountKey;
import com.enonic.wem.api.command.relationship.UpdateRelationship;
import com.enonic.wem.api.relationship.Relationship;
import com.enonic.wem.api.relationship.UpdateRelationshipFailureException;
import com.enonic.wem.core.command.CommandHandler;
import com.enonic.wem.core.relationship.dao.RelationshipDao;

import static com.enonic.wem.api.relationship.UpdateRelationshipFailureException.newUpdateRelationshipsResult;


public final class UpdateRelationshipHandler
    extends CommandHandler<UpdateRelationship>
{
    private RelationshipDao relationshipDao;

    @Override
    public void handle( final UpdateRelationship command )
        throws Exception
    {
        final Session session = context.getJcrSession();

        final Relationship existing = relationshipDao.select( command.getRelationshipKey(), session );

        try
        {
            Relationship changed = command.getEditor().edit( existing );
            existing.checkIllegalEdit( changed );

            changed = Relationship.newRelationship( changed ).
                modifier( AccountKey.anonymous() ).
                modifiedTime( DateTime.now() ).build();

            relationshipDao.update( changed, session );
            session.save();
        }
        catch ( Exception e )
        {
            final UpdateRelationshipFailureException.Builder result = newUpdateRelationshipsResult();
            result.relationshipKey( existing.getKey() );
            result.failure( e );
            throw result.build();
        }
    }

    @Inject
    public void setRelationshipDao( final RelationshipDao relationshipDao )
    {
        this.relationshipDao = relationshipDao;
    }
}
