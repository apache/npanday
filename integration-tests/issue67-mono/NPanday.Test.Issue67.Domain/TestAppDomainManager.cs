using System;
using System.IO;
using System.Reflection;

namespace NPanday.Test.Issue67.Domain
{
    public sealed class TestAppDomainManager : AppDomainManager
    {
        /// <summary>
        /// Default constructor
        /// </summary>
        public TestAppDomainManager() : base()
        {
            Console.WriteLine("Creating Plugin Domain Manager");
        }

        public void LoadPlugin(FileInfo assemblyFile)
        {
            Assembly assembly = null;
            try
            {
                string assemblyName = assemblyFile.Name.Replace(assemblyFile.Extension,"");
                assembly = AppDomain.CurrentDomain.Load(assemblyName);
            }
            catch(FileNotFoundException e)
            {
                Console.WriteLine("FNE: " + e.Message);
                return;
            }
        }
    }
}