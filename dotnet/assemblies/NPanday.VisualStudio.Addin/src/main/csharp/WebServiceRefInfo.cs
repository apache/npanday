namespace NPanday.VisualStudio.Addin
{
    public class WebServiceRefInfo : IWebServiceRefInfo
    {
        public WebServiceRefInfo() { }
        public WebServiceRefInfo(string name, string wsdlUrl)
        {
            this.name = name;
            this.wsdlUrl = wsdlUrl;
        }

        #region IWebServiceRefInfo Members
        string name;
        public string Name
        {
            get
            {
                return name;
            }
            set
            {
                name = value;
            }
        }

        string wsdlUrl;
        public string WSDLUrl
        {
            get
            {
                return wsdlUrl;
            }
            set
            {
                wsdlUrl = value;
            }
        }

        string outputFile;
        public string OutputFile
        {
            get
            {
                return outputFile;
            }
            set
            {
                outputFile = value;
            }
        }

        string wsdlFile;
        public string WsdlFile
        {
            get
            {
                return wsdlFile;
            }
            set
            {
                wsdlFile = value;
            }
        }

        #endregion


    }
}