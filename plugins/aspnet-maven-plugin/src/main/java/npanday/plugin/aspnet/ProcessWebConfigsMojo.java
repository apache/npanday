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

package npanday.plugin.aspnet;

import com.google.common.collect.Lists;
import npanday.PathUtil;
import npanday.packaging.ConfigFileHandler;
import npanday.vendor.VendorRequirement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Finds, transforms and copies arbitrary configuration files for packaging.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @phase prepare-package
 * @goal process-configs
 * @since 1.5.0-incubating
 */
public class ProcessWebConfigsMojo
    extends AbstractNPandaySettingsAwareMojo
{
    /**
     * The transformation to apply to the configurations
     * specified in {@link #configIncludes}.
     *
     * The Mojo will search for transformations by 'injecting' the
     * configured hint between a file name and its extension:
     * <code>&lt;file-name&gt;.&lt;transformation-hint&gt;.&lt;file-extension&gt;</code>.
     *
     * @parameter default-value="package"
     */
    private String transformationHint;

    /**
     * Expressions for config files to additionally include further configuration files that
     * need to be copied and/or transformed.
     *
     * Defaults to <code>*.config</code>, if not overridden by {@see configIncludesList}.
     *
     * Transformations are automatically excluded from being processed as main configs,
     * while a transformation is detected as such, if it follows the format described in {@see #transformationHint}
     * and a potential target file (without the hint) exists.
     *
     * @parameter
     */
    private String[] configIncludes;

    /**
     * Expressions for config files to explicitely exclude from config processing.
     *
     * <code>pom.xml</code> will always be excluded by default.
     *
     * @parameter
     */
    private String[] configExcludes;

    /**
     * Semicolon-separated command line version of {@link #configIncludes}
     *
     * @parameter expression="${config.includes}"
     */
    private String configIncludesList;

    /**
     * Semicolon-separated command line version of {@link #configExcludes}
     *
     * @parameter expression="${config.excludes}"
     */
    private String configExcludesList;

    /**
     * Place to store temporary files for the transformation process.
     *
     * @parameter default-value="${project.build.directory}/xdt"
     */
    private File workingFolder;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * @component
     */
    protected ConfigFileHandler configFileHandler;

    @Override
    protected void setupParameters()
    {
        configIncludes = firstNonNull(
            configIncludesList, configIncludes, new String[]{ "**/*.config" }
        );
        configExcludes = firstNonNull( configExcludesList, configExcludes );
    }

    @Override
    protected void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        super.innerExecute();

        final List<String> excludes = Lists.newArrayList( configExcludes );

        final String[] includes = configIncludes;
        final File targetFolder = PathUtil.getPreparedPackageFolder( project );

        configFileHandler.setWorkingFolder( workingFolder );

        final VendorRequirement vendorRequirement = getVendorRequirement();

        if ( includes.length > 0 )
        {
            configFileHandler.handleConfigFiles(
                vendorRequirement, project.getBasedir(), includes, excludes.toArray( new String[0] ),
                transformationHint, targetFolder
            );
        }
    }


}
