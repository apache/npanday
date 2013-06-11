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

package npanday.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:me@lcorneliussen.de>Lars Corneliussen, Faktum Software</a>
 */
public interface ArtifactResolvingContributor
{
    String role = ArtifactResolvingContributor.class.getName();

    /**
     * Tries to resolve the contributor artifact. If it is successful, the
     * artifact should be set to resolved, and a file should be passed to it.<br>
     *
     * <b>Note:</b><i>run before maven artifact resolver.</i>
     * @param artifact the artifact to be resolved.
     * @param additionalDependenciesCollector additional dependencies of resolved artifact.
     */
    void tryResolve(Artifact artifact, Set<Artifact> additionalDependenciesCollector ) throws ArtifactNotFoundException;
    
    /**
     * Contribute with additional dependencies for resolved artifact.<br>
     * 
     * <b>Note:</b><i>run after maven artifact resolver.</i>
     * @param artifact resolved artifact.
     * @param localRepository maven local repository.
     * @param remoteRepositories maven remote repositories.
     * @param additionalDependenciesCollector additional dependencies of resolved artifact.
     * @throws ArtifactNotFoundException
     */
    void contribute(Artifact artifact, ArtifactRepository localRepository,
            List remoteRepositories, Set<Artifact> additionalDependenciesCollector) throws ArtifactNotFoundException;
}
