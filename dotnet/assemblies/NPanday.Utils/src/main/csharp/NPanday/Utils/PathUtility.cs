using System;
using System.Collections.Generic;
using System.IO;

namespace NPanday.Utils
{
    /// <summary>
    /// Whereever NPanday needs to construct or parse a file or directory path, it should happen here.
    /// </summary>
    public class PathUtility
    {
        /// <summary>
        /// Usually all maven projects must have a pom.xml. You can specify a different name
        /// via a command line parameter, though.
        /// </summary>
        public static readonly string DefaultPomFileName = "pom.xml";

        /// <summary>
        /// Instead of using this constant, we rather find a way to get the build directory from
        /// the pom, as it can be overridden there.
        /// </summary>
        public static readonly string DefaultBuildDirectoryName = "target";

        /// <summary>
        /// Tries to find the project root for the given file or directory, by locating the first pom.xml on the way up.
        /// </summary>
        public static DirectoryInfo FindProjectRoot(FileSystemInfo projectFileOrDirectory)
        {
            DirectoryInfo projectRoot;
            if (TryFindProjectRoot(projectFileOrDirectory, out projectRoot))
            {
                return projectRoot;
            }
            else
            {
                throw new Exception("Could not find project root for " + projectFileOrDirectory.FullName);
            }
        }

        public static bool TryFindProjectRoot(FileSystemInfo projectFileOrDirectory, out DirectoryInfo projectRoot)
        {
            if (projectFileOrDirectory == null) throw new ArgumentNullException("projectFileOrDirectory");

            DirectoryInfo directory = projectFileOrDirectory as DirectoryInfo;

            if (directory == null)
            {
                directory = ((FileInfo)projectFileOrDirectory).Directory;
            }

            if (directory.GetFiles(DefaultPomFileName).Length == 1)
            {
                projectRoot = directory;
                return true;
            }

            if (directory.Parent == null)
            {
                projectRoot = null;
                return false;
            }

            return TryFindProjectRoot(directory.Parent, out projectRoot);
        }

        public static DirectoryInfo GetBuildDirectory(DirectoryInfo projectRoot)
        {
            return new DirectoryInfo(Path.Combine(projectRoot.FullName, DefaultBuildDirectoryName));
        }
    }
}