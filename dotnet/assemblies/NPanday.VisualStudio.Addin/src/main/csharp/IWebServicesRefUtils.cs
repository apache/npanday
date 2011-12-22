using System.Collections.Generic;
using EnvDTE;

namespace NPanday.VisualStudio.Addin
{
    public interface IWebServicesRefUtils
    {
        bool ProjectHasWebReferences(Project project);
        //bool RemovePomWebReferenceInfo(string webRefNamespace);
        bool AddPomWebReferenceInfo(IWebServiceRefInfo webref);
        List<IWebServiceRefInfo> GetWebReferences(Project project);
        void UpdateWebReferences(Project project);
        bool UpdateWSDLFile(IWebServiceRefInfo webRef);
        bool GenerateProxies(IWebServiceRefInfo webRef);
    }
}