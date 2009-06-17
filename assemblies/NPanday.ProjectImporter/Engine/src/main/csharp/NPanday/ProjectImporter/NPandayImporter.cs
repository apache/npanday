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
        
        delegate string[] ImportProjectTypeDelegate(ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag, bool writePom);
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


        public static string[] ImportProjectType(ProjectStructureType structureType, ProjectDigest[] prjDigests, string solutionFile, string groupId, string artifactId, string version, string scmTag)
        {
            return _importProject[structureType](prjDigests, solutionFile, groupId, artifactId, version, scmTag, true);
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
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version)
        {
            return ImportProject(solutionFile, groupId, artifactId, version, string.Empty,true);
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
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag,bool verifyTests)
        {
            if (verifyTests)
            {
                return ImportProject(solutionFile, groupId, artifactId, version,  scmTag, VerifyUnitTestsToUser.VerifyTests);    
            }
            else
            {
                return ImportProject(solutionFile, groupId, artifactId, version, scmTag, null);    
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

        private static bool HasValidFolderStructure(List<Dictionary<string, object>> projectList)
        {
            bool isValid = true;
            foreach (Dictionary<string,object> project in projectList)
            {
                string holder;
                if (project.ContainsKey("ProjectFullPath"))
                { 
                    holder = (string)project["ProjectFullPath"];
                    if (holder.Contains("..\\"))
                    {
                        isValid = false;
                        break;
                    }
                }
            }
            return isValid;
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
        public static string[] ImportProject(string solutionFile, string groupId, string artifactId, string version, string scmTag,VerifyProjectToImport verifyProjectToImport)
        {
            FileInfo solutionFileInfo = new FileInfo(solutionFile);

            List<Dictionary<string, object>> list = ParseSolution(solutionFileInfo);

            //Checks for Invalid folder structure
            if (!HasValidFolderStructure(list))
            {
                throw new Exception("The Project Importer Failed, Project Directory may not be supported");
            }

            ProjectDigest[] prjDigests = DigestProjects(list);


            ProjectStructureType structureType = GetProjectStructureType(solutionFile, prjDigests);


            // Filtering of unsupported project types.
            String UnsupportedProjectsMessage = string.Empty;
            
            List<ProjectDigest> filteredPrjDigests = new List<ProjectDigest>();
            
            foreach (ProjectDigest pDigest in prjDigests)
            {
                if (PomConverter.__converterAlgorithms.ContainsKey(pDigest.ProjectType))
                {
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
                string warningMSG = "Project Import Warning: \n Unsupported Projects: " + UnsupportedProjectsMessage;
                MessageBox.Show(warningMSG, "Project Import Warning", MessageBoxButtons.OK, MessageBoxIcon.Warning);

            }

            prjDigests = filteredPrjDigests.ToArray();

            //check first for if project is valid
            //try
            //{
              //  return 
            
            string[] result =ImportProjectType(structureType, filteredPrjDigests.ToArray(), solutionFile, groupId, artifactId, version, scmTag);

            if (result != null)
            {
                if (verifyProjectToImport != null && filteredPrjDigests.Count > 0)
                {
                    verifyProjectToImport(ref prjDigests, structureType, solutionFile, ref groupId, ref artifactId, ref version);
                }
                return result;
            }

            else
            {
                MessageBox.Show("The Project Importer Failed, Project Directory may not be supported");
                return null;
            }
            //}

            //catch
            //{
              //  throw new Exception("The Project Importer Failed, Project Directory may not be supported");
            //}


            //return ImportProjectType(structureType, prjDigests, solutionFile, groupId, artifactId, version);
            
            //return ImportProjectType(structureType, filteredPrjDigests.ToArray(), solutionFile, groupId, artifactId, version, scmTag);

        }

        #endregion


        #region Re-Import Project Entry

        public static string[] ReImportProject(string solutionFile)
        {
            return ImportProject(solutionFile, null, null, null, null, VerifyProjectImportSyncronization.SyncronizePomValues);    
        }

        #endregion




        /// <summary>
        /// Facade for Parsing A solution File to get its projects
        /// calls NPanday.ProjectImporter.Parser.SlnParserParser.ParseSolution(FileInfo)
        /// </summary>
        /// <param name="solutionFile">the full path of the *.sln (visual studio solution) file you want to parse</param>
        /// <returns></returns>
        public static List<Dictionary<string, object>> ParseSolution(FileInfo solutionFile)
        {
            return SolutionParser.ParseSolution(solutionFile);
        }

        /// <summary>
        /// Facade for Digesting parsed projects
        /// calls NPanday.ProjectImporter.Digest.ProjectDigester.DigestProjects(List<Dictionary<string, object>>)
        /// </summary>
        /// <param name="projects">list retured from ParseSolution</param>
        /// <returns></returns>
        public static ProjectDigest[] DigestProjects(List<Dictionary<string, object>> projects)
        {
            return ProjectDigester.DigestProjects(projects);
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
