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
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using NPanday.VisualStudio.Addin;
using NPanday.Artifact;
using System.IO;

namespace ClassLibrary1
{
    [TestFixture]
    public class ResyncArtifactSnapshot
    {
        public void artifactSetUp()
        {
            ReferenceManager refMngr = new ReferenceManager();
            Artifact artifact = new Artifact();

            artifact.GroupId = "npanday.artifact";
            artifact.Version = "1.1-SNAPSHOT";
            artifact.ArtifactId = "NPanday.Artifact";
            artifact.Extension = "dll";

            refMngr.CopyArtifact(artifact,null);
        }

        [Test]
        public void downloadArtifact()
        {
            string user = System.Security.Principal.WindowsIdentity.GetCurrent().Name.ToString();
            Assert.IsNotNull(new FileInfo(string.Format("C:\\Documents and Settings\\{0}\\.m2\\uac\\gac_msil\\npanday.artifact\\1.1-SNAPSHOT__npanday.artifact\\NPanday.Artifact.dll", user)));
        }

    }
}
