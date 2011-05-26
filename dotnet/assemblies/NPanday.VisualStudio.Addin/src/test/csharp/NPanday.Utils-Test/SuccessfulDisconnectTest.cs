using System;
using NUnit.Framework;
using NPanday.Utils;
using System.IO;
using NPanday.VisualStudio.Addin;
using Extensibility;

namespace ConnectTest.UtilsTest
{
    [TestFixture]
    public class SuccessfulDisconnectTest
    {
        public class ConnectTest : Connect
        {
            public bool IsNpandayDisconnected()
            {             
                return this.IsApplicationObjectNull();
            }
        }

        [Test]
        public void CheckCleanDisconnectTest()
        {
            ConnectTest npandayConnect = new ConnectTest();

            ext_DisconnectMode disconnectMode = ext_DisconnectMode.ext_dm_HostShutdown;
            Object[] holder = {""};
            Array custom = (Array)holder;

            npandayConnect.OnDisconnection(disconnectMode,ref custom);

            Assert.AreEqual(true,npandayConnect.IsNpandayDisconnected()); 
        }
    }
}
