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
using System.IO;
using Microsoft.VisualStudio.CommandBars;
using Microsoft.VisualStudio.TemplateWizard;
using System.Windows.Forms;
using EnvDTE;
using EnvDTE80;

namespace WindowsApplication2
{
    public class ArchetypeProjectWizard : IWizard
    {
        private ArchetypeProjectForm inputForm;

        // This method is called before opening any item that 
        // has the OpenInEditor attribute.
        public void BeforeOpeningFile(ProjectItem projectItem)
        {
        }

        public void ProjectFinishedGenerating(Project project)
        {
        }
        
        // This method is only called for item templates,
        // not for project templates.
        public void ProjectItemFinishedGenerating(ProjectItem 
            projectItem)
        {
        }

        // This method is called after the project is created.
        public void RunFinished()
        {
        }
        private OutputWindowPane outputWindowPane;

        private void cbShowAddArtifactsForm_Click(CommandBarButton btn, ref bool Cancel)
        {
            outputWindowPane.OutputString("It Works");
        }

        public void RunStarted(object automationObject,
            Dictionary<string, string> replacementsDictionary,
            WizardRunKind runKind, object[] customParams)
        {

            DTE2 dte2 = (DTE2)automationObject;
                Window win = dte2.Windows.Item(EnvDTE.Constants.vsWindowKindOutput);
                OutputWindow outputWindow = (OutputWindow)win.Object;
                outputWindowPane = outputWindow.OutputWindowPanes.Add("Test");
                outputWindowPane.OutputString("Start");
                CommandBarControl addRef = null;
            foreach (CommandBar commandBar in (CommandBars)dte2.CommandBars)
            {
                outputWindowPane.OutputString("Command Bar = " + commandBar.Name + Environment.NewLine);
                foreach(CommandBarControl control in commandBar.Controls)
                {
                    outputWindowPane.OutputString(control.Caption + Environment.NewLine);
                    if (control.Caption.Equals("Add &Reference..."))
                    {
                        addRef = control;
                        CommandBarButton ctl = (CommandBarButton) 
                            commandBar.Controls.Add(MsoControlType.msoControlButton,
                            System.Type.Missing, System.Type.Missing, control.Index, true);
                        ctl.Click += new _CommandBarButtonEvents_ClickEventHandler(cbShowAddArtifactsForm_Click);
                        
                            //new _CommandBarButtonEvents_ClickEventHandler(ShowAddArtifactsForm); 
                            //new ClickEventHandler();
                        ctl.Caption = "Add Maven Artifact...";
                        ctl.Visible = true;
                        
                    }
                }
            }
            
            
            //  dte2. += new EventHandler(ClearOutputWindowPane);
         //   Window solutionExplorerWindow 
         //       = (Window) dte2.Windows.Item(Constants.vsWindowKindSolutionExplorer);
         //   solutionExplorerWindow
         //   Window w; UIHierarchy u;
         //   UIHierarchyItem i;
         //   dte2.Events.
         //   ;
          //  EnvDTE.s
           // DirectoryInfo projectDirectoryInfo = new FileInfo(dte2.Solution.Projects.Item(1).FullName).Directory;
          //  dte2.Solution.Projects.Item(1).SaveAs(projectDirectoryInfo.FullName + @"\src\test.csproj");
            
            try
            {
                // Display a form to the user. The form collects 
                // input for the custom message.
                inputForm = new ArchetypeProjectForm();
                String projectName = replacementsDictionary["$projectname$"];
                inputForm.GroupId = (projectName.Contains(".")) ? 
                    projectName.Substring(0, projectName.LastIndexOf(".")) : projectName;
                inputForm.ArtifactId = projectName;
                inputForm.Version = "0.0.0.0-SNAPSHOT";
                
                inputForm.ShowDialog();
                String projectPath = @"src\main\csharp";
                replacementsDictionary.Add("$artifactId$", 
                    inputForm.ArtifactId);
                replacementsDictionary.Add("$groupId$",
                    inputForm.GroupId);
                replacementsDictionary.Add("$version$",
                    inputForm.Version);
                replacementsDictionary.Add("$projectPath$",
                    projectPath);
                replacementsDictionary.Add("$classPath$",
                    projectPath + @"\" + inputForm.ArtifactId.Replace(".", @"\"));
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
            }
        }

        // This method is only called for item templates,
        // not for project templates.
        public bool ShouldAddProjectItem(string filePath)
        {
            return true;
        }        
    }
}

