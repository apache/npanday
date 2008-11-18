using System;
using System.Collections.Generic;
using System.Text;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.Validator
{
    public enum ProjectStructureType
    {
        AbnormalProject = 0,
        NormalSingleProject = 1,
        NormalMultiModuleProject = 2,
        FlatSingleModuleProject = 3,
        FlatMultiModuleProject = 4
    }
}
