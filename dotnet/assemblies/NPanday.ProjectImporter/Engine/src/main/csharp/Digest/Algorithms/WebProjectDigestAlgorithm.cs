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
using System.IO;


using Microsoft.Build.BuildEngine;

using NPanday.ProjectImporter.Parser;
using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Parser.VisualStudioProjectTypes;
using NPanday.Utils;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Algorithms
{
    public class WebProjectDigestAlgorithm : BaseProjectDigestAlgorithm, IProjectDigestAlgorithm
    {
        public ProjectDigest DigestProject(Dictionary<string, object> projectMap, DependencySearchConfiguration depSearchConfig)
        {
            ProjectDigest projectDigest = new ProjectDigest();
            projectDigest.ProjectType = (VisualStudioProjectTypeEnum)projectMap["ProjectType"];
            projectDigest.FullFileName = projectMap["ProjectFullPath"].ToString();
            projectDigest.FullDirectoryName = projectDigest.FullFileName;
            if (projectMap.ContainsKey("TargetFramework"))
                projectDigest.TargetFramework = projectMap["TargetFramework"].ToString();
            if (projectMap.ContainsKey("Configuration"))
                projectDigest.Configuration = projectMap["Configuration"].ToString();
            projectDigest.DependencySearchConfig = depSearchConfig;

            FileInfo existingPomFile = new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml"));
            if(existingPomFile.Exists)
            {
                projectDigest.ExistingPom = PomHelperUtility.ReadPomAsModel(existingPomFile);
            }


            // get Assembly name
            if (projectMap.ContainsKey("Release.AspNetCompiler.VirtualPath"))
            {
                projectDigest.Name = projectMap["Release.AspNetCompiler.VirtualPath"].ToString()
                    .Replace(@"/", "")
                    .Replace(@"\\", "");
            }
            else if (projectMap.ContainsKey("Debug.AspNetCompiler.VirtualPath"))
            {
                projectDigest.Name = projectMap["Debug.AspNetCompiler.VirtualPath"].ToString()
                    .Replace(@"/", "")
                    .Replace(@"\\", "");
            }
            else if(projectMap.ContainsKey("ProjectFullPath"))
            {
                projectDigest.Name = new DirectoryInfo(projectMap["ProjectFullPath"].ToString()).Name;
            }




            // InterProjectReferences
            List<ProjectReference> prjRefList = new List<ProjectReference>();
            if (projectMap.ContainsKey("InterProjectReferences"))
            {
                
                foreach (Project var in (Project[])projectMap["InterProjectReferences"])
                {
                    ProjectReference prjRef = new ProjectReference(projectMap["ProjectFullPath"].ToString());
                    prjRef.Name = GetProjectAssemblyName(Path.GetFullPath(var.FullFileName));
                    prjRef.ProjectPath = Path.GetFullPath(var.FullFileName);

                    prjRefList.Add(prjRef);
                }
            }

            projectDigest.ProjectReferences = prjRefList.ToArray();


            //WebConfigAssemblies
            List<Reference> webConfigRefList = new List<Reference>();
            //if (projectMap.ContainsKey("WebConfigAssemblies"))
            //{
            //    foreach (string var in (string[]) projectMap["WebConfigAssemblies"])
            //    {
            //        Reference reference = new Reference(projectMap["ProjectFullPath"].ToString(), gac);
            //        reference.AssemblyInfo = var;

            //        webConfigRefList.Add(reference);
            //    }

            //}

			//WebReferenceURL
            //if (projectMap.ContainsKey("WebReferencesUrl"))
            //{
            //    List<WebReferenceUrl> webReferenceUrls = new List<WebReferenceUrl>();
            //    if (projectDigest.WebReferenceUrls != null && projectDigest.WebReferenceUrls.Length > 0)
            //    {
            //        webReferenceUrls.AddRange(projectDigest.WebReferenceUrls);
            //    }
            //    foreach (WebReferenceUrl webReferenceUrl in (WebReferenceUrl[])projectMap["WebReferencesUrl"])
            //    {
            //        if (webReferenceUrl != null && !string.IsNullOrEmpty(webReferenceUrl.RelPath) && !string.IsNullOrEmpty(webReferenceUrl.UpdateFromURL))
            //            webReferenceUrls.Add(webReferenceUrl);
            //    }
            //    projectDigest.WebReferenceUrls = webReferenceUrls.ToArray();
            //}


            //BinAssemblies
            List<Reference> binRefList = new List<Reference>();
            //if (projectMap.ContainsKey("BinAssemblies"))
            //{
            //    foreach (string var in (string[])projectMap["BinAssemblies"])
            //    {
            //        // exclude if its already in the webconfig
                    
            //        Reference reference = new Reference(projectMap["ProjectFullPath"].ToString(), gac);
            //        reference.HintPath = var;

            //        // check if its not in project-reference or webconfig-assemblies references
            //        if (!ReferenceInReferenceList(reference, webConfigRefList) && !ReferenceInProjectReferenceList(reference, prjRefList))
            //        {
            //            binRefList.Add(reference);
            //        }
            //    }

            //}

            // combine both web and bin assemblies
            List<Reference> referenceList = new List<Reference>();
            //referenceList.AddRange(webConfigRefList);
            //referenceList.AddRange(binRefList);

            projectDigest.References = referenceList.ToArray();


            return projectDigest;
        }

        private bool ReferenceInReferenceList(Reference reference, List<Reference> list)
        {
            foreach (Reference var in list)
            {
                if (reference.Name.Equals(var.Name, StringComparison.OrdinalIgnoreCase))
                {
                    return true;
                }
            }


            return false;
        }



        private bool ReferenceInProjectReferenceList(Reference reference, List<ProjectReference> list)
        {
            foreach (ProjectReference var in list)
            {
                if (reference.Name.Equals(var.Name, StringComparison.OrdinalIgnoreCase))
                {
                    return true;
                }
            }


            return false;
        }
    }
}
