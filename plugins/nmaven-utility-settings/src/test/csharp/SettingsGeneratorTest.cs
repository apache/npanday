namespace NMaven.Utility.Settings {
	using NUnit.Framework;
	using System;
    using System.IO;

	[TestFixture]
	public class SettingsGeneratorTest  {
		[SetUp]
		protected void SetUp() {

		}


		[Test]
		public void TestGetDefaultSetupWithNullMicrosoftInstallRoot() {
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            nmavenSettingsDefaultSetup setup = settingGenerator.GetDefaultSetup("1.1.18", null);
            Assert.AreEqual("MONO", setup.vendorName, "Incorrect Vendor Name");
            Assert.AreEqual("1.1.18", setup.vendorVersion, "Incorrect Vendor Version");
		}

		[Test]
		public void TestGetDefaultSetupWithNullMonoCLRAndNullMicrosoftInstallRoot() {
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            nmavenSettingsDefaultSetup setup = settingGenerator.GetDefaultSetup(null, null);
            Assert.IsNull(setup, "Default setup should be null");
		}

		[Test]
		public void TestGetDefaultSetupWithNullMonoCLR() {
		    string installRoot = @"C:\WINDOWS\Microsoft.NET\Framework\v2.0.50727";
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            nmavenSettingsDefaultSetup setup =
                settingGenerator.GetDefaultSetup(null, installRoot);
            if(new DirectoryInfo(Path.Combine(installRoot, "v2.0.50727")).Exists)
            {
                Assert.AreEqual("MICROSOFT", setup.vendorName, "Incorrect Vendor Name");
                Assert.AreEqual("v2.0.50727", setup.vendorVersion, "Incorrect Vendor Version");
            }
            else
                Assert.IsNull(setup, "Vendor should be null");

            setup = settingGenerator.GetDefaultSetup(null, @"C:\WINDOWS\Microsoft.NET\Framework-Bogus\v2.0.50727");
                Assert.IsNull(setup, "Vendor should be null");
		}

		[Test]
		public void TestGetVendorForGnuWithNullLibPath() {
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            try {
                settingGenerator.GetVendorForGnu(null);
                Assert.Fail("Should have thrown exception when lib path is null");
            }
            catch(ExecutionException e)
            {
                if(!e.ToString().Contains("NMAVEN-9011-000")) Assert.Fail("Unexpected failure code: Message = "
                    + e.ToString());;
            }
		}

		[Test]
		public void TestGetVendorForGnuWithBadLibPath() {
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            try {
                settingGenerator.GetVendorForGnu(@"C:\\tmp\blah");
                Assert.Fail("Should have thrown exception on bad lib path");
            }
            catch(ExecutionException e)
            {
                if(!e.ToString().Contains("NMAVEN-9011-002")) Assert.Fail("Unexpected failure code: Message = "
                    + e.ToString());;
            }
		}

		[Test]
		public void TestGetVendorForGnuWithValidLibPath() {
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            try {
                nmavenSettingsVendor vendor =
                settingGenerator.GetVendorForGnu(@"C:\Program Files\Portable.NET\0.7.2\lib\cscc\lib");
                Assert.AreEqual("DotGNU", vendor.vendorName, "Incorrect vendor name");
            }
            catch(ExecutionException e)
            {
                Assert.Fail("Failed on valid path: Message = " + e.ToString());;
            }
		}

		[Test]
		public void TestGetVendorForGnuWithInvalidVesionInLibPath() {
            SettingsGeneratorDelegator settingGenerator = new SettingsGeneratorDelegator();
            try {
                settingGenerator.GetVendorForGnu(@"C:\Program Files\Portable.NET\0.7.2-bogus\lib\cscc\lib");
                Assert.Fail("Should have thrown exception on bad version within lib path");
            }
            catch(ExecutionException e)
            {
                if(!e.ToString().Contains("NMAVEN-9011-001")) Assert.Fail("Unexpected failure code: Message = "
                    + e.ToString());;
            }
		}
	}
}