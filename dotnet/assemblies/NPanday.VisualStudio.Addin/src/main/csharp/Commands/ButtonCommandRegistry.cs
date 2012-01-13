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
using System.Collections.Generic;
using EnvDTE80;
using log4net;
using Microsoft.VisualStudio.CommandBars;

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

        private static readonly ILog log = LogManager.GetLogger(typeof(ButtonCommandRegistry));

        public ButtonCommandRegistry(DTE2 application, BuildCommandContext buildContext)
        {
            _application = application;
            _buildContext = buildContext;
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
            log.Debug("Adding command " + typeof(TCommand).Name + " on " + bar.Name + " at index " + atIndex);

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
                log.Debug("TODO!! Remove " + commandBarControl.Caption + " on ?? at index " + commandBarControl.Index);
            }
        }
    }
}
