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

import npanday.registry.NPandayRepositoryException;
import npanday.registry.Repository;
import npanday.registry.RepositoryRegistry;
import npanday.model.configurationappenders.io.xpp3.ConfigurationAppendersXpp3Reader;
import npanday.model.configurationappenders.ConfigurationAppenderModel;
import npanday.model.configurationappenders.ConfigurationAppender;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ConfigurationAppendersRepository implements Repository
{

    private Set<Class> appenderClasses;

    public void load( InputStream inputStream, Hashtable properties )
        throws NPandayRepositoryException
    {
        ConfigurationAppendersXpp3Reader xpp3Reader = new ConfigurationAppendersXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        ConfigurationAppenderModel model;
        try
        {
            model = xpp3Reader.read( reader );
        }
        catch( IOException e )
        {
            throw new NPandayRepositoryException( "NPANDAY-062-000: An error occurred while reading plugins-compiler.xml", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new NPandayRepositoryException( "NPANDAY-062-001: Could not read plugins-compiler.xml", e );
        }
        List<ConfigurationAppender> appenders  = model.getConfigurationAppenders();
        appenderClasses = new HashSet<Class>();
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

    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {

    }
    
    /**
     * @see Repository#setSourceUri(String)
     */
    public void setSourceUri( String fileUri )
    {
        // not supported
    }

    /**
     * @see Repository#reload()
     */
    public void reload() throws IOException
    {
        // not supported
    }
}
