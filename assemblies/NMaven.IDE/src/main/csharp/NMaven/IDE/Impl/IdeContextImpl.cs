using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Web.Services.Protocols;

using NMaven.Logging;
using NMaven.IDE;
using NMaven.Service;


namespace NMaven.IDE.Impl
{
	/// <summary>
	/// Description of IdeContextImpl.
	/// </summary>
	public class IdeContextImpl : IIdeContext
	{		
		private Logger logger;
		
		private Socket socket;
		
		private IIdeConfiguration configuration;
		
		public IdeContextImpl()
		{
		}
		
		public void Build(MavenExecutionRequest request)
		{  
			configuration.SocketLoggerPort = FindOpenPort();
            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            socket.Bind(new IPEndPoint(IPAddress.Any, configuration.SocketLoggerPort));
            socket.Listen(10);
          
			Thread thread = new Thread(new ThreadStart(WriteBuildResults));			
			thread.Start();		
			
            request.loggerPort = configuration.SocketLoggerPort;
            request.loggerPortSpecified = true;
			
            MavenEmbedderService service = new MavenEmbedderService();
			service.execute(request);			
		}
		
		public List<MavenProject> GetMavenProjectsFrom(DirectoryInfo buildDirectory)
		{
			MavenEmbedderService service = new MavenEmbedderService();
            return new List<MavenProject>(service.getMavenProjectsFor(buildDirectory.FullName));
		}
		
		public void Init(IIdeConfiguration configuration)
		{
			this.configuration = configuration;
			this.logger = configuration.Logger;	
		}
		
		public void Dispose()
		{
			//socket.Close();
		}
		
		public Logger GetLogger()
		{
			return logger;
		}
		
		private int FindOpenPort()
		{
			for(int i = 1; i < 10; i++)
			{
				int port = (new Random()).Next(1025, 65536);
				try {
		            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
		            socket.Bind(new IPEndPoint(IPAddress.Any, port));
		            socket.Close();
		            return port;
				}
				catch (SocketException e)
				{					
				}								
			}
			return -1;
		}		
		                         
        private void WriteBuildResults()
        {
            try
            {
                Socket client = socket.Accept();
                NetworkStream networkStream = new NetworkStream(client);
                StreamReader streamReader = new StreamReader(new NetworkStream(client));
                while (!streamReader.EndOfStream)
                {
                    logger.Log(Level.INFO, String.Concat(streamReader.ReadLine(), Environment.NewLine));

                }
                streamReader.Close();
                client.Close();    
            }
            catch (IOException ex)
            {
                logger.Log(Level.INFO, "Problem reading socket logger: Message = " + ex.Message);
            }   
        }		
	}
}
