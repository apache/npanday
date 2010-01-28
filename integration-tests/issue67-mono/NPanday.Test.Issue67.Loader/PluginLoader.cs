using System;
using System.IO;
using System.Reflection;
using System.Runtime.Remoting;
using NPanday.Test.Issue67.Domain;

namespace NPanday.Test.Issue67.Loader
{
	/// <summary>
	/// Provides methods for loading plugins and for creating Mojos.
	/// </summary>
	internal sealed class PluginLoader
	{
		internal PluginLoader()
		{
		}

		/// <summary>
		/// Loads the specified plugin assembly file into the returned plugin application domain.
		/// </summary>
		/// <param name="pluginAssemblyFile">the.NET maven plugin</param>
		/// <returns>application domain for .NET maven plugin</returns>
		/// 
		internal AppDomain LoadPlugin(FileInfo pluginAssemblyFile)
		{
			Console.WriteLine("Loading Plugin: " + pluginAssemblyFile.DirectoryName);
			AppDomainSetup setup = new AppDomainSetup();
			setup.ApplicationBase = pluginAssemblyFile.DirectoryName;

			AppDomain applicationDomain = AppDomain.CreateDomain("Loader", null, setup);
			TestAppDomainManager pluginDomainManager = (TestAppDomainManager) applicationDomain.DomainManager;
            if (pluginDomainManager == null)
            {
                throw new Exception("-----Failed to find test app domain manager-----");
            }
            pluginDomainManager.LoadPlugin(pluginAssemblyFile);
			return applicationDomain;
		}

		public static int Main(string[] args)
		{
		    Console.WriteLine("-----Starting Plugin Loader-----");
			string assemblyFilePath = GetArgFor("assemblyFile", args);
            Console.WriteLine("Assembly File = " + assemblyFilePath);

			PluginLoader pluginLoader = new PluginLoader();
            try
            {
                pluginLoader.LoadPlugin(new FileInfo(assemblyFilePath));
            }
            catch (Exception e)
            {
                Console.Error.WriteLine(e.Message);
                Console.WriteLine("-----Test Failed----");
                return 1;
            } 
            Console.WriteLine("-----Ending Plugin Loader-----");
            Console.WriteLine("-----Test OK-----");
            return 0;

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
	}
}
