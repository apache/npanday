using System;
using System.Collections.Generic;
using System.Text;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Parser.SlnParser.Model
{
    public class Global
    {
        List<GlobalSection> globalSections = new List<GlobalSection>();

        public List<GlobalSection> GlobalSections
        {
            get { return globalSections; }
            set { globalSections = value; }
        }
    }
}
