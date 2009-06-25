using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

using NPanday.Utils;
using System.Reflection;
using NPanday.Artifact;
using NPanday.Model.Setting;
using System.Windows.Forms;
using System.Net;

/// Author: Leopoldo Lee Agdeppa III

namespace NPanday.ProjectImporter.Digest.Model
{

    public class Reference : IncludeBase
    {

        #region Constructors

        public Reference(string projectBasePath, GacUtility gac) 
            : base(projectBasePath)
        {
            this.gac = gac;
        }

        #endregion

        #region Properties

        private GacUtility gac; 
        public GacUtility GacUtility
        {
            get 
            {
                if (gac == null)
                {
                    gac = new GacUtility();
                }
 
                return gac; 
            }
        }

        private string name;
        public string Name
        {
            get { return name; }
            set { name = value; }
        }

        private string hintPath;
        public string HintPath
        {
            get { return hintPath; }
            set 
            {
                if (string.IsNullOrEmpty(value))
                {
                    return;
                }

                hintPath = value; 
                SetReferenceFromFile(value); 
            }
        }

        public string HintFullPath
        {
            get
            {
                if (string.IsNullOrEmpty(hintPath))
                {
                    return null;
                }
                else if (Path.IsPathRooted(hintPath))
                {
                    return Path.GetFullPath(hintPath);
                }
                else
                {
                    return Path.GetFullPath(Path.Combine(projectBasePath, hintPath));
                }

            }
        }

        public string AssemblyInfo
        {
            set
            {
                if (value.Split(',').Length > 1)
                {
                    SetAssemblyInfoValues(value);
                }
                else
                {
                    SetAssemblyValuesFromGac(value);
                }
            }
        }

        private string version;
        public string Version
        {
            get { return version; }
            set { version = value; }
        }

        private string publicKeyToken;
        public string PublicKeyToken
        {
            get { return publicKeyToken; }
            set { publicKeyToken = value; }
        }

        private string culture;
        public string Culture
        {
            get { return culture; }
            set { culture = value; }
        }

        private string processorArchitecture;
        public string ProcessorArchitecture
        {
            get { return processorArchitecture; }
            set { processorArchitecture = value; }
        }

        #endregion


        #region HelperMethods

        private void SetReferenceFromFile(string dll)
        {
            if (string.IsNullOrEmpty(dll))
            {
                return;
            }
            SetReferenceFromFile(new FileInfo(dll));
        }


        private void SetReferenceFromFile(FileInfo dll)
        {
            Assembly asm = null ;
            string path = string.Empty;

            //if (dll.Exists)
            if (dll.Exists)
            {
                //asm = Assembly.ReflectionOnlyLoadFrom(dll.FullName);
                path = dll.FullName;
            }
            else
            {
                ArtifactContext artifactContext = new ArtifactContext();
                Artifact.Artifact a = artifactContext.GetArtifactRepository().GetArtifact(dll);
                
                if (a != null)
				{
					if (!a.FileInfo.Exists)
                    {
						if (!a.FileInfo.Directory.Exists)
							a.FileInfo.Directory.Create();

						string localRepoPath = artifactContext.GetArtifactRepository().GetLocalRepositoryPath(a, dll.Extension);
						if (File.Exists(localRepoPath))
						{
							File.Copy(localRepoPath, a.FileInfo.FullName);
							//asm = Assembly.ReflectionOnlyLoadFrom();
							path = a.FileInfo.FullName;
						}
						else
						{
							if (downloadArtifactFromRemoteRepository(a, dll.Extension))
							{
								//asm = Assembly.ReflectionOnlyLoadFrom(a.FileInfo.FullName);
								path = a.FileInfo.FullName;
							}
							else
							{
								path = getBinReference(dll.Name);
								if (!string.IsNullOrEmpty(path))
								{
									File.Copy(path, a.FileInfo.FullName);
								}
							}
							//copy assembly to repo if not found.
							if (!string.IsNullOrEmpty(path) && !File.Exists(localRepoPath))
							{
								if (!Directory.Exists(Path.GetDirectoryName(localRepoPath)))
									Directory.CreateDirectory(Path.GetDirectoryName(localRepoPath));

								File.Copy(path, localRepoPath);
							}
						}
					}
					else
					{
						path = a.FileInfo.FullName;
					}
				}
                if (a == null || string.IsNullOrEmpty(path))
                {
                    MessageBox.Show("Cannot find or download the artifact " + dll.Name + ",  project may not build properly.");
                    return;
                }
            }

            bool asmNotLoaded = true;
            foreach (Assembly asmm in AppDomain.CurrentDomain.ReflectionOnlyGetAssemblies())
            {
                if (Path.GetFileNameWithoutExtension(asmm.Location).Equals(Path.GetFileNameWithoutExtension(path)))
                {
                    asm = asmm;
                    asmNotLoaded = false;
                    break;
                }
            }
            if (asmNotLoaded)
            {
                asm = Assembly.ReflectionOnlyLoadFrom(path);
            }

            SetAssemblyInfoValues(asm.ToString());
            //asm = null;

        }

