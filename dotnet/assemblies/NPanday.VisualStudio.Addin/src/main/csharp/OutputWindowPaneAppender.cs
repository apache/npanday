using log4net.Appender;
using log4net.Core;
using log4net.Filter;
using log4net.Layout;

namespace NPanday.VisualStudio
{
    class OutputWindowPaneAppender : AppenderSkeleton
    {
        private EnvDTE.OutputWindowPane outputWindowPane;

        public OutputWindowPaneAppender(EnvDTE.OutputWindowPane outputWindowPane, Level maxLevel)
        {
            this.outputWindowPane = outputWindowPane;

            LevelRangeFilter filter = new LevelRangeFilter();
            filter.LevelMin = maxLevel;
            base.AddFilter(filter);

            PatternLayout layout = new PatternLayout();
            layout.ConversionPattern = "%date %-5level %logger - %message%newline";
            layout.ActivateOptions();
            base.Layout = layout;
        }

        override protected void Append(LoggingEvent loggingEvent)
        {
            outputWindowPane.OutputString(RenderLoggingEvent(loggingEvent));
        }
    }
}
