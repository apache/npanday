namespace NPanday.VisualStudio.Addin.Commands
{
    public class ImportSelectedProjectCommand : ButtonCommand
    {
        public override string Caption
        {
            get { return Messages.MSG_C_IMPORT_PROJECT; }
        }

        public override void Execute(IButtonCommandContext context)
        {
            context.ExecuteCommand("File.SaveAll");

            NPandayImportProjectForm frm = new NPandayImportProjectForm(Application, context.Logger);
            frm.ShowDialog();
        }
    }
}