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
using System.Text;
using NUnit.Framework;
using System.IO;
using NPanday.Utils;

namespace NPanday.VisualStudio.Addin_Test
{
    [TestFixture]
    public class RenameWebReferenceTest
    {
        private PomHelperUtility pomCopy;
        private String pomPath;
        private String pomCopyPath;
        private String fullPath;
        private String fullPathCopy;
        private String path;
        private String output;
        private String oldName = "WebRef";
        private String newName = "WebRef2";

        private StringBuilder strLine;
        private String line;
        private StreamReader strm;

        public RenameWebReferenceTest()
        {
            pomPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml").FullName);

            pomCopyPath = pomPath.Replace("pom.xml", "pomCopy.xml");

            pomCopy = new PomHelperUtility(pomCopyPath);

            File.Copy(pomPath, pomCopyPath);

            fullPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\Web References\\WebRef").FullName);
            fullPathCopy = fullPath.Replace(oldName, newName); 
            path = "Web References\\" + oldName + "\\demoService.wsdl";
        }

        [Test]
        public void RenameExistingWebReferenceTest()
        {
            int ctr = 0;

            pomCopy.RenameWebReference(fullPath, oldName, newName, path, output);

            strm = new StreamReader(pomCopyPath);
            strLine = new StringBuilder();

            while ((line = strm.ReadLine()) != null)
            {
                strLine.Append(line);

                if (line.ToString().Contains("<webreference>"))
                {
                    ctr++;
                }
            }

            strm.Close();
            File.Delete(pomCopyPath);

            Assert.AreEqual(1, ctr);
            Assert.IsFalse(strLine.ToString().Contains("<namespace>WebRef</namespace>"));
        }

    }
}
