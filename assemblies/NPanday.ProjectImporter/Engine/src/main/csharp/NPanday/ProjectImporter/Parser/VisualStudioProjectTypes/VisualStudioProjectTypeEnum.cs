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
        Windows__CSharp = 1,


        /// <summary>
        /// Project Type: Windows (VB.NET), 
        /// GUID: {F184B08F-C81C-45F6-A57F-5ABD9991F28F}
        /// </summary>
        Windows__VbDotNet = 2, 
        
        /// <summary>
        /// Project Type: Windows (Visual C++), 
        /// GUID: {8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942}
        /// </summary>
        Windows__VCpp = 4,

        /// <summary>
        /// Project Type: Web Application, 
        /// GUID: {349C5851-65DF-11DA-9384-00065B846F21}
        /// </summary>
        Web_Application = 8,
        
        /// <summary>
        /// Project Type: Web Site, 
        /// GUID: {E24C65DC-7377-472B-9ABA-BC803B73C61A}
        /// </summary>
        Web_Site = 16,
        
        /// <summary>
        /// Project Type: Distributed System, 
        /// GUID: {F135691A-BF7E-435D-8960-F99683D2D49C}
        /// </summary>
        Distributed_System = 32,
        
        /// <summary>
        /// Project Type: Windows Communication Foundation (WCF), 
        /// GUID: {3D9AD99F-2412-4246-B90B-4EAA41C64699}
        /// </summary>
        Windows_Communication_Foundation__WCF = 64, 
        
        /// <summary>
        /// Project Type: Windows Presentation Foundation (WPF), 
        /// GUID: {60DC8134-EBA5-43B8-BCC9-BB4BC16C2548}
        /// </summary>
        Windows_Presentation_Foundation__WPF = 128, 
        
        /// <summary>
        /// Project Type: Visual Database Tools, 
        /// GUID: {C252FEB5-A946-4202-B1D4-9916A0590387}
        /// </summary>
        Visual_Database_Tools = 256, 
        
        /// <summary>
        /// Project Type: Database, 
        /// GUID: {A9ACE9BB-CECE-4E62-9AA4-C7E7C5BD2124}
        /// </summary>
        Database = 512, 
        
        /// <summary>
        /// Project Type: Database (other project types), 
        /// GUID: {4F174C21-8C12-11D0-8340-0000F80270F8}
        /// </summary>
        Database__other_project_types = 1024, 
        
        /// <summary>
        /// Project Type: Test, 
        /// GUID: {3AC096D0-A1C2-E12C-1390-A8335801FDAB}
        /// </summary>
        Test = 2048, 
        
        /// <summary>
        /// Project Type: Legacy (2003) Smart Device (C#), 
        /// GUID: {20D4826A-C6FA-45DB-90F4-C717570B9F32}
        /// </summary>
        Legacy__2003_Smart_Device__CSharp = 4096, 
        
        /// <summary>
        /// Project Type: Legacy (2003) Smart Device (VB.NET), 
        /// GUID: {CB4CE8C6-1BDB-4DC7-A4D3-65A1999772F8}
        /// </summary>
        Legacy__2003_Smart_Device__VbDotNet = 8192, 
        
        /// <summary>
        /// Project Type: Smart Device (C#), 
        /// GUID: {4D628B5B-2FBC-4AA6-8C16-197242AEB884}
        /// </summary>
        Smart_Device__CSharp = 16384, 
        
        /// <summary>
        /// Project Type: Smart Device (VB.NET), 
        /// GUID: {68B1623D-7FB9-47D8-8664-7ECEA3297D4F}
        /// </summary>
        Smart_Device__VbDotNet = 32768, 
        
        /// <summary>
        /// Project Type: Workflow (C#), 
        /// GUID: {14822709-B5A1-4724-98CA-57A101D1B079}
        /// </summary>
        Workflow__CSharp = 65536, 
        
        /// <summary>
        /// Project Type: Workflow (VB.NET), 
        /// GUID: {D59BE175-2ED0-4C54-BE3D-CDAA9F3214C8}
        /// </summary>
        Workflow__VbDotNet = 131072, 
        
        /// <summary>
        /// Project Type: Deployment Merge Module, 
        /// GUID: {06A35CCD-C46D-44D5-987B-CF40FF872267}
        /// </summary>
        Deployment_Merge_Module = 262144, 
        
        /// <summary>
        /// Project Type: Deployment Cab, 
        /// GUID: {3EA9E505-35AC-4774-B492-AD1749C4943A}
        /// </summary>
        Deployment_Cab = 524288, 
        
        /// <summary>
        /// Project Type: Deployment Setup, 
        /// GUID: {978C614F-708E-4E1A-B201-565925725DBA}
        /// </summary>
        Deployment_Setup = 1048576, 
        
        /// <summary>
        /// Project Type: Deployment Smart Device Cab, 
        /// GUID: {AB322303-2255-48EF-A496-5904EB18DA55}
        /// </summary>
        Deployment_Smart_Device_Cab = 2097152, 
        
        /// <summary>
        /// Project Type: Visual Studio Tools for Applications (VSTA), 
        /// GUID: {A860303F-1F3F-4691-B57E-529FC101A107}
        /// </summary>
        Visual_Studio_Tools_for_Applications__VSTA = 4194304, 
        
        /// <summary>
        /// Project Type: Visual Studio Tools for Office (VSTO), 
        /// GUID: {BAA0C2D2-18E2-41B9-852F-F413020CAA33}
        /// </summary>
        Visual_Studio_Tools_for_Office__VSTO = 8388608, 
        
        /// <summary>
        /// Project Type: SharePoint Workflow, 
        /// GUID: {F8810EC1-6754-47FC-A15F-DFABD2E3FA90}
        /// </summary>
        SharePoint_Workflow = 16777216,

        /// <summary>
        /// Project Type: Microsoft Installer, 
        /// GUID: {54435603-DBB4-11D2-8724-00A0C9A8B90C}
        /// </summary>
        Microsoft_Installer = 33554432,

        /// <summary>
        /// Project Type: MvcApplication C#, 
        /// GUID: {8BFE4558-546D-4A7F-9F81-C9ED6C262C7A}
        /// </summary>
        MvcApplication_CSharp = 3,

        /// <summary>
        /// Project Type: WcfService C#, 
        /// GUID: {72EC2439-1192-4FA1-8378-DF92A3AC699F}
        /// </summary>
        WcfService_CSharp = 9,

        /// <summary>
        /// Project Type: WpfControlLibrary C#, 
        /// GUID: {44003C12-25E0-477E-8D80-540845649931}
        /// </summary>
        WpfControlLibrary_CSharp = 18,

        /// <summary>
        /// Project Type: WpfBrowserApplication C#, 
        /// GUID: {12F25641-2540-4B7A-97E7-72FA722017A1}
        /// </summary>
        WpfBrowserApplication_CSharp = 36,

        /// <summary>
        /// Project Type: WpfCustomControlLibrary C#, 
        /// GUID: {C968A6D7-E8DD-4A5B-AE08-CB0CE06B0CF5}
        /// </summary>
        WpfCustomControlLibrary_CSharp = 72,

        /// <summary>
        /// Project Type: MvcApplication VB, 
        /// GUID: {8D8DAB46-DB23-4502-8948-DCF34D6A51FF}
        /// </summary>
        MvcApplication_VB = 144,

        /// <summary>
        /// Project Type: MvcApplicationTest VB, 
        /// GUID: {55CDCB5B-CC2B-43A5-A550-466D32066F35}
        /// </summary>
        MvcApplicationTest_VB = 288,

        /// <summary>
        /// Project Type: WcfService VB, 
        /// GUID: {79B52CBC-145B-4356-9404-FC5970145EB2}
        /// </summary>
        WcfService_VB = 576,

        /// <summary>
        /// Project Type: WpfApplication VB, 
        /// GUID: {928ADDCC-90A6-4FEA-8090-6BF3C296131B}
        /// </summary>
        WpfApplication_VB = 1152,

        /// <summary>
        /// Project Type: WpfBrowserApplication VB, 
        /// GUID: {F927C28D-3A52-49CD-9FDD-BA8825EFEC6C}
        /// </summary>
        WpfBrowserApplication_VB = 2304,
    }
}
