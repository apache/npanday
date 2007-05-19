package org.apache.maven.dotnet.vendor.impl;

import org.apache.maven.dotnet.registry.RepositoryRegistry;
import org.apache.maven.dotnet.registry.RepositoryLoader;
import org.apache.maven.dotnet.registry.RegistryLoader;
import org.apache.maven.dotnet.registry.Repository;


import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

final class RepositoryRegistryTestStub
    implements RepositoryRegistry
{
    private SettingsRepository settingsRepository;

    public boolean isEmpty()
    {
        return false;
    }

    public synchronized void setRepositoryLoader( RepositoryLoader loader )
    {
    }

    public synchronized void setRegistryLoader( RegistryLoader loader )
    {
    }

    public synchronized void loadFromInputStream( InputStream inputStream )
        throws IOException
    {
    }

    public synchronized void loadFromFile( String fileName )
        throws IOException
    {
    }

    public synchronized void loadFromResource( String fileName, Class sourceClass )
        throws IOException
    {
    }

    public synchronized void addRepository( String name, Repository repository )
    {
    }

    public synchronized Repository find( String name )
    {
        return settingsRepository;
    }

    public synchronized void removeRepository( String name )
    {
    }

    public synchronized Set getRepositoryNames()
    {
        return null;
    }

    public synchronized void empty()
    {
    }

    void setSettingRepository(SettingsRepository settingsRepository)
    {
        this.settingsRepository = settingsRepository;
    }
}
