package npanday.plugin.msdeploy;

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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 */
public class UnpackDependencyIterationItem
{
    private File packageSource;

    private File packageTarget;

    private Artifact artifact;

    public UnpackDependencyIterationItem( MavenProject project, Artifact artifact ) throws MojoFailureException
    {
        this.artifact = artifact;

        if (!artifact.isResolved()){
           throw new MojoFailureException( "NPANDAY-124-000: The artifact should already have been resolved: " + artifact);
        }

        packageSource = artifact.getFile();
        assert packageSource != null : "package source should not be null here";

        packageTarget = new File( PathUtil.getPreparedPackageFolder( project ), artifact.getArtifactId() );
    }

    public File getPackageSource()
    {
        return packageSource;
    }

    public File getPackageTarget()
    {
        return packageTarget;
    }

    @Override
    public String toString()
    {
        return "UnpackDependencyIterationItem{" + "packageSource=" + packageSource + ", packageTarget=" + packageTarget
            + ", artifact=" + artifact + '}';
    }
}
