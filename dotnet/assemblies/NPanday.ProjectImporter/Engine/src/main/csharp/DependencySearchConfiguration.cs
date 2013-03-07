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

namespace NPanday.ProjectImporter
{
    public class DependencySearchConfiguration
    {
        private bool searchFramework = true;
        public bool SearchFramework { get { return searchFramework; } set { searchFramework = value; } }

        private bool searchAssemblyFoldersEx = true;
        public bool SearchAssemblyFoldersEx { get { return searchAssemblyFoldersEx; } set { searchAssemblyFoldersEx = value; } }

        private bool searchGac = true;
        public bool SearchGac { get { return searchGac; } set { searchGac = value; } }

        private bool copyToMaven = true;
        public bool CopyToMaven { get { return copyToMaven; } set { copyToMaven = value; } }
    }
}
