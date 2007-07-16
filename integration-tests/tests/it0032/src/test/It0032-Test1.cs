
namespace org.apache.maven.it.unit {
	using NUnit.Framework;
	using System;
	using org.apache.maven.it;

	[TestFixture]
	public class It0032Test1  {
		private String hello = "hello";

		[SetUp]
		protected void SetUp() {
			new It0032();
		}

		[Test]
		public void TestSample() {
			Assert.AreEqual("hello", hello);
		}
		
	}

}
