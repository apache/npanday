package npanday.plugin.impl;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
