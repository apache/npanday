using System;
using System.Collections.Generic;
using System.Text;


using NPanday.ProjectImporter.Digest;
using NPanday.ProjectImporter.Digest.Model;
using NPanday.Utils;
using NPanday.Model.Pom;

using NPanday.Artifact;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Converter.Algorithms
{
    public interface IPomConverter
    {
        void ConvertProjectToPomModel(bool writePom, string scmTag);
        void ConvertProjectToPomModel(string scmTag);

        NPanday.Model.Pom.Model Model
        {
            get;
        }

    }
}
