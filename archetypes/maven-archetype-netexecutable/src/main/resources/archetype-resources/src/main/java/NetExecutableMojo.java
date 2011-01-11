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
package $package;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import npanday.executable.ExecutionException;
import npanday.PlatformUnsupportedException;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.ArrayList;

/**
 * To complete the implementation:
 * 1) Make sure to add an entry in the net-executables.xml file, located within the dotnet-core module. Replace each
 * param: ${vendor}, ${exe}, .. with the appropriate values.
<pre>
  <executablePlugin>
    <identifier>${ID}</identifier>
    <pluginClass>npanday.executable.impl.DefaultNetExecutable</pluginClass>
    <vendor>${vendor}</vendor>
    <executable>${exe}</executable>
    <profile>${profile}</profile>
    <frameworkVersions>
      <frameworkVersion>2.0.50727</frameworkVersion>
      <frameworkVersion>1.1.4322</frameworkVersion>
    </frameworkVersions>
    <platforms>
      <platform>
        <operatingSystem>Windows</operatingSystem>
      </platform>
    </platforms>
  </executablePlugin>
</pre>
 * 2) Add profile <<ADD_PROFILE>> to the meta-data of the profile field of this class. This profile name should match
 * the ${profile} within the net-executables.xml.
 * 3) Add any special commands to the getCommands method.
 * 4) Recompile the dotnet-core component.
 * 5) Rename this class and install this Mojo component.
 */
public class NetExecutableMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The Vendor for the executable.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * The profile that the executable should use.
     *
     * @parameter expression = "${profile}" default-value = "<<ADD_PROFILE>>"
     */
    private String profile;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            netExecutableFactory.getNetExecutableFor( vendor, frameworkVersion, profile, getCommands(),
                                                      null ).execute();
        }
        catch ( ExecutionException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-000: Unable to execute: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
        catch ( PlatformUnsupportedException e )
        {
            throw new MojoExecutionException( "NPANDAY-xxx-001: Platform Unsupported: Vendor " + vendor +
                ", frameworkVersion = " + frameworkVersion + ", Profile = " + profile, e );
        }
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        //<<ADD COMMANDS HERE>>
        return commands;
    }

}
