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
using System.IO;


/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Parser.SlnParser
{
    public sealed class SolutionParser
    {
        public delegate List<Dictionary<string, object>> ParserAlgoDelegate(System.IO.FileInfo solutionFile, Dictionary<string, string> globalProperties, ref string warningMsg);

        static ParserAlgoDelegate[] ALGORITHMS = 
        {
            new ProjectSolutionParser().Parse
        };


        public static List<Dictionary<string, object>> ParseSolution(FileInfo solutionFile, Dictionary<string, string> globalProperties, ref string warningMsg)
        {
            List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();

            foreach (ParserAlgoDelegate algo in ALGORITHMS)
            {
               list.AddRange(algo(solutionFile, globalProperties, ref warningMsg));
            }

            return list;
        }

        
    }
}
