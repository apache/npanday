using System;
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
using System.Collections.Generic;
using System.Text;
using Microsoft.Build.Framework;
using System.Collections;
using log4net;

namespace NPanday.ProjectImporter.Digest.Algorithms
{
    class DigestingBuildEngine : IBuildEngine
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(NormalProjectDigestAlgorithm));

        public int ColumnNumberOfTaskNode { get { throw new NotImplementedException(); } }
        public int LineNumberOfTaskNode { get { throw new NotImplementedException(); } }
        public bool ContinueOnError { get { throw new NotImplementedException(); } }
        public string ProjectFileOfTaskNode { get { throw new NotImplementedException(); } }

        public bool BuildProjectFile(string projectFileName, string[] targetNames, IDictionary globalProperties, IDictionary targetOutputs)
        {
            throw new NotImplementedException();
        }

        public void LogCustomEvent(CustomBuildEventArgs e)
        {
            log.Info(e.Message);
        }

        public void LogErrorEvent(BuildErrorEventArgs e)
        {
            log.Error(e.Message);
        }

        public void LogMessageEvent(BuildMessageEventArgs e)
        {
            log.Info(e.Message);
        }

        public void LogWarningEvent(BuildWarningEventArgs e)
        {
            log.Warn(e.Message);
        }
    }
}
