//
//  Copyright 2006 Shane Isbell
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

using System;

namespace NMaven.Utility.ResX
{
    /// <summary>
    /// Provides mime-type information
    /// </summary>
    internal class MimeType
    {
        private string subType;

        private string type;

        private string extension;

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="type"></param>
        /// <param name="subType"></param>
        /// <param name="extension">the extension of a file with the mime-type</param>
        
        internal MimeType(string type, string subType, string extension)
        {
            this.type = type;
            this.subType = subType;
            this.extension = extension;
        }

        internal string GetSubType()
        {
            return subType;
        }

        internal string GetPrimaryType()
        {
            return type;
        }

        internal string GetExtension()
        {
            return extension;
        }

        public override string ToString()
        {
            return "SubType = " + subType + ", Type = " + type + ", Extension = " + extension;
        }
    }
}