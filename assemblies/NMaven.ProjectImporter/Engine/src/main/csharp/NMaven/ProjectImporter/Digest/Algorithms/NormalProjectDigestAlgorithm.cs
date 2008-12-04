using System;
using System.Collections.Generic;
using System.IO;
using Microsoft.Build.BuildEngine;
using NMaven.ProjectImporter.Parser.VisualStudioProjectTypes;
using NMaven.ProjectImporter.Digest.Model;

using NMaven.Utils;



/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Digest.Algorithms 
{
    public class NormalProjectDigestAlgorithm : IProjectDigestAlgorithm
    {

        public ProjectDigest DigestProject(Dictionary<string, object> projectMap)
        {


            Project project = (Project)projectMap["Project"];


            if (!projectMap.ContainsKey("ProjectType"))
            {
                if (project.FullFileName.ToUpper().EndsWith(".CSPROJ"))
                {
                    projectMap.Add("ProjectType",VisualStudioProjectTypeEnum.Windows__CSharp);
                }
                else if (project.FullFileName.ToUpper().EndsWith(".VBPROJ"))
                {
                    projectMap.Add("ProjectType", VisualStudioProjectTypeEnum.Windows__VbDotNet);
                }
            }

            GacUtility gac = new GacUtility();
            

            ProjectDigest projectDigest = new ProjectDigest();
            string projectBasePath = Path.GetDirectoryName(project.FullFileName);
            projectDigest.ProjectType = (VisualStudioProjectTypeEnum)projectMap["ProjectType"];
            projectDigest.FullFileName = project.FullFileName;
            projectDigest.FullDirectoryName = Path.GetDirectoryName(project.FullFileName);

            FileInfo existingPomFile = new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml"));
            if (existingPomFile.Exists)
            {
                projectDigest.ExistingPom = NMavenPomHelperUtility.ReadPomAsModel(existingPomFile);
            }

            if ((projectDigest.ProjectType & VisualStudioProjectTypeEnum.Windows__CSharp) == VisualStudioProjectTypeEnum.Windows__CSharp)
            {
                projectDigest.Language = "csharp";
            }
            else if ((projectDigest.ProjectType & VisualStudioProjectTypeEnum.Windows__VbDotNet) == VisualStudioProjectTypeEnum.Windows__VbDotNet)
            {
                projectDigest.Language = "vb";
            }


            List<Reference> references = new List<Reference>();
            
            List<ProjectReference> projectReferences = new List<ProjectReference>();
            if (projectMap.ContainsKey("InterProjectReferences")
                && projectMap["InterProjectReferences"] != null
                && projectMap["InterProjectReferences"] is Microsoft.Build.BuildEngine.Project[]
                )
            {
                Microsoft.Build.BuildEngine.Project[] interProjectReferences = (Microsoft.Build.BuildEngine.Project[])projectMap["InterProjectReferences"];

                foreach (Microsoft.Build.BuildEngine.Project p in interProjectReferences)
                {
                    ProjectReference prjRef = new ProjectReference(projectBasePath);
                    prjRef.ProjectPath = p.FullFileName;
                    prjRef.Name = GetProjectAssemblyName(Path.GetFullPath(prjRef.ProjectFullPath));
                    projectReferences.Add(prjRef);

                }

            }

            List<Compile> compiles = new List<Compile>();
            List<None> nones = new List<None>();
            List<WebReferenceUrl> webReferenceUrls = new List<WebReferenceUrl>();
            List<Content> contents = new List<Content>();
            List<WebReferences> webReferencesList = new List<WebReferences>();
            List<EmbeddedResource> embeddedResources = new List<EmbeddedResource>();
            List<BootstrapperPackage> bootstrapperPackages = new List<BootstrapperPackage>();
            List<Folder> folders = new List<Folder>();
            List<string> globalNamespaceImports = new List<string>();
			List<ComReference> comReferenceList = new List<ComReference>();

            DigestBuildProperties(project, projectDigest);
            DigestBuildItems(project, projectDigest, projectBasePath, projectReferences, gac, references, compiles, nones, webReferenceUrls, contents, folders, webReferencesList, embeddedResources, bootstrapperPackages, globalNamespaceImports, comReferenceList);
            DigestImports(project);

            projectDigest.ProjectReferences = projectReferences.ToArray();
            projectDigest.References = references.ToArray();
            projectDigest.Compiles = compiles.ToArray();
            projectDigest.Contents = contents.ToArray();
            projectDigest.Nones = nones.ToArray();
            projectDigest.WebReferenceUrls = webReferenceUrls.ToArray();
            projectDigest.WebReferences = webReferencesList.ToArray();
            projectDigest.EmbeddedResources = embeddedResources.ToArray();
            projectDigest.BootstrapperPackages = bootstrapperPackages.ToArray();
            projectDigest.Folders = folders.ToArray();
            projectDigest.GlobalNamespaceImports = globalNamespaceImports.ToArray();
			projectDigest.ComReferenceList = comReferenceList.ToArray();


            return projectDigest;


        }

        private static void DigestImports(Project project)
        {
            foreach (Import import in project.Imports)
            {
                if (!import.IsImported)
                {
                    if (
                        @"$(MSBuildBinPath)\Microsoft.CSharp.targets".Equals(import.ProjectPath, StringComparison.OrdinalIgnoreCase)
                        || @"$(MSBuildBinPath)\Microsoft.VisualBasic.targets".Equals(import.ProjectPath, StringComparison.OrdinalIgnoreCase)
                        )
                    {
                        // Ignore these this are not used by the compiler
                    }
                    else
                    {
                        // TODO: check for implimentations for imports
                    }

                }

            }
        }

        private static void DigestBuildItems(Project project, ProjectDigest projectDigest, string projectBasePath, ICollection<ProjectReference> projectReferences, GacUtility gac, ICollection<Reference> references, ICollection<Compile> compiles, ICollection<None> nones, ICollection<WebReferenceUrl> webReferenceUrls, ICollection<Content> contents, ICollection<Folder> folders, ICollection<WebReferences> webReferencesList, ICollection<EmbeddedResource> embeddedResources, ICollection<BootstrapperPackage> bootstrapperPackages, ICollection<string> globalNamespaceImports, IList<ComReference> comReferenceList)
        {
            foreach (BuildItemGroup buildItemGroup in project.ItemGroups)
            {
                foreach (BuildItem buildItem in buildItemGroup)
                {
                    if (!buildItem.IsImported)
                    {
                        if ("ProjectReference".Equals(buildItem.Name))
                        {
                            ProjectReference prjRef = new ProjectReference(projectBasePath);


                            prjRef.ProjectPath = buildItem.Include;
                            prjRef.Name = GetProjectAssemblyName(Path.GetFullPath(prjRef.ProjectFullPath));
                            projectReferences.Add(prjRef);
                        }
                        else if ("Reference".Equals(buildItem.Name))
                        {
                            Reference reference = new Reference(projectBasePath, gac);
                            string hintPath = buildItem.GetMetadata("HintPath");
                            if (!string.IsNullOrEmpty(hintPath))
                                reference.HintPath = Path.GetFullPath(Path.Combine(projectBasePath, hintPath)); 
                            if (string.IsNullOrEmpty(reference.HintPath) || !(new FileInfo(reference.HintPath).Exists))
                            {
                                reference.AssemblyInfo = buildItem.Include;
                            }

                            if("NUnit.Framework".Equals(reference.Name, StringComparison.OrdinalIgnoreCase))
                            {
                                reference.Name = "NUnit.Framework";
                                projectDigest.UnitTest = true;
                            }

                            if (!string.IsNullOrEmpty(reference.Name))
                            {
                                references.Add(reference);
                            }
                        }
                        else if ("Compile".Equals(buildItem.Name))
                        {
                            Compile compile = new Compile(projectBasePath);
                            compile.IncludePath = buildItem.Include;

                            compile.AutoGen = buildItem.GetMetadata("AutoGen");
                            compile.DesignTimeSharedInput = buildItem.GetMetadata("DesignTimeSharedInput");
                            compile.DependentUpon = buildItem.GetMetadata("DependentUpon");
                            compile.DesignTime = buildItem.GetMetadata("DesignTime");
                            compile.SubType = buildItem.GetMetadata("SubType");

                           


                            compiles.Add(compile);
                        }

                        else if ("None".Equals(buildItem.Name))
                        {
                            None none = new None(projectBasePath);
                            none.IncludePath = buildItem.Include;


                            none.Link = buildItem.GetMetadata("Link");

                            none.Generator = buildItem.GetMetadata("Generator");
                            none.LastGenOutput = buildItem.GetMetadata("LastGenOutput");
                            none.DependentUpon = buildItem.GetMetadata("DependentUpon");

                            nones.Add(none);
                        }
                        else if ("WebReferenceUrl".Equals(buildItem.Name))
                        {
                            WebReferenceUrl web = new WebReferenceUrl();
                            web.UrlBehavior = buildItem.GetMetadata("UrlBehavior");
                            web.RelPath = buildItem.GetMetadata("RelPath");
                            web.UpdateFromURL = buildItem.GetMetadata("UpdateFromURL");
                            web.ServiceLocationURL = buildItem.GetMetadata("ServiceLocationURL");
                            web.CachedDynamicPropName = buildItem.GetMetadata("CachedDynamicPropName");
                            web.CachedAppSettingsObjectName = buildItem.GetMetadata("CachedAppSettingsObjectName");
                            web.CachedSettingsPropName = buildItem.GetMetadata("CachedSettingsPropName");
                            webReferenceUrls.Add(web);
                        }
                        else if ("COMReference".Equals(buildItem.Name))
                        {
                            ComReference comRef = new ComReference();
                            comRef.Include = buildItem.Include;
                            comRef.Guid = buildItem.GetMetadata("Guid");
                            comRef.VersionMajor = buildItem.GetMetadata("VersionMajor");
                            comRef.VersionMinor = buildItem.GetMetadata("VersionMinor");
                            comRef.Lcid = buildItem.GetMetadata("Lcid");
                            comRef.Isolated = buildItem.GetMetadata("Isolated");
                            comRef.WrapperTool = buildItem.GetMetadata("WrapperTool");
                            comReferenceList.Add(comRef);
                        }
                        else if ("Content".Equals(buildItem.Name))
                        {
                            Content content = new Content(projectBasePath);
                            content.IncludePath = buildItem.Include;
                            contents.Add(content);
                        }
                        else if ("Folder".Equals(buildItem.Name))
                        {
                            Folder folder = new Folder(projectBasePath);
                            folder.IncludePath = buildItem.Include;
                            folders.Add(folder);
                        }
                        else if ("WebReferences".Equals(buildItem.Name))
                        {
                            WebReferences webReferences = new WebReferences(projectBasePath);
                            webReferences.IncludePath = buildItem.Include;
                            webReferencesList.Add(webReferences);
                        }
                        else if ("EmbeddedResource".Equals(buildItem.Name))
                        {
                            EmbeddedResource embeddedResource = new EmbeddedResource(projectBasePath);
                            embeddedResource.IncludePath = buildItem.Include;
                            
                            embeddedResource.DependentUpon = buildItem.GetMetadata("DependentUpon");
                            embeddedResource.SubType = buildItem.GetMetadata("SubType");
                            embeddedResource.Generator = buildItem.GetMetadata("Generator");
                            embeddedResource.LastGenOutput = buildItem.GetMetadata("LastGenOutput");


                            embeddedResources.Add(embeddedResource);
                        }
                        else if ("BootstrapperPackage".Equals(buildItem.Name))
                        {
                            BootstrapperPackage bootstrapperPackage = new BootstrapperPackage(projectBasePath);
                            bootstrapperPackage.IncludePath = buildItem.Include;
                            bootstrapperPackage.Visible = buildItem.GetMetadata("Visible");
                            bootstrapperPackage.ProductName = buildItem.GetMetadata("ProductName");
                            bootstrapperPackage.Install = buildItem.GetMetadata("Install");


                            bootstrapperPackages.Add(bootstrapperPackage);
                        }
                        else if ("Import".Equals(buildItem.Name))
                        {
                            globalNamespaceImports.Add(buildItem.Include);
                        }
                        else if ("BaseApplicationManifest".Equals(buildItem.Name))
                        {
                            projectDigest.BaseApplicationManifest = buildItem.Include;
                        }
                        else
                        {
                            Console.WriteLine("Unhandled ItemGroup: " + buildItem.Name);
                        }

                    }
                }
            }
        }

        private static void DigestBuildProperties(Project project, ProjectDigest projectDigest)
        {
            foreach (BuildPropertyGroup buildPropertyGroup in project.PropertyGroups)
            {
                foreach (BuildProperty buildProperty in buildPropertyGroup)
                {
                    if (!buildProperty.IsImported)
                    {
                        
                        if ("RootNameSpace".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.RootNamespace = buildProperty.Value;
                        }
                        else if ("AssemblyName".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.AssemblyName = buildProperty.Value;
                        }
                        else if ("StartupObject".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.StartupObject = buildProperty.Value;
                        }
                        else if ("OutputType".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.OutputType = buildProperty.Value;
                        }
                        else if ("SignAssembly".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.SignAssembly = buildProperty.Value;
                        }
                        else if ("AssemblyOriginatorKeyFile".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.AssemblyOriginatorKeyFile = buildProperty.Value;
                        }
                        else if ("DelaySign".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DelaySign = buildProperty.Value;
                        }
                        else if ("Optimize".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.Optimize = buildProperty.Value;
                        }
                        else if ("AllowUnsafeBlocks".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.AllowUnsafeBlocks = buildProperty.Value;
                        }
                        else if ("DefineConstants".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DefineConstants = buildProperty.Value;
                        }
                        else if ("ApplicationIcon".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.ApplicationIcon = buildProperty.Value;
                        }
                        else if ("Win32Resource".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.Win32Resource = buildProperty.Value;
                        }
                        else if ("ProjectGuid".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.ProjectGuid = buildProperty.Value;
                        }
                        else if ("Configuration".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.Configuration = buildProperty.Value;
                        }
                        else if ("BaseIntermediateOutputPath".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.BaseIntermediateOutputPath = buildProperty.Value;
                        }
                        else if ("OutputPath".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.OutputPath = buildProperty.Value;
                        }
                        else if ("TreatWarningsAsErrors".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.TreatWarningsAsErrors = buildProperty.Value;
                        }
                        else if ("Platform".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.Platform = buildProperty.Value;
                        }
                        else if ("ProductVersion".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.ProductVersion = buildProperty.Value;
                        }
                        else if ("SchemaVersion".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.SchemaVersion = buildProperty.Value;
                        }
                        else if ("AppDesignerFolder".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.AppDesignerFolder = buildProperty.Value;
                        }
                        else if ("DebugSymbols".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DebugSymbols = buildProperty.Value;
                        }
                        else if ("DebugType".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DebugType = buildProperty.Value;
                        }
                        else if ("ErrorReport".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.ErrorReport = buildProperty.Value;
                        }
                        else if ("WarningLevel".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.WarningLevel = buildProperty.Value;
                        }
                        else if ("DocumentationFile".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DocumentationFile = buildProperty.Value;
                        }
                        else if ("PostBuildEvent".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.PostBuildEvent = buildProperty.Value;
                        }
                        else if ("PublishUrl".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.PublishUrl = buildProperty.Value;
                        }
                        else if ("Install".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.Install = buildProperty.Value;
                        }
                        else if ("InstallFrom".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.InstallFrom = buildProperty.Value;
                        }
                        else if ("UpdateEnabled".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.UpdateEnabled = buildProperty.Value;
                        }
                        else if ("UpdateMode".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.UpdateMode = buildProperty.Value;
                        }
                        else if ("UpdateInterval".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.UpdateInterval = buildProperty.Value;
                        }
                        else if ("UpdateIntervalUnits".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.UpdateIntervalUnits = buildProperty.Value;
                        }
                        else if ("UpdatePeriodically".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.UpdatePeriodically = buildProperty.Value;
                        }
                        else if ("UpdateRequired".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.UpdateRequired = buildProperty.Value;
                        }
                        else if ("MapFileExtensions".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.MapFileExtensions = buildProperty.Value;
                        }
                        else if ("ApplicationVersion".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.ApplicationVersion = buildProperty.Value;
                        }
                        else if ("IsWebBootstrapper".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.IsWebBootstrapper = buildProperty.Value;
                        }
                        else if ("BootstrapperEnabled".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.BootstrapperEnabled = buildProperty.Value;
                        }
                        else if ("PreBuildEvent".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.PreBuildEvent = buildProperty.Value;
                        }
                        else if ("MyType".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.MyType = buildProperty.Value;
                        }
                        else if ("DefineDebug".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DefineDebug = buildProperty.Value;
                        }
                        else if ("DefineTrace".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.DefineTrace = buildProperty.Value;
                        }
                        else if ("NoWarn".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.NoWarn = buildProperty.Value;
                        }
                        else if ("WarningsAsErrors".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            projectDigest.WarningsAsErrors = buildProperty.Value;
                        }
                        else if ("ProjectTypeGuids".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
                        {
                            if (!string.IsNullOrEmpty(buildProperty.Value))
                            {
                                try
                                {
                                    VisualStudioProjectTypeEnum t = VisualStudioProjectType.GetVisualStudioProjectType(buildProperty.Value);
                                    projectDigest.ProjectType = t;
                                }
                                catch
                                {
                                    throw;
                                }
                            }
                        }
                        else
                        {
                            Console.WriteLine("Unhandled Property:" + buildProperty.Name);
                        }

                    }

                }
            }
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
