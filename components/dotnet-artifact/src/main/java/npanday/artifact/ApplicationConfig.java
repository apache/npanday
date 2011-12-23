package npanday.artifact;

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

import org.apache.maven.artifact.Artifact;
import npanday.PathUtil;

import java.io.File;

/**
 * Provides information about the location of a .NET executable artifact's exe.config file.
 *
 * @author Shane Isbell
 */
public interface ApplicationConfig
{

    /**
     * Returns the Maven repository path of the exe.config file.
     *
     * @param localRepository the local maven repository
     * @return the Maven repository path of the exe.config file
     */
    File getRepositoryPath( File localRepository );

    /**
     * Returns the source path of the (original) *.exe.config file
     *
     * @return the source path of the (original) *.exe.config file
     */
    File getConfigSourcePath();

    /**
     * Returns the target path of the (copied) *.exe.config file
     *
     * @return the target path of the (copied) *.exe.config file
     */
    File getConfigBuildPath();

    /**
     * Factory class for generating default executable configs.
     */
    public static class Factory
    {
        /**
         * Default constructor
         */
        public Factory()
        {
        }

        /**
         * Creates the the application config for the specified artifact. By default, the config source path for the
         * exe.config is located within the project's src/main/config directory. Neither parameter value may be null.
         *
         * @param artifact              the executable artifact to which the exe.config file is associated
         * @param projectBaseDirectory  the base directory of the build (which contains the pom.xml file)
         * @param projectBuildDirectory the target directory of the build
         * @return the application config for the specified artifact
         */
        public static ApplicationConfig createDefaultApplicationConfig( final Artifact artifact,
                                                                        final File projectBaseDirectory,
                                                                        final File projectBuildDirectory )
        {
            return new ApplicationConfig()
            {

                public File getRepositoryPath( File localRepository )
                {
                    File basedir = PathUtil.getMavenLocalRepositoryFileFor( artifact, localRepository ).getParentFile();
                    StringBuffer buffer = new StringBuffer();
                    buffer.append( artifact.getArtifactId() ).append( "-" ).append( artifact.getVersion() );
                    if ( artifact.getClassifier() != null )
                    {
                        buffer.append( "-" ).append( artifact.getClassifier() );
                    }
                    buffer.append( ".exe.config" );

                    return new File( basedir, buffer.toString() );
                }

                public File getConfigSourcePath()
                {
                    return new File( projectBaseDirectory,
                                     "/src/main/config/" + artifact.getArtifactId() + ".exe.config" );
                }

                public File getConfigBuildPath()
                {
                    return new File( projectBuildDirectory, artifact.getArtifactId() + ".exe.config" );
                }
            };
        }

    }
}
