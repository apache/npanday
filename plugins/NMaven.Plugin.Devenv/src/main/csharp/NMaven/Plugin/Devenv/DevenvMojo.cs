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

using System;
using System.Collections.Generic;
using System.IO;
using System.Diagnostics;

using Microsoft.Win32;

using NMaven.Plugin;

namespace NMaven.Plugin.Devenv
{
	/// <summary>
	/// Description
	/// </summary>
	[Serializable]
	[ClassAttribute(Phase = "deploy", Goal = "start")]
	public sealed class DevenvMojo : AbstractMojo
	{
		public DevenvMojo()
		{
		}
		
		[FieldAttribute("artifactId", Expression = "${project.artifactId}", Type = "java.lang.String")]
		public String artifactId;

        [FieldAttribute("buildDirectory", Expression = "${project.build.directory}", Type = "java.lang.String")]
        public String buildDirectory;
		
		public override Type GetMojoImplementationType()
		{
			return this.GetType();
		}
		
		public override void Execute()
		{
            string args = "/ResetAddin " + artifactId + ".Connect " + "/Log " + @"""" + @buildDirectory 
                + @"\VisualStudio.log" + @"""";
            RegistryKey visualStudioKey = 
                Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\VisualStudio\8.0");
            String installDir = (String) visualStudioKey.GetValue("InstallDir");
            ProcessStartInfo processStartInfo =
                new ProcessStartInfo(@installDir + "devenv.exe", args);
            Process.Start(processStartInfo);
		}
	}
}
