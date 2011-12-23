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

namespace NPanday.VisualStudio.Addin
{
    public class NPandayBuildSystemProperties : System.ComponentModel.ISynchronizeInvoke
    {

        private object application;

        public object Application
        {
            get { return application; }
            set { application = value; }
        }

        #region ISynchronizeInvoke Members

        public IAsyncResult BeginInvoke(Delegate method, object[] args)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public object EndInvoke(IAsyncResult result)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public object Invoke(Delegate method, object[] args)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public bool InvokeRequired
        {
            get { return false; }
        }

        #endregion
    }
}