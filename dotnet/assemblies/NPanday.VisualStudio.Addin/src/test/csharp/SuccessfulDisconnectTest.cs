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
using NUnit.Framework;
using NPanday.VisualStudio.Addin;
using Extensibility;

namespace NPanday.VisualStudio.Addin_Test
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
            Object[] holder = { "" };
            Array custom = (Array)holder;

            npandayConnect.OnDisconnection(disconnectMode, ref custom);

            Assert.AreEqual(true, npandayConnect.IsNpandayDisconnected());
        }
    }
}
