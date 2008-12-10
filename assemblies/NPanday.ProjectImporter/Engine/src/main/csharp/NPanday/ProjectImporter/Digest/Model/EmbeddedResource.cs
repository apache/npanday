using System;
using System.Collections.Generic;
using System.Text;

using NPanday.ProjectImporter.Digest;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{


    public class EmbeddedResource : IncludeBase
    {
        public EmbeddedResource(string projectBasePath) 
            : base(projectBasePath)
        {
        }
        
        private string subType;
        public string SubType
        {
            get { return subType; }
            set { subType = value; }
        }

        private string dependentUpon;
        public string DependentUpon
        {
            get { return dependentUpon; }
            set { dependentUpon = value; }
        }


        private string generator;
        public string Generator
        {
            get { return generator; }
            set { generator = value; }
        }

        private string lastGenOutput;
        public string LastGenOutput
        {
            get { return lastGenOutput; }
            set { lastGenOutput = value; }
        }
            
    }
}
