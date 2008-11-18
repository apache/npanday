using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

using NMaven.ProjectImporter.Digest;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Digest.Model
{

    public class None : IncludeBase
    {
        public None(string projectBasePath) 
            : base(projectBasePath)
        {
        }


        private string link;
        public string Link
        {
            get { return link; }
            set { link = value; }
        }

        public string LinkFullPath
        {
            get
            {
                if (Path.IsPathRooted(link))
                {
                    return Path.GetFullPath(link);
                }
                else
                {
                    return Path.GetFullPath(projectBasePath + @"\" + link);
                }

            }
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

        private string dependentUpon;
        public string DependentUpon
        {
            get { return dependentUpon; }
            set { dependentUpon = value; }
        }

    }
}
