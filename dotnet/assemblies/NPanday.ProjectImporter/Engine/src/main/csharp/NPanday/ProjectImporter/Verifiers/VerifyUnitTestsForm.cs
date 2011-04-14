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
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Verifiers
{
    public partial class VerifyUnitTestsForm : Form
    {
        private ProjectDigest[] projectDigets;

        public VerifyUnitTestsForm(ProjectDigest[] projectDigets)
        {
            this.projectDigets = projectDigets;
            InitializeComponent();
            FillCheckList();
        }

        private void cmdOK_Click(object sender, EventArgs e)
        {
            // reset all to false
            foreach (ProjectDigest projectDigest in projectDigets)
            {
                projectDigest.UnitTest = false;
            }

            // select all selected projects
            foreach (ProjectDigest projectDigest in projectDigets)
            {
                String[] projectNameTokens = projectDigest.FullFileName.Split("\\".ToCharArray());
                String projectName = projectNameTokens[projectNameTokens.Length - 1];

                if (projectName.Equals(string.Empty))
                {
                    projectName = projectNameTokens[projectNameTokens.Length - 2];
                }

                if (projectName.Contains(".csproj") || projectName.Contains(".vbproj"))
                {
                    if (projectName.Contains(".csproj"))
                    {
                        projectName = projectName.Substring(0, projectName.LastIndexOf(".csproj"));
                   }
                    else
                    {
                        projectName = projectName.Substring(0, projectName.LastIndexOf(".vbproj"));
                    }
                    
                    if (chkListTestUnits.CheckedItems.Contains(projectName))
                    {
                        projectDigest.UnitTest = true;
                    }
                }
            }
            this.Close();
        }

        private void cmdReset_Click(object sender, EventArgs e)
        {
            Reset();
        }

        private void FillCheckList()
        {
            chkListTestUnits.Items.Clear();
            foreach (ProjectDigest projectDigest in projectDigets)
            {
                String[] projectNameTokens = projectDigest.FullFileName.Split("\\".ToCharArray());
                String projectName = projectNameTokens[projectNameTokens.Length-1];
                
                //instances where in the project name has added \\ at the end of the full file name
                if (projectName.Equals(string.Empty))
                {
                    projectName = projectNameTokens[projectNameTokens.Length - 2];
                }

                if (projectName.Contains(".csproj") || projectName.Contains(".vbproj"))
                {
                    if (projectName.Contains(".csproj"))
                    {
                        projectName = projectName.Substring(0, projectName.LastIndexOf(".csproj"));
                    }
                    else
                    {
                        projectName = projectName.Substring(0, projectName.LastIndexOf(".vbproj"));
                    }
                    
                }

                chkListTestUnits.Items.Add(projectName, projectDigest.UnitTest);
            }
        }

        private void Reset()
        {
            for (int i = 0; i < projectDigets.Length; i++)
            {

                chkListTestUnits.SetItemChecked(i, projectDigets[i].UnitTest);
            }
        }


        private void MarkAll(bool check)
        {
            for(int i=0; i< chkListTestUnits.Items.Count; i++ )
            {
                chkListTestUnits.SetItemChecked(i, check);
            }
            

        }

        private void cmdSelectNone_Click(object sender, EventArgs e)
        {
            MarkAll(false);
        }

        private void cmdSelectAll_Click(object sender, EventArgs e)
        {
            MarkAll(true);
        }
    }
}
