using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using System.IO;
using NPanday.Utils;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class RenameWebReferenceTest
    {
        private PomHelperUtility pom;
        private PomHelperUtility pomCopy;
        private String pomPath;
        private String pomCopyPath;
        private String fullPath;
        private String path;
        private String output;
        private String oldName = "WebRef";
        private String newName = "WebRef2";

        public RenameWebReferenceTest()
        {
            pom = new PomHelperUtility(new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml"));

            pomPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml").FullName);

            pomCopyPath = pomPath.Replace("pom.xml", "pomCopy.xml");

            pomCopy = new PomHelperUtility(pomCopyPath);

            File.Copy(pomPath, pomCopyPath);

            fullPath = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\Web References\\WebRef").FullName);
            fullPath = fullPath.Replace(oldName, newName); 
            path = "Web References\\WebRef\\demoService.wsdl";
        }

        [Test]
        public void RenameExistingWebReferenceTest()
        {
            pomCopy.RenameWebReference(fullPath, oldName, newName, path, output);
            pomCopy.HasPlugin("npanday.plugin", "maven-wsdl-plugin");
            File.Delete(pomCopyPath);
        }

    }
}
