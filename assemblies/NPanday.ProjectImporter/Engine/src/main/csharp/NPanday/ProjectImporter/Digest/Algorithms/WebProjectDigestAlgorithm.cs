using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


using Microsoft.Build.BuildEngine;

using NPanday.ProjectImporter.Parser;
using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;

using NPanday.Utils;
using NPanday.ProjectImporter.Parser.VisualStudioProjectTypes;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Algorithms
{
    public class WebProjectDigestAlgorithm : IProjectDigestAlgorithm
    {
        public ProjectDigest DigestProject(Dictionary<string, object> projectMap)
        {
            GacUtility gac = new GacUtility();

            ProjectDigest projectDigest = new ProjectDigest();
            projectDigest.ProjectType = (VisualStudioProjectTypeEnum)projectMap["ProjectType"];
            projectDigest.FullFileName = projectMap["ProjectFullPath"].ToString();
            projectDigest.FullDirectoryName = projectDigest.FullFileName;
            projectDigest.TargetFramework = projectMap["TargetFramework"].ToString();

            FileInfo existingPomFile = new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml"));
            if(existingPomFile.Exists)
            {
                projectDigest.ExistingPom = PomHelperUtility.ReadPomAsModel(existingPomFile);
            }


            // get Assembly name
            if (projectMap.ContainsKey("Release.AspNetCompiler.VirtualPath"))
            {
                projectDigest.AssemblyName = projectMap["Release.AspNetCompiler.VirtualPath"].ToString()
                    .Replace(@"/", "")
                    .Replace(@"\\", "");
            }
            else if (projectMap.ContainsKey("Debug.AspNetCompiler.VirtualPath"))
            {
                projectDigest.AssemblyName = projectMap["Debug.AspNetCompiler.VirtualPath"].ToString()
                    .Replace(@"/", "")
                    .Replace(@"\\", "");
            }
            else if(projectMap.ContainsKey("ProjectFullPath"))
            {
                projectDigest.AssemblyName = new DirectoryInfo(projectMap["ProjectFullPath"].ToString()).Name;
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

        public static string GetProjectAssemblyName(string projectFile)
        {
            Project project = ProjectDigester.GetProject(projectFile);

            if (project == null)
            {
                return null;
            }

            foreach (BuildPropertyGroup buildPropertyGroup in project.PropertyGroups)
            {
                foreach (BuildProperty buildProperty in buildPropertyGroup)
                {
                    if (!buildProperty.IsImported && "AssemblyName".Equals(buildProperty.Name))
                    {
                        return buildProperty.Value;
                    }

                }
            }

            return null;
        }


    }
}
