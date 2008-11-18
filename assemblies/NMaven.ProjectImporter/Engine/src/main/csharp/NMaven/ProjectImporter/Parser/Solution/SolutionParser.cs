using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Parser.Solution
{
    public sealed class SolutionParser
    {
        static ISolutionParserAlgorithm[] ALGORITHMS = 
        {
            new ProjectSolutionParserWithInnerWebsitePropertiesDataAlgorithm(),
            new ProjectSolutionParserWithoutInnerDataAlgorithm()
        };


        public static List<Dictionary<string, object>> ParseSolution(FileInfo solutionFile)
        {
            List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();

            foreach (ISolutionParserAlgorithm algo in ALGORITHMS)
            {
               list.AddRange(algo.Parse(solutionFile));
            }

            return list;
        }

        
    }
}
