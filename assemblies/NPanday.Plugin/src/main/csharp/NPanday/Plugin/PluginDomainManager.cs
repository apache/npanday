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
using System.Reflection;

namespace NPanday.Plugin
{
    /// <summary>
    /// Allows loading of .NET Maven Plugins (and its dependencies) into a separate application domain.
    /// </summary>
    public sealed class PluginDomainManager : AppDomainManager
    {
        /// <summary>
        /// Default constructor
        /// </summary>
        public PluginDomainManager() : base()
        {
            Console.WriteLine("Creating Plugin Domain Manager");
        }
        
        /// <summary>
        /// Loads the specified .NET Maven plugin into the plugin domain 
        /// </summary>
        /// <param name="assemblyFile">The .NET Maven plugin</param>
        public void LoadPlugin(FileInfo assemblyFile)
        {
            
            Assembly assembly = null;
            try 
            {
                string assemblyName = assemblyFile.Name.Replace(assemblyFile.Extension,"");
                assembly = AppDomain.CurrentDomain.Load(assemblyName);                
            }
            catch(FileNotFoundException e)
            {
                Console.WriteLine("FNE: " + e.Message);
                return;
            }
        }
    }
}
