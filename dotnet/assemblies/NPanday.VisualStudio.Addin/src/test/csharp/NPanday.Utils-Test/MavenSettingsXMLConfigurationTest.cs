using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using NPanday.Utils;
using NPanday.Model.Setting;
using System.IO;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    class MavenSettingsXMLConfigurationTest
    {
        private Settings settings;

        public MavenSettingsXMLConfigurationTest()
        {
            settings = SettingsUtil.ReadSettings(new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target")) + "\\src\\test\\resource\\m2\\settings.xml"));
        }

        [Test]
        public void CheckIfSettingsXMLIsValidTest()
        {
            Assert.IsNull(settings.profiles);
        }
    }
}
