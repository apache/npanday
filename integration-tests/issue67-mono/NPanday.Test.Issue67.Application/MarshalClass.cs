using System;
using System.IO;
using System.Reflection;

namespace NPanday.Test.Issue67.Application
{
	public class MarshalClass : MarshalByRefObject
    {
        public void Execute()
        {
            Console.WriteLine("Executed the Test Class.");
        }
    }
}
