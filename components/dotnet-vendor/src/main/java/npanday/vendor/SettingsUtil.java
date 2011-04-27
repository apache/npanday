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

package npanday.vendor;

import npanday.PathUtil;
import npanday.registry.RepositoryRegistry;
import npanday.registry.impl.StandardRepositoryLoader;
import npanday.vendor.impl.SettingsRepository;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 *   Central handling of creation and retrieval of the SettingsRepository.
 *   @author Lars Corneliussen (me@lcorneliussen.de)
 */
public class SettingsUtil
{
    /**
     * Return the registered settings, or create from default settings file location (.m2/npanday-settings.xml)
     * @param repositoryRegistry The registry.
     * @return The current, or just created SettingsRepository
     * @throws SettingsException If anything goes wrong reading or registering the settings
     */
    public static SettingsRepository getOrPopulateSettingsRepository( RepositoryRegistry repositoryRegistry)
        throws SettingsException
    {
          return getOrPopulateSettingsRepository(repositoryRegistry, PathUtil.getHomeM2Folder());
    }

    /**
     * Return the registered settings, or creates them from the given path or file.
     * @param repositoryRegistry The registry.
     * @param settingsPathOrFile If a path, 'npanday-settings.xml' is added.
     * @return The current, or just created SettingsRepository
     * @throws SettingsException If anything goes wrong reading or registering the settings
     */
    public static SettingsRepository getOrPopulateSettingsRepository( RepositoryRegistry repositoryRegistry, String settingsPathOrFile )
        throws SettingsException
    {
        SettingsRepository settingsRepository = (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        if (settingsRepository == null){
            return populateSettingsRepository( repositoryRegistry, settingsPathOrFile);
        }
        return settingsRepository;
    }

    /**
     * Creates and registers the settings from the given path or file.
     * @param repositoryRegistry The registry.
     * @param settingsPathOrFile If a path, 'npanday-settings.xml' is added.
     * @return The new Settings Repository.
     * @throws SettingsException If anything goes wrong reading or registering the settings
     */
    public static SettingsRepository populateSettingsRepository( RepositoryRegistry repositoryRegistry, String settingsPathOrFile )
        throws SettingsException
    {
        SettingsRepository settingsRepository;
        try
        {
            settingsRepository = ( SettingsRepository) repositoryRegistry.find( "npanday-settings" );
        }
        catch ( Exception ex )
        {
            throw new SettingsException( "NPANDAY-108-001: Error finding npanday-settings in registry", ex );
        }

        if ( settingsRepository != null )
        {
            try
            {
                repositoryRegistry.removeRepository( "npanday-settings" );
            }
            catch ( Exception ex )
            {
                throw new SettingsException( "NPANDAY-108-002: Error removing npanday-settings from registry", ex );
            }
        }

        File settingsFile = PathUtil.buildSettingsFilePath( settingsPathOrFile );

        if (!settingsFile.exists())
        {
            return null;
        }

        try
        {
            StandardRepositoryLoader repoLoader = new StandardRepositoryLoader();
            repoLoader.setRepositoryRegistry( repositoryRegistry );
            settingsRepository = (SettingsRepository) repoLoader.loadRepository( settingsFile.getAbsolutePath(),
                                                                                 SettingsRepository.class.getName(),
                                                                                 new Hashtable() );
            repositoryRegistry.addRepository( "npanday-settings", settingsRepository );
            assert settingsRepository != null;

            return settingsRepository;
        }
        catch ( IOException e )
        {
            throw new SettingsException( "NPANDAY-108-003: Error loading " + settingsFile.getAbsolutePath(), e );
        }
    }
}
