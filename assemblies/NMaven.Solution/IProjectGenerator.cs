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
using System.IO;
using System.Collections.Generic;

using NMaven.Model;

namespace NMaven.Solution
{
	/// <summary>
	/// Provides services for generating .NET project and solution files.
	/// </summary>
    [CLSCompliantAttribute(false)]
	public interface IProjectGenerator
	{
		
        /// <summary>
        /// Generates a .csproj file from the specified maven model.
        /// </summary>
        /// <param name="model">the pom model</param>
        /// <param name="sourceFileDirectory">the directory containing the source files </param>
        /// <param name="projectFileName">the name of the project: usually corresponds to the artifact id</param>
        /// <param name="projectReferences">references to other projects that this project is dependent upon</param>
        /// <returns></returns>
        [CLSCompliantAttribute(false)]
		IProjectReference GenerateProjectFor(NMaven.Model.Pom.Model model, 
		                            DirectoryInfo sourceFileDirectory,
		                            string projectFileName,
                                    ICollection<IProjectReference> projectReferences,
                                    DirectoryInfo localRepository);
		
        /// <summary>
        /// Generates a solution file that references the specified projects.
        /// </summary>
        /// <param name="fileInfo">the solution file</param>
        /// <param name="projectReferences">csproj references</param>
        void GenerateSolutionFor(FileInfo fileInfo, ICollection<IProjectReference> projectReferences);
		
        /// <summary>
        /// Creates a model from the pom.
        /// </summary>
        /// <param name="fileName">file name of the pom.xml file</param>
        /// <returns>a model binding of the pom file</returns>
        /// 
        [CLSCompliantAttribute(false)]
		NMaven.Model.Pom.Model CreatePomModelFor(string fileName);
		
	}
}
