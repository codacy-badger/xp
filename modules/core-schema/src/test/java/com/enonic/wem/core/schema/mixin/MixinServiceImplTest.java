package com.enonic.wem.core.schema.mixin;

import org.junit.Before;
import org.junit.Test;

import com.enonic.wem.api.module.ModuleKey;
import com.enonic.wem.api.schema.mixin.Mixin;
import com.enonic.wem.api.schema.mixin.MixinProvider;
import com.enonic.wem.api.schema.mixin.Mixins;

import static org.junit.Assert.*;

public class MixinServiceImplTest
{
    private MixinServiceImpl service;

    private MixinProvider provider;

    private Mixin mixin1;

    private Mixin mixin2;

    @Before
    public void setup()
    {
        this.service = new MixinServiceImpl();
        this.mixin1 = createMixin( "mymodule:test" );
        this.mixin2 = createMixin( "othermodule:test" );
        this.provider = () -> Mixins.from( this.mixin1, this.mixin2 );
    }

    @Test
    public void testEmpty()
    {
        final Mixins result = this.service.getAll();
        assertNotNull( result );
        assertEquals( 0, result.getSize() );
    }

    @Test
    public void testGetByName()
    {
        this.service.addProvider( this.provider );

        final Mixin result1 = this.service.getByName( this.mixin1.getName() );
        assertNotNull( result1 );

        this.service.removeProvider( this.provider );

        final Mixin result2 = this.service.getByName( this.mixin1.getName() );
        assertNull( result2 );
    }

    @Test
    public void testGetByLocalName()
    {
        this.service.addProvider( this.provider );

        final Mixin result1 = this.service.getByLocalName( "test" );
        assertNotNull( result1 );
        assertEquals( "test", result1.getName().getLocalName() );
    }

    @Test
    public void testGetAll()
    {
        this.service.addProvider( this.provider );

        final Mixins result = this.service.getAll();
        assertNotNull( result );
        assertEquals( 2, result.getSize() );
        assertSame( this.mixin1, result.get( 1 ) );
        assertSame( this.mixin2, result.get( 0 ) );
    }

    @Test
    public void testGetByModule()
    {
        this.service.addProvider( this.provider );

        final Mixins result = this.service.getByModule( ModuleKey.from( "mymodule" ) );
        assertNotNull( result );
        assertEquals( 1, result.getSize() );
        assertSame( this.mixin1, result.get( 0 ) );
    }

    private Mixin createMixin( final String name )
    {
        return Mixin.newMixin().name( name ).build();
    }
}
