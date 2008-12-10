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

namespace WindowsApplication2
{
    public partial class ArchetypeProjectForm : Form
    {
        private String artifactId;

        private String version;

        private String groupId;

        public ArchetypeProjectForm()
        {
            InitializeComponent();
        }

        public String Version
        {
            get
            {
                return version;
            }

            set
            {
                version = value;
            }
        }

        public String ArtifactId
        {
            get
            {
                return artifactId;
            }

            set
            {
                artifactId = value;
            }
        }

        public String GroupId
        {
            get
            {
                return groupId;
            }

            set
            {
                groupId = value;
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            artifactId = artifactIdTextBox.Text;
            groupId = groupIdTextBox.Text;
            version = versionTextBox.Text;
            this.Dispose();            
        }

        private void ArchetypeProjectForm_Load(object sender, EventArgs e)
        {
            groupIdTextBox.Text = this.GroupId;
            artifactIdTextBox.Text = this.ArtifactId;
            versionTextBox.Text = this.Version;
        }

        private void listBoxArtifactType_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void label5_Click(object sender, EventArgs e)
        {

        }
    }
}