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
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Threading;
using System.Xml.XPath;
using System.Windows.Forms;

namespace NPanday.VisualStudio.Addin
{
    public class WebServicesReferenceUtils
    {
        public static string GetReferenceFile(string referenceDirectory)
        {
            string fname = "";
            if (!string.IsNullOrEmpty(referenceDirectory))
            {
                foreach (string f in Directory.GetFiles(referenceDirectory))
                {
                    string fext = Path.GetExtension(f).ToLower();
                    if (fext.Equals(".map", StringComparison.InvariantCultureIgnoreCase) || fext.Equals(".discomap", StringComparison.InvariantCultureIgnoreCase)
                        || fext.Equals(".svcmap", StringComparison.InvariantCultureIgnoreCase))
                    {
                        fname = f;
                        break;
                    }
                }
            }
            return fname;
        }

        public static string GetWsdlFile(string referenceDirectory)
        {
            string fname = "";
            if (!string.IsNullOrEmpty(referenceDirectory))
            {
                foreach (string f in Directory.GetFiles(referenceDirectory))
                {
                    string fext = Path.GetExtension(f).ToLower();
                    if (fext.Equals(".wsdl", StringComparison.InvariantCultureIgnoreCase))
                    {
                        fname = f;
                        break;
                    }

                }
            }
            return fname;
        }


        public static string GetWsdlUrl(string referencePath)
        {
            string url = string.Empty;
            
            if (!string.IsNullOrEmpty(referencePath))
            {
                XPathDocument xDoc = new XPathDocument(referencePath);
                XPathNavigator xNav = xDoc.CreateNavigator();
                string xpathExpression;

                if (referencePath.Contains(Messages.MSG_D_SERV_REF))
                {
                    xpathExpression = @"ReferenceGroup/Metadata/MetadataFile[MetadataType='Wsdl']/@SourceUrl";
                }
                else
                {
                    xpathExpression = @"DiscoveryClientResultsFile/Results/DiscoveryClientResult[@referenceType='System.Web.Services.Discovery.ContractReference']/@url";
                }

                System.Xml.XPath.XPathNodeIterator xIter = xNav.Select(xpathExpression);
                if (xIter.MoveNext())
                {
                    url = xIter.Current.TypedValue.ToString();
                }
            }
            return url;
        }
    }
}
