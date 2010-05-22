#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion

using System;
using System.IO;
using System.Collections.Generic;
using System.Text;

using NPanday.Model.Setting;

namespace NPanday.Artifact
{
    public sealed class ArtifactContext : IArtifactContext
    {

        public ArtifactRepository GetArtifactRepository()
        {
            ArtifactRepository artifactRepository = new ArtifactRepository();
            artifactRepository.Init(this, new FileInfo(SettingsUtil.GetLocalRepositoryPath()).Directory);
            return artifactRepository;
        }

        public Artifact GetArtifactFor(NPanday.Model.Pom.Model model)
        {
            Artifact artifact = new Artifact();
            artifact.ArtifactId = model.artifactId;
            artifact.GroupId = model.groupId;
            artifact.Version = model.version;
            artifact.Extension = GetExtensionFor(model.packaging);
            return artifact;
        }

        public Artifact CreateArtifact(String groupId, String artifactId, String version, String packaging)
        {
            Artifact artifact = new Artifact();
            artifact.ArtifactId = artifactId;
            artifact.GroupId = groupId;
            artifact.Version = version;
            artifact.Extension = GetExtensionFor(packaging);
            return artifact;
        }

        public String GetExtensionFor(String packaging)
        {
            if (packaging.Equals("dotnet-library") || packaging.Equals("library")
                || packaging.Equals("dotnet-maven-plugin") || packaging.Equals("netplugin")
                || packaging.Equals("visual-studio-addin") || packaging.Equals("sharp-develop-addin"))
            {
                return "dll";
            }
            else if(packaging.Equals("dotnet-executable")
                    ||  packaging.Equals("winexe") ||  packaging.Equals("exe"))
            {
                return "exe";
            }
            else if (packaging.Equals("dotnet-module") || packaging.Equals("module"))
            {
                return "netmodule";
            }
            return null;
        }
    }
}
