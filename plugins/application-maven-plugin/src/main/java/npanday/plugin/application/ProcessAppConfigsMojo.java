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

package npanday.plugin.application;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import npanday.ArtifactType;
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
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @phase prepare-package
 * @goal process-configs
 * @since 1.5.0-incubating
 */
public class ProcessAppConfigsMojo
    extends AbstractNPandaySettingsAwareMojo
{
    /**
     * The main configuration file (relative to the basedir)
     * to be packaged along with the application.
     *
     * This one will be added to {@see #additionalConfigExcludes}
     * in order to avoid duplicate handling.
     *
     * @parameter default-value="app.config"
     */
    private String appConfigFile;

    /**
     * By default this will be the ${artifactId}.${extension}.config; but in
     * some cases it might be necessary to have the config named differently.
     *
     * @parameter
     */
    private String targetConfigFileNameOverride;

    /**
     * The transformation to apply to the configurations
     * specified in {@link #appConfigFile} and {@link #additionalConfigIncludes}.
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
     * Defaults to <code>*.config</code>, if not overridden by {@see additionalConfigIncludesList}.
     *
     * Transformations are automatically excluded from being processed as main configs,
     * while a transformation is detected as such, if it follows the format described in {@see #transformationHint}
     * and a potential target file (without the hint) exists.
     *
     * @parameter
     */
    private String[] additionalConfigIncludes;

    /**
     * Expressions for config files to explicitely exclude from config processing.
     *
     * <code>pom.xml</code> will always be excluded by default.
     *
     * @parameter
     */
    private String[] additionalConfigExcludes;

    /**
     * Semicolon-separated command line version of {@link #additionalConfigIncludes}
     *
     * @parameter expression="${config.includes}"
     */
    private String additionalConfigIncludesList;

    /**
     * Semicolon-separated command line version of {@link #additionalConfigExcludes}
     *
     * @parameter expression="${config.excludes}"
     */
    private String additionalConfigExcludesList;

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
        additionalConfigIncludes = firstNonNull(
            additionalConfigIncludesList, additionalConfigIncludes, new String[]{ "*.config" }
        );
        additionalConfigExcludes = firstNonNull( additionalConfigExcludesList, additionalConfigExcludes );
    }

    @Override
    public void innerExecute() throws MojoExecutionException, MojoFailureException
    {
        super.innerExecute();

        final List<String> excludes = Lists.newArrayList( additionalConfigExcludes );
        excludes.add( appConfigFile );

        final String[] includes = additionalConfigIncludes;
        final File targetFolder = PathUtil.getPreparedPackageFolder( project );

        configFileHandler.setWorkingFolder( workingFolder );

        String extension = ArtifactType.getArtifactTypeForPackagingName( project.getPackaging() ).getExtension();

        String targetConfigFileName = targetConfigFileNameOverride;
        if ( Strings.isNullOrEmpty( targetConfigFileName ) )
        {
            targetConfigFileName = project.getArtifactId() + "." + extension + ".config";
        }

        File targetConfigFile = new File(
            targetFolder, targetConfigFileName
        );

        final VendorRequirement vendorRequirement = getVendorRequirement();

        configFileHandler.handleConfigFile(
            vendorRequirement, new File( project.getBasedir(), appConfigFile ), transformationHint, targetConfigFile
        );

        if ( includes.length > 0 )
        {
            configFileHandler.handleConfigFiles(
                vendorRequirement, project.getBasedir(), includes, excludes.toArray( new String[0] ),
                transformationHint, targetFolder
            );
        }
    }

}