        string getBinReference(string fileName) {
            string path = Path.Combine(this.IncludeFullPath, @"bin\" + Path.GetFileName(fileName));
            
            if (File.Exists(path))
                return path;

            path = Path.Combine(this.IncludeFullPath, @"bin\debug\" + Path.GetFileName(fileName));
            if (File.Exists(path))
                return path;

            path = Path.Combine(this.IncludeFullPath, @"bin\release\" + Path.GetFileName(fileName));
            if (File.Exists(path))
                return path;

            path = Path.Combine(this.IncludeFullPath, @"obj\debug\" + Path.GetFileName(fileName));
            if (File.Exists(path))
                return path;

            path = Path.Combine(this.IncludeFullPath, @"obj\release\" + Path.GetFileName(fileName));
            if (File.Exists(path))
                return path;

            return string.Empty;
        }

        public static bool DownloadArtifact(Artifact.Artifact artifact)
        {
            return downloadArtifactFromRemoteRepository(artifact, artifact.FileInfo.Extension);
        }

        static bool downloadArtifactFromRemoteRepository(Artifact.Artifact artifact, string ext)
        {
            try
            {
                Settings settings = SettingsUtil.ReadSettings(SettingsUtil.GetUserSettingsPath());
                if (settings == null || settings.profiles == null)
                {
                    MessageBox.Show("Cannot add reference of "+ artifact.ArtifactId + ", no valid Remote Repository was found that contained the Artifact to be Resolved. Please add a Remote Repository that contains the Unresolved Artifact.");
                    return false;
                }
                List<string> activeProfiles = new List<string>();
                activeProfiles.AddRange(settings.activeProfiles);
                foreach (Profile profile in settings.profiles)
                {
                    //if(!activeProfiles.Contains(profile.id))
                    //    continue;
                    foreach (Repository repo in profile.repositories)
                    {
                        if ("NPanday.id".Equals(repo.id, StringComparison.OrdinalIgnoreCase))
                        {
                            ArtifactContext artifactContext = new ArtifactContext();
                            artifact.RemotePath = artifactContext.GetArtifactRepository().GetRemoteRepositoryPath(artifact, repo.url, ext);
                            return downloadArtifact(artifact) ;
                        }
                    }
                }
                return false;
            }
            catch 
            {
                return false;
            }
        }

        static bool downloadArtifact(Artifact.Artifact artifact)
        {
            try
            {
                WebClient client = new WebClient();
                byte[] assembly = client.DownloadData(artifact.RemotePath);
                if (!artifact.FileInfo.Directory.Exists) 
                {
                    artifact.FileInfo.Directory.Create();
                }
                FileStream stream = new FileStream(artifact.FileInfo.FullName, FileMode.Create);
                stream.Write(assembly, 0, assembly.Length);
                stream.Flush();
                stream.Close();
                stream.Dispose();
                client.Dispose();
                

                string artifactDir = GetLocalUacPath(artifact, artifact.FileInfo.Extension);
                
                if(!Directory.Exists(Path.GetDirectoryName(artifactDir)))
                {
                    Directory.CreateDirectory(Path.GetDirectoryName(artifactDir));
                }
                if (!File.Exists(artifactDir))
                {
                    File.Copy(artifact.FileInfo.FullName, artifactDir);
                }
                
                return true;
            }
            catch
            {
                return false;
            }
        }

        public static string GetLocalUacPath(Artifact.Artifact artifact, string ext)
        {
            return Path.Combine(Directory.GetParent(SettingsUtil.GetLocalRepositoryPath()).FullName, string.Format(@"uac\gac_msil\{1}\{2}__{0}\{1}{3}", artifact.GroupId, artifact.ArtifactId, artifact.Version, ext));
        }
        


        private void SetAssemblyInfoValues(string assemblyInfo)
        {
            if (!string.IsNullOrEmpty(assemblyInfo))
            {
                string[] referenceValues = assemblyInfo.Split(',');
                this.Name = referenceValues[0].Trim();

                if (referenceValues.Length > 1)
                {
                    for (int i = 1; i < referenceValues.Length; i++)
                    {
                        if (referenceValues[i].Contains("Version="))
                        {
                            this.Version = referenceValues[i].Replace("Version=", "").Trim();
                        }
                        else if (referenceValues[i].Contains("PublicKeyToken="))
                        {
                            this.PublicKeyToken = referenceValues[i].Replace("PublicKeyToken=", "").Trim();
                        }
                        else if (referenceValues[i].Contains("Culture="))
                        {
                            this.Culture = referenceValues[i].Replace("Culture=", "").Trim();
                        }
                        else if (referenceValues[i].Contains("processorArchitecture="))
                        {
                            this.ProcessorArchitecture = referenceValues[i].Replace("processorArchitecture=", "").Trim();
                        }
                    }
                }

            }

        }



        private void SetAssemblyValuesFromGac(string name)
        {
            this.Name = name.Split(',')[0].Trim();
            string str = GacUtility.GetAssemblyInfo(this.Name);
            SetAssemblyInfoValues(str);
        }

        #endregion






    }
}
