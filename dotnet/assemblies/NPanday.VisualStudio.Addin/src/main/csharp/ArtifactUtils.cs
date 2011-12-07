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
using System.Globalization;
using System.IO;
using System.Xml;

namespace NPanday.VisualStudio.Addin
{
    static class ArtifactUtils
    {
        public static bool IsSnapshot(Artifact.Artifact artifact)
        {
            return artifact.Version.EndsWith("-SNAPSHOT");
        }

        public static bool Exists(Artifact.Artifact artifact)
        {
            return artifact.FileInfo.Exists;
        }

        public static bool DownloadFromRemoteRepository(Artifact.Artifact artifact, NPanday.Logging.Logger logger)
        {
            return NPanday.ProjectImporter.Digest.Model.Reference.DownloadArtifact(artifact, logger);
        }

        public static string GetArtifactReferenceFolder(Artifact.Artifact artifact, string referenceFolder)
        {
            //modified artifactFolder to match the .dll searched in NPanday.ProjectImporter.Digest.Model.Reference.cs
            string artifactFolder = Path.Combine(
                referenceFolder, 
                string.Format("{0}\\{1}-{2}", 
                    artifact.GroupId, 
                    artifact.ArtifactId, 
                    artifact.Version));
            return artifactFolder;
        }

        public static string GetArtifactReferenceFilePath(Artifact.Artifact artifact, string referenceFolder)
        {
            string artifactReferenceFolder = GetArtifactReferenceFolder(artifact, referenceFolder);
            Directory.CreateDirectory(artifactReferenceFolder);

            string artifactReferenceFilePath = Path.Combine(artifactReferenceFolder,
                String.Concat(artifact.ArtifactId, artifact.FileInfo.Extension));

            return artifactReferenceFilePath;
        }

        public static DateTime GetArtifactTimestamp(Artifact.Artifact artifact)
        {
            // try maven-metadata-${repoId}.xml or maven-metadata-local.xml
            string localRepoArtifactFolder = artifact.FileInfo.Directory.FullName;
            string[] metadataFilePaths = Directory.GetFiles(localRepoArtifactFolder, "maven-metadata-*.xml");

            DateTime metadataTimestamp;
            if (!TryGetArtifactTimestampFromMetadataFiles(metadataFilePaths, out metadataTimestamp))
            {
                // if that fails, get the file's timestamp)
                metadataTimestamp = new FileInfo(artifact.FileInfo.FullName).LastWriteTimeUtc;
            }

            return metadataTimestamp;
        }

        public static bool TryGetArtifactTimestampFromMetadataFiles(
            IEnumerable<string> metadataFilePaths,
            out DateTime timestamp)
        {
            foreach (string metadataFilePath in metadataFilePaths)
            {
                if (TryGetArtifactTimestampFromMetadataFile(metadataFilePath, out timestamp))
                {
                    return true;
                }
            }

            timestamp = DateTime.MinValue;
            return false;
        }

        public static bool TryGetArtifactTimestampFromMetadataFile(
            string metadataFilePath,
            out DateTime timestamp)
        {
            // Try to get the timestamp from metadata/versioning/lastUpdated
            using (FileStream stream = new FileStream(metadataFilePath, FileMode.Open, FileAccess.Read, FileShare.Read))
            {
                XmlDocument doc = new XmlDocument();
                doc.Load(stream);
                XmlNode node = doc.SelectSingleNode("metadata/versioning/lastUpdated");
                string nodeInnerText;
                if (node != null && !String.IsNullOrEmpty(nodeInnerText = node.InnerText))
                {
                    string value = nodeInnerText.Trim();
                    // format is yyyyMMddHHmmss e.g. 20111028030112)
                    bool parsed = DateTime.TryParseExact(value,
                                                         "yyyyMMddHHmmss",
                                                         CultureInfo.InvariantCulture,
                                                         DateTimeStyles.AssumeUniversal,
                                                         out timestamp);
                    if (parsed)
                    {
                        timestamp = timestamp.ToUniversalTime();
                    }
                    return parsed;
                }
            }

            timestamp = DateTime.MinValue;
            return false;
        }

        public static bool IsEarlierArtifactTimestamp(DateTime value, DateTime comparand)
        {
            return CompareDatesWithoutMilliseconds(value, comparand) < 0;
        }

        public static int CompareDatesWithoutMilliseconds(DateTime left, DateTime right)
        {
            DateTime l = CreateDateTimeWithoutMilliseconds(left);
            DateTime r = CreateDateTimeWithoutMilliseconds(right);
            return l.CompareTo(r);
        }

        public static DateTime CreateDateTimeWithoutMilliseconds(DateTime dateTime)
        {
            DateTime result = new DateTime(
                dateTime.Year, dateTime.Month, dateTime.Day,
                dateTime.Hour, dateTime.Minute, dateTime.Second, 0,
                dateTime.Kind);
            return result;
        }
    }
}
