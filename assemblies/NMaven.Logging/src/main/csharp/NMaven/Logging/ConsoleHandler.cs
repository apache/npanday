using System;
using System.Runtime.CompilerServices;

namespace NMaven.Logging
{

	public class ConsoleHandler : IHandler
	{
		
		private Level level;
		
		public ConsoleHandler()
		{
			this.level = Level.INFO;
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void publish(LogRecord record)
		{
			if(record.GetLevel().GetValue() >= level.GetValue())
			{
				Console.WriteLine(record.GetMessage());
			}
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public void SetLevel(Level level)
		{
			this.level = level;
		}
		
		[MethodImpl(MethodImplOptions.Synchronized)]
		public Level GetLevel()
		{
			return level;
		}
	}
}
