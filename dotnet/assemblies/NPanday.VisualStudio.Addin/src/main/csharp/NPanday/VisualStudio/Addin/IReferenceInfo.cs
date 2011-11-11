namespace NPanday.VisualStudio.Addin
{
    public interface IReferenceInfo
    {
        string Path { get;set;}
        string FileName { get;set;}
        string Version { get; set;}
        Artifact.Artifact Artifact { get; set; }
    }
}