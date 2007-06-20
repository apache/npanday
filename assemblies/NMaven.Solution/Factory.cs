using System;
using System.Collections.Generic;
using System.Text;

using NMaven.Solution.Impl;
[assembly: CLSCompliantAttribute(true)]
namespace NMaven.Solution
{
    [CLSCompliantAttribute(false)]
    public sealed class Factory
    {

        private Factory() { } 

        public static IProjectReference createDefaultProjectReference()
        {
            return new ProjectReferenceImpl();
        }

        public static IProjectGenerator createDefaultProjectGenerator()
        {
            return new ProjectGeneratorImpl();
        }
    }
}
