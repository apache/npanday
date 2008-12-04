using System;
using System.Collections.Generic;
using System.Text;

namespace NMaven.ProjectImporter.SlnParser.Model
{
    public class ProjectSection
    {
        string name;
        public string Name
        {
            get { return name; }
            set { name = value; }
        }

        string value;
        public string Value
        {
            get { return this.value; }
            set { this.value = value; }
        }

        Dictionary<string, string> map = new Dictionary<string, string>();
        public Dictionary<string, string> Map
        {
            get { return map; }
            set { map = value; }
        }
    }
}
