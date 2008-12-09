package org.apache.maven.dotnet.plugin.impl;

import org.apache.maven.dotnet.registry.Repository;
import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.model.configurationappenders.io.xpp3.ConfigurationAppendersXpp3Reader;
import org.apache.maven.dotnet.model.configurationappenders.ConfigurationAppenderModel;
import org.apache.maven.dotnet.model.configurationappenders.ConfigurationAppender;
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
        throws IOException
    {
        ConfigurationAppendersXpp3Reader xpp3Reader = new ConfigurationAppendersXpp3Reader();
        Reader reader = new InputStreamReader( inputStream );
        ConfigurationAppenderModel model;
        try
        {
            model = xpp3Reader.read( reader );
        }
        catch ( XmlPullParserException e )
        {
            e.printStackTrace();
            throw new IOException( "NPANDAY-062-000: Could not read plugins-compiler.xml" );
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
                e.printStackTrace();
                throw new IOException("NPANDAY-xxx-000: Could not load class appender: Name = ");
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
}
