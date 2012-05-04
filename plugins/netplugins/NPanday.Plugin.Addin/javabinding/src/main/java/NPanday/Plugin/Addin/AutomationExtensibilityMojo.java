package NPanday.Plugin.Addin;

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

import npanday.plugin.FieldAnnotation;
import org.apache.maven.artifact.factory.ArtifactFactory;

/**
 * @phase package
 * @goal package
 */
public class AutomationExtensibilityMojo
    extends npanday.plugin.AbstractMojo
{
       /**
        * @parameter expression = "${settings.localRepository}"
        */
        @FieldAnnotation()
        public java.lang.String repository;

       /**
        * @parameter expression = "${project}"
        */
        @FieldAnnotation()
        public org.apache.maven.project.MavenProject mavenProject;

       /**
        * @parameter expression = "${project}"
        */
        private org.apache.maven.project.MavenProject project;

       /**
        * @parameter expression = "${settings.localRepository}"
        */
        private String localRepository;

       /**
        * @parameter expression = "${vendor}"
        */
        private String vendor;

       /**
        * @parameter expression = "${vendorVersion}"
        */
        private String vendorVersion;

       /**
        * @parameter expression = "${frameworkVersion}"
        */
        private String frameworkVersion;

       /**
        * @component
        */
        private npanday.executable.NetExecutableFactory netExecutableFactory;

       /**
        * @component
        */
        private npanday.plugin.PluginContext pluginContext;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    public String getMojoArtifactId()
        {
            return "NPanday.Plugin.Addin";
        }

        public String getMojoGroupId()
        {
            return "org.apache.npanday.plugins";
        }

        public String getClassName()
        {
            return "NPanday.Plugin.Addin.AutomationExtensibilityMojo";
        }

        public npanday.plugin.PluginContext getNetPluginContext()
        {
            return pluginContext;
        }

        public npanday.executable.NetExecutableFactory getNetExecutableFactory()
        {
            return netExecutableFactory;
        }

        public org.apache.maven.project.MavenProject getMavenProject()
        {
            return project;
        }

        public String getLocalRepository()
        {
            return localRepository;
        }

        public String getVendorVersion()
        {
            return vendorVersion;
        }

        public String getVendor()
        {
            return vendor;
        }

        public String getFrameworkVersion()
        {
            return frameworkVersion;
        }

    public ArtifactFactory getArtifactFactory()
    {
        return artifactFactory;
    }
}
