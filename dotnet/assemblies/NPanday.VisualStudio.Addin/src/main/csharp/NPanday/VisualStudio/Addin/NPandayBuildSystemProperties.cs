using System;

namespace NPanday.VisualStudio.Addin
{
    public class NPandayBuildSystemProperties : System.ComponentModel.ISynchronizeInvoke
    {

        private object application;

        public object Application
        {
            get { return application; }
            set { application = value; }
        }

        #region ISynchronizeInvoke Members

        public IAsyncResult BeginInvoke(Delegate method, object[] args)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public object EndInvoke(IAsyncResult result)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public object Invoke(Delegate method, object[] args)
        {
            throw new Exception(Messages.MSG_E_NOTIMPLEMENTED);
        }

        public bool InvokeRequired
        {
            get { return false; }
        }

        #endregion
    }
}