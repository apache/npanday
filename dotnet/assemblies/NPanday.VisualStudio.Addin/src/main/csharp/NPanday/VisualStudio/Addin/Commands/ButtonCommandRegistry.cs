using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.CommandBars;
using EnvDTE80;

namespace NPanday.VisualStudio.Addin.Commands
{
    public delegate IButtonCommandContext BuildCommandContext();

    /// <summary>
    /// Manages and registers NPanday commands.
    /// </summary>
    public class ButtonCommandRegistry
    {
        List<CommandBarControl> _keepReferences = new List<CommandBarControl>();

        Dictionary<Type, ButtonCommand> _commands = new Dictionary<Type, ButtonCommand>();

        private readonly DTE2 _application;
        private BuildCommandContext _buildContext;

        public ButtonCommandRegistry(DTE2 application, BuildCommandContext buildContext)
        {
            _application = application;
            _buildContext = buildContext;
        }

        public TCommand AddBefore<TCommand>(CommandBarControl barControl)
            where TCommand : ButtonCommand, new()
        {
            return Add<TCommand>(barControl.Parent, barControl.Index);
        }

        public TCommand Add<TCommand>(CommandBar bar, int atIndex)
            where TCommand : ButtonCommand, new()
        {
            TCommand command = getOrCreate<TCommand>();

            CommandBarButton ctl = (CommandBarButton)
                  bar.Controls.Add(MsoControlType.msoControlButton,
                                   System.Type.Missing, System.Type.Missing, atIndex, true);
            ctl.Click += delegate(CommandBarButton btn, ref bool Cancel)
                {
                    command.Execute(_buildContext()); 
                };
            ctl.Caption = command.Caption;
            ctl.Visible = true;

            _keepReferences.Add(ctl);

            return command;
        }

        private TCommand getOrCreate<TCommand>()
            where TCommand : ButtonCommand, new()
        {
            ButtonCommand command;
            if (_commands.TryGetValue(typeof(TCommand), out command))
            {
                return (TCommand)command;
            }

            command = new TCommand();
            command.Application = _application;
            _commands[typeof (TCommand)] = command;
            return (TCommand)command;
        }
    }
}
