using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace ConnectTest.UtilsTest
{
    public class FileUtils
    {
        public static string getBaseDirectory()
        {
            return new FileInfo(Directory.GetCurrentDirectory().Substring(0, Directory.GetCurrentDirectory().LastIndexOf("target"))).FullName;
        }

        public static string getLocalRepository()
        {
            string homePath = string.Empty;
            if (Environment.OSVersion.Platform == PlatformID.Unix || Environment.OSVersion.Platform == PlatformID.MacOSX)
            {
                homePath = Environment.GetEnvironmentVariable("HOME");
            }
            else
            {
                homePath = Environment.ExpandEnvironmentVariables("%HOMEDRIVE%%HOMEPATH%");

                if (homePath == null || homePath == string.Empty)
                {
                    homePath = Environment.GetEnvironmentVariable("USERPROFILE");
                }
            }

            return new FileInfo(homePath + "\\.m2").FullName;

        }

        public static void CopyDirectory(DirectoryInfo source, DirectoryInfo destination)
        {
            if (!destination.Exists)
            {
                destination.Create();
            }

            FileInfo[] files = source.GetFiles();
            foreach (FileInfo filePath in files)
            {
                if (filePath.Name != null && !filePath.Name.EndsWith(".test"))
                    filePath.CopyTo(Path.Combine(destination.FullName, filePath.Name));
            }

            DirectoryInfo[] subDirectories = source.GetDirectories();
            foreach (DirectoryInfo dirPath in subDirectories)
            {
                if (!dirPath.Name.Equals(".svn"))
                {
                    CopyDirectory(new DirectoryInfo(Path.Combine(source.FullName, dirPath.Name)), new DirectoryInfo(Path.Combine(destination.FullName, dirPath.Name)));
                }
            }
        }
    }
}
