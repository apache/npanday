using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using NPanday.Utils;
using System.IO;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class MavenResxPluginConfigurationTest
    {
        private PomHelperUtility pomHelper;

        
        public MavenResxPluginConfigurationTest()
        {

            pomHelper = new PomHelperUtility(new FileInfo(Directory.GetCurrentDirectory().Substring(0,Directory.GetCurrentDirectory().LastIndexOf("target"))+"\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml"));
        }

        [Test]
        public void AddMavenResxPluginConfigurationTest()
        {
            pomHelper.AddMavenResxPluginConfiguration("npanday.plugin", "maven-resgen-plugin", "embeddedResources", "embeddedResource", "Copy of Resource1.resx", "ClassLibrary1.Copy of Resource1");
        }

        [Test]
        public void RenameMavenResxPluginConfigurationTest()
        {
            pomHelper.RenameMavenResxPluginConfiguration("npanday.plugin", "maven-resgen-plugin", "embeddedResources", "embeddedResource", "Copy of Resource1.resx", "ClassLibrary1.Copy of Resource1", "ToBeDeleted.resx", "ClassLibrary1.ToBeDeleted");
        }

        [Test]
        public void RemoveMavenResxPluginConfigurationTest()
        {
            pomHelper.RemoveMavenResxPluginConfiguration("npanday.plugin", "maven-resgen-plugin", "embeddedResources", "embeddedResource", "ToBeDeleted.resx", "ClassLibrary1.ToBeDeleted");
        }

    }
}
