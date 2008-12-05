using System;
using System.Collections.Generic;
using System.Text;

/// Author: Leopoldo Lee Agdeppa III

namespace NMaven.ProjectImporter.SlnParser
{
    public enum Semantics
    {
        PROJECT,
        END_PROJECT,
        PROJECT_SECTION,
        END_PROJECT_SECTION,
        GLOBAL,
        END_GLOBAL,
        GLOBAL_SECTION,
        END_GLOBAL_SECTION,

        QUOTED_STRING,
        EQUALS,
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        COMMA,
        STRING_VALUE,
        EOL

    }
}
