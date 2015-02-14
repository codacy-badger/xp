package com.enonic.xp.core.impl.schema.relationship;

import org.junit.Before;
import org.junit.Test;

import com.enonic.xp.module.ModuleKey;
import com.enonic.xp.schema.relationship.RelationshipType;
import com.enonic.xp.schema.relationship.RelationshipTypeProvider;
import com.enonic.xp.schema.relationship.RelationshipTypes;

import static org.junit.Assert.*;

public class RelationshipTypeServiceImplTest
{
    private RelationshipTypeServiceImpl service;

    private RelationshipTypeProvider provider;

    private RelationshipType type1;

    private RelationshipType type2;

    @Before
    public void setup()
    {
        this.service = new RelationshipTypeServiceImpl();
        this.type1 = createType( "mymodule:test" );
        this.type2 = createType( "othermodule:test" );
        this.provider = () -> RelationshipTypes.from( this.type1, this.type2 );
    }

    @Test
    public void testEmpty()
    {
        final RelationshipTypes result = this.service.getAll();
        assertNotNull( result );
        assertEquals( 0, result.getSize() );
    }

    @Test
    public void testGetByName()
    {
        this.service.addProvider( this.provider );

        final RelationshipType result1 = this.service.getByName( this.type1.getName() );
        assertNotNull( result1 );

        this.service.removeProvider( this.provider );

        final RelationshipType result2 = this.service.getByName( this.type1.getName() );
        assertNull( result2 );
    }

    @Test
    public void testGetAll()
    {
        this.service.addProvider( this.provider );

        final RelationshipTypes result = this.service.getAll();
        assertNotNull( result );
        assertEquals( 2, result.getSize() );
        assertSame( this.type1, result.get( 1 ) );
        assertSame( this.type2, result.get( 0 ) );
    }

    @Test
    public void testGetByModule()
    {
        this.service.addProvider( this.provider );

        final RelationshipTypes result = this.service.getByModule( ModuleKey.from( "mymodule" ) );
        assertNotNull( result );
        assertEquals( 1, result.getSize() );
        assertSame( this.type1, result.get( 0 ) );
    }

    private RelationshipType createType( final String name )
    {
        return RelationshipType.newRelationshipType().name( name ).build();
    }
}
