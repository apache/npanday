package npanday.plugin.wix;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which executes WiX light to create a .msi file.
 *
 * @goal light
 * @phase package
 */
public class LightMojo
    extends AbstractWixMojo
{
    /**
     * Location of the WiX object files.
     *
     * @parameter expression="${objectFiles}"
     * @required
     */
    private File[] objectFiles;

    /**
     * Output file
     *
     * @parameter expression="${outputFile}"
     */
    private File outputFile;

    /**
     * Location of the WiX localization files.
     *
     * @parameter expression="${localizationFiles}"
     */
    private File[] localizationFiles;

    /**
     * Output file
     *
     * @parameter expression="${outputDirectory}"
     */
    private File outputDirectory;


    /**
     * Localized string cultures to load from .wxl files and libraries.
     *
     * @parameter expression="${cultures}"
     */
    private String[] cultures;

    /**
     * The executable identifier used to locate the right configurations from executable-plugins.xml. Can't be changed.
     */
    private String executableIdentifier = "LIGHT";

    @Override
    public String getExecutableIdentifier()
    {
        return executableIdentifier;
    }

    @Override
    public List<String> getArguments()
        throws MojoExecutionException
    {
        List<String> arguments = new ArrayList<String>();

        for ( File objectFile : objectFiles )
        {
            if ( !objectFile.exists() )
            {
                throw new MojoExecutionException( "Object file does not exist " + objectFile );
            }
            arguments.add( objectFile.getAbsolutePath() );
        }

        if ( localizationFiles.length > 0 )
        {
            arguments.add( "-loc" );
            for ( File localizationFile : localizationFiles )
            {
                if ( !localizationFile.exists() )
                {
                    throw new MojoExecutionException( "Localization file does not exist " + localizationFile );
                }
                arguments.add( localizationFile.getAbsolutePath() );
            }
        }

        if ( outputFile != null )
        {
            arguments.add( "-o" );
            arguments.add( outputFile.getAbsolutePath() );
        }
        else if ( outputDirectory != null )
        {
            arguments.add( "-out" );
            arguments.add( outputDirectory.getAbsolutePath() + "\\" );
        }

        if ( cultures != null && cultures.length > 0 )
        {
            String commaDelimitedCultures = StringUtils.join( cultures, "," );
            arguments.add( "-cultures:" + commaDelimitedCultures );
        }

        return arguments;
    }
}
