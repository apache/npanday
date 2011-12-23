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
using Microsoft.VisualStudio.CommandBars;
using EnvDTE80;

namespace NPanday.VisualStudio.Addin.Commands
{
    /// <summary>
    /// TODO: For now we only have buttons and when migrating to VSIX this 
    /// has to be changed dramatically anyway.
    /// </summary>
    public abstract class ButtonCommand
    {
        private DTE2 _application;
        public DTE2 Application
        {
            get
            {
                return _application;
            }
            internal set
            {
                _application = value;
            }
        }

        public abstract string Caption
        { 
            get;
        }

        public abstract void Execute(IButtonCommandContext context);
    }
}
