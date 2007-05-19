using System;

namespace NMaven.Logging
{
	/// <summary>
	/// Description of IHandler.
	/// </summary>
	public interface IHandler
	{
		void publish(LogRecord record);
		
		void SetLevel(Level level);

		Level GetLevel();
	}
}
