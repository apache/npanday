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
using System.Collections.Generic;
using System.IO;
using NPanday.ProjectImporter.Parser.VisualStudioProjectTypes;

/// Author: Leopoldo Lee Agdeppa III


namespace NPanday.ProjectImporter.Digest.Model
{
    public class ProjectDigest
    {

        private string assemblyName;
        public string AssemblyName
        {
            get { return assemblyName; }
            set { assemblyName = value; }
        }

        private string name;
        public string Name
        {
            get { return name; }
            set { name = value; }
        }

        public string ProjectName
        {
            get
            {
                if (name != null)
                    return name;
                else if (assemblyName != null)
                    return assemblyName;
                else
                {
                    FileInfo f = new FileInfo(FullFileName);
                    return f.Name.Substring(0, f.Name.Length - f.Extension.Length);
                }
            }
        }

        private VisualStudioProjectTypeEnum projectType;

        public VisualStudioProjectTypeEnum ProjectType
        {
            get { return projectType; }
            set { projectType = value; }
        }


        private string fullFileName;
        public string FullFileName
        {
            get { return fullFileName; }
            set { fullFileName = value; }
        }

        private string fullDirectoryName;
        public string FullDirectoryName
        {
            get { return fullDirectoryName; }
            set { fullDirectoryName = value; }
        }






        private string language;
        public string Language
        {
            get { return language; }
            set { language = value; }
        }




        private string outputType;
        public string OutputType
        {
            get { return outputType; }
            set { outputType = value; }
        }

        private string roleType;
        public string RoleType
        {
            get { return roleType; }
            set { roleType = value; }
        }

        private bool silverlightApplication;
        public bool SilverlightApplication
        {
            get { return silverlightApplication; }
            set { silverlightApplication = value; }
        }

        private List<SilverlightApplicationReference> silverlightApplicationList;
        public List<SilverlightApplicationReference> SilverlightApplicationList
        {
            get { return silverlightApplicationList; }
            set { silverlightApplicationList = value; }
        }

        private Reference[] references;
        public Reference[] References
        {
            get { return references; }
            set { references = value; }
        }

        private ProjectReference[] projectReferences;
        public ProjectReference[] ProjectReferences
        {
            get { return projectReferences; }
            set { projectReferences = value; }
        }




        private bool unitTest = false;
        public bool UnitTest
        {
            get { return unitTest; }
            set { unitTest = value; }
        }




        public override string ToString()
        {
            return this.assemblyName;
        }




        private string rootNamespace;
        public string RootNamespace
        {
            get { return rootNamespace; }
            set { rootNamespace = value; }
        }
        private string startupObject;
        public string StartupObject
        {
            get { return startupObject; }
            set { startupObject = value; }
        }

        private string signAssembly;
        public string SignAssembly
        {
            get { return signAssembly; }
            set { signAssembly = value; }
        }

        private string assemblyOriginatorKeyFile;
        public string AssemblyOriginatorKeyFile
        {
            get { return assemblyOriginatorKeyFile; }
            set { assemblyOriginatorKeyFile = value; }
        }

        private string delaySign;
        public string DelaySign
        {
            get { return delaySign; }
            set { delaySign = value; }
        }

        private string optimize;
        public string Optimize
        {
            get { return optimize; }
            set { optimize = value; }
        }

        private string allowUnsafeBlocks;
        public string AllowUnsafeBlocks
        {
            get { return allowUnsafeBlocks; }
            set { allowUnsafeBlocks = value; }
        }

        private string defineConstants;
        public string DefineConstants
        {
            get { return defineConstants; }
            set { defineConstants = value; }
        }

        private string applicationIcon;
        public string ApplicationIcon
        {
            get { return applicationIcon; }
            set { applicationIcon = value; }
        }

        private string win32Resource;
        public string Win32Resource
        {
            get { return win32Resource; }
            set { win32Resource = value; }
        }

        private string projectGuid;
        public string ProjectGuid
        {
            get { return projectGuid; }
            set { projectGuid = value; }
        }

        private string configuration;
        public string Configuration
        {
            get { return configuration; }
            set { configuration = value; }
        }

        private string baseIntermediateOutputPath;
        public string BaseIntermediateOutputPath
        {
            get { return baseIntermediateOutputPath; }
            set { baseIntermediateOutputPath = value; }
        }

        private string outputPath;
        public string OutputPath
        {
            get { return outputPath; }
            set { outputPath = value; }
        }

        private string treatWarningsAsErrors;
        public string TreatWarningsAsErrors
        {
            get { return treatWarningsAsErrors; }
            set { treatWarningsAsErrors = value; }
        }

        private string platform;
        public string Platform
        {
            get { return platform; }
            set { platform = value; }
        }

        private string productVersion;
        public string ProductVersion
        {
            get { return productVersion; }
            set { productVersion = value; }
        }

        private string schemaVersion;
        public string SchemaVersion
        {
            get { return schemaVersion; }
            set { schemaVersion = value; }
        }

        private string appDesignerFolder;
        public string AppDesignerFolder
        {
            get { return appDesignerFolder; }
            set { appDesignerFolder = value; }
        }

        private string debugSymbols;
        public string DebugSymbols
        {
            get { return debugSymbols; }
            set { debugSymbols = value; }
        }

        private string debugType;
        public string DebugType
        {
            get { return debugType; }
            set { debugType = value; }
        }

        private string errorReport;
        public string ErrorReport
        {
            get { return errorReport; }
            set { errorReport = value; }
        }

        private string warningLevel;
        public string WarningLevel
        {
            get { return warningLevel; }
            set { warningLevel = value; }
        }

        private string documentationFile;
        public string DocumentationFile
        {
            get { return documentationFile; }
            set { documentationFile = value; }
        }

