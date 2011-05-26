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
using NUnit.Framework;
using System.IO;
using NPanday.Utils;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class AddWebReferenceTest
    {
        private PomHelperUtility pomCopy;
        private PomHelperUtility pomCopy2;        
        private String pomPath;
        private String pomPath2;        
        private String pomCopyPath;
        private String pomCopyPath2;        
        private String fullPath;
        private String path;
        private String testFullPath;
        private String testPath;
        private String output;
        private StringBuilder strLine;

        public AddWebReferenceTest()
        {
            pomPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml").FullName);
            pomCopyPath = pomPath.Replace("pom.xml", "pomCopy.xml");

            fullPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\Web References\\WebRef").FullName);
            path = "Web References\\WebRef\\demoService.wsdl";

            testFullPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\Web References\\WebRef2").FullName);
            testPath = "Web References\\WebRef2\\dilbert.wsdl";

            pomPath2 = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom2.xml").FullName);
            pomCopyPath2 = pomPath2.Replace("pom2.xml", "pomCopy2.xml");
        }

        [SetUp]
        public void TestSetUp()
        {
            File.Copy(pomPath, pomCopyPath);
            File.Copy(pomPath2, pomCopyPath2);

            pomCopy = new PomHelperUtility(pomCopyPath);
            pomCopy2 = new PomHelperUtility(pomCopyPath2);
            
        }
        [Test]
        public void AddNewWebReferenceTest()
        {
            int ctr = 0;

            ctr = GetWebReferenceCount(pomCopyPath);
            Assert.AreEqual(0, ctr);
            pomCopy.AddWebReference("WebRef", path, output, null);

            ctr = GetWebReferenceCount(pomCopyPath);
            Assert.AreEqual(1, ctr);
            Assert.IsTrue(strLine.ToString().Contains("<path>Web References/WebRef/demoService.wsdl</path>"));
        }

        [Test]
        public void AddDuplicateWebReferenceTest()
        {
            int ctr = 0;
            ctr = GetWebReferenceCount(pomCopyPath);
            Assert.AreEqual(0, ctr);

            pomCopy.AddWebReference("WebRef", path, output, null);
            pomCopy.AddWebReference("WebRef", path, output, null);

            ctr = GetWebReferenceCount(pomCopyPath);
            Assert.AreEqual(1, ctr);
        }

        [Test]
        public void AddWithExistingWebReferenceTest()
        {
            int ctr = 0;
            
            ctr = GetWebReferenceCount(pomCopyPath2);
            Assert.AreEqual (1, ctr);

            pomCopy2.AddWebReference("WebRef", path, output, null);

            ctr = GetWebReferenceCount(pomCopyPath2);
            Assert.AreEqual(2, ctr);
        }

        [Test]
        public void CheckIncludeSourceWithDiscoFileTest()
        {
            pomCopy.AddWebReference("WebRef", testPath, output, null);
            Assert.IsFalse(GetIncludeSource(pomCopyPath));
        }

        private bool GetIncludeSource(String pom_path)
        {
            bool exists = false;
            String line;
            StreamReader strm = new StreamReader(pom_path);
            strLine = new StringBuilder();

            while ((line = strm.ReadLine()) != null)
            {
                strLine.Append(line);

                if (line.ToString().Contains(".disco"))
                {
                    exists = true;
                }
            }

            strm.Close();
            return exists;
        }

        private int GetWebReferenceCount(String pom_path)
        {
            int ctr = 0;
            String line;
            StreamReader strm = new StreamReader(pom_path);
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
            return ctr;
        }

        [TearDown]
        public void TestTearDown()
        {
            File.Delete(pomCopyPath);
            File.Delete(pomCopyPath2);            
        }

    }
}
