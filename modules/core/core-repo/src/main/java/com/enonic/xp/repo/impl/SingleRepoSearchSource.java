package com.enonic.xp.repo.impl;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.PrincipalKeys;

public class SingleRepoSearchSource
    implements SearchSource
{
    private final RepositoryId repositoryId;

    private final Branch branch;

    private final PrincipalKeys acl;

    public RepositoryId getRepositoryId()
    {
        return repositoryId;
    }

    public Branch getBranch()
    {
        return branch;
    }

    public PrincipalKeys getAcl()
    {
        return acl;
    }

    private SingleRepoSearchSource( final Builder builder )
    {
        repositoryId = builder.repositoryId;
        branch = builder.branch;
        acl = builder.acl;
    }

    public static SingleRepoSearchSource from( final InternalContext context )
    {
        return create( context ).build();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static Builder create( final InternalContext context )
    {
        return create().
            repositoryId( context.getRepositoryId() ).
            branch( context.getBranch() ).
            acl( context.getPrincipalsKeys() );
    }

    public static final class Builder
    {
        private RepositoryId repositoryId;

        private Branch branch;

        private PrincipalKeys acl;

        private Builder()
        {
        }

        public Builder repositoryId( final RepositoryId val )
        {
            repositoryId = val;
            return this;
        }

        public Builder branch( final Branch val )
        {
            branch = val;
            return this;
        }

        public Builder acl( final PrincipalKeys val )
        {
            acl = val;
            return this;
        }

        public SingleRepoSearchSource build()
        {
            return new SingleRepoSearchSource( this );
        }
    }
}
