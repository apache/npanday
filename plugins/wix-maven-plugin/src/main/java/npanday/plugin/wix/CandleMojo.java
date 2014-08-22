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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which executes WiX candle to create a .wixobj file.
 *
 * @goal candle
 * @phase package
 */
public class CandleMojo
    extends AbstractWixMojo
{
    /**
     * Location of the WiX source files.
     *
     * @parameter expression="${sourceFiles}"
     * @required
     */
    private File[] sourceFiles;

    /**
     * Definitions to be passed on before pre Compilation
     *
     * @parameter expression="${definitions}"
     */
    private String[] definitions;

    /**
     * x86, intel, x64, intel64, or ia64 (default: x86)
     *
     * @parameter expression="${arch}"
     */
    private String arch;

    /**
     * Output file
     *
     * @parameter expression="${outputDirectory}"
     */
    private File outputDirectory;

    /**
     * The executable identifier used to locate the right configurations from executable-plugins.xml. Can't be changed.
     */
    private String executableIdentifier = "CANDLE";

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

        arguments.add( "-nologo" );
        arguments.add( "-sw" );

        if ( definitions.length > 0 )
        {
            for ( String definition : definitions )
            {
                arguments.add( "-d" + definition );
            }
        }

        if ( outputDirectory != null )
        {
            if ( !outputDirectory.exists() )
            {
                outputDirectory.mkdir();
            }
            arguments.add( "-out" );
            arguments.add( outputDirectory.getAbsolutePath() + "\\" );
        }

        if ( arch != null )
        {
            arguments.add( "-arch" );
            arguments.add( arch );
        }

        for ( File sourceFile : sourceFiles )
        {
            if ( !sourceFile.exists() )
            {
                throw new MojoExecutionException( "Source file does not exist " + sourceFile );
            }
            arguments.add( sourceFile.getAbsolutePath() );
        }
        return arguments;
    }
}
