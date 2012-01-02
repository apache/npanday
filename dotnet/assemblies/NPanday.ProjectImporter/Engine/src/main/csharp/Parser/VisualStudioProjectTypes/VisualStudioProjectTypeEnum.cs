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

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Parser.VisualStudioProjectTypes
{
    [Flags]
    public enum VisualStudioProjectTypeEnum : long
    {
        /// <summary>
        /// Project Type: Windows (C#), 
        /// GUID: {FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}
        /// </summary>
        Windows__CSharp = 1 << 0,


        /// <summary>
        /// Project Type: Windows (VB.NET), 
        /// GUID: {F184B08F-C81C-45F6-A57F-5ABD9991F28F}
        /// </summary>
        Windows__VbDotNet = 1 << 1,

        /// <summary>
        /// Project Type: Windows (Visual C++), 
        /// GUID: {8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942}
        /// </summary>
        Windows__VCpp = 1 << 2,

        /// <summary>
        /// Project Type: Web Application, 
        /// GUID: {349C5851-65DF-11DA-9384-00065B846F21}
        /// </summary>
        Web_Application = 1 << 3,

        /// <summary>
        /// Project Type: Web Site, 
        /// GUID: {E24C65DC-7377-472B-9ABA-BC803B73C61A}
        /// </summary>
        Web_Site = 1 << 4,

        /// <summary>
        /// Project Type: Distributed System, 
        /// GUID: {F135691A-BF7E-435D-8960-F99683D2D49C}
        /// </summary>
        Distributed_System = 1 << 5,

        /// <summary>
        /// Project Type: Windows Communication Foundation (WCF), 
        /// GUID: {3D9AD99F-2412-4246-B90B-4EAA41C64699}
        /// </summary>
        Windows_Communication_Foundation__WCF = 1 << 6,

        /// <summary>
        /// Project Type: Windows Presentation Foundation (WPF), 
        /// GUID: {60DC8134-EBA5-43B8-BCC9-BB4BC16C2548}
        /// </summary>
        Windows_Presentation_Foundation__WPF = 1 << 7,

        /// <summary>
        /// Project Type: Visual Database Tools, 
        /// GUID: {C252FEB5-A946-4202-B1D4-9916A0590387}
        /// </summary>
        Visual_Database_Tools = 1 << 8,

        /// <summary>
        /// Project Type: Database, 
        /// GUID: {A9ACE9BB-CECE-4E62-9AA4-C7E7C5BD2124}
        /// </summary>
        Database = 1 << 9,

        /// <summary>
        /// Project Type: Database (other project types), 
        /// GUID: {4F174C21-8C12-11D0-8340-0000F80270F8}
        /// </summary>
        Database__other_project_types = 1 << 10,

        /// <summary>
        /// Project Type: Test, 
        /// GUID: {3AC096D0-A1C2-E12C-1390-A8335801FDAB}
        /// </summary>
        Test = 1 << 11,

        /// <summary>
        /// Project Type: Legacy (2003) Smart Device (C#), 
        /// GUID: {20D4826A-C6FA-45DB-90F4-C717570B9F32}
        /// </summary>
        Legacy__2003_Smart_Device__CSharp = 1 << 12,

        /// <summary>
        /// Project Type: Legacy (2003) Smart Device (VB.NET), 
        /// GUID: {CB4CE8C6-1BDB-4DC7-A4D3-65A1999772F8}
        /// </summary>
        Legacy__2003_Smart_Device__VbDotNet = 1 << 13,

        /// <summary>
        /// Project Type: Smart Device (C#), 
        /// GUID: {4D628B5B-2FBC-4AA6-8C16-197242AEB884}
        /// </summary>
        Smart_Device__CSharp = 1 << 14,

        /// <summary>
        /// Project Type: Smart Device (VB.NET), 
        /// GUID: {68B1623D-7FB9-47D8-8664-7ECEA3297D4F}
        /// </summary>
        Smart_Device__VbDotNet = 1 << 15,

        /// <summary>
        /// Project Type: Workflow (C#), 
        /// GUID: {14822709-B5A1-4724-98CA-57A101D1B079}
        /// </summary>
        Workflow__CSharp = 1 << 16,

        /// <summary>
        /// Project Type: Workflow (VB.NET), 
        /// GUID: {D59BE175-2ED0-4C54-BE3D-CDAA9F3214C8}
        /// </summary>
        Workflow__VbDotNet = 1 << 17,

        /// <summary>
        /// Project Type: Deployment Merge Module, 
        /// GUID: {06A35CCD-C46D-44D5-987B-CF40FF872267}
        /// </summary>
        Deployment_Merge_Module = 1 << 18,

        /// <summary>
        /// Project Type: Deployment Cab, 
        /// GUID: {3EA9E505-35AC-4774-B492-AD1749C4943A}
        /// </summary>
        Deployment_Cab = 1 << 19,

        /// <summary>
        /// Project Type: Deployment Setup, 
        /// GUID: {978C614F-708E-4E1A-B201-565925725DBA}
        /// </summary>
        Deployment_Setup = 1 << 20,

        /// <summary>
        /// Project Type: Deployment Smart Device Cab, 
        /// GUID: {AB322303-2255-48EF-A496-5904EB18DA55}
        /// </summary>
        Deployment_Smart_Device_Cab = 1 << 21,

        /// <summary>
        /// Project Type: Visual Studio Tools for Applications (VSTA), 
        /// GUID: {A860303F-1F3F-4691-B57E-529FC101A107}
        /// </summary>
        Visual_Studio_Tools_for_Applications__VSTA = 1 << 22,

        /// <summary>
        /// Project Type: Visual Studio Tools for Office (VSTO), 
        /// GUID: {BAA0C2D2-18E2-41B9-852F-F413020CAA33}
        /// </summary>
        Visual_Studio_Tools_for_Office__VSTO = 1 << 23,

        /// <summary>
        /// Project Type: SharePoint Workflow, 
        /// GUID: {F8810EC1-6754-47FC-A15F-DFABD2E3FA90}
        /// </summary>
        SharePoint_Workflow = 1 << 24,

        /// <summary>
        /// Project Type: Microsoft Installer, 
        /// GUID: {54435603-DBB4-11D2-8724-00A0C9A8B90C}
        /// </summary>
        Microsoft_Installer = 1 << 25,

        /// <summary>
        /// Project Type: Website MVC, 
        /// GUID: {603c0e0b-db56-11dc-be95-000d561079b0}
        /// </summary>
        Website_MVC = 3,


        /// <summary>
        /// Project Type: ASP MVC,
        /// GUID: {69150728-AFCB-45A3-9D78-D96A5E0F1A27}
        /// </summary>
        Model_View_Controller_MVC = 1 << 26,

        /// <summary>
        /// Project Type: ASP MVC 3,
        /// GUID: {E53F8FEA-EAE0-44A6-8774-FFD645390401}
        /// </summary>
        Model_View_Controller_MVC3 = 1 << 27,

        /// <summary>
        /// Project Type: Windows Azure Cloud Service
        /// GUID: {CC5FD16D-436D-48AD-A40C-5A424C6E3E797}
        /// </summary>
        WindowsAzure_CloudService = 1 << 28,

        // Aux types

        WebDeploy2 = 1 << 29,

        WindowsAzure_Worker = 1 << 30,
        // TODO: if we get to 31 we'll run out, refactor so this is an enum of the GUIDs instead
    }
}
