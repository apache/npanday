
namespace org.apache.maven.it.unit {
	using NUnit.Framework;
	using System;
	using org.apache.maven.it;

	[TestFixture]
	public class It0003Test1  {
		private String hello = "hello";

		[SetUp]
		protected void SetUp() {
			new It0003();
		}

		[Test]
		public void TestSample() {
			new It0001();
			new It0002();
			Assert.AreEqual("hello", hello);
			It0003 it = new It0003();
			Assert.AreEqual("test", it.GetValue());
		}
		
	}

}