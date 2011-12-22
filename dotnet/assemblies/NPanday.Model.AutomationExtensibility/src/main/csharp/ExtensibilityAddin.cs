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