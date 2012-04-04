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

using Microsoft.Win32;
using System;
using System.IO;

namespace NPanday.Plugin.Settings
{
    public class DotnetSdkLocator
    {
        RegistryKey Microsoft_NETFramework = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\.NETFramework");
        RegistryKey Microsoft_SDKs_NETFramework = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\SDKs\.NETFramework");
        RegistryKey Microsoft_SDKs_Windows_70 = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.0");
        RegistryKey Microsoft_SDKs_Windows_70a = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.0A");

        string ProgramFilesX86(string subfolders){
            string programFiles = Environment.GetEnvironmentVariable("PROGRAMFILES(X86)")
                ?? Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            return Path.Combine(programFiles, subfolders);
        }

        string ProgramFiles(string subfolders){
            string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            return Path.Combine(programFiles, subfolders);
        }

        public string Find1_1()
        {
            return (string)Microsoft_NETFramework.GetValue("sdkInstallRootv1.1");
        }

        public string Find2_0()
        {
            return PathUtil.FirstExisting(
                registryFind(Microsoft_NETFramework, "sdkInstallRootv2.0"),
                registryFind(Microsoft_SDKs_NETFramework, "v2.0", "InstallationFolder"),
                ProgramFilesX86(@"Microsoft.NET\SDK\v2.0"),
                ProgramFilesX86(@"Microsoft.NET\SDK\v2.0 64bit"),
                ProgramFiles(@"Microsoft SDKs\Windows\v6.0A\bin"),
                ProgramFilesX86(@"Microsoft SDKs\Windows\v6.0A\bin")
                );
        }

        public string Find3_5()
        {
            return PathUtil.FirstExisting(
                registryFind(Microsoft_NETFramework, "sdkInstallRootv3.5"),
                // prefer 32 bit until its made explicit
                registryFind(Microsoft_SDKs_Windows_70, "WinSDKNetFx35Tools-x86", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx35Tools-x86", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70, "WinSDKNetFx35Tools", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx35Tools", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70, "WinSDKNetFx35Tools-x64", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx35Tools-x64", "InstallationFolder")
            );
        }

        public string Find4_0()
        {
            return PathUtil.FirstExisting(
                registryFind(Microsoft_NETFramework, "sdkInstallRootv4.0"), // does not exist, I thinkg (lcorneliussen)
                // prefer 32 bit until its made explicit
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx40Tools", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx40Tools-x86", "InstallationFolder"),
                registryFind(Microsoft_SDKs_Windows_70a, "WinSDK-NetFx40Tools-x64", "InstallationFolder")
            );
        }

        private string registryFind(RegistryKey root, string valueKey)
        {
            return registryFind(root, null, valueKey);
        }

        private string registryFind(RegistryKey root, string subkey, string valueKey)
        {
            RegistryKey current = root;

            if (current == null)
                return null;

            if (subkey != null)
            {
                current = current.OpenSubKey(subkey);
                if (current == null)
                    return null;
            }

            return (string)current.GetValue(valueKey);
        }
    }
}
