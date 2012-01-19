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
using System.Windows.Forms;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Parser.VisualStudioProjectTypes
{
    public class VisualStudioProjectType
    {
        static Dictionary<string, VisualStudioProjectTypeEnum> __visualStudioProjectTypes;
        static Dictionary<VisualStudioProjectTypeEnum, string> __visualStudioProjectTypeGuids;
        static Dictionary<string, bool> __visualStudioProjectTypeSupported; // TODO: should remove, and just rely on the converter registrations
        static VisualStudioProjectType()
        {
            __visualStudioProjectTypes = new Dictionary<string, VisualStudioProjectTypeEnum>();
            __visualStudioProjectTypeGuids = new Dictionary<VisualStudioProjectTypeEnum, string>();
            __visualStudioProjectTypeSupported = new Dictionary<string, bool>();


            //Windows (C#)	 {FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}
            __visualStudioProjectTypes.Add("FAE04EC0-301F-11D3-BF4B-00C04F79EFBC", VisualStudioProjectTypeEnum.Windows__CSharp);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Windows__CSharp, "FAE04EC0-301F-11D3-BF4B-00C04F79EFBC");
            __visualStudioProjectTypeSupported.Add("FAE04EC0-301F-11D3-BF4B-00C04F79EFBC", true);


            //Windows (VB.NET)	 {F184B08F-C81C-45F6-A57F-5ABD9991F28F}
            __visualStudioProjectTypes.Add("F184B08F-C81C-45F6-A57F-5ABD9991F28F", VisualStudioProjectTypeEnum.Windows__VbDotNet);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Windows__VbDotNet, "F184B08F-C81C-45F6-A57F-5ABD9991F28F");
            __visualStudioProjectTypeSupported.Add("F184B08F-C81C-45F6-A57F-5ABD9991F28F", true);


            //Windows (Visual C++)	 {8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942}
            __visualStudioProjectTypes.Add("8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942", VisualStudioProjectTypeEnum.Windows__VCpp);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Windows__VCpp, "8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942");
            __visualStudioProjectTypeSupported.Add("8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942", false);


            //Web Application	 {349C5851-65DF-11DA-9384-00065B846F21}
            __visualStudioProjectTypes.Add("349C5851-65DF-11DA-9384-00065B846F21", VisualStudioProjectTypeEnum.Web_Application);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Web_Application, "349C5851-65DF-11DA-9384-00065B846F21");
            __visualStudioProjectTypeSupported.Add("349C5851-65DF-11DA-9384-00065B846F21", true);


            //Web Site	 {E24C65DC-7377-472B-9ABA-BC803B73C61A}
            __visualStudioProjectTypes.Add("E24C65DC-7377-472B-9ABA-BC803B73C61A", VisualStudioProjectTypeEnum.Web_Site);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Web_Site, "E24C65DC-7377-472B-9ABA-BC803B73C61A");
            __visualStudioProjectTypeSupported.Add("E24C65DC-7377-472B-9ABA-BC803B73C61A", true);


            //Distributed System	 {F135691A-BF7E-435D-8960-F99683D2D49C}
            __visualStudioProjectTypes.Add("F135691A-BF7E-435D-8960-F99683D2D49C", VisualStudioProjectTypeEnum.Distributed_System);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Distributed_System, "F135691A-BF7E-435D-8960-F99683D2D49C");
            __visualStudioProjectTypeSupported.Add("F135691A-BF7E-435D-8960-F99683D2D49C", false);


            //Windows Communication Foundation (WCF)	 {3D9AD99F-2412-4246-B90B-4EAA41C64699}
            __visualStudioProjectTypes.Add("3D9AD99F-2412-4246-B90B-4EAA41C64699", VisualStudioProjectTypeEnum.Windows_Communication_Foundation__WCF);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Windows_Communication_Foundation__WCF, "3D9AD99F-2412-4246-B90B-4EAA41C64699");
            __visualStudioProjectTypeSupported.Add("3D9AD99F-2412-4246-B90B-4EAA41C64699", true);


            //Windows Presentation Foundation (WPF)	 {60DC8134-EBA5-43B8-BCC9-BB4BC16C2548}
            __visualStudioProjectTypes.Add("60DC8134-EBA5-43B8-BCC9-BB4BC16C2548", VisualStudioProjectTypeEnum.Windows_Presentation_Foundation__WPF);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Windows_Presentation_Foundation__WPF, "60DC8134-EBA5-43B8-BCC9-BB4BC16C2548");
            __visualStudioProjectTypeSupported.Add("60DC8134-EBA5-43B8-BCC9-BB4BC16C2548", true);


            //Visual Database Tools	 {C252FEB5-A946-4202-B1D4-9916A0590387}
            __visualStudioProjectTypes.Add("C252FEB5-A946-4202-B1D4-9916A0590387", VisualStudioProjectTypeEnum.Visual_Database_Tools);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Visual_Database_Tools, "C252FEB5-A946-4202-B1D4-9916A0590387");
            __visualStudioProjectTypeSupported.Add("C252FEB5-A946-4202-B1D4-9916A0590387", false);


            //Database	 {A9ACE9BB-CECE-4E62-9AA4-C7E7C5BD2124}
            __visualStudioProjectTypes.Add("A9ACE9BB-CECE-4E62-9AA4-C7E7C5BD2124", VisualStudioProjectTypeEnum.Database);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Database, "A9ACE9BB-CECE-4E62-9AA4-C7E7C5BD2124");
            __visualStudioProjectTypeSupported.Add("A9ACE9BB-CECE-4E62-9AA4-C7E7C5BD2124", false);


            //Database (other project types)	 {4F174C21-8C12-11D0-8340-0000F80270F8}
            __visualStudioProjectTypes.Add("4F174C21-8C12-11D0-8340-0000F80270F8", VisualStudioProjectTypeEnum.Database__other_project_types);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Database__other_project_types, "4F174C21-8C12-11D0-8340-0000F80270F8");
            __visualStudioProjectTypeSupported.Add("4F174C21-8C12-11D0-8340-0000F80270F8", false);
            

            //Test	 {3AC096D0-A1C2-E12C-1390-A8335801FDAB}
            __visualStudioProjectTypes.Add("3AC096D0-A1C2-E12C-1390-A8335801FDAB", VisualStudioProjectTypeEnum.Test);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Test, "3AC096D0-A1C2-E12C-1390-A8335801FDAB");
            __visualStudioProjectTypeSupported.Add("3AC096D0-A1C2-E12C-1390-A8335801FDAB", true);


            //Legacy (2003) Smart Device (C#)	 {20D4826A-C6FA-45DB-90F4-C717570B9F32}
            __visualStudioProjectTypes.Add("20D4826A-C6FA-45DB-90F4-C717570B9F32", VisualStudioProjectTypeEnum.Legacy__2003_Smart_Device__CSharp);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Legacy__2003_Smart_Device__CSharp, "20D4826A-C6FA-45DB-90F4-C717570B9F32");
            __visualStudioProjectTypeSupported.Add("20D4826A-C6FA-45DB-90F4-C717570B9F32", false);


            //Legacy (2003) Smart Device (VB.NET)	 {CB4CE8C6-1BDB-4DC7-A4D3-65A1999772F8}
            __visualStudioProjectTypes.Add("CB4CE8C6-1BDB-4DC7-A4D3-65A1999772F8", VisualStudioProjectTypeEnum.Legacy__2003_Smart_Device__VbDotNet);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Legacy__2003_Smart_Device__VbDotNet, "CB4CE8C6-1BDB-4DC7-A4D3-65A1999772F8");
            __visualStudioProjectTypeSupported.Add("CB4CE8C6-1BDB-4DC7-A4D3-65A1999772F8", false);


            //Smart Device (C#)	 {4D628B5B-2FBC-4AA6-8C16-197242AEB884}
            __visualStudioProjectTypes.Add("4D628B5B-2FBC-4AA6-8C16-197242AEB884", VisualStudioProjectTypeEnum.Smart_Device__CSharp);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Smart_Device__CSharp, "4D628B5B-2FBC-4AA6-8C16-197242AEB884");
            __visualStudioProjectTypeSupported.Add("4D628B5B-2FBC-4AA6-8C16-197242AEB884", false);


            //Smart Device (VB.NET)	 {68B1623D-7FB9-47D8-8664-7ECEA3297D4F}
            __visualStudioProjectTypes.Add("68B1623D-7FB9-47D8-8664-7ECEA3297D4F", VisualStudioProjectTypeEnum.Smart_Device__VbDotNet);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Smart_Device__VbDotNet, "68B1623D-7FB9-47D8-8664-7ECEA3297D4F");
            __visualStudioProjectTypeSupported.Add("68B1623D-7FB9-47D8-8664-7ECEA3297D4F", false);


            //Workflow (C#)	 {14822709-B5A1-4724-98CA-57A101D1B079}
            __visualStudioProjectTypes.Add("14822709-B5A1-4724-98CA-57A101D1B079", VisualStudioProjectTypeEnum.Workflow__CSharp);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Workflow__CSharp, "14822709-B5A1-4724-98CA-57A101D1B079");
            __visualStudioProjectTypeSupported.Add("14822709-B5A1-4724-98CA-57A101D1B079", false);


            //Workflow (VB.NET)	 {D59BE175-2ED0-4C54-BE3D-CDAA9F3214C8}
            __visualStudioProjectTypes.Add("D59BE175-2ED0-4C54-BE3D-CDAA9F3214C8", VisualStudioProjectTypeEnum.Workflow__VbDotNet);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Workflow__VbDotNet, "D59BE175-2ED0-4C54-BE3D-CDAA9F3214C8");
            __visualStudioProjectTypeSupported.Add("D59BE175-2ED0-4C54-BE3D-CDAA9F3214C8", false);


            //Deployment Merge Module	 {06A35CCD-C46D-44D5-987B-CF40FF872267}
            __visualStudioProjectTypes.Add("06A35CCD-C46D-44D5-987B-CF40FF872267", VisualStudioProjectTypeEnum.Deployment_Merge_Module);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Deployment_Merge_Module, "06A35CCD-C46D-44D5-987B-CF40FF872267");
            __visualStudioProjectTypeSupported.Add("06A35CCD-C46D-44D5-987B-CF40FF872267", false);


            //Deployment Cab	 {3EA9E505-35AC-4774-B492-AD1749C4943A}
            __visualStudioProjectTypes.Add("3EA9E505-35AC-4774-B492-AD1749C4943A", VisualStudioProjectTypeEnum.Deployment_Cab);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Deployment_Cab, "3EA9E505-35AC-4774-B492-AD1749C4943A");
            __visualStudioProjectTypeSupported.Add("3EA9E505-35AC-4774-B492-AD1749C4943A", false);


            //Deployment Setup	 {978C614F-708E-4E1A-B201-565925725DBA}
            __visualStudioProjectTypes.Add("978C614F-708E-4E1A-B201-565925725DBA", VisualStudioProjectTypeEnum.Deployment_Setup);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Deployment_Setup, "978C614F-708E-4E1A-B201-565925725DBA");
            __visualStudioProjectTypeSupported.Add("978C614F-708E-4E1A-B201-565925725DBA", false);


            //Deployment Smart Device Cab	 {AB322303-2255-48EF-A496-5904EB18DA55}
            __visualStudioProjectTypes.Add("AB322303-2255-48EF-A496-5904EB18DA55", VisualStudioProjectTypeEnum.Deployment_Smart_Device_Cab);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Deployment_Smart_Device_Cab, "AB322303-2255-48EF-A496-5904EB18DA55");
            __visualStudioProjectTypeSupported.Add("AB322303-2255-48EF-A496-5904EB18DA55", false);


            //Visual Studio Tools for Applications (VSTA)	 {A860303F-1F3F-4691-B57E-529FC101A107}
            __visualStudioProjectTypes.Add("A860303F-1F3F-4691-B57E-529FC101A107", VisualStudioProjectTypeEnum.Visual_Studio_Tools_for_Applications__VSTA);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Visual_Studio_Tools_for_Applications__VSTA, "A860303F-1F3F-4691-B57E-529FC101A107");
            __visualStudioProjectTypeSupported.Add("A860303F-1F3F-4691-B57E-529FC101A107", false);


            //Visual Studio Tools for Office (VSTO)	 {BAA0C2D2-18E2-41B9-852F-F413020CAA33}
            __visualStudioProjectTypes.Add("BAA0C2D2-18E2-41B9-852F-F413020CAA33", VisualStudioProjectTypeEnum.Visual_Studio_Tools_for_Office__VSTO);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Visual_Studio_Tools_for_Office__VSTO, "BAA0C2D2-18E2-41B9-852F-F413020CAA33");
            __visualStudioProjectTypeSupported.Add("BAA0C2D2-18E2-41B9-852F-F413020CAA33", false);


            //SharePoint Workflow	 {F8810EC1-6754-47FC-A15F-DFABD2E3FA90}
            __visualStudioProjectTypes.Add("F8810EC1-6754-47FC-A15F-DFABD2E3FA90", VisualStudioProjectTypeEnum.SharePoint_Workflow);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.SharePoint_Workflow, "F8810EC1-6754-47FC-A15F-DFABD2E3FA90");
            __visualStudioProjectTypeSupported.Add("F8810EC1-6754-47FC-A15F-DFABD2E3FA90", false);
            
            //Microsoft Installer	 {54435603-DBB4-11D2-8724-00A0C9A8B90C}
            __visualStudioProjectTypes.Add("54435603-DBB4-11D2-8724-00A0C9A8B90C", VisualStudioProjectTypeEnum.Microsoft_Installer);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Microsoft_Installer, "54435603-DBB4-11D2-8724-00A0C9A8B90C");
            __visualStudioProjectTypeSupported.Add("54435603-DBB4-11D2-8724-00A0C9A8B90C", false);

            //Website_MVC {603C0E0B-DB56-11DC-BE95-000D561079B0}
            __visualStudioProjectTypes.Add("603C0E0B-DB56-11DC-BE95-000D561079B0", VisualStudioProjectTypeEnum.Website_MVC);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Website_MVC, "603C0E0B-DB56-11DC-BE95-000D561079B0");
            __visualStudioProjectTypeSupported.Add("603C0E0B-DB56-11DC-BE95-000D561079B0", true);
			
			//Model View Controller (MVC) {F85E285D-A4E0-4152-9332-AB1D724D3325}
            __visualStudioProjectTypes.Add("F85E285D-A4E0-4152-9332-AB1D724D3325", VisualStudioProjectTypeEnum.Model_View_Controller_MVC);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Model_View_Controller_MVC, "F85E285D-A4E0-4152-9332-AB1D724D3325");
            __visualStudioProjectTypeSupported.Add("F85E285D-A4E0-4152-9332-AB1D724D3325", true);

            //Model View Controller (MVC) {E53F8FEA-EAE0-44A6-8774-FFD645390401}
            __visualStudioProjectTypes.Add("E53F8FEA-EAE0-44A6-8774-FFD645390401", VisualStudioProjectTypeEnum.Model_View_Controller_MVC3);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Model_View_Controller_MVC3, "E53F8FEA-EAE0-44A6-8774-FFD645390401");
            __visualStudioProjectTypeSupported.Add("E53F8FEA-EAE0-44A6-8774-FFD645390401", true);
            
            //Windows Azure Project {CC5FD16D-436D-48AD-A40C-5A424C6E3E79}
            __visualStudioProjectTypes.Add("CC5FD16D-436D-48AD-A40C-5A424C6E3E79", VisualStudioProjectTypeEnum.WindowsAzure_CloudService);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.WindowsAzure_CloudService, "CC5FD16D-436D-48AD-A40C-5A424C6E3E79");
            __visualStudioProjectTypeSupported.Add("CC5FD16D-436D-48AD-A40C-5A424C6E3E79", true);

            //Silverlight Project {A1591282-1198-4647-A2B1-27E5FF5F6F3B}
            __visualStudioProjectTypes.Add("A1591282-1198-4647-A2B1-27E5FF5F6F3B", VisualStudioProjectTypeEnum.Silverlight);
            __visualStudioProjectTypeGuids.Add(VisualStudioProjectTypeEnum.Silverlight, "A1591282-1198-4647-A2B1-27E5FF5F6F3B");
            __visualStudioProjectTypeSupported.Add("A1591282-1198-4647-A2B1-27E5FF5F6F3B", true);
        }



        /// <summary>
        /// Gets the VisualStudioProjectTypeEnum of the given GUID (Global Unique Identifier)
        /// </summary>
        /// <param name="guid">VisualStudio Project Type GUID</param>
        /// <returns>VisualStudioProjectTypeEnum equivalent of the GUID</returns>
        public static VisualStudioProjectTypeEnum GetVisualStudioProjectType(string guid)
        {

            string strGuid = guid.Replace("{", "");
            strGuid = strGuid.Replace("}", "");


            VisualStudioProjectTypeEnum projectType = 0;

            foreach (string guidItem in strGuid.Split(';'))
            {
                string upperGuid = guidItem.ToUpper();
                Console.WriteLine("UG: " + upperGuid);
                if (!__visualStudioProjectTypes.ContainsKey(upperGuid))
                {
                    Console.WriteLine("UG WTF: " + upperGuid);

                    throw new NotSupportedException("Unknown project type GUID: " + guidItem);
                }
                Console.WriteLine("UG UG: " + upperGuid);
                projectType |= __visualStudioProjectTypes[upperGuid];
                if (!__visualStudioProjectTypeSupported[upperGuid])
                {
                    throw new NotSupportedException("NPanday does not support projects with type GUID: " + guidItem);
                }
            }

            return projectType;
        }

        public static string GetVisualStudioProjectTypeGuid(VisualStudioProjectTypeEnum visualStudioProjectTypeEnum)
        {
            List<string> list = new List<string>();
            foreach (VisualStudioProjectTypeEnum value in Enum.GetValues(typeof(VisualStudioProjectTypeEnum)))
            {
                if ((visualStudioProjectTypeEnum & value) == value)
                {
                    list.Add("{" + __visualStudioProjectTypeGuids[value] + "}");
                }
            }

            return string.Join(";", list.ToArray());
        }
    }
}
