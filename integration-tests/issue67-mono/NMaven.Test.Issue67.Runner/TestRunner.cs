using System;
using System.Text;
using System.Diagnostics;

namespace NMaven.Test.Issue67.Runner
{
	public class TestRunner
	{
		public TestRunner()
		{
		}

		[STAThread]
		static void Main(string[] args)
		{
		    Console.WriteLine("-----Starting Test Runner-----");
			Console.WriteLine(@flattenArgs(args));
			String vendor = GetArgFor("vendor", args);
			String startProcessAssembly = @GetArgFor("startProcessAssembly", args);
			ProcessStartInfo processStartInfo = null;

			if(vendor != null && vendor.Equals("MONO"))
			{
                processStartInfo =
                    new ProcessStartInfo("mono", startProcessAssembly + " " + @flattenArgs(args));
            }
            else
            {
                processStartInfo =
                    new ProcessStartInfo(startProcessAssembly, @flattenArgs(args));
            }

			processStartInfo.EnvironmentVariables["APPDOMAIN_MANAGER_ASM"]
				= "NMaven.Test.Issue67.Domain, Version=0.0.0.0, PublicKeyToken=4b435f4d76e2f0e6, culture=neutral";
			processStartInfo.EnvironmentVariables["APPDOMAIN_MANAGER_TYPE"]
				= "NMaven.Test.Issue67.Domain.TestAppDomainManager";

			processStartInfo.UseShellExecute = false;
			Process.Start(processStartInfo);
            Console.WriteLine("-----Ending Test Runner-----");
		}

		private static string GetArgFor(string name, string[] args)
		{
			char[] delim = {'='};
			foreach(string arg in args)
			{
                string[] tokens = arg.Split(delim);
                if (tokens[0].Equals(name)) return tokens[1];
			}
            return null;
		}

		private static string flattenArgs(string[] args)
		{
			StringBuilder stringBuilder = new StringBuilder();
			foreach(string arg in args)
			{
				stringBuilder.Append(@"""").Append(@arg).Append(@"""").Append(" ");
			}
			return stringBuilder.ToString();
		}
	}
}
