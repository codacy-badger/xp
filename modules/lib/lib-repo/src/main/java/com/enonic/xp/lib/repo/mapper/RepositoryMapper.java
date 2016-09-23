package com.enonic.xp.lib.repo.mapper;

import com.enonic.xp.repository.Repository;
import com.enonic.xp.repository.RepositorySettings;
import com.enonic.xp.repository.ValidationSettings;
import com.enonic.xp.script.serializer.MapGenerator;
import com.enonic.xp.script.serializer.MapSerializable;

public class RepositoryMapper
    implements MapSerializable
{
    private Repository repository;

    public RepositoryMapper( final Repository repository )
    {
        this.repository = repository;
    }

    @Override
    public void serialize( final MapGenerator gen )
    {
        gen.value( "id", repository.getId() );
        serialize( gen, repository.getSettings() );

    }

    private void serialize( final MapGenerator gen, final RepositorySettings settings )
    {
        gen.map( "settings" );
        serialize( gen, settings.getValidationSettings() );
        gen.end();
    }

    private void serialize( final MapGenerator gen, final ValidationSettings validationSettings )
    {
        gen.map( "validationSettings" );
        gen.value( "checkExists", validationSettings.isCheckExists() );
        gen.value( "checkParentExists", validationSettings.isCheckParentExists() );
        gen.end();
    }
}
