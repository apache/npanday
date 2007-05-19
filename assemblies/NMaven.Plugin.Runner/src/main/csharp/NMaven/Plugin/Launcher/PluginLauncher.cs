using System;
using System.Text;
using System.Diagnostics;

namespace NMaven.Plugin.Launcher
{
	/// <summary>
	/// Description of PluginLauncher.
	/// </summary>
	public class PluginLauncher
	{
		public PluginLauncher()
		{
		}

		[STAThread]
		static void Main(string[] args)
		{

			Console.WriteLine("NMAVEN: Start Process = " + DateTime.Now);
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
				= "NMaven.Plugin, Version=0.14.0.0, PublicKeyToken=4b435f4d76e2f0e6, culture=neutral";
			processStartInfo.EnvironmentVariables["APPDOMAIN_MANAGER_TYPE"]
				= "NMaven.Plugin.PluginDomainManager";

			processStartInfo.UseShellExecute = false;
			Process.Start(processStartInfo);
            Console.WriteLine("NMAVEN: End Process = " + DateTime.Now);
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
				//Console.WriteLine("ARG {0}: ", arg);
				stringBuilder.Append(@"""").Append(@arg).Append(@"""").Append(" ");
			}
			return stringBuilder.ToString();
		}
	}
}
