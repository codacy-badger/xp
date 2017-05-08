package com.enonic.xp.repo.impl.search;

import com.enonic.xp.query.Query;
import com.enonic.xp.repo.impl.ReturnFields;
import com.enonic.xp.repo.impl.SearchSource;
import com.enonic.xp.repo.impl.StorageSource;

public class SearchRequest
{
    private final StorageSource settings;

    private final Query query;

    private final ReturnFields returnFields;

    private final SearchSource searchSource;

    private SearchRequest( Builder builder )
    {
        this.settings = builder.settings;
        this.query = builder.query;
        this.returnFields = builder.returnFields;
        this.searchSource = builder.searchSource;
    }

    public SearchSource getSearchSource()
    {
        return searchSource;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public StorageSource getSettings()
    {
        return settings;
    }

    public Query getQuery()
    {
        return query;
    }

    public ReturnFields getReturnFields()
    {
        return returnFields;
    }

    public static final class Builder
    {
        private StorageSource settings;

        private Query query;

        private ReturnFields returnFields;

        private SearchSource searchSource;

        private Builder()
        {
        }

        public Builder settings( StorageSource settings )
        {
            this.settings = settings;
            return this;
        }

        public Builder searchSource( final SearchSource searchSource )
        {
            this.searchSource = searchSource;
            return this;
        }

        public Builder query( Query query )
        {
            this.query = query;
            return this;
        }

        public Builder returnFields( final ReturnFields returnFields )
        {
            this.returnFields = returnFields;
            return this;
        }

        public SearchRequest build()
        {
            return new SearchRequest( this );
        }
    }
}
