package com.enonic.xp.core.impl.schema.inputtype;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.inputtype.InputType;
import com.enonic.xp.inputtype.InputTypeName;
import com.enonic.xp.inputtype.InputTypeResolver;
import com.enonic.xp.inputtype.InputTypes;
import com.enonic.xp.resource.ResourceService;

@Component(immediate = true)
public final class InputTypeRegistry
    implements InputTypeResolver/*, ApplicationListener*/
{
    private static final Logger LOG = LoggerFactory.getLogger( InputTypeRegistry.class );

    private final Map<InputTypeName, InputType> inputTypes;

    private InputTypeLoader inputTypeLoader;

    @Activate
    public InputTypeRegistry( @Reference final ResourceService resourceService )
    {
        this.inputTypeLoader = new InputTypeLoader( resourceService );
        this.inputTypes = new ConcurrentHashMap<>(); // TODO: use like concurrent

        InputTypes.BUILTIN.forEach( inputType -> this.inputTypes.put( inputType.getName(), inputType ) );
    }

    public InputTypes resolveByApplication( final ApplicationKey key )
    {
        return InputTypes.create()
            .add( inputTypes.entrySet()
                      .stream()
                      .filter( entry -> key.equals( entry.getKey().getApplicationKey() ) )
                      .map( Map.Entry::getValue )
                      .collect( Collectors.toSet() ) )
            .build();
    }

//    @Override
//    public void activated( final Application application )
//    {
//        final Set<InputTypeName> names = inputTypeLoader.findNames( application.getKey() );
//
//        names.forEach( name -> {
//            final InputType inputType = inputTypeLoader.get( name );
//            inputTypes.put( name, inputType );
//        } );
//    }

//    @Override
//    public void deactivated( final Application application )
//    {
//        final ApplicationKey applicationKey = application.getKey();
//        final Set<InputTypeName> names =
//            inputTypes.keySet().stream().filter( name -> applicationKey.equals( name.getApplicationKey() ) ).collect( Collectors.toSet() );
//
//        names.forEach( inputTypes::remove );
//    }

    @Override
    public InputType resolve( final InputTypeName name )
    {
        if ( ApplicationKey.BASE.equals( name.getApplicationKey() ) )
        {
            return inputTypes.get( name );
        }

        return inputTypeLoader.get( name );
    }
}
