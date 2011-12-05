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
import npanday.registry.NPandayRepositoryException;
import npanday.registry.RepositoryRegistry;
import npanday.vendor.impl.SettingsRepository;
import org.codehaus.plexus.util.StringUtils;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.IOException;

/**
 *   Central handling of creation and retrieval of the SettingsRepository.
 *   @author Lars Corneliussen (me@lcorneliussen.de)
 */
public class SettingsUtil
{
    /**
     * Return the registered settings, or create from configured (-Dnpanday-settings=...) or default settings file location (.m2/npanday-settings.xml)
     * @param repositoryRegistry The registry.
     * @return The current, or just created SettingsRepository
     * @throws SettingsException If anything goes wrong reading or registering the settings
     */
    public static SettingsRepository getOrPopulateSettingsRepository( RepositoryRegistry repositoryRegistry)
        throws SettingsException
    {
        String settingsFolder = PathUtil.getHomeM2Folder();
        String customFolder = System.getProperty( "npanday.settings" );
        if ( !StringUtils.isEmpty( customFolder ) )
        {
            settingsFolder = customFolder;
        }
        return getOrPopulateSettingsRepository(repositoryRegistry, settingsFolder );
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
        if (settingsRepository.isEmpty()){
            populateSettingsRepository( repositoryRegistry, settingsPathOrFile);
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
    public static void populateSettingsRepository( RepositoryRegistry repositoryRegistry, String settingsPathOrFile )
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

        File settingsFile = PathUtil.buildSettingsFilePath( settingsPathOrFile );

        if (!settingsFile.exists())
        {
            throw new SettingsException( "NPANDAY-108-005: Settings file does not exist: " + settingsFile );
        }

        try
        {
            settingsRepository.clearAll();
        }
        catch ( OperationNotSupportedException e )
        {
            throw new SettingsException( "NPANDAY-108-006: Error clearing settings repository.", e );
        }

        try
        {
            settingsRepository.load( settingsFile.toURI().toURL() );
        }
        catch ( IOException e )
        {
            throw new SettingsException( "NPANDAY-108-003: Error loading " + settingsFile.getAbsolutePath(), e );
        }
        catch( NPandayRepositoryException e )
        {
            throw new SettingsException( "NPANDAY-108-004: Error loading settings repository.", e );
        }
    }
}
