using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;
using EnvDTE;
using EnvDTE80;
using Microsoft.VisualStudio.CommandBars;
using NPanday.Logging;

namespace NPanday.VisualStudio.Addin.Helper
{
    public class BuiltinCommandFinder
    {
        private readonly DTE2 _application;
        private readonly Logger _logger;

        IDictionary<string, List<string>> _containingCommandBarsByControlCaption = new Dictionary<string, List<string>>();
        IDictionary<string, List<string>> _containingCommandBarsByCammandName = new Dictionary<string, List<string>>();

        IDictionary<string, CommandBarControl> _controlByCaptionPath = new Dictionary<string, CommandBarControl>();
        IDictionary<string, CommandBarControl> _controlByBarAndCommandNamePath = new Dictionary<string, CommandBarControl>();
        private bool _indexed = false;


        public BuiltinCommandFinder(DTE2 application, Logger logger)
        {
            _application = application;
            _logger = logger;
        }

        /// <summary>
        /// Finds command controls using a <paramref name="lookup"/>.
        /// </summary>
        /// <param name="lookup">
        /// Possible Formats:
        ///   "buttoncaption", "commandname", "barname->buttoncaption", "barname->buttoncaption"
        /// </param>
        public bool TryFindCommands(string lookup, out CommandBarControl[] controls)
        {
            ensureIsIndexed();
            CommandBars bars = ((CommandBars)_application.CommandBars);
            if (!lookup.Contains("->"))
            {
                List<string> barNames;
                if (_containingCommandBarsByCammandName.TryGetValue(lookup, out barNames))
                {
                    List<CommandBarControl> controlList = new List<CommandBarControl>();
                    foreach (string barName in barNames)
                    {
                        CommandBarControl commandOrNull = FindCommandOrNull(bars[barName], lookup);
                        Debug.Assert(commandOrNull != null, "controls index seems to be incositent");
                        controlList.Add(commandOrNull);
                    }
                    controls = controlList.ToArray();

                    _logger.Log(Level.DEBUG,
                                string.Format("Found {0} command bars containing a control bound to command '{1}': {2}",
                                barNames.Count, lookup, String.Join(",", barNames.ToArray())));

                    return true;
                }

                string normalizedCaption = normalizeControlCaption(lookup);
                if (_containingCommandBarsByControlCaption.TryGetValue(normalizedCaption, out barNames))
                {
                    List<CommandBarControl> controlList = new List<CommandBarControl>();
                    foreach (string barName in barNames)
                    {
                        CommandBarControl commandOrNull = FindCommandOrNull(bars[barName], normalizedCaption);
                        Debug.Assert(commandOrNull != null, "controls index seems to be incositent");
                        controlList.Add(commandOrNull);
                    }
                    controls = controlList.ToArray();

                    _logger.Log(Level.DEBUG,
                                string.Format("Found {0} command bars containing a control with caption '{1}': {2}",
                                barNames.Count, normalizedCaption, String.Join(",", barNames.ToArray())));

                    return true;
                }

                controls = new CommandBarControl[0];
                _logger.Log(Level.DEBUG, "Could not find any command bar containing a command or caption named: '" + lookup + "'.");
                return false;
            }


            string[] parts = lookup.Split(new string[] { "=>" }, 2, StringSplitOptions.RemoveEmptyEntries);
            string commandBarName = parts[0];
            string controlCaptionOrCommandName = parts[1];

            CommandBar commandBar = bars[commandBarName];

            if (commandBar == null)
            {
                controls = new CommandBarControl[0];
                _logger.Log(Level.DEBUG, "Could not find any command bar named: '" + commandBarName + "', and hence no contained command or caption named: " + controlCaptionOrCommandName + ".");
                return false;
            }


            CommandBarControl control = FindCommandOrNull(commandBar, controlCaptionOrCommandName);

            if (control == null)
            {
                controls = new CommandBarControl[0];
                _logger.Log(Level.DEBUG, "Found commandbar '" + commandBarName + "', but it did not contain a control with command name or caption '" + controlCaptionOrCommandName + "'.");
                return false;
            }

            controls = new CommandBarControl[] { control };
            return true;
        }

        public CommandBarControl FindCommandOrNull(CommandBar commandBar, string controlCaptionOrCommandName)
        {
            ensureIsIndexed();
            CommandBarControl control;

            if (_controlByBarAndCommandNamePath.TryGetValue(buildCommandCaptionPath(commandBar, controlCaptionOrCommandName), out control))
            {
                string commandName = getButtonTargetCommand(control).Name;
                if (!string.IsNullOrEmpty(commandName))
                {
                    _logger.Log(Level.DEBUG,
                                "Control found using it's caption; its better to use the command name: " + commandName);
                }
                return control;
            }


            if (_controlByCaptionPath.TryGetValue(buildCommandCaptionPath(commandBar, controlCaptionOrCommandName), out control))
            {
                string commandName = getButtonTargetCommand(control).Name;
                if (!string.IsNullOrEmpty(commandName))
                {
                    _logger.Log(Level.DEBUG,
                                "Control found using it's caption; its better to use the command name: " + commandName);
                }
                return control;
            }

            return null;
        }

