using Microsoft.Build.BuildEngine;
using System.IO;

namespace NPanday.ProjectImporter.Digest.Algorithms
{
    public class BaseProjectDigestAlgorithm
    {
        public static string GetProjectAssemblyName(string projectFile)
        {
            Project project = ProjectDigester.GetProject(projectFile);

            if (project == null)
            {
                if (projectFile != null)
                    return Path.GetFileNameWithoutExtension(projectFile);

                return null;
            }

            foreach (BuildPropertyGroup buildPropertyGroup in project.PropertyGroups)
            {
                foreach (BuildProperty buildProperty in buildPropertyGroup)
                {
                    if (!buildProperty.IsImported && "AssemblyName".Equals(buildProperty.Name))
                    {
                        return buildProperty.Value;
                    }

                }
            }

            return null;
        }
    }
}