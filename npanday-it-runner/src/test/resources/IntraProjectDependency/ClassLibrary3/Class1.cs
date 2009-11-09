using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;

namespace ClassLibrary3
{
    [TestFixture]
    public class Class1
    {
        [Test]
        public void test()
        {
            ClassLibrary1.Class1 cls1 = new ClassLibrary1.Class1();
            ClassLibrary2.Class1 cls2 = new ClassLibrary2.Class1();

            cls1.Display();
            cls2.Display();
        }
    }
}
