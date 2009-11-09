using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using System.Web.Services;

namespace ClassLibraryWithWebReference
{
    [TestFixture]
    public class Class1
    {
        [Test]
        public void Test()
        {
            try
            {
                webService.LegacyCreateFileRequest request = new webService.LegacyCreateFileRequest();
                request.sessionToken = "invalidSession";
                request.fileName = "someFile.txt";

                webService.DiomedeStorageLegacyTransfer svc = new webService.DiomedeStorageLegacyTransfer();

                svc.CreateFile(request);
            }

            catch (System.Web.Services.Protocols.SoapException ex)
            {
                Assert.IsTrue(ex.Message.Contains("Invalid session"));
            }
        }
    }
}