        private string postBuildEvent;
        public string PostBuildEvent
        {
            get { return postBuildEvent; }
            set { postBuildEvent = value; }
        }


        private string publishUrl;
        public string PublishUrl
        {
            get { return publishUrl; }
            set { publishUrl = value; }
        }

        private string install;
        public string Install
        {
            get { return install; }
            set { install = value; }
        }

        private string installFrom;
        public string InstallFrom
        {
            get { return installFrom; }
            set { installFrom = value; }
        }

        private string updateEnabled;
        public string UpdateEnabled
        {
            get { return updateEnabled; }
            set { updateEnabled = value; }
        }

        private string updateMode;
        public string UpdateMode
        {
            get { return updateMode; }
            set { updateMode = value; }
        }

        private string updateInterval;
        public string UpdateInterval
        {
            get { return updateInterval; }
            set { updateInterval = value; }
        }

        private string updateIntervalUnits;
        public string UpdateIntervalUnits
        {
            get { return updateIntervalUnits; }
            set { updateIntervalUnits = value; }
        }

        private string updatePeriodically;
        public string UpdatePeriodically
        {
            get { return updatePeriodically; }
            set { updatePeriodically = value; }
        }

        private string updateRequired;
        public string UpdateRequired
        {
            get { return updateRequired; }
            set { updateRequired = value; }
        }

        private string mapFileExtensions;
        public string MapFileExtensions
        {
            get { return mapFileExtensions; }
            set { mapFileExtensions = value; }
        }

        private string applicationVersion;
        public string ApplicationVersion
        {
            get { return applicationVersion; }
            set { applicationVersion = value; }
        }

        private string isWebBootstrapper;
        public string IsWebBootstrapper
        {
            get { return isWebBootstrapper; }
            set { isWebBootstrapper = value; }
        }

        private string bootstrapperEnabled;
        public string BootstrapperEnabled
        {
            get { return bootstrapperEnabled; }
            set { bootstrapperEnabled = value; }
        }

        private string preBuildEvent;
        public string PreBuildEvent
        {
            get { return preBuildEvent; }
            set { preBuildEvent = value; }
        }


        private string myType;
        public string MyType
        {
            get { return myType; }
            set { myType = value; }
        }

        private string defineDebug;
        public string DefineDebug
        {
            get { return defineDebug; }
            set { defineDebug = value; }
        }

        private string defineTrace;
        public string DefineTrace
        {
            get { return defineTrace; }
            set { defineTrace = value; }
        }

        private string noWarn;
        public string NoWarn
        {
            get { return noWarn; }
            set { noWarn = value; }
        }

        private string warningsAsErrors;
        public string WarningsAsErrors
        {
            get { return warningsAsErrors; }
            set { warningsAsErrors = value; }
        }

        private string baseApplicationManifest;
        public string BaseApplicationManifest
        {
            get { return baseApplicationManifest; }
            set { baseApplicationManifest = value; }
        }

        private string targetFramework;
        public string TargetFramework
        {
            get { return targetFramework; }
            set { targetFramework = value; }
        }

        private string targetFrameworkVersion = "v2.0";
        public string TargetFrameworkVersion
        {
            get { return targetFrameworkVersion; }
            set { targetFrameworkVersion = value; }
        }

        private Compile[] compiles;
        public Compile[] Compiles
        {
            get { return compiles; }
            set { compiles = value; }
        }

        private Content[] contents;
        public Content[] Contents
        {
            get { return contents; }
            set { contents = value; }
        }

        private None[] nones;
        public None[] Nones
        {
            get { return nones; }
            set { nones = value; }
        }

        private WebReferenceUrl[] webReferenceUrls;
        public WebReferenceUrl[] WebReferenceUrls
        {
            get { return webReferenceUrls; }
            set { webReferenceUrls = value; }
        }
		
		private ComReference[] comReferenceList;
        public ComReference[] ComReferenceList
        {
            get { return comReferenceList; }
            set { comReferenceList = value; }
        }

        private WebReferences[] webReferences;
        public WebReferences[] WebReferences
        {
            get { return webReferences; }
            set { webReferences = value; }
        }

        private EmbeddedResource[] embeddedResources;
        public EmbeddedResource[] EmbeddedResources
        {
            get { return embeddedResources; }
            set { embeddedResources = value; }
        }

        private BootstrapperPackage[] bootstrapperPackages;
        public BootstrapperPackage[] BootstrapperPackages
        {
            get { return bootstrapperPackages; }
            set { bootstrapperPackages = value; }
        }

        private Folder[] folders;
        public Folder[] Folders
        {
            get { return folders; }
            set { folders = value; }
        }

        private string[] globalNamespaceImports;
        public string[] GlobalNamespaceImports
        {
            get { return globalNamespaceImports; }
            set { globalNamespaceImports = value; }
        }



        // used by project-importer during auto-import
        // for getting sa information from the existing pom.xml
        NPanday.Model.Pom.Model existingPom;
        public NPanday.Model.Pom.Model ExistingPom
        {
            get { return existingPom; }
            set { existingPom = value; }
        }

        private bool useMsDeploy;
        public bool UseMsDeploy
        {
            get { return useMsDeploy; }
            set { useMsDeploy = value; }
        }

        private string cloudConfig;
        public string CloudConfig
        {
            get { return cloudConfig; }
            set { cloudConfig = value; }
        }

        private string targetFrameworkIdentifier = ".NETFramework";
        public string TargetFrameworkIdentifier
        {
            get { return targetFrameworkIdentifier; }
            set { targetFrameworkIdentifier = value; } 
        }

        private string silverlightVersion;
        public string SilverlightVersion
        {
            get { return silverlightVersion; }
            set { silverlightVersion = value; }
        }

    }
}
