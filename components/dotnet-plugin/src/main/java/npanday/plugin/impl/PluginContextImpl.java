package npanday.plugin.impl;

import npanday.plugin.PluginContext;
import npanday.plugin.ConfigurationAppender;
import npanday.plugin.ConfigurationAppenderAnnotation;
import npanday.registry.RepositoryRegistry;

import java.util.Set;
import java.lang.reflect.Field;

public class PluginContextImpl
    implements PluginContext
{
    private RepositoryRegistry repositoryRegistry;

    public ConfigurationAppender getConfigurationAppenderFor( Field field )
    {
        ConfigurationAppendersRepository repository =
            (ConfigurationAppendersRepository) repositoryRegistry.find( "configuration-appenders" );
        Set<Class> appenderClasses = repository.getAppenderClasses();
        for ( Class c : appenderClasses )
        {
            ConfigurationAppenderAnnotation annotation =
                (ConfigurationAppenderAnnotation) c.getAnnotation( ConfigurationAppenderAnnotation.class );
            if ( field.getType().getName().equals( annotation.targetClassName() ) )
            {
                Object o;
                try
                {
                    o = c.newInstance();
                }
                catch ( InstantiationException e )
                {
                    e.printStackTrace();
                    return null;//TODO: throw
                }
                catch ( IllegalAccessException e )
                {
                    e.printStackTrace();
                    return null;//TODO: throw
                }

                return (ConfigurationAppender) o;
            }
        }
        return null; //TODO: throw
    }
}
