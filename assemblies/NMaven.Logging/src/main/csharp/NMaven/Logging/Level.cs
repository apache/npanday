using System;

namespace NMaven.Logging
{
	public class Level
	{		
		
		public static Level SEVERE = new Level("SEVERE", 10);
		
		public static Level WARNING = new Level("WARNING", 9);
		
		public static Level INFO = new Level("INFO", 8);		
		
		public static Level FINE = new Level("FINE", 7);
		
		public static Level DEBUG = new Level("DEBUG", 6);		
		
		private String name;
		
		private int value;
		
		private Level(String name, int value)
		{
			this.name = name;
			this.value = value;
		}
		
		public String GetName()
		{
			return name;
		}
		
		public int GetValue()
		{
			return value;
		}
	}
}
