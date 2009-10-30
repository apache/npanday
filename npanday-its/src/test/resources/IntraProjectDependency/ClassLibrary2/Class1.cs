using System;
using System.Collections.Generic;
using System.Text;

namespace ClassLibrary2
{
    public class Class1
    {
        public Class1()
        {
            ClassLibrary1.Class1 cls = new ClassLibrary1.Class1();
            cls.Display();
        }

        public void Display()
        {
            // do nothing
        }
    }
}
