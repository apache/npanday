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

            refMngr.CopyArtifact(artifact);
        }

        [Test]
        public void downloadArtifact()
        {
            string user = System.Security.Principal.WindowsIdentity.GetCurrent().Name.ToString();
            Assert.IsNotNull(new FileInfo(string.Format("C:\\Documents and Settings\\{0}\\.m2\\uac\\gac_msil\\npanday.artifact\\1.1-SNAPSHOT__npanday.artifact\\NPanday.Artifact.dll", user)));
        }

    }
}
