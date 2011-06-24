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
using System.Text.RegularExpressions;

namespace NPanday.Utils
{
    public class RspUtility
    {
        private string vbRsp = "";
        private string csRsp = "";

        public RspUtility()
        {
            string msBuildPath = Path.GetDirectoryName(System.Reflection.Assembly.GetAssembly(typeof(string)).Location);
            string f35 = Path.GetFullPath(Environment.SystemDirectory + @"\..\Microsoft.NET\Framework\v3.5");
            string f4 = Path.GetFullPath(Environment.SystemDirectory + @"\..\Microsoft.NET\Framework\v4.0.30319");
            if (Directory.Exists(f4))
            {
                msBuildPath = f4;
            }
            try
            {
                csRsp = File.OpenText(msBuildPath + @"\csc.rsp").ReadToEnd();
            }
            catch (Exception) { }


            try
            {
                vbRsp = File.OpenText(msBuildPath + @"\vbc.rsp").ReadToEnd();
            }
            catch (Exception) { }
        }


        public bool IsRspIncluded(string assemblyName, string language)
        {
            if ("vb".Equals(language, StringComparison.OrdinalIgnoreCase))
            {
                return IsVbcRspIncluded(assemblyName);
            }
            else
            {
                return IsCscRspIncluded(assemblyName);
            }
        }



        public bool IsCscRspIncluded(string assemblyName)
        {
            if (string.IsNullOrEmpty(assemblyName))
            {
                return false;
            }

            Regex regex = new Regex(@"\s*/r:" + assemblyName + @"\.dll", RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(csRsp);


            foreach (Match match in matches)
            {
                return true;
            }


            return false;
        }


        public bool IsVbcRspIncluded(string assemblyName)
        {
            if (string.IsNullOrEmpty(assemblyName))
            {
                return false;
            }

            Regex regex = new Regex(@"\s*/r:" + assemblyName + @"\.dll", RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(csRsp);


            foreach (Match match in matches)
            {
                return true;
            }


            return false;
        }
    }
}
