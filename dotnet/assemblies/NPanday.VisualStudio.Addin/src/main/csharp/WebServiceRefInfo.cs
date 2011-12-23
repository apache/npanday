#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion
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