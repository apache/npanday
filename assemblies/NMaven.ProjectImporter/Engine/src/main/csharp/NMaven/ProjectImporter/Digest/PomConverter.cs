using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Windows.Forms;


using NMaven.ProjectImporter.Digest;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.Utils;
using NMaven.Model.Pom;

using NMaven.Artifact;

using NMaven.ProjectImporter.Converter.Algorithms;
using NMaven.ProjectImporter.Parser.VisualStudioProjectTypes;


/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Converter
{
    public class PomConverter
    {
        // Converter Algorithm Registry
        static Dictionary<VisualStudioProjectTypeEnum, Type> __converterAlgorithms;
        static PomConverter()
        {
            __converterAlgorithms = new Dictionary<VisualStudioProjectTypeEnum, Type>();
            
            // register converter algorithms
            __converterAlgorithms.Add(VisualStudioProjectTypeEnum.Windows__CSharp, typeof(NormalPomConverter));
            __converterAlgorithms.Add(VisualStudioProjectTypeEnum.Windows__VbDotNet, typeof(NormalPomConverter));
            __converterAlgorithms.Add(VisualStudioProjectTypeEnum.Web_Site, typeof(WebPomConverter));
            __converterAlgorithms.Add(VisualStudioProjectTypeEnum.Web_Application, typeof(WebPomConverter));


            // combination of types
            __converterAlgorithms.Add(
                VisualStudioProjectTypeEnum.Web_Site | VisualStudioProjectTypeEnum.Windows__CSharp,
                typeof(WebWithVbOrCsProjectFilePomConverter)
              );
            __converterAlgorithms.Add(
                VisualStudioProjectTypeEnum.Web_Site | VisualStudioProjectTypeEnum.Windows__VbDotNet,
                typeof(WebWithVbOrCsProjectFilePomConverter)
              );

            __converterAlgorithms.Add(
                VisualStudioProjectTypeEnum.Web_Application | VisualStudioProjectTypeEnum.Windows__CSharp,
                typeof(WebWithVbOrCsProjectFilePomConverter)
              );
            __converterAlgorithms.Add(
                VisualStudioProjectTypeEnum.Web_Application | VisualStudioProjectTypeEnum.Windows__VbDotNet,
                typeof(WebWithVbOrCsProjectFilePomConverter)
              );

        }


        public static NMaven.Model.Pom.Model MakeProjectsParentPomModel(ProjectDigest[] projectDigests, string pomFileName, string groupId, string artifactId, string version, bool writePom)
        {

            try
            {
                NMaven.Model.Pom.Model model = new NMaven.Model.Pom.Model();

                model.modelVersion = "4.0.0";
                model.packaging = "pom";
                model.groupId = groupId;
                model.artifactId = artifactId;
                model.version = version;
                model.name = string.Format("{0} : {1}", groupId, artifactId);

                List<string> modules = new List<string>();
                foreach (ProjectDigest projectDigest in projectDigests)
                {
                    DirectoryInfo prjDir = new DirectoryInfo
                        (
                            projectDigest.ProjectType == VisualStudioProjectTypeEnum.Web_Site
                            ? projectDigest.FullFileName
                            : Path.GetDirectoryName(projectDigest.FullFileName)
                        );
                    DirectoryInfo pomDir = new DirectoryInfo(Path.GetDirectoryName(pomFileName));

                    string moduleDir = NMavenPomHelperUtility.GetRelativePath(pomDir, prjDir);
                    if (string.IsNullOrEmpty(moduleDir))
                    {
                        moduleDir = ".";
                    }
                    modules.Add(moduleDir);

                }

                model.modules = modules.ToArray();

                if (writePom)
                {
                    NMavenPomHelperUtility.WriteModelToPom(new FileInfo(pomFileName), model);
                }
                return model;
            }
            catch
            {
                throw;
            }

            
        }



        public static NMaven.Model.Pom.Model[] ConvertProjectsToPomModels(ProjectDigest[] projectDigests, string mainPomFile, NMaven.Model.Pom.Model parent, string groupId, bool writePoms)
        {
            try
            {
                string version = parent != null ? parent.version : null;

                List<NMaven.Model.Pom.Model> models = new List<NMaven.Model.Pom.Model>();
                foreach (ProjectDigest projectDigest in projectDigests)
                {
                    NMaven.Model.Pom.Model model = ConvertProjectToPomModel(projectDigest, mainPomFile, parent, groupId, writePoms);
                    models.Add(model);
                }
                return models.ToArray();
            }
            catch
            {
                throw;
            }
        }

        public static NMaven.Model.Pom.Model ConvertProjectToPomModel(ProjectDigest projectDigest, string mainPomFile, NMaven.Model.Pom.Model parent, string groupId, bool writePom)
        {
            if (!__converterAlgorithms.ContainsKey(projectDigest.ProjectType))
            {
                throw new NotSupportedException("Not Supported Project Type: " + projectDigest.ProjectType );
            }


            try
            {
                IPomConverter converter = (IPomConverter)System.Activator.CreateInstance(
                                                __converterAlgorithms[projectDigest.ProjectType],
                                                projectDigest,
                                                mainPomFile,
                                                parent,
                                                groupId
                                                );

                converter.ConvertProjectToPomModel();

                return converter.Model;
            }
            catch
            {
                throw;
            }
        }


    }
}
