using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Parser.Solution
{
    public sealed class SolutionParser
    {
        public delegate List<Dictionary<string, object>> ParserAlgoDelegate(System.IO.FileInfo solutionFile);

        static ParserAlgoDelegate[] ALGORITHMS = 
        {
            new ProjectSolutionParser().Parse
        };


        public static List<Dictionary<string, object>> ParseSolution(FileInfo solutionFile)
        {
            List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();

            foreach (ParserAlgoDelegate algo in ALGORITHMS)
            {
               list.AddRange(algo(solutionFile));
            }

            return list;
        }

        
    }
}
