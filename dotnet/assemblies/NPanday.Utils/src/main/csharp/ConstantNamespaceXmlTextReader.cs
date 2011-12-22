#region Apache License, Version 2.0
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
#endregion
using System;
using System.Xml;

namespace NPanday.Utils
{
    /// <summary>
    /// An XmlTextReader that overrides the namespaces to always be empty, effectively ignoring them when deserializing
    /// This avoids inconsistencies in Maven settings.xml files that sometimes include the namespace declaration and
    /// sometimes omit it.
    /// </summary>
    /// <seealso cref="http://stackoverflow.com/questions/870293/can-i-make-xmlserializer-ignore-the-namespace-on-deserialization"/>
    public class ConstantNamespaceXmlTextReader : XmlTextReader
    {
        private readonly string _fromNs;
        private readonly string _toNs;

        public ConstantNamespaceXmlTextReader(System.IO.TextReader reader, string fromNs, string toNs)
            : base(reader)
        {
            _fromNs = fromNs;
            _toNs = toNs;
        }

        public override string NamespaceURI
        {
            get
            {
                if (base.NamespaceURI == _fromNs)
                    return _toNs;

                return base.NamespaceURI;
            }
        }

        public override string Value
        {
            get
            {
                if (base.Value == _fromNs)
                {
                    return _toNs;
                }
                return base.Value;
            }
        }
    }
}