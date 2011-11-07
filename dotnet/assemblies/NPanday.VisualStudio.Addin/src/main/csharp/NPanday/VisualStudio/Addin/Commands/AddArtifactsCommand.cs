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
                    else if (result == DialogResult.OK)
                    {
                        context.ExecuteCommand(VSCommandCaptions.Standard_SaveAll);

                        NPandayImportProjectForm frm = new NPandayImportProjectForm(Application, context.Logger);
                        frm.SetOutputWindowPane(context.OutputWindowPane);
                        frm.ShowDialog();
                        currentPom = context.CurrentSelectedProjectPom;

                        // if import failed
                        if (currentPom == null || Path.GetDirectoryName(currentPom.FullName) != Path.GetDirectoryName(project.FullName))
                        {
                            return;
                        }
                    }
                }
                AddArtifactsForm form = new AddArtifactsForm(project, context.ArtifactContext, context.Logger, currentPom);
                form.Show();
                break;
            }
        }
    }
}