        private static string buildCommandCaptionPath(CommandBar commandBar, string controlCaptionOrCommandName)
        {
            return commandBar.Name + "->" + controlCaptionOrCommandName.Replace("&", "");
        }

        public void IndexCommands()
        {
            _indexed = true;

            Stopwatch watch = new Stopwatch();
            watch.Start();
            CommandBars commandBars = (CommandBars)_application.CommandBars;
            foreach (CommandBar commandBar in commandBars)
            {
                try
                {
                    // sometimes accessing the commandbar name fails
                    string dummy = commandBar.Name;
                }
                catch
                {
                    // if this is the case, we simply omit it from the index
                    continue;
                }

                foreach (CommandBarControl control in commandBar.Controls)
                {
                    try
                    {
                        if (control is CommandBarButton)
                        {
                            string captionPath = indexByCaption(commandBar, control);

                            indexByCommandName(captionPath, commandBar, control);

                            //_logger.Log(Level.DEBUG, commandBar.Name + "->" + control.Caption + " -> " + command.Name);
                        }
                        else if (control is CommandBarPopup)
                        {
                            // TODO: Handle submenus
                            // http://www.mztools.com/articles/2010/MZ2010001.aspx
                        }
                    }
                    catch (Exception e)
                    {
                        _logger.Log(Level.DEBUG, "Error on getting details for " + commandBar.Name + "->" + control.Caption + ":" + e.Message);
                    }
                }
            }
            watch.Stop();
            _logger.Log(Level.DEBUG, string.Format("Indexed {0} command items in {1:0.00} seconds.", 0, watch.Elapsed.TotalSeconds));
        }

        private void indexByCommandName(string captionPath, CommandBar commandBar, CommandBarControl control)
        {
            Command command = getButtonTargetCommand(control);
            if (!string.IsNullOrEmpty(command.Name))
            {
                ensureValueForKey(_containingCommandBarsByCammandName, command.Name).Add(commandBar.Name);
                string commandPath = buildCommandCaptionPath(commandBar, command.Name);
                if (_controlByBarAndCommandNamePath.ContainsKey(commandPath))
                {
                    _logger.Log(Level.DEBUG,
                                "Command path " + commandPath + " is ambiguous; " + captionPath +
                                " ignored, first occurance wins.");
                }
                else
                {
                    _controlByBarAndCommandNamePath.Add(commandPath, control);
                }
            }
        }

        private string indexByCaption(CommandBar commandBar, CommandBarControl control)
        {
            ensureValueForKey(_containingCommandBarsByControlCaption, normalizeControlCaption(control.Caption)).Add(
                commandBar.Name);

            string captionPath = buildCommandCaptionPath(commandBar, control.Caption);
            ;
            if (_controlByCaptionPath.ContainsKey(captionPath))
            {
                _logger.Log(Level.DEBUG, "Caption path '" + captionPath + "' is ambiguous; first occurance wins.");
            }
            else
            {
                _controlByCaptionPath[captionPath] = control;
            }
            return captionPath;
        }

        private Command getButtonTargetCommand(CommandBarControl control)
        {
            string guid;
            int id;
            _application.Commands.CommandInfo(control, out guid, out id);
            Command command = _application.Commands.Item(guid, id);
            return command;
        }

        public bool IsThisCommand(CommandBarControl control, string commandCaption)
        {
            CommandBar bar = control.Parent;
            string caption = control.Caption;

            if (string.IsNullOrEmpty(caption))
                return false;

            if (control.Caption == commandCaption)
                return true;

            if (normalizeControlCaption(caption) == commandCaption)
                return true;

            try
            {
                string barName = bar.Name; // fails sometimes
            }
            catch
            {
                return false;
            }

            return (buildCommandCaptionPath(bar, caption) == commandCaption);
        }

        private static string normalizeControlCaption(string caption)
        {
            return caption.Replace("&", "");
        }

        private void ensureIsIndexed()
        {
            if (!_indexed)
            {
                throw new InvalidOperationException("Please make sure IndexControls() ran, before any other methods are called.");
            }
        }

        private static TValue ensureValueForKey<TKey, TValue>(IDictionary<TKey, TValue> dict, TKey key) where TValue : new()
        {
            TValue list;
            if (!dict.TryGetValue(key, out list))
            {
                dict.Add(key, list = new TValue());
            }
            return list;
        }
    }
}
