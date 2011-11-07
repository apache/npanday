using System.IO;
using EnvDTE;
using NPanday.Artifact;
using NPanday.Logging;

namespace NPanday.VisualStudio.Addin.Commands
{
    public interface IButtonCommandContext
    {
        FileInfo CurrentSelectedProjectPom { get; }
        ArtifactContext ArtifactContext { get; }
        Logger Logger { get; }
        OutputWindowPane OutputWindowPane { get; }

        bool ExecuteCommand(string barAndCaption);
    }
}
