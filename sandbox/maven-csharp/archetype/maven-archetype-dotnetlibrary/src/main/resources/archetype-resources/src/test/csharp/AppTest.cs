using System;

using NUnit.Framework;

namespace $package
{
	/// <summary>
	/// A very simple example of a dotnet test.
	/// </summary>
	[TestFixture] public class AppTest
	{

		[Test] public void doTest()
		{
			Assert.AreEqual(true, true);
			Console.WriteLine("Who said Java and dotnet don\'t mix? :-)");
		}
		
		
	}
}