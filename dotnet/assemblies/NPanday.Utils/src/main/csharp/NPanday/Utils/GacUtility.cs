using System;
using System.Collections.Generic;
using System.Text;

using System.Diagnostics;

using System.Text.RegularExpressions;

using System.Reflection;
using System.IO;

namespace NPanday.Utils
{
    public class GacUtility
    {
        private string gacs = "";
        private string vbRsp = "";
        private string csRsp = "";

        public GacUtility()
        {
            Process p = new Process();

            try
            {
                p.StartInfo.FileName = "gacutil.exe";
                p.StartInfo.Arguments = "/l";
                p.StartInfo.UseShellExecute = false;
                p.StartInfo.ErrorDialog = false;
                p.StartInfo.CreateNoWindow = true;
                p.StartInfo.RedirectStandardOutput = true; 
                p.Start();

                System.IO.StreamReader oReader2 = p.StandardOutput;
    
                gacs = oReader2.ReadToEnd();
    
                oReader2.Close();

                p.WaitForExit();
            }
            catch ( Exception exception )
            {
                throw new Exception( "Unable to execute gacutil - check that your PATH has been set correctly (Message: " + exception.Message + ")" );
            }


            string msBuildPath = Path.GetDirectoryName(System.Reflection.Assembly.GetAssembly(typeof(string)).Location);
            string f35 = Path.GetFullPath(Environment.SystemDirectory + @"\..\Microsoft.NET\Framework\v3.5");
            string f4 = Path.GetFullPath(Environment.SystemDirectory + @"\..\Microsoft.NET\Framework\v4.0.30319");
            if (Directory.Exists(f4))
            {
                msBuildPath = f4;
            }
            try
            {
                csRsp = File.OpenText(msBuildPath + @"\csc.rsp").ReadToEnd();
            }
            catch (Exception){}


            try
            {
                vbRsp = File.OpenText(msBuildPath + @"\vbc.rsp").ReadToEnd();
            }
            catch (Exception){}



        }


        public string GetAssemblyInfo(string assemblyName, string version, string processorArchitecture)
        {
            if (string.IsNullOrEmpty(assemblyName))
            {
                return null;
            }

            string architecture = String.Empty;
            if (! string.IsNullOrEmpty(processorArchitecture))
            {
                architecture = GetRegexProcessorArchitectureFromString(processorArchitecture);
            }

            Regex regex;
            if (string.IsNullOrEmpty(version))
            {
                regex = new Regex(@"\s*" + assemblyName + @",.*processorArchitecture=" + architecture + ".*", RegexOptions.IgnoreCase);

            }
            else
            {
                regex = new Regex(@"\s*" + assemblyName + @",\s*Version=" + Regex.Escape(version) + @".*processorArchitecture=" + architecture + ".*", RegexOptions.IgnoreCase);
            }

            MatchCollection matches = regex.Matches(gacs);

            foreach (Match match in matches)
            {
                return match.Value.Trim();
            }


            return null;
        }

        public bool IsRspIncluded(string assemblyName, string language)
        {
            if ("vb".Equals(language, StringComparison.OrdinalIgnoreCase))
            {
                return IsVbcRspIncluded(assemblyName);
            }
            else
            {
                return IsCscRspIncluded(assemblyName);
            }
        }
        


        public bool IsCscRspIncluded(string assemblyName)
        {
            if (string.IsNullOrEmpty(assemblyName))
            {
                return false;
            }

            Regex regex = new Regex(@"\s*/r:" + assemblyName + @"\.dll", RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(csRsp);


            foreach (Match match in matches)
            {
                return true;
            }


            return false;
        }


        public bool IsVbcRspIncluded(string assemblyName)
        {
            if (string.IsNullOrEmpty(assemblyName))
            {
                return false;
            }

            Regex regex = new Regex(@"\s*/r:" + assemblyName + @"\.dll", RegexOptions.IgnoreCase);
            MatchCollection matches = regex.Matches(csRsp);


            foreach (Match match in matches)
            {
                return true;
            }


            return false;
        }

        private static string GetRegexProcessorArchitectureFromString(string input)
        {
            switch (input)
            {
                case "x64":
                    return "(AMD64|MSIL)";
                case "Itanium":
                    return "(IA64|MSIL)";
                case "x86":
                    return "(x86|MSIL)";
                case "AnyCPU":
                    return "(x86|MSIL)";
                default:
                    return input;
            }
        }
    }
}
