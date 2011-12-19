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
package npanday.plugin.resgen;

import npanday.artifact.ArtifactContext;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates resources
 *
 * @author Shane Isbell
 * @goal generate
 * @phase process-resources
 */
@Deprecated
public class ResourceGeneratorMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${settings.localRepository}"
     */
    private File localRepository;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The home directory of your .NET SDK.
     *
     * @parameter expression="${netHome}"
     */
    private File netHome;

    /**
     * The Vendor for the executable. Supports MONO and MICROSOFT: the default value is <code>MICROSOFT</code>. Not
     * case or white-space sensitive.
     *
     * @parameter expression="${vendor}"
     */
    private String vendor;

    /**
     * @parameter expression = "${frameworkVersion}"
     */
    private String frameworkVersion;

    /**
     * @parameter expression = "${vendorVersion}"
     */
    private String vendorVersion;

    /**
     * @component
     */
    private ArtifactContext artifactContext;

    /**
     * @component
     */
    private npanday.executable.NetExecutableFactory netExecutableFactory;

    public void execute()
        throws MojoExecutionException
    {
          // TODO: this plugin can be removed; it is a no-op!
    }

    public List<String> getCommands()
        throws MojoExecutionException
    {
        List<String> commands = new ArrayList<String>();
        commands.add( project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator +
            project.getArtifactId() + ".resx" );
        commands.add( project.getBuild().getDirectory() + File.separator + "assembly-resources" + File.separator +
            "resource" + File.separator + project.getArtifactId() + ".resources" );
        return commands;
    }
}
