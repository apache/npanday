using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

using Microsoft.Build.BuildEngine;
using NMaven.ProjectImporter.Verifiers;
using NMaven.Utils;
using NMaven.ProjectImporter.Digest;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.ProjectImporter.Parser;
using NMaven.ProjectImporter.Converter;
using NMaven.ProjectImporter.Converter.Algorithms;
using NMaven.ProjectImporter.Validator;
using NMaven.ProjectImporter.ImportProjectStructureAlgorithms;
using NMaven.ProjectImporter.Parser.SlnParser;


/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter
{
    public class NMavenImporter
    {
        #region Import Project Type Strategy Pattern
        // A strategy pattern with a twists, using c# delegates
        
        delegate string[] ImportProjectTypeDelegate(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, bool writePom);
        static Dictionary<ProjectStructureType, ImportProjectTypeDelegate> _importProject;

        /// <summary>
        /// Used for registering the strategies (alogrithms) for importing project type
        /// </summary>
        static NMavenImporter()
        {
            // register the algorithms here
            _importProject = new Dictionary<ProjectStructureType, ImportProjectTypeDelegate>();
            _importProject.Add(ProjectStructureType.AbnormalProject, new AbnormalProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.FlatMultiModuleProject, new FlatMultiModuleProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.FlatSingleModuleProject, new FlatSingleModuleProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.NormalMultiModuleProject, new NormalMultiModuleProject().ImportProjectType);
            _importProject.Add(ProjectStructureType.NormalSingleProject, new NormalSingleProject().ImportProjectType);
        }


        public static string[] ImportProjectType(ProjectStructureType structureType, ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version)
        {
            return _importProject[structureType](prjDigests, solutionFile, groupId, artifactId, version, true);
        }

        #endregion

        #region Import Project Entry


        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NMaven Pom,
        /// This is the Project-Importer Entry Method
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, true);
        }

        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NMaven Pom,
        /// This is the Project-Importer Entry Method
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <param name="verifyTests">if true, a dialog box for verifying tests will show up and requires user interaction</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, bool verifyTests)
        {
            if (verifyTests)
            {
                return ImportProject(solutionFile, groupId, artifactId, version, VerifyUnitTestsToUser.VerifyTests);    
            }
            else
            {
                return ImportProject(solutionFile, groupId, artifactId, version, null);    
            }
            

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


        /// <summary>
        /// Imports a specified Visual Studio Projects in a Solution to an NMaven Pom,
        /// This is the Project-Importer Entry Method,
        /// This method accepts a delegate to use as A project Verifier algorithm
        /// </summary>
        /// <param name="solutionFile">Path to your Visual Studio Solution File *.sln </param>
        /// <param name="groupId">Project Group ID, for maven groupId</param>
        /// <param name="artifactId">Project Parent Pom Artifact ID, used as a maven artifact ID for the parent pom.xml</param>
        /// <param name="version">Project version, used as a maven version for the entire pom.xmls</param>
        /// <param name="verifyProjectToImport">A delegate That will Accept a method for verifying Projects To Import</param>
        /// <returns>An array of generated pom.xml filenames</returns>
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, VerifyProjectToImport verifyProjectToImport)
        {
            FileInfo solutionFileInfo = new FileInfo(solutionFile);

            List<Dictionary<string, object>> list = ParseSolution(solutionFileInfo);


            ProjectDigest[] prjDigests = DigestProjects(list);


            ProjectStructureType structureType = GetProjectStructureType(solutionFile, prjDigests);


            if (verifyProjectToImport != null)
            {
                verifyProjectToImport(ref prjDigests, structureType, solutionFile, ref groupId, ref artifactId, ref version);
            }


            return ImportProjectType(structureType, prjDigests, solutionFile, groupId, artifactId, version);

        }

        #endregion


        #region Re-Import Project Entry

        public static string[] ReImportProject(string solutionFile)
        {
            return ImportProject(solutionFile, null, null, null, VerifyProjectImportSyncronization.SyncronizePomValues);    
        }

        #endregion




        /// <summary>
        /// Facade for Parsing A solution File to get its projects
        /// calls NMaven.ProjectImporter.Parser.SlnParserParser.ParseSolution(FileInfo)
        /// </summary>
        /// <param name="solutionFile">the full path of the *.sln (visual studio solution) file you want to parse</param>
        /// <returns></returns>
        public static List<Dictionary<string, object>> ParseSolution(FileInfo solutionFile)
        {
            return SolutionParser.ParseSolution(solutionFile);
        }

        /// <summary>
        /// Facade for Digesting parsed projects
        /// calls NMaven.ProjectImporter.Digest.ProjectDigester.DigestProjects(List<Dictionary<string, object>>)
        /// </summary>
        /// <param name="projects">list retured from ParseSolution</param>
        /// <returns></returns>
        public static ProjectDigest[] DigestProjects(List<Dictionary<string, object>> projects)
        {
            return ProjectDigester.DigestProjects(projects);
        }



        
        


        /// <summary>
        /// Facade for Getting the Project Type. 
        /// calls NMaven.ProjectImporter.Validator.ProjectValidator.GetProjectStructureType(...)
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
