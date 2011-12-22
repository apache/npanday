package npanday.vendor;

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

import npanday.PathUtil;
import npanday.registry.NPandayRepositoryException;
import npanday.registry.RepositoryRegistry;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;

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
     * Replaces npanday settings from the given path or file.
     *
     * @param failIfFileNotFound
     *  If <code>true</code>, fails, if the file can't be found. Else,
     *  just leaves the settings untouched.
     * @return The new Settings Repository.
     * @throws MojoExecutionException If anything goes wrong reading or initializing the settings
     */
    private static boolean overrideDefaultSettings(
        Log log,
        RepositoryRegistry repositoryRegistry, String settingsPathOrFile, boolean failIfFileNotFound )
        throws MojoExecutionException
    {
        File settingsFile = PathUtil.buildSettingsFilePath( settingsPathOrFile );

        if ( !settingsFile.exists() )
        {
            if ( failIfFileNotFound )
            {
                throw new MojoExecutionException(
                    "NPANDAY-108-005: Configured settings file does not exist: " + settingsFile
                );
            }
            else
            {
                log.warn(
                    "NPANDAY-108-006: Settings file does not exist: " + settingsFile
                        + "; current Mojo will adhere to configured defaults."
                );
                return false;
            }
        }

        SettingsRepository settingsRepository = findSettingsFromRegistry( repositoryRegistry );
        try
        {
            settingsRepository.clearAll();
        }
        catch ( OperationNotSupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-108-006: Error clearing settings repository.", e );
        }

        try
        {
            settingsRepository.load( settingsFile.toURI().toURL() );
            log.debug(
                "NPANDAY-108-007: Replaced default npanday-settings with contents from '" + settingsFile + "'."
            );
            return true;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "NPANDAY-108-003: Error loading " + settingsFile.getAbsolutePath(), e );
        }
        catch( NPandayRepositoryException e )
        {
            throw new MojoExecutionException( "NPANDAY-108-004: Error loading settings repository.", e );
        }
    }

    public static SettingsRepository findSettingsFromRegistry( RepositoryRegistry repositoryRegistry )
    {
        return (SettingsRepository) repositoryRegistry.find( "npanday-settings" );
    }

    /**
     * Applies the custom settings provided in settingsPathOrFile.
     *
     * @param settingsPathOrFile If a path, 'npanday-settings.xml' is added.
     * @throws MojoExecutionException If anything goes wrong reading or initializing the settings
     */
    public static void applyCustomSettings(
        Log log, RepositoryRegistry repositoryRegistry, String settingsPathOrFile )
        throws  MojoExecutionException
    {
        overrideDefaultSettings(
            log,
            repositoryRegistry,
            settingsPathOrFile,
            /*throw error, if file doesn exist*/ true );
    }

    /**
     * Applies the custom settings provided in settingsPathOrFile, if the file does exist.
     *
     * @param settingsPathOrFile If a path, 'npanday-settings.xml' is added.
     * @throws MojoExecutionException If anything goes wrong reading or initializing the settings
     */
    public static boolean applyCustomSettingsIfAvailable( Log log, RepositoryRegistry repositoryRegistry,
                                                   String settingsPathOrFile)
        throws  MojoExecutionException
    {
        return overrideDefaultSettings(
            log,
            repositoryRegistry,
            settingsPathOrFile,
            /*throw error, if file doesn exist*/ false );
    }


    public static void warnIfSettingsAreEmpty( Logger logger, RepositoryRegistry repositoryRegistry )
    {
        if (findSettingsFromRegistry( repositoryRegistry ).isEmpty()){
          logger.warn( "NPANDAY-108-008: The registered settings repository is empty; defaults will be applied." );
        }
    }
}
