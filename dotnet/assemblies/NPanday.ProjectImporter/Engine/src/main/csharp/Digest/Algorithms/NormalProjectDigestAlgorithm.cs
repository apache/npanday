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
using System.IO;
using System.Reflection;
using log4net;
using Microsoft.Build.BuildEngine;
using NPanday.Artifact;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Parser.VisualStudioProjectTypes;
using NPanday.Utils;
using System.Text.RegularExpressions;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using NPanday.ProjectImporter.Utils;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Algorithms 
{
    public class NormalProjectDigestAlgorithm : BaseProjectDigestAlgorithm, IProjectDigestAlgorithm
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(NormalProjectDigestAlgorithm));

        public ProjectDigest DigestProject(Dictionary<string, object> projectMap, DependencySearchConfiguration depSearchConfig)
        {
            Project project = (Project)projectMap["Project"];

            log.DebugFormat("Digesting project {0}", project.FullFileName);

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

            ProjectDigest projectDigest = new ProjectDigest();
            string projectBasePath = Path.GetDirectoryName(project.FullFileName);
            projectDigest.ProjectType = (VisualStudioProjectTypeEnum)projectMap["ProjectType"];
            projectDigest.FullFileName = project.FullFileName;
            projectDigest.FullDirectoryName = Path.GetDirectoryName(project.FullFileName);
            if (projectMap.ContainsKey("Configuration"))
                projectDigest.Configuration = projectMap["Configuration"].ToString();

            projectDigest.DependencySearchConfig = depSearchConfig;

            FileInfo existingPomFile = new FileInfo(Path.Combine(projectDigest.FullDirectoryName, "pom.xml"));
            if (existingPomFile.Exists)
            {
                projectDigest.ExistingPom = PomHelperUtility.ReadPomAsModel(existingPomFile);
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
            DigestBuildItems(project, projectDigest, projectBasePath, projectReferences, references, compiles, nones, webReferenceUrls, contents, folders, webReferencesList, embeddedResources, bootstrapperPackages, globalNamespaceImports, comReferenceList);
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
                    else if (
                    @"$(MSBuildToolsPath)\Microsoft.CSharp.targets".Equals(import.ProjectPath, StringComparison.OrdinalIgnoreCase)
                    || @"$(MSBuildToolsPath)\Microsoft.VisualBasic.targets".Equals(import.ProjectPath, StringComparison.OrdinalIgnoreCase)
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

        // TODO: belongs in another utility class
        private static void SetReferenceFromFile(FileInfo dll, Reference reference)
        {
            Assembly asm = null;
            string path = string.Empty;

            //if (dll.Exists)
            if (dll.Exists)
            {
                //asm = Assembly.ReflectionOnlyLoadFrom(dll.FullName);
                path = dll.FullName;
            }
            else
            {
                ArtifactContext artifactContext = new ArtifactContext();
                Artifact.Artifact a = artifactContext.GetArtifactRepository().GetArtifact(dll);

                if (a != null)
                {
                    if (!a.FileInfo.Exists)
                    {
                        if (!a.FileInfo.Directory.Exists)
                            a.FileInfo.Directory.Create();

                        string localRepoPath = artifactContext.GetArtifactRepository().GetLocalRepositoryPath(a, dll.Extension);
                        if (File.Exists(localRepoPath))
                        {
                            File.Copy(localRepoPath, a.FileInfo.FullName);
                            //asm = Assembly.ReflectionOnlyLoadFrom();
                            path = a.FileInfo.FullName;
                        }
                        else
                        {
                            if (Reference.downloadArtifactFromRemoteRepository(a, dll.Extension))
                            {
                                //asm = Assembly.ReflectionOnlyLoadFrom(a.FileInfo.FullName);
                                path = a.FileInfo.FullName;
                            }
                            else
                            {
                                path = reference.getBinReference(dll.Name);
                                if (!string.IsNullOrEmpty(path))
                                {
                                    File.Copy(path, a.FileInfo.FullName);
                                }
                            }
                            //copy assembly to repo if not found.
                            if (!string.IsNullOrEmpty(path) && !File.Exists(localRepoPath))
                            {
                                if (!Directory.Exists(Path.GetDirectoryName(localRepoPath)))
                                    Directory.CreateDirectory(Path.GetDirectoryName(localRepoPath));

                                File.Copy(path, localRepoPath);
                            }
                        }
                    }
                    else
                    {
                        path = a.FileInfo.FullName;
                    }
                }
                if (a == null || string.IsNullOrEmpty(path))
                {
                    log.Warn("Cannot find or download the artifact " + dll.Name + ",  project may not build properly.");
                    return;
                }
            }

            bool asmNotLoaded = true;
            foreach (Assembly asmm in AppDomain.CurrentDomain.ReflectionOnlyGetAssemblies())
            {
                // compare the assembly name to the filename of the reference to determine if it is a match
                // as the location might not be set
                // TODO: why do we need to load the assembly?
                // added StringComparison.OrdinalIgnoreCase to assembly name compratison in order to avoid errors with 
                // already loaded assemblies like nunit.framework and NUnit.Framework etc (note this can be reconsidered)
                if (asmm.GetName().Name.Equals(Path.GetFileNameWithoutExtension(path), StringComparison.OrdinalIgnoreCase))
                {
                    asm = asmm;
                    asmNotLoaded = false;
                    break;
                }
            }
            if (asmNotLoaded)
            {
                asm = Assembly.ReflectionOnlyLoadFrom(path);
            }

            reference.SetAssemblyInfoValues(asm.ToString());
            //asm = null;
        }

        private static void DigestBuildItems(Project project, ProjectDigest projectDigest, string projectBasePath, ICollection<ProjectReference> projectReferences, ICollection<Reference> references, ICollection<Compile> compiles, ICollection<None> nones, ICollection<WebReferenceUrl> webReferenceUrls, ICollection<Content> contents, ICollection<Folder> folders, ICollection<WebReferences> webReferencesList, ICollection<EmbeddedResource> embeddedResources, ICollection<BootstrapperPackage> bootstrapperPackages, ICollection<string> globalNamespaceImports, IList<ComReference> comReferenceList)
        {
            string targetFramework = projectDigest.TargetFramework != null ? projectDigest.TargetFramework.Substring(0,3) : "2.0";
            RspUtility rsp = new RspUtility();
            foreach (BuildItemGroup buildItemGroup in project.ItemGroups)
            {
                foreach (BuildItem buildItem in buildItemGroup)
                {
                    if (!buildItem.IsImported)
                    {
                        
                        switch (buildItem.Name)
                        {
                            case "ProjectReference":
                                ProjectReference prjRef = new ProjectReference(projectBasePath);
                                prjRef.ProjectPath = buildItem.Include;
                                prjRef.Name = GetProjectAssemblyName(Path.GetFullPath(prjRef.ProjectFullPath));
                                prjRef.RoleType = buildItem.GetMetadata("RoleType");
                                projectReferences.Add(prjRef);
                                break;
                            case "Reference":
                                // TODO: significant refactoring needed here - it should be calling the same resolution code that is in
                                //   AbstractPomConverter to find the right artifact based on the simple name

                                Reference reference = new Reference(projectBasePath);
                                //set processorArchitecture property to platform, it will be used by GacUtility in 
                                // order to resolve artifact to right processor architecture
                                if (!string.IsNullOrEmpty(projectDigest.Platform))
                                {
                                    reference.ProcessorArchitecture = projectDigest.Platform;
                                }
                                string hintPath = buildItem.GetMetadata("HintPath");
                                if (!string.IsNullOrEmpty(hintPath))
                                {
                                    string fullHint = Path.Combine(projectBasePath, hintPath);
                                    if(File.Exists(fullHint))
                                        reference.HintPath = Path.GetFullPath(fullHint);
                                    else
                                        reference.HintPath = fullHint;
                                    SetReferenceFromFile(new FileInfo(fullHint), reference);
                                }
                                if (string.IsNullOrEmpty(reference.HintPath) || !(new FileInfo(reference.HintPath).Exists))
                                {
                                    if (buildItem.Include.Contains(","))
                                    {
                                        // complete name
                                        reference.SetAssemblyInfoValues(buildItem.Include);
                                    }
                                    else if (!rsp.IsRspIncluded(buildItem.Include,projectDigest.Language) && projectDigest.DependencySearchConfig.SearchGac && projectDigest.TargetFrameworkIdentifier != "Silverlight")
                                    {
                                        // simple name needs to be resolved
                                        List<string> refs = GacUtility.GetInstance().GetAssemblyInfo(buildItem.Include, null, null);
                                        if (refs.Count == 0)
                                        {
                                            log.Warn("Unable to find reference '" + buildItem.Include + "' in " + string.Join("; ", refs.ToArray()));
                                        }
                                        else if (refs.Count > 1)
                                        {
                                            string best = null;
                                            string bestFramework = "0.0";
                                            foreach (string s in refs)
                                            {
                                                try
                                                {
                                                    Assembly a = Assembly.ReflectionOnlyLoad(s);
                                                    string framework = a.ImageRuntimeVersion.Substring(1, 3);
                                                    if (framework.CompareTo(targetFramework) <= 0 && framework.CompareTo(bestFramework) > 0)
                                                    {
                                                        best = s;
                                                        bestFramework = framework;
                                                    }
                                                }
                                                catch (Exception e)
                                                {
                                                    // skip this assembly
                                                    log.Error("An error occurred loading assembly '" + s + "' - check that your PATH to gacutil matches your runtime environment: " + e.Message);
                                                }
                                            }
                                            reference.SetAssemblyInfoValues(best);
                                        }
                                        else 
                                        {
                                            reference.SetAssemblyInfoValues(refs[0]);
                                        }
                                    }
                                }
                                if ("NUnit.Framework".Equals(reference.Name, StringComparison.OrdinalIgnoreCase))
                                {
                                    reference.Name = "NUnit.Framework";
                                    projectDigest.UnitTest = true;
                                }
                                if (!string.IsNullOrEmpty(reference.Name))
                                {
                                    references.Add(reference);
                                }
                                break;
                            case "Compile":
                                Compile compile = new Compile(projectBasePath);
                                compile.IncludePath = buildItem.Include;
                                compile.AutoGen = buildItem.GetMetadata("AutoGen");
                                compile.DesignTimeSharedInput = buildItem.GetMetadata("DesignTimeSharedInput");
                                compile.DependentUpon = buildItem.GetMetadata("DependentUpon");
                                compile.DesignTime = buildItem.GetMetadata("DesignTime");
                                compile.SubType = buildItem.GetMetadata("SubType");
                                compiles.Add(compile);
                                break;
                            case "None":
                                None none = new None(projectBasePath);
                                none.IncludePath = buildItem.Include;

                                none.Link = buildItem.GetMetadata("Link");

                                none.Generator = buildItem.GetMetadata("Generator");
                                none.LastGenOutput = buildItem.GetMetadata("LastGenOutput");
                                none.DependentUpon = buildItem.GetMetadata("DependentUpon");

                                nones.Add(none);
                               
                                //add included web reference when reimporting
                                if (buildItem.Include.Contains(".wsdl"))
                                {
                                    string path = Path.GetDirectoryName(buildItem.Include) + "\\";

                                    WebReferenceUrl webUrl = new WebReferenceUrl();
                                    webUrl.UrlBehavior = "Dynamic";
                                    webUrl.RelPath = path;
                                    
                                    if (!webRefExists(webUrl, webReferenceUrls))
                                    {
                                        webReferenceUrls.Add(webUrl);
                                    }

                                }
                                break;
                            case "WebReferenceUrl":
                                WebReferenceUrl web = new WebReferenceUrl();
                                web.UrlBehavior = buildItem.GetMetadata("UrlBehavior");
                                web.RelPath = buildItem.GetMetadata("RelPath");
                                web.UpdateFromURL = buildItem.GetMetadata("UpdateFromURL");
                                web.ServiceLocationURL = buildItem.GetMetadata("ServiceLocationURL");
                                web.CachedDynamicPropName = buildItem.GetMetadata("CachedDynamicPropName");
                                web.CachedAppSettingsObjectName = buildItem.GetMetadata("CachedAppSettingsObjectName");
                                web.CachedSettingsPropName = buildItem.GetMetadata("CachedSettingsPropName");

                                if (!webRefExists(web, webReferenceUrls))
                                {
                                    webReferenceUrls.Add(web);
                                }
                                break;
                            case "COMReference":
                                ComReference comRef = new ComReference();
                                comRef.Include = buildItem.Include;
                                comRef.Guid = buildItem.GetMetadata("Guid");
                                comRef.VersionMajor = buildItem.GetMetadata("VersionMajor");
                                comRef.VersionMinor = buildItem.GetMetadata("VersionMinor");
                                comRef.Lcid = buildItem.GetMetadata("Lcid");
                                comRef.Isolated = buildItem.GetMetadata("Isolated");
                                comRef.WrapperTool = buildItem.GetMetadata("WrapperTool");
                                comReferenceList.Add(comRef);
                                break;
                            case "Content":
                                Content content = new Content(projectBasePath);
                                content.IncludePath = buildItem.Include;
                                contents.Add(content);

                                //add web reference in <includes> tag of compile-plugin
                                if (content.IncludePath.Contains("Web References"))
                                {
                                    Compile compileWebRef = new Compile(projectBasePath);
                                    compileWebRef.IncludePath = buildItem.Include;
                                    compiles.Add(compileWebRef);
                                }
                                break;
                            case "Folder":
                                Folder folder = new Folder(projectBasePath);
                                folder.IncludePath = buildItem.Include;
                                folders.Add(folder);
                                break;
                            case "WebReferences":
                                WebReferences webReferences = new WebReferences(projectBasePath);
                                webReferences.IncludePath = buildItem.Include;
                                webReferencesList.Add(webReferences);
                                break;
                            case "EmbeddedResource":
                                EmbeddedResource embeddedResource = new EmbeddedResource(projectBasePath);
                                embeddedResource.IncludePath = buildItem.Include;

                                embeddedResource.DependentUpon = buildItem.GetMetadata("DependentUpon");
                                embeddedResource.SubType = buildItem.GetMetadata("SubType");
                                embeddedResource.Generator = buildItem.GetMetadata("Generator");
                                embeddedResource.LastGenOutput = buildItem.GetMetadata("LastGenOutput");
                                embeddedResource.WithCulture = buildItem.GetMetadata("WithCulture");
                                if (string.IsNullOrEmpty(embeddedResource.WithCulture))
                                {
                                    embeddedResource.WithCulture = MSBuildUtils.DetermineResourceCulture(buildItem.Include);
                                }

                                embeddedResources.Add(embeddedResource);
                                break;
                            case "BootstrapperPackage":
                                BootstrapperPackage bootstrapperPackage = new BootstrapperPackage(projectBasePath);
                                bootstrapperPackage.IncludePath = buildItem.Include;
                                bootstrapperPackage.Visible = buildItem.GetMetadata("Visible");
                                bootstrapperPackage.ProductName = buildItem.GetMetadata("ProductName");
                                bootstrapperPackage.Install = buildItem.GetMetadata("Install");


                                bootstrapperPackages.Add(bootstrapperPackage);
                                break;
                            case "Import":
                                globalNamespaceImports.Add(buildItem.Include);
                                break;
                            case "BaseApplicationManifest":
                                projectDigest.BaseApplicationManifest = buildItem.Include;
                                break;
                           default:
                                log.Debug("Unhandled ItemGroup: " + buildItem.Name);
                                break;
                        }
                    }
                }
            }
        }

        public static bool webRefExists(WebReferenceUrl webUrl, ICollection<WebReferenceUrl> webReferenceUrls)
        {
            bool webRefExists = false;

            foreach (WebReferenceUrl w in webReferenceUrls)
            {
                if (w.RelPath == webUrl.RelPath)
                {
                    webRefExists = true;
                }
            }

            return webRefExists;
        }

        private static void DigestBuildProperties(Project project, ProjectDigest projectDigest)
        {
            foreach (BuildPropertyGroup buildPropertyGroup in project.PropertyGroups)
            {
                if (string.IsNullOrEmpty(buildPropertyGroup.Condition) || evaluateCondition(projectDigest, buildPropertyGroup.Condition))
                {
                    foreach (BuildProperty buildProperty in buildPropertyGroup)
                    {
                        if (!buildProperty.IsImported)
                        {
                            if (!string.IsNullOrEmpty(buildProperty.Condition))
                            {
                                log.DebugFormat("Property {0} = {1} if {2}", buildProperty.Name, buildProperty.Value, buildProperty.Condition);
                                if (evaluateCondition(projectDigest, buildProperty.Condition))
                                {
                                    processProperty(projectDigest, buildProperty);
                                }
                            }
                            else
                            {
                                log.DebugFormat("Property {0} = {1}", buildProperty.Name, buildProperty.Value);
                                processProperty(projectDigest, buildProperty);
                            }
                        }
                    }
                }
            }
        }

        private static bool evaluateCondition(ProjectDigest projectDigest, string condition)
        {
            // basic evaluation of conditions, unable to find a MSBuild API to do it - many will not be supported

            bool val = true;

            string newCond = condition.Replace("$(Configuration)", projectDigest.Configuration);
            newCond = newCond.Replace("$(Platform)", projectDigest.Platform);
            newCond = newCond.Replace("$(Language)", projectDigest.Language);

            if (newCond.Contains("$("))
            {
                log.DebugFormat("Unable to evaluation condition: {0}, unrecognized expression, assuming 'true'", newCond);
                return true;
            }

            Match match = Regex.Match(newCond, @"^\s*'(.*)'\s*(==|!=)\s*'(.*)'\s*$");
            if (match.Success)
            {
                string op = match.Groups[2].Value;
                if (op == "==")
                {
                    val = match.Groups[1].Value == match.Groups[3].Value;
                }
                else if (op == "!=")
                {
                    val = match.Groups[1].Value != match.Groups[3].Value;
                }
                else
                {
                    log.WarnFormat("Unable to evaluate condition: {0}, unrecognized operator {1}, assuming 'true'", condition, op);
                }
            }
            else
            {
                log.WarnFormat("Unable to parse condition: {0}, assuming 'true'", condition);
            }

            log.DebugFormat("Condition = {0}, Substituted = {1}, Result = {2}", condition, newCond, val);
            return val;
        }

        private static void processProperty(ProjectDigest projectDigest, BuildProperty buildProperty)
        {
            if ("RootNameSpace".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.RootNamespace = buildProperty.Value;
            }
            else if ("AssemblyName".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.AssemblyName = buildProperty.Value;
            }
            else if ("Name".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.Name = buildProperty.Value;
            }
            else if ("StartupObject".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.StartupObject = buildProperty.Value;
            }
            else if ("OutputType".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.OutputType = buildProperty.Value;
            }
            else if ("HostInBrowser".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.HostInBrowser = bool.Parse(buildProperty.Value);
            }
            else if ("SilverlightVersion".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.SilverlightVersion = buildProperty.Value.Replace("$(TargetFrameworkVersion)", projectDigest.TargetFrameworkVersion);
            }
            else if ("SilverlightApplication".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.SilverlightApplication = bool.Parse(buildProperty.Value);
            }
            else if ("SilverlightApplicationList".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.SilverlightApplicationList = SilverlightApplicationReference.parseApplicationList(buildProperty.Value);
            }
            else if ("RoleType".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.RoleType = buildProperty.Value;
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
            else if ("TargetFrameworkIdentifier".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase))
            {
                projectDigest.TargetFrameworkIdentifier = buildProperty.Value;
            }
            else if ("TargetFrameworkVersion".Equals(buildProperty.Name, StringComparison.OrdinalIgnoreCase) && projectDigest.TargetFramework == null)
            {
                // Raw value from project
                projectDigest.TargetFrameworkVersion = buildProperty.Value;

                // changed the version to the more specific version
                string frameworkVersion = buildProperty.Value.Substring(1);

                if ("2.0".Equals(frameworkVersion))
                {
                    frameworkVersion = "2.0.50727";
                }

                projectDigest.TargetFramework = frameworkVersion;
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
                    projectDigest.ProjectType = VisualStudioProjectType.GetVisualStudioProjectType(buildProperty.Value);
                }
            }
            else
            {
                log.Debug("Unhandled Property:" + buildProperty.Name);
            }
        }
    }
}
