namespace NPanday.VisualStudio.Addin
{
    public interface IWebServiceRefInfo
    {
        string Name { get; set; }
        string WSDLUrl { get; set; }
        string OutputFile { get; set; }
        string WsdlFile { get; set; }
    }
}