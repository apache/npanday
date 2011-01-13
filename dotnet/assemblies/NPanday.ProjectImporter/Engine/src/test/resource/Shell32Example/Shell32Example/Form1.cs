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

namespace Shell32Example
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        //Program does nothing.  Just a compile test

        private void UnZipFiles(string zipFilePath, string unzipFilePath)
        {
            Shell32.ShellClass shApp = new Shell32.ShellClass();
            Shell32.Folder3 destFolder = (Shell32.Folder3)shApp.NameSpace(unzipFilePath);
            Shell32.FolderItems3 zippedItems = (Shell32.FolderItems3)shApp.NameSpace(zipFilePath).Items();
            destFolder.CopyHere(zippedItems, 0);
        }
    }
}