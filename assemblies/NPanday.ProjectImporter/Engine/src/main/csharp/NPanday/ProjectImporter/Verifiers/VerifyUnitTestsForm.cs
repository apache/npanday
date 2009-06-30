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
            foreach (ProjectDigest checkedProjectDigest in chkListTestUnits.CheckedItems)
            {
                checkedProjectDigest.UnitTest = true;
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

                if (projectName.Contains("."))
                { 
                    projectName = projectName.Substring(0,projectName.IndexOf("."));
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