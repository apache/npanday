using System.IO;

namespace NPanday.VisualStudio.Addin
{
    public class WebReferencesClasses
    {
        private bool running;
        private string webRefPath;

        public WebReferencesClasses(string webRefPath)
        {
            this.webRefPath = webRefPath;
            this.running = true;
        }

        public void WaitForClasses(string nSpace)
        {
            while (running)
            { 
                //check if classes are generated
                string[] files = Directory.GetFiles(this.webRefPath);
                foreach (string file in files)
                {
                    if (file.Contains(".cs") || file.Contains(".vb"))
                    {
                        if (!string.IsNullOrEmpty(nSpace) && file.Contains(nSpace))
                        {
                            running = false;
                            break;
                        }
                        else
                        {
                            running = false;
                            break;
                        }
                    }
                }
            }
        }
    }
}