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
using System.Collections.Generic;
using System.Text;
using Microsoft.Build.Utilities;

namespace NPanday.ProjectImporter.Utils
{
    public class MSBuildUtils
    {
        public static string DetermineResourceCulture(string name)
        {
            Microsoft.Build.Tasks.AssignCulture task = new Microsoft.Build.Tasks.AssignCulture();
            task.Files = new TaskItem[] { new TaskItem(name) };
            task.BuildEngine = new LogOnlyBuildEngine();
            bool result = task.Execute();
            return task.AssignedFiles[0].GetMetadata("Culture");
        }
    }
}
