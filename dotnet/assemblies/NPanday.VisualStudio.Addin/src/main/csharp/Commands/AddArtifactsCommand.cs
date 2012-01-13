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
using EnvDTE;
using System.IO;
using System.Windows.Forms;

namespace NPanday.VisualStudio.Addin.Commands
{
    public class AddArtifactsCommand : ButtonCommand
    {
        public override string Caption
        {
            get
            {
                return Messages.MSG_C_ADD_MAVEN_ARTIFACT;
            }
        }

        public override void Execute(IButtonCommandContext context)
        {
            //First selected project
            foreach (Project project in (Array)Application.ActiveSolutionProjects)
            {
                FileInfo currentPom = context.CurrentSelectedProjectPom;
                if (currentPom == null || Path.GetDirectoryName(currentPom.FullName) != Path.GetDirectoryName(project.FullName))
                {
                    DialogResult result = MessageBox.Show("Pom file not found, do you want to import the projects first before adding Maven Artifact?", "Add Maven Artifact", MessageBoxButtons.OKCancel, MessageBoxIcon.Question);
                    if (result == DialogResult.Cancel)
                        return;

                    if (result == DialogResult.OK)
                    {
                        context.ExecuteCommand<ImportSelectedProjectCommand>(); ;

                        currentPom = context.CurrentSelectedProjectPom;

                        // if import failed
                        if (currentPom == null || Path.GetDirectoryName(currentPom.FullName) != Path.GetDirectoryName(project.FullName))
                        {
                            return;
                        }
                    }
                }

                AddArtifactsForm form = new AddArtifactsForm(project, context.ArtifactContext, currentPom);
                form.Show();
                break;
            }
        }
    }
}
