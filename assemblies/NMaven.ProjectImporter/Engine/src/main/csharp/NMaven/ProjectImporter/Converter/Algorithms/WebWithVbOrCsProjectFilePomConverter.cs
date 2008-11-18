using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.IO;

using NMaven.ProjectImporter.Digest;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.Utils;
using NMaven.Model.Pom;

using NMaven.Artifact;

using System.Reflection;

using NMaven.ProjectImporter.Converter;

using NMaven.ProjectImporter.Validator;


/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Converter.Algorithms
{
    public class WebWithVbOrCsProjectFilePomConverter : NormalPomConverter
    {
        public WebWithVbOrCsProjectFilePomConverter(ProjectDigest projectDigest, string mainPomFile, NMaven.Model.Pom.Model parent, string groupId) 
            : base(projectDigest,mainPomFile,parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom)
        {
            // just call the base, but dont write it we still need some minor adjustments for it
            base.ConvertProjectToPomModel(false);
            Model.packaging = "asp";

            Model.build.sourceDirectory = ".";

            // change the outputDirectory of the plugin
            Plugin compilePlugin = FindPlugin("org.apache.maven.dotnet.plugins", "maven-compile-plugin");
            AddPluginConfiguration(compilePlugin, "outputDirectory", "bin");

            if (writePom)
            {
                NMavenPomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }


        }


    }
}
