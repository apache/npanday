using System;

namespace NMaven.Logging
{
	/// <summary>
	/// Description of LogRecord.
	/// </summary>
	public class LogRecord
	{
		private Level level;
		
		private String message;
		
		public LogRecord(Level level, String message) 
		{
			this.level = level;
			this.message = message;
		}
		
		public Level GetLevel()
		{
			return level;
		}
		
		public String GetMessage()
		{
			return message;
		}
	}
}
