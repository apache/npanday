using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.VisualStudio.CommandBars;
using EnvDTE80;
using NPanday.Logging;

namespace NPanday.VisualStudio.Addin.Commands
{
    /// <summary>
    /// Manages and registers NPanday commands.
    /// </summary>
    public class ButtonCommandRegistry
    {
        readonly List<CommandBarControl> _keepReferences = new List<CommandBarControl>();

        readonly Dictionary<Type, ButtonCommand> _commands = new Dictionary<Type, ButtonCommand>();

        private readonly DTE2 _application;
        private readonly BuildCommandContext _buildContext;
        private readonly Logger _logger;

        public ButtonCommandRegistry(DTE2 application, BuildCommandContext buildContext, Logger logger)
        {
            _application = application;
            _buildContext = buildContext;
            _logger = logger;
        }

        public void Excecute<TCommand>(IButtonCommandContext context)
           where TCommand : ButtonCommand, new()
        {
            getOrCreate<TCommand>().Execute(context);
        }

        public TCommand AddBefore<TCommand>(CommandBarControl barControl)
            where TCommand : ButtonCommand, new()
        {
            return Add<TCommand>(barControl.Parent, barControl.Index);
        }

        public TCommand AddAfter<TCommand>(CommandBarControl barControl)
            where TCommand : ButtonCommand, new()
        {
            return Add<TCommand>(barControl.Parent, barControl.Index + 1);
        }

        public TCommand Add<TCommand>(CommandBar bar, int atIndex)
            where TCommand : ButtonCommand, new()
        {
            _logger.Log(Level.DEBUG, "Adding command " + typeof(TCommand).Name + " on " + bar.Name + " at index " + atIndex);

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
            _commands[typeof(TCommand)] = command;
            return (TCommand)command;
        }

        public void UnregisterAll()
        {
            foreach (CommandBarControl commandBarControl in _keepReferences.ToArray())
            {
				// TODO: find (or previouvsly cache the commandbar)
                _logger.Log(Level.DEBUG, "TODO!! Remove " + commandBarControl.Caption + " on ?? at index " + commandBarControl.Index);
            }
        }
    }
}
