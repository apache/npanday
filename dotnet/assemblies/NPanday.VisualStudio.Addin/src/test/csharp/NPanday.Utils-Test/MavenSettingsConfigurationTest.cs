using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Windows.Forms;
using NUnit.Framework;
using NPanday.VisualStudio.Addin;
using NPanday.Utils;
using NPanday.Model.Setting;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    class MavenSettingsConfigurationTest
    {
        private Settings settings;
        private string settingsPathOriginal;
        private string settingsPath;

        private AddArtifactsForm addArtifactsFrm;

        public MavenSettingsConfigurationTest()
        {

            settingsPathOriginal = (new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\m2\\test-settings.xml")).FullName;
            settingsPath = settingsPathOriginal.Replace("test-settings.xml", "test-settings2.xml");

            File.Copy(settingsPathOriginal, settingsPath);

            addArtifactsFrm = new AddArtifactsForm();
            addArtifactsFrm.addProfilesTag(settingsPath);

            settings = SettingsUtil.ReadSettings(settingsPath);
        }

        [Test]
        public void CheckIfSettingsXMLIsValidTest()
        {
            Assert.IsNotNull(settings.profiles);
            File.Delete(settingsPath);
        }
    }
}
