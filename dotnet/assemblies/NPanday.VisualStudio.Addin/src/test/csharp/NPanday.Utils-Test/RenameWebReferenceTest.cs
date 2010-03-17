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
