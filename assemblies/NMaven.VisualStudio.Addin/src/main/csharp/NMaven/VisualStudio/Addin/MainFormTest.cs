using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace NMaven.VisualStudio.Addin
{
    class MainFormTest
    {
        public static void Main()
        {
            AddArtifactsForm form = new AddArtifactsForm();
            form.Activate();
            Console.ReadLine();
           // Thread t = new Thread(startForm);
          //  t.Start();
         
            //while (true) { System.Threading.Thread.Sleep(1000); }
        }

        public static void startForm()
        {
            AddArtifactsForm form = new AddArtifactsForm();
            form.Show();
            //while (true) { System.Threading.Thread.Sleep(1000); }
        }
    }
}
