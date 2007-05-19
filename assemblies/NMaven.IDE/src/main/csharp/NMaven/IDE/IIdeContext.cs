using System;
using System.Collections.Generic;
using System.IO;

using NMaven.Service;
using NMaven.Logging;

namespace NMaven.IDE
{
	/// <summary>
	/// Description of IIdeContext.
	/// </summary>
	public interface IIdeContext
	{
		void Init(IIdeConfiguration configuration);
		
		void Dispose();
		
		Logger GetLogger();

        List<MavenProject> GetMavenProjectsFrom(DirectoryInfo buildDirectory);
		
		void Build(MavenExecutionRequest request);
		
	}
}
