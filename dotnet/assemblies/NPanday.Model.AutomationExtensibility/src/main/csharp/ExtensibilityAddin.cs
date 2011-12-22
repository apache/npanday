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
namespace NPanday.Model
{
    /// <remarks/>
    [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "2.0.50727.42")]
    [System.SerializableAttribute()]
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.ComponentModel.DesignerCategoryAttribute("code")]
    [System.Xml.Serialization.XmlTypeAttribute(AnonymousType=true)]
    public partial class ExtensibilityAddin {
        
        private string[] itemsField;
        
        private ItemsChoiceType1[] itemsElementNameField;
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("AboutBoxDetails", typeof(string))]
        [System.Xml.Serialization.XmlElementAttribute("AboutIconData", typeof(string))]
        [System.Xml.Serialization.XmlElementAttribute("Assembly", typeof(string))]
        [System.Xml.Serialization.XmlElementAttribute("CommandLineSafe", typeof(string), DataType="integer")]
        [System.Xml.Serialization.XmlElementAttribute("CommandPreload", typeof(string), DataType="integer")]
        [System.Xml.Serialization.XmlElementAttribute("Description", typeof(string))]
        [System.Xml.Serialization.XmlElementAttribute("FriendlyName", typeof(string))]
        [System.Xml.Serialization.XmlElementAttribute("FullClassName", typeof(string))]
        [System.Xml.Serialization.XmlElementAttribute("LoadBehavior", typeof(string), DataType="integer")]
        [System.Xml.Serialization.XmlChoiceIdentifierAttribute("ItemsElementName")]
        public string[] Items {
            get {
                return this.itemsField;
            }
            set {
                this.itemsField = value;
            }
        }
        
        /// <remarks/>
        [System.Xml.Serialization.XmlElementAttribute("ItemsElementName")]
        [System.Xml.Serialization.XmlIgnoreAttribute()]
        public ItemsChoiceType1[] ItemsElementName {
            get {
                return this.itemsElementNameField;
            }
            set {
                this.itemsElementNameField = value;
            }
        }
    }
}