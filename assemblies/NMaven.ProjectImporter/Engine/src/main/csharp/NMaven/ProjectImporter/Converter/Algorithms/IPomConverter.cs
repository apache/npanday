using System;
using System.Collections.Generic;
using System.Text;


using NMaven.ProjectImporter.Digest;
using NMaven.ProjectImporter.Digest.Model;
using NMaven.Utils;
using NMaven.Model.Pom;

using NMaven.Artifact;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Converter.Algorithms
{
    public interface IPomConverter
    {
        void ConvertProjectToPomModel(bool writePom);
        void ConvertProjectToPomModel();

        NMaven.Model.Pom.Model Model
        {
            get;
        }

    }
}
