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

using System;
using System.Text;
using System.Resources;
using System.IO;
using System.Configuration;
using System.Drawing;

namespace NPanday.Plugin.ResX
{
    /// <summary>
    /// Utility for generating resource files
    /// </summary>
    public class ResourceGenerator
    {
        ///<summary>
        /// Generates a resource file
        ///</summary>
        ///<param name = "sourceDirectory">directory containing resources (jpg, txt, wav)</param>
        ///<param name = "outputFile">the generated .resources file</param>
        ///<returns>void</returns>       
        public void Execute(string sourceDirectory, string outputFile) 
        {
            AppSettingsReader appSettingsReader = new AppSettingsReader();
            if (!hasConfig(appSettingsReader)) throw new Exception("NPANDAY-9000-003: Could not find exe.config file.");

            ResXResourceWriter resourceWriter = new ResXResourceWriter(@outputFile);

            DirectoryInfo directoryInfo = 
                new DirectoryInfo(@sourceDirectory);
            foreach (FileInfo fileInfo in directoryInfo.GetFiles())
            {
                MimeType mimeType = GetMimeTypeFor(fileInfo.Name, appSettingsReader);
                string extension = mimeType.GetExtension();
                if (extension.Equals("ico"))
                    resourceWriter.AddResource(fileInfo.Name, new Icon(@fileInfo.FullName));
                else if (extension.Equals("x-properties"))
                {
                    StreamReader reader = new StreamReader(fileInfo.OpenRead(),Encoding.Default);
                    while(reader.Peek() >= 0) 
                    {
                        string[] values =  reader.ReadLine().Split('=');
                        if(values != null && values.Length == 2 && !values[0].StartsWith("#")) 
                            resourceWriter.AddResource(values[0], values[1]);
                    }
                    reader.Close();                                
                }
                else if (extension.Equals("db"))
                    continue;//Thumbnail
                else if(extension.Equals("wav")) 
                {
                    MemoryStream memoryStream = FileInfoToMemoryStream(fileInfo);
                    resourceWriter.AddResource(fileInfo.Name, memoryStream);
                    memoryStream.Close();
                }
                else if (mimeType.GetPrimaryType().Equals("image"))
                    resourceWriter.AddResource(fileInfo.Name, new Bitmap(@fileInfo.FullName));
                else if(mimeType.GetPrimaryType().Equals("text"))
                    resourceWriter.AddResource(fileInfo.Name, fileInfo.OpenText().ReadToEnd()); 
                else
                {
                    MemoryStream memoryStream = FileInfoToMemoryStream(fileInfo);
                    resourceWriter.AddResource(fileInfo.Name, memoryStream.ToArray());
                    memoryStream.Close();
                }
            }
            resourceWriter.Generate();
            resourceWriter.Close();
        }

        /// <summary>
        /// Entry method
        /// </summary>
        /// <param name="args">array of arguments: 1) source directory of resources and 2) resource output file</param>
        /// <returns>if successful, return 0, otherwise returns 1</returns>
        public static int Main(string[] args)
        {
            Console.WriteLine("NPANDAY Resource Generator Utility ");
/*
            if (args.Length != 2)
            {
                StringBuilder stringBuilder = new StringBuilder();
                foreach (string arg in args)
                    stringBuilder.Append("Arg = ").Append(arg).Append(", ");

                Console.Error.WriteLine("NPANDAY-9000-000: Exiting program: Incorrect number of args (should have 2): Number Found = {0}, {1} ", 
                    args.Length, stringBuilder.ToString());
                return 1;
            } 
*/            
            string sourceDirectory = args[0];
            string outputFile = args[1];
            
            ResourceGenerator resX = new ResourceGenerator();
            try
            {
                resX.Execute(sourceDirectory, outputFile);
            }
            catch (Exception e)
            {                
                Console.Error.WriteLine("NPANDAY-9000-002: Unable to generate resources: " + e.ToString());
                return 1;
            }
            return 0;
        }

        /// <summary>
        /// Returns mime-type information for the given file
        /// </summary>
        /// <param name="fileName">file name</param>
        /// <param name="appSettingsReader">reader for the utility config</param>
        /// <returns>mime-type information for the given file</returns>
        private MimeType GetMimeTypeFor(String fileName, AppSettingsReader appSettingsReader)
        {
            string extension = new FileInfo(fileName).Extension.Substring(1);
            string mimeType = null;
            try
            {
                mimeType = (string)appSettingsReader.GetValue(extension, typeof(string));
            }
            catch (InvalidOperationException e)
            {
                Console.WriteLine("NPANDAY-9000-001: Could not find the mime-type: Extension = {0} "
                    , extension);
                return new MimeType("", "", extension);
            }
            return new MimeType(mimeType.Split('/')[0], mimeType.Split('/')[1], extension);
        }
        
        /// <summary>
        /// Converts file info to memory stream
        /// </summary>
        /// <param name="fileInfo">file info</param>
        /// <returns>memory stream for the file</returns>
        private MemoryStream FileInfoToMemoryStream(FileInfo fileInfo)
        {
            MemoryStream memoryStream = new MemoryStream();
            BinaryReader reader = new BinaryReader(fileInfo.Open(FileMode.Open), Encoding.Default);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = reader.Read(buffer, 0, 1024)) > 0)
                memoryStream.Write(buffer, 0, n);
            return memoryStream;
        }

        private bool hasConfig(AppSettingsReader appSettingsReader)
        {
            try
            {
                string s = (string)appSettingsReader.GetValue("txt", typeof(string));
                return true;
            }
            catch (InvalidOperationException e)
            {
                return false;
            }
        }
    }
}
