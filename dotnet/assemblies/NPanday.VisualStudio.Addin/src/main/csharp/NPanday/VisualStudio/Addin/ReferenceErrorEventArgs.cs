using System;

namespace NPanday.VisualStudio.Addin
{
    public class ReferenceErrorEventArgs : EventArgs
    {
        string message;
        public string Message
        {
            get { return message; }
            set { message = value; }
        }
    }
}