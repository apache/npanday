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

import npanday.PathUtil;
import npanday.packaging.ConfigFileHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * TODO: document, when config handling is figured out
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @phase prepare-package
 * @goal process-web-config
 * @since 1.5.0-incubating
 */
public class ProcessWebConfigMojo
    extends AbstractMojo
{
    /**
     * @parameter default-value="false"
     */
    private boolean skip;

    /**
     * @parameter default-value="${basedir}/web.package.config"
     */
    private File webConfig;

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

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "NPANDAY-132-000: Automatic handling of web.config was skipped by configuration" );
            return;
        }

        final File sourceConfigFile = webConfig;
        File targetConfigFile = new File(
            PathUtil.getPreparedPackageFolder( project ), "web.config"
        );

        configFileHandler.handleConfigFile( sourceConfigFile, targetConfigFile );
    }


}
