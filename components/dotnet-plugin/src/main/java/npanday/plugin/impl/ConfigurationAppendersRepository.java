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
package npanday.plugin.impl;

import npanday.model.configurationappenders.ConfigurationAppender;
import npanday.model.configurationappenders.ConfigurationAppenderModel;
import npanday.model.configurationappenders.io.xpp3.ConfigurationAppendersXpp3Reader;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.impl.AbstractMultisourceRepository;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * @author Shane Isbell
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @plexus.component
 *   role="npanday.plugin.impl.ConfigurationAppendersRepository"
 */
public class ConfigurationAppendersRepository
    extends AbstractMultisourceRepository<ConfigurationAppenderModel>
    implements Repository
{

    private Set<Class> appenderClasses = new HashSet<Class>();

    @Override
    protected ConfigurationAppenderModel loadFromReader( Reader reader, Hashtable properties )
        throws IOException, XmlPullParserException
    {
        ConfigurationAppendersXpp3Reader xpp3Reader = new ConfigurationAppendersXpp3Reader();

           return xpp3Reader.read( reader );
    }

    @Override
    protected void mergeLoadedModel( ConfigurationAppenderModel model )
        throws NPandayRepositoryException
    {
        List<ConfigurationAppender> appenders  = model.getConfigurationAppenders();
        for(ConfigurationAppender appender : appenders)
        {
            try
            {
                appenderClasses.add(Class.forName( appender.getName() ));
            }
            catch ( ClassNotFoundException e )
            {
                throw new NPandayRepositoryException("NPANDAY-xxx-000: Could not load class appender: Name = " + appender.getName(), e );
            }
        }
    }

    public Set<Class> getAppenderClasses()
    {
        return appenderClasses;
    }

    /**
     * Remove all stored values in preparation for a reload.
     */
    @Override
    protected void clear()
    {
        appenderClasses.clear();
    }
}
