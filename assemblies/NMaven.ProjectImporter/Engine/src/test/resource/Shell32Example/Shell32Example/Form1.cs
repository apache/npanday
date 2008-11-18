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