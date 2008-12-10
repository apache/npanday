using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace NPanday.Utils
{
    public class RepositoryUtility
    {
        public static bool InstallAssembly(string filename, string groupId, string artifactId, string version)
        {
            return InstallAssembly(filename, groupId, artifactId, version, false);
        }

        public static bool InstallAssembly(string filename, string groupId, string artifactId, string version, bool overwrite)
        {
            try
            {
                string m2Dir = Path.GetFullPath(string.Format("{0}\\..\\.m2", System.Environment.GetFolderPath(Environment.SpecialFolder.Personal)));
                string artifactDir = Path.Combine(m2Dir, string.Format(@"repository\{0}\{1}\{2}", groupId, artifactId, version));
                string artifactFilename = string.Format("{0}-{1}{2}", artifactId, version, Path.GetExtension(filename));

                if (!File.Exists(filename))
                    throw new Exception("Cannot find Assembly to install.");

                if (!Directory.Exists(artifactDir))
                    Directory.CreateDirectory(artifactDir);

                //if assembly already installed skip the copying
                if (File.Exists(Path.Combine(artifactDir, artifactFilename)))
                {
                    if (overwrite)
                    {
                        File.Delete(Path.Combine(artifactDir, artifactFilename));
                    }
                    else
                    {
                        return true;
                    }
                }

                //copy file
                File.Copy(filename, Path.Combine(artifactDir, artifactFilename));
                return true;

            }
            catch
            {
                throw;
            }
        }
    }
}
