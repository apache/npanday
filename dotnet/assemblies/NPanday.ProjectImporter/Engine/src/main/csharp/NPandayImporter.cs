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
using NPanday.ProjectImporter.Verifiers;
using NPanday.Utils;
using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.ProjectImporter.Parser;
using NPanday.ProjectImporter.Converter;
using NPanday.ProjectImporter.Converter.Algorithms;
using NPanday.ProjectImporter.Validator;
using NPanday.ProjectImporter.ImportProjectStructureAlgorithms;
using NPanday.ProjectImporter.Parser.SlnParser;
using System.Windows.Forms;



/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter
{
    public class NPandayImporter
    {
        #region Import Project Type Strategy Pattern
        // A strategy pattern with a twists, using c# delegates

        delegate string[] ImportProjectTypeDelegate(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, bool writePom, List<Reference> missingReferences, List<string> nonPortableReferences);
        static Dictionary<ProjectStructureType, ImportProjectTypeDelegate> _importProject;

        /// <summary>
        /// Used for registering the strategies (alogrithms) for importing project type
        /// </summary>
        static NPandayImporter()
        {
            // register the algorithms here
            _importProject = new Dictionary<ProjectStructureType, ImportProjectTypeDelegate>();
            _importProject.Add(ProjectStructureType.AbnormalProject, new AbnormalProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.FlatMultiModuleProject, new FlatMultiModuleProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.FlatSingleModuleProject, new FlatSingleModuleProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.NormalMultiModuleProject, new NormalMultiModuleProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.NormalSingleProject, new NormalSingleProject().ImportProjectType);
        }


        public static string[] ImportProjectType(ProjectStructureType structureType, ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, List<Reference> missingReferences, List<string> nonPortableReferences)
        {
            return _importProject[structureType](prjDigests, solutionFile, groupId, artifactId, version, scmTag, true, missingReferences, nonPortableReferences);
        }

        #endregion

        #region Import Project Entry


        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NPanday Pom,
        /// This is the Project-Importer Entry Method
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, ref string warningMsg)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, string.Empty, true, ref warningMsg);
        }

        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NPanday Pom,
        /// This is the Project-Importer Entry Method
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <param name="verifyTests">if true, a dialog box for verifying tests will show up and requires user interaction</param>
        /// <param name="scmTag">generates scm tags if txtboxfield is not empty or null</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag, bool verifyTests, ref string warningMsg)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, scmTag, verifyTests, false, ref warningMsg);
        }

        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NPanday Pom,
        /// This is the Project-Importer Entry Method
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <param name="verifyTests">if true, a dialog box for verifying tests will show up and requires user interaction</param>
        /// <param name="scmTag">generates scm tags if txtboxfield is not empty or null</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag, bool verifyTests, bool useMsDeploy, ref string warningMsg)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, scmTag, verifyTests, useMsDeploy, null, null, null, ref warningMsg);    
        }

        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag, bool verifyTests, bool useMsDeploy, string configuration, string cloudConfig, DependencySearchConfiguration depSearchConfig, ref string warningMsg)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, scmTag, verifyTests, useMsDeploy, configuration, cloudConfig, depSearchConfig, null, ref warningMsg);
        }

        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag, bool verifyTests, bool useMsDeploy, string configuration, string cloudConfig, DependencySearchConfiguration depSearchConfig, Dictionary<string, string> globalProperties, ref string warningMsg)
        {
            VerifyProjectToImport method = verifyTests ? VerifyUnitTestsToUser.VerifyTests : (VerifyProjectToImport)null;
            return ImportProject(solutionFile, groupId, artifactId, version, scmTag, method, useMsDeploy, configuration, cloudConfig, depSearchConfig, globalProperties, ref warningMsg);
        }

        /// <summary>
        /// Delegate Alogrithm for Verifying Project To Import
        /// </summary>
        /// <param name="projectDigests"></param>
        /// <param name="structureType"></param>
        /// <param name="solutionFile"></param>
        /// <param name="groupId"></param>
        /// <param name="artifactId"></param>
        /// <param name="version"></param>
        public delegate void VerifyProjectToImport(ref ProjectDigest[] projectDigests, ProjectStructureType structureType, string solutionFile, ref string groupId,ref string artifactId,ref string version);

        private static void HasValidFolderStructure(List<Dictionary<string, object>> projectList)
        {
            string errorProject = string.Empty;
            foreach (Dictionary<string,object> project in projectList)
            {
                string holder;
                if (project.ContainsKey("ProjectFullPath"))
                {
                    holder = (string)project["ProjectFullPath"];
                    if (holder.Contains("..\\"))
                    {
                        throw new Exception( "Invalid folder structure for project." + " One of it's path contains '..' : " + holder);
                    }
                }
            }
        }

        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NPanday Pom,
        /// This is the Project-Importer Entry Method,
        /// This method accepts a delegate to use as A project Verifier algorithm
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <param name="verifyProjectToImport">A delegate That will Accept a method for verifying Projects To Import</param>
        /// <param name="scmTag">adds scm tags to parent pom.xml if not string.empty or null</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag, VerifyProjectToImport verifyProjectToImport, ref string warningMsg)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, scmTag, verifyProjectToImport, false, null, null, null, null, ref warningMsg);
        }

        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NPanday Pom,
        /// This is the Project-Importer Entry Method,
        /// This method accepts a delegate to use as A project Verifier algorithm
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <param name="verifyProjectToImport">A delegate That will Accept a method for verifying Projects To Import</param>
        /// <param name="scmTag">adds scm tags to parent pom.xml if not string.empty or null</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag, VerifyProjectToImport verifyProjectToImport, bool useMsDeploy, string configuration, string cloudConfig, DependencySearchConfiguration depSearchConfig, Dictionary<string, string> globalProperties, ref string warningMsg)
        {
            string[] result = null;

            if (depSearchConfig == null)
                depSearchConfig = new DependencySearchConfiguration();

            FileInfo solutionFileInfo = new FileInfo(solutionFile);

            List<Dictionary<string, object>> list = ParseSolution(solutionFileInfo, globalProperties, ref warningMsg);

            if (configuration != null)
            {
                foreach (Dictionary<string, object> projectMap in list)
                {
                    projectMap.Add("Configuration", configuration);
                }
            }

            //Checks for Invalid folder structure
            HasValidFolderStructure(list);

            ProjectDigest[] prjDigests = DigestProjects(list, ref warningMsg);


            ProjectStructureType structureType = GetProjectStructureType(solutionFile, prjDigests);


            // Filtering of unsupported project types.
            String UnsupportedProjectsMessage = string.Empty;
            
            List<ProjectDigest> filteredPrjDigests = new List<ProjectDigest>();
            
            foreach (ProjectDigest pDigest in prjDigests)
            {
                if (PomConverter.__converterAlgorithms.ContainsKey(pDigest.ProjectType))
                {
                    // set the project flag so that converters can look at it later
                    pDigest.UseMsDeploy = useMsDeploy;
                    pDigest.CloudConfig = cloudConfig;
                    pDigest.DependencySearchConfig = depSearchConfig;
                    filteredPrjDigests.Add(pDigest);
                }
                else
                {
                    if (UnsupportedProjectsMessage == string.Empty)
                    {
                        UnsupportedProjectsMessage += pDigest.FullFileName;
                    }
                    else
                    {
                        UnsupportedProjectsMessage += ", " + pDigest.FullFileName;
                    }
                }
            }
            
            if (!string.Empty.Equals(UnsupportedProjectsMessage))
            {
                warningMsg = string.Format("{0}\n    Unsupported Projects: {1}", warningMsg, UnsupportedProjectsMessage);
            }

            prjDigests = filteredPrjDigests.ToArray();

            if (verifyProjectToImport != null && filteredPrjDigests.Count > 0)
            {
               verifyProjectToImport(ref prjDigests, structureType, solutionFile, ref groupId, ref artifactId, ref version);
            }

            List<Reference> missingReferences = new List<Reference>();
            List<string> nonPortableReferences = new List<string>();
            result = ImportProjectType(structureType, filteredPrjDigests.ToArray(), solutionFile, groupId, artifactId, version, scmTag, missingReferences, nonPortableReferences);
            if (missingReferences.Count > 0)
            {
                warningMsg += "\nThe following references could not be resolved from Maven or the GAC:";
                foreach (Reference missingReference in missingReferences)
                {
                    warningMsg += "\n\t" + missingReference.Name + " (" + missingReference.Version + ")";
                }
                warningMsg += "\nPlease update the defaults in pom.xml and re-sync references, or re-add them using 'Add Maven Artifact'.";
            }
            if (nonPortableReferences.Count > 0)
            {
                if (depSearchConfig.CopyToMaven)
                {
                    warningMsg += "The following artifacts were copied to the local Maven repository:"
                         + "\n\t" + string.Join("\n\t", nonPortableReferences.ToArray())
                         + "\nDeploying the reference to a Repository will make the code portable to other machines.";
                }
                else
                {
                    warningMsg += "\nThe build may not be portable if local references are used:"
                         + "\n\t" + string.Join("\n\t", nonPortableReferences.ToArray())
                         + "\nDeploying the reference to a Repository will make the code portable to other machines." 
                         + "\n\nNote: artifacts with a system path will not be packaged or used as a runtime dependency by Maven";
                }
            }
            return result;
        }

        #endregion


        #region Re-Import Project Entry

        public static string[] ReImportProject(string solutionFile, ref string warningMsg)
        {
            return ImportProject(solutionFile, null, null, null, null, VerifyProjectImportSyncronization.SyncronizePomValues, ref warningMsg);    
        }

        #endregion




        /// <summary>
        /// Facade for Parsing A solution File to get its projects
        /// calls NPanday.ProjectImporter.Parser.SlnParserParser.ParseSolution(FileInfo)
        /// </summary>
        /// <param name="solutionFile">the full path of the *.sln (visual studio solution) file you want to parse</param>
        /// <returns></returns>
        public static List<Dictionary<string, object>> ParseSolution(FileInfo solutionFile, Dictionary<string, string> globalProperties, ref string warningMsg)
        {
            return SolutionParser.ParseSolution(solutionFile, globalProperties, ref warningMsg);
        }

        /// <summary>
        /// Facade for Digesting parsed projects
        /// calls NPanday.ProjectImporter.Digest.ProjectDigester.DigestProjects(List<Dictionary<string, object>>)
        /// </summary>
        /// <param name="projects">list retured from ParseSolution</param>
        /// <returns></returns>
        public static ProjectDigest[] DigestProjects(List<Dictionary<string, object>> projects, ref string warningMsg)
        {
            return ProjectDigester.DigestProjects(projects, ref warningMsg);
        }

        /// <summary>
        /// Facade for Getting the Project Type. 
        /// calls NPanday.ProjectImporter.Validator.ProjectValidator.GetProjectStructureType(...)
        /// </summary>
        /// <param name="solutionFile">the full path of the *.sln (visual studio solution) file you want to import</param>
        /// <param name="projectDigests">Digested Projects</param>
        /// <returns></returns>
        public static ProjectStructureType GetProjectStructureType(string solutionFile, ProjectDigest[] projectDigests)
        {
            return ProjectValidator.GetProjectStructureType(solutionFile, projectDigests);
        }
    }
}
