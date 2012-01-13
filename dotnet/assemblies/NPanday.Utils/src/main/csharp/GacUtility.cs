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
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;
using log4net;
using Microsoft.Win32;

namespace NPanday.Utils
{
    public class GacUtility
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(GacUtility));

        private string gacs;

        private static GacUtility instance;

        private GacUtility()
        {
            // Used to determine which references exist in the GAC, used during VS project import
            // TODO: we need a better way to determine this by querying the GAC using .NET
            //  rather than parsing command output
            //  consider this: http://www.codeproject.com/KB/dotnet/undocumentedfusion.aspx
            //  (works, but seems to be missing the processor architecture)
            // Can also use LoadWithPartialName, but it is deprecated

            // First, let's find gacutil.exe. For now, we use .NET 4.0 if available to list everything and let
            // the rest of the logic sort out the right ones - but we might want to pre-filter this to CLR 2.0
            // and CLR 4.0 versions
            string gacutil = "gacutil.exe";
            Dictionary<string, KeyValuePair<string, string>> paths = new Dictionary<string, KeyValuePair<string, string>>();
            try
            {
                RegistryKey sdks = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Microsoft SDKs\Windows");
                foreach (string sdk in sdks.GetSubKeyNames())
                {
                    using (RegistryKey key = sdks.OpenSubKey(sdk)) 
                    {
                        foreach (string location in key.GetSubKeyNames())
                        {
                            using (RegistryKey subkey = key.OpenSubKey(location))
                            {
                                object value = subkey.GetValue("InstallationFolder");

                                if (value != null)
                                {
                                    string exe = Path.Combine(value.ToString(), "gacutil.exe");
                                    if (new FileInfo(exe).Exists)
                                    {
                                        // override with later ones in the list
                                        paths.Add(location, new KeyValuePair<string,string>(sdk, exe));
                                    }
                                }
                            }
                        }
                    }
                }

                string[] search = new string[] { "WinSDK-NetFx40Tools", "WinSDK-NetFx40Tools-x86",  "WinSDK-NetFx35Tools", "WinSDK-NetFx35Tools-x86", "WinSDK-SDKTools" };

                foreach (string s in search)
                {
                    if (paths.ContainsKey(s))
                    {
                        KeyValuePair<string, string> pair = paths[s];
                        gacutil = pair.Value;
                        log.Info("Found gacutil from SDK " + pair.Key + " at " + gacutil);
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                log.Error("Unable to find gacutil in the registry due to an exception: " + e.Message);
            }

            Process p = new Process();

            try
            {
                p.StartInfo.FileName = gacutil;
                p.StartInfo.Arguments = "/l";
                p.StartInfo.UseShellExecute = false;
                p.StartInfo.ErrorDialog = false;
                p.StartInfo.CreateNoWindow = true;
                p.StartInfo.RedirectStandardOutput = true; 
                p.Start();

                System.IO.StreamReader oReader2 = p.StandardOutput;
    
                gacs = oReader2.ReadToEnd();
    
                oReader2.Close();

                p.WaitForExit();
            }
            catch ( Exception exception )
            {
                throw new Exception( "Unable to execute gacutil - check that your PATH has been set correctly (Message: " + exception.Message + ")" );
            }
        }

        public static GacUtility GetInstance()
        {
            if (instance == null)
            {
                instance = new GacUtility();
            }
            return instance;
        }

        public static string GetNPandayGacType(System.Reflection.Assembly a, string publicKeyToken)
        {
            ProcessorArchitecture architecture = a.GetName().ProcessorArchitecture;
            return GetNPandayGacType(a.ImageRuntimeVersion, architecture, publicKeyToken);
        }

        public static string GetNPandayGacType(string runtimeVersion, ProcessorArchitecture architecture, string publicKeyToken)
        {
            string type;

            if (architecture == ProcessorArchitecture.MSIL)
            {
                if (runtimeVersion.StartsWith("v4.0"))
                {
                    type = "gac_msil4";
                }
                else
                {
                    type = "gac_msil";
                }
            }
            else if (architecture == ProcessorArchitecture.X86)
            {
                if (runtimeVersion.StartsWith("v4.0"))
                {
                    type = "gac_32_4";
                }
                else
                {
                    type = "gac_32";
                }
            }
            else if (architecture == ProcessorArchitecture.IA64 || architecture == ProcessorArchitecture.Amd64)
            {
                if (runtimeVersion.StartsWith("v4.0"))
                {
                    type = "gac_64_4";
                }
                else
                {
                    type = "gac_64";
                }
            }
            else
            {
                type = "gac";
            }
            return type;
        }

        public List<string> GetAssemblyInfo(string assemblyName, string version, string processorArchitecture)
        {
            if (string.IsNullOrEmpty(assemblyName))
            {
                return null;
            }

            List<string> results = new List<string>();

            string architecture = String.Empty;
            if (! string.IsNullOrEmpty(processorArchitecture))
            {
                architecture = GetRegexProcessorArchitectureFromString(processorArchitecture);
            }

            Regex regex;
            if (string.IsNullOrEmpty(version))
            {
                regex = new Regex(@"\s*" + assemblyName + @",.*processorArchitecture=" + architecture + ".*", RegexOptions.IgnoreCase);

            }
            else
            {
                regex = new Regex(@"\s*" + assemblyName + @",\s*Version=" + Regex.Escape(version) + @".*processorArchitecture=" + architecture + ".*", RegexOptions.IgnoreCase);
            }

            MatchCollection matches = regex.Matches(gacs);

            foreach (Match match in matches)
            {
                results.Add(match.Value.Trim());
            }
            return results;
        }

        private static string GetRegexProcessorArchitectureFromString(string input)
        {
            switch (input)
            {
                case "x64":
                    return "(AMD64|MSIL)";
                case "Itanium":
                    return "(IA64|MSIL)";
                case "x86":
                    return "(x86|MSIL)";
                case "AnyCPU":
                    return "(x86|MSIL)";
                default:
                    return input;
            }
        }
    }
}
