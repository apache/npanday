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
using System.IO;
using NPanday.Model.Pom;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;
using System.Collections.Generic;
using System.Xml;
using System.Text;

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public class AzureWorkerPomConverter : NormalPomConverter
    {
        public AzureWorkerPomConverter(ProjectDigest projectDigest, string mainPomFile, NPanday.Model.Pom.Model parent, string groupId)
            : base(projectDigest, mainPomFile, parent, groupId)
        {
        }

        public override void ConvertProjectToPomModel(bool writePom, string scmTag)
        {
            // just call the base, but dont write it we still need some minor adjustments for it
            base.ConvertProjectToPomModel(false, scmTag);

            List<string> goals = new List<string>();
            goals.Add("assemble-package-files");
            goals.Add("process-configs");
            goals.Add("create-package");

            Plugin plugin = AddPlugin("org.apache.npanday.plugins", "application-maven-plugin", null, false);
            AddPluginExecution(plugin, "package-application", goals.ToArray(), null);

            if (projectDigest.Contents.Length > 0)
            {
                AddPluginConfiguration(plugin, "mixinAssemblyComponentDescriptors", "mixinAssemblyComponentDescriptor", new string[] { "npanday-content.xml" });
                WriteAssemblyDescriptor(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\npanday-content.xml"), projectDigest.Contents);
            }

            if (writePom)
            {
                PomHelperUtility.WriteModelToPom(new FileInfo(Path.GetDirectoryName(projectDigest.FullFileName) + @"\pom.xml"), Model);
            }
        }

        private void WriteAssemblyDescriptor(FileInfo fileInfo, Content[] content)
        {
            XmlDocument doc = new XmlDocument();
            XmlNode node = doc.CreateXmlDeclaration("1.0", "UTF-8", null);
            doc.AppendChild(node);

            string xmlns = "http://maven.apache.org/plugins/maven-assembly-plugin/component/1.1.2";
            XmlElement component = doc.CreateElement("component", xmlns);
            XmlAttribute attrib = doc.CreateAttribute("xsi", "schemaLocation", "http://www.w3.org/2001/XMLSchema-instance");
            attrib.Value = xmlns + " http://maven.apache.org/xsd/component-1.1.2.xsd";
            component.Attributes.Append(attrib);
            doc.AppendChild(component);

            XmlElement files = doc.CreateElement("files", xmlns);
            foreach (Content c in content)
            {
                XmlElement file = doc.CreateElement("file", xmlns);

                XmlElement el = doc.CreateElement("source", xmlns);
                el.InnerText = c.IncludePath;
                file.AppendChild(el);

                el = doc.CreateElement("outputDirectory", xmlns);
                el.InnerText = "/";
                file.AppendChild(el);

                files.AppendChild(file);
            }
            component.AppendChild(files);

            XmlTextWriter writer = null;
            try
            {
                writer = new XmlTextWriter(fileInfo.FullName, Encoding.ASCII);
                writer.Formatting = Formatting.Indented;
                doc.WriteTo(writer);
            }
            finally
            {
                if (writer != null)
                {
                    writer.Close();
                }
            }
        }
    }
}
