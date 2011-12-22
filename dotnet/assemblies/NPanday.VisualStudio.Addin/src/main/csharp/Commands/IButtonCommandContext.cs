using System.IO;
using EnvDTE;
using NPanday.Artifact;
using NPanday.Logging;

namespace NPanday.VisualStudio.Addin.Commands
{
    public interface IButtonCommandContext
    {
        Logger Logger { get; }
        ArtifactContext ArtifactContext { get; }
        FileInfo CurrentSelectedProjectPom { get; }

        void ExecuteCommand(string visualStudioCommandName);
        void ExecuteCommand<TCommand>() where TCommand : ButtonCommand, new();
    }
}
