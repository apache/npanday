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

import com.google.common.collect.Lists;
import npanday.ArtifactType;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Resolves all MSDeploy-Packages and unpacks them for further use.
 *
 * @author <a href="mailto:lcorneliussen@apache.org">Lars Corneliussen</a>
 *
 * @goal unpack-dependencies
 * @requiresDependencyResolution compile
 */
public class MsDeployUnpackDependenciesMojo
    extends AbstractMsDeployMojo<UnpackDependencyIterationItem>
{
    @Override
    protected void afterCommandExecution( UnpackDependencyIterationItem iterationItem ) throws MojoExecutionException
    {

    }

    @Override
    protected void beforeCommandExecution( UnpackDependencyIterationItem iterationItem )
    {

    }

    @Override
    protected List<UnpackDependencyIterationItem> prepareIterationItems() throws MojoFailureException
    {
        List<UnpackDependencyIterationItem> items = newArrayList();

        final Set projectDependencyArtifacts = project.getDependencyArtifacts();
        for ( Object artifactAsObject : projectDependencyArtifacts )
        {
            Artifact artifact = (Artifact)artifactAsObject;
            if (artifact.getType().equals( ArtifactType.MSDEPLOY_PACKAGE.getPackagingType())
                || artifact.getType().equals( ArtifactType.MSDEPLOY_PACKAGE.getExtension()))
            {
                items.add( new UnpackDependencyIterationItem(project, artifact) );
            }
        }

        return items;
    }

    @Override
    protected List<String> getCommands(UnpackDependencyIterationItem item) throws MojoExecutionException
    {
        List<String> commands = Lists.newArrayList();

        // this requires npanday.executable.impl.DefaultNetExecutable to be used, as it will respect
        // the "home-made" quoting

        commands.add( "-verb:sync" );
        commands.add( "-source:package=" + item.getPackageSource().getAbsolutePath());
        commands.add( "-dest:contentPath=" + item.getPackageTarget().getAbsolutePath() );

        return commands;
    }
}
