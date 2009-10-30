using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NUnit.Framework;

namespace ConsoleApplicationEx
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.Write("Enter your name, please: ");

            string name = Console.ReadLine();

            Console.WriteLine();

            Console.WriteLine("Hello, !" + name);

            Console.WriteLine();

            Console.WriteLine("Press enter to end..");

            Console.Read();

        }
    }
}
