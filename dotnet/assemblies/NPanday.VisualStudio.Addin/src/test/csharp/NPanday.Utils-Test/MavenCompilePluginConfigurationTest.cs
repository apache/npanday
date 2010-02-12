using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using NPanday.Utils;
using System.IO;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class MavenCompilePluginConfigurationTest
    {
        private PomHelperUtility pomHelper;

        public MavenCompilePluginConfigurationTest()
        {
            pomHelper = new PomHelperUtility(new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\ClassLibrary1\\ClassLibrary1\\pom.xml"));
        }

        [Test]
        public void AddMavenCompilePluginConfigurationTest()
        {
            pomHelper.AddMavenCompilePluginConfiguration("npanday.plugin", "maven-compile-plugin", "includeSources", "includeSource", "IISHandler1.cs");
        }

        [Test]
        public void RenameMavenCompilePluginConfigurationTest()
        {
            pomHelper.RenameMavenCompilePluginConfiguration("npanday.plugin", "maven-compile-plugin", "includeSources", "includeSource", "IISHandler1.cs","IISHandlerRenamed.cs");
        }

        [Test]
        public void RemoveMavenCompilePluginConfigurationTest()
        {
            pomHelper.RemoveMavenCompilePluginConfiguration("npanday.plugin", "maven-compile-plugin", "includeSources", "includeSource", "IISHandler1.cs");
        }

    }
}
