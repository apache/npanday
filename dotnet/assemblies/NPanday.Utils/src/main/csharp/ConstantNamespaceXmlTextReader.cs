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