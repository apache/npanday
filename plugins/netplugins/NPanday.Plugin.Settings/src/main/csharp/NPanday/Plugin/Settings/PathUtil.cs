using System.IO;

namespace NPanday.Plugin.Settings
{
    public static class PathUtil
    {
        public static string FirstExisting(params string[] probingPaths)
        {
            foreach (string dir in probingPaths)
            {
                if (Directory.Exists(dir))
                    return dir;
            }

            return null;
        }

        public static string FirstContainingFile(string fileToBeContained, params string[] probingPaths)
        {
            foreach (string dir in probingPaths)
            {
                if (File.Exists(Path.Combine(dir, fileToBeContained)))
                    return dir;
            }

            return null;
        }
    }
}
