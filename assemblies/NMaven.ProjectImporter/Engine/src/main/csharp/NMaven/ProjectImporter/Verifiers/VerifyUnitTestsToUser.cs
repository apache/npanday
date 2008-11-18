using System;
using System.Collections.Generic;
using System.Text;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.ProjectImporter.Validator;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Verifiers
{
    public class VerifyUnitTestsToUser
    {
        public static void VerifyTests(ref ProjectDigest[] projectDigests, ProjectStructureType structureType, string solutionFile, ref string groupId, ref string artifactId, ref string version)
        {
            VerifyUnitTestsForm verifyForm = new VerifyUnitTestsForm(projectDigests);
            verifyForm.ShowDialog();
        }
    }
}
