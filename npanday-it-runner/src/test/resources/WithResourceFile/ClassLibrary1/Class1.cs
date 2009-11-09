using System;
using System.Collections;
using System.Text;
using System.Resources;
using System.Windows.Forms;
using NUnit.Framework;

namespace ClassLibrary1
{
    [TestFixture]
    public class Class1
    {
        [Test]
        public void test()
        {
            // display string from resource file
            string appDir = System.Reflection.Assembly.GetExecutingAssembly().Location;
            System.Resources.ResXResourceReader reader = new ResXResourceReader(@"..\..\Resource1.resx");
            IDictionaryEnumerator rsxr = reader.GetEnumerator();

            int i = 1;
            foreach (DictionaryEntry d in reader)
            {
                Assert.AreEqual("Key" + i, d.Key.ToString());
                Assert.AreEqual("Value" + i, d.Value.ToString());
                i++;
            }
            //Close the reader.
            reader.Close();
        }
    }
}