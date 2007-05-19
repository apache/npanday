package org.apache.maven.dotnet.plugin;

import java.lang.reflect.Field;

public interface PluginContext
{
    /**
     * Role used to register component implementations with the container.
     */
    String ROLE = PluginContext.class.getName();

    ConfigurationAppender getConfigurationAppenderFor( Field field );
}
