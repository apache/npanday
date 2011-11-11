using System;

namespace NPanday.VisualStudio.Addin
{
    /// <summary>
    /// A custom Reference Manager that will handle all artifact reference that can be easily transferable.
    /// </summary>
    public interface IReferenceManager
    {
        Artifact.Artifact Add(IReferenceInfo reference);
        void Remove(IReferenceInfo reference);
        void Initialize(VSLangProj80.VSProject2 project);
        string ReferenceFolder { get; }
        void CopyArtifact(Artifact.Artifact artifact, NPanday.Logging.Logger logger);
        void ResyncArtifacts(NPanday.Logging.Logger logger);
        void ResyncArtifactsFromLocalRepository(NPanday.Logging.Logger logger);
        event EventHandler<ReferenceErrorEventArgs> OnError;
    }
}