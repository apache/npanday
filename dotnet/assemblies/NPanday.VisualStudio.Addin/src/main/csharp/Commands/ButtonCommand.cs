using Microsoft.VisualStudio.CommandBars;
using EnvDTE80;

namespace NPanday.VisualStudio.Addin.Commands
{
    /// <summary>
    /// TODO: For now we only have buttons and when migrating to VSIX this 
    /// has to be changed dramatically anyway.
    /// </summary>
    public abstract class ButtonCommand
    {
        private DTE2 _application;
        public DTE2 Application
        {
            get
            {
                return _application;
            }
            internal set
            {
                _application = value;
            }
        }

        public abstract string Caption
        { 
            get;
        }

        public abstract void Execute(IButtonCommandContext context);
    }
}
