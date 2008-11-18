using System;
using System.Collections.Generic;
using System.Text;


using NMaven.ProjectImporter.Digest;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Digest.Model
{


    public class Compile : IncludeBase
    {
        public Compile(string projectBasePath) 
            : base(projectBasePath)
        {
        }


        private string autoGen;
        public string AutoGen
        {
            get { return autoGen; }
            set { autoGen = value; }
        }

        private string designTimeSharedInput;
        public string DesignTimeSharedInput
        {
            get { return designTimeSharedInput; }
            set { designTimeSharedInput = value; }
        }

        private string dependentUpon;
        public string DependentUpon
        {
            get { return dependentUpon; }
            set { dependentUpon = value; }
        }

        private string designTime;
        public string DesignTime
        {
            get { return designTime; }
            set { designTime = value; }
        }

        private string subType;
        public string SubType
        {
            get { return subType; }
            set { subType = value; }
        }
    }
}
