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

package npanday.plugin.azure;

import npanday.ArtifactType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * Resolves the cloud service configuration and attaches it as a build artifact.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 * @goal process-cloud-service-configuration
 */
public class ProcessCloudServiceConfigurationMojo
    extends AbstractNPandaySettingsAwareMojo
{
    /**
     * The cloud service configuration file to attach.
     *
     * @parameter
     *  expression="${azure.serviceConfigurationFile}"
     *  default-value="${basedir}/ServiceConfiguration.Package.cscfg"
     */
    private File serviceConfigurationFile;

     /**
     * @parameter expression="${azure.serviceConfigurationClassifier}"
     * default-value="package"
     */
    private String serviceConfigurationClassifier;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        super.execute();

        if ( !serviceConfigurationFile.exists() )
        {
            throw new MojoExecutionException(
                "NPANDAY-125-001: Couldn't find the cloud configuration file to attach along with the package "
                    + serviceConfigurationFile.getAbsolutePath()
            );
        }

        projectHelper.attachArtifact(
            project, ArtifactType.AZURE_CLOUD_SERVICE_CONFIGURATION.getPackagingType(), serviceConfigurationClassifier,
            serviceConfigurationFile
        );
    }
}
