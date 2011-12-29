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

package npanday.packaging;

import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @plexus.component role="npanday.packaging.ConfigFileHandler"
 */
public class ConfigFileHandler
    extends AbstractLogEnabled
{
    public void handleConfigFile( File sourceConfigFile, File targetConfigFile ) throws MojoFailureException
    {
        if ( !sourceConfigFile.exists() )
        {
            getLogger().warn( "NPANDAY-133-001: The configuration file '" + sourceConfigFile + "' couldn't be found" );
            return;
        }

        // TODO: add XDT-support here

        getLogger().info( "NPANDAY-133-004: Transforming/copying config file for packaging" );

        try
        {
            getLogger().debug( "NPANDAY-133-002: Copying config file '" + sourceConfigFile + "' to '" + targetConfigFile + "'" );
            FileUtils.copyFile( sourceConfigFile, targetConfigFile );
        }
        catch ( IOException e )
        {
            throw new MojoFailureException(
                "NPANDAY-133-003: Unable to copy config file '" + sourceConfigFile + "' to '" + targetConfigFile + "'"
            );
        }
    }
}
