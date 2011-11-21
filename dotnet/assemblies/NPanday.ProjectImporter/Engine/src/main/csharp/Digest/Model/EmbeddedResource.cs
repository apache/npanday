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

using NPanday.ProjectImporter.Digest;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{


    public class EmbeddedResource : IncludeBase
    {
        public EmbeddedResource(string projectBasePath) 
            : base(projectBasePath)
        {
        }
        
        private string subType;
        public string SubType
        {
            get { return subType; }
            set { subType = value; }
        }

        private string dependentUpon;
        public string DependentUpon
        {
            get { return dependentUpon; }
            set { dependentUpon = value; }
        }


        private string generator;
        public string Generator
        {
            get { return generator; }
            set { generator = value; }
        }

        private string lastGenOutput;
        public string LastGenOutput
        {
            get { return lastGenOutput; }
            set { lastGenOutput = value; }
        }
            
    }
}
