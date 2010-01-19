using System;
using System.Collections.Generic;
using System.Text;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Parser.SlnParser
{

    public class SemanticalData
    {
        public SemanticalData(Semantics token)
            : this(token, null)
        { }


        public SemanticalData(Semantics token, string value)
        {
            this.token = token;
            this.value = value;
        }


        Semantics token;
        public Semantics Token
        {
            get { return token; }
        }

        string value;
        public string Value
        {
            get { return this.value; }
        }
    }
}
