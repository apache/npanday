using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Text;
using System.Windows.Forms;

using NMaven.Model.Setting;

using System.Xml;
using System.Xml.Serialization;

namespace NMaven.VisualStudio.Addin
{
    public partial class ConfigureMavenRepositoryForm : Form
    {
        private Settings settings;

        private String settingsPath;

        public ConfigureMavenRepositoryForm()
        {
            InitializeComponent();
            settingsPath = SettingsUtil.GetUserSettingsPath();
            try
            {
                settings = SettingsUtil.ReadSettings(new FileInfo(settingsPath));
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message + e.StackTrace);
            }
            
        }

        private void update_Click(object sender, EventArgs e)
        {
            XmlSerializer serializer = new XmlSerializer(typeof(NMaven.Model.Setting.Settings));
            TextWriter writer = new StreamWriter(settingsPath);
            if (settings.profiles != null)
            {
                foreach (Profile profile in settings.profiles)
                {
                    foreach (Repository repository in profile.repositories)
                    {
                        if (repository.id.Equals("nmaven.id"))
                        {
                            UpdateRepositoryFor(profile, repository);
                            serializer.Serialize(writer, settings);
                            writer.Close();
                            this.Close();
                            return;
                        }
                    }
                }
            }

            Profile profile1 = new Profile();
            Repository repository1 = new Repository();
            profile1.repositories = new Repository[] { repository1 };
            UpdateRepositoryFor(profile1, repository1);

            if (settings.profiles == null)
            {
                settings.profiles = new Profile[] { profile1 };
            }
            else
            {
                List<Profile> profiles = new List<Profile>();
                profiles.AddRange(settings.profiles);
                profiles.Add(profile1);
                settings.profiles = profiles.ToArray();
            }
            serializer.Serialize(writer, settings);
            writer.Close();
            this.Close();
        }

        private void UpdateRepositoryFor(Profile profile, Repository repository)
        {
            Activation activation = new Activation();
            activation.activeByDefault = true;
            profile.activation = activation;

            repository.url = textBox1.Text;
            repository.id = "nmaven.id";

            RepositoryPolicy releasesPolicy = new RepositoryPolicy();
            RepositoryPolicy snapshotsPolicy = new RepositoryPolicy();
            releasesPolicy.enabled = checkBoxRelease.Checked;
            snapshotsPolicy.enabled = checkBoxSnapshot.Checked;
            repository.releases = releasesPolicy;
            repository.snapshots = snapshotsPolicy;
        }

        private void ConfigureMavenRepositoryForm_Load(object sender, EventArgs e)
        {

            if (settings == null || settings.profiles == null)
            {
                return;
            }

            foreach (Profile profile in settings.profiles)
            {
                foreach (Repository repository in profile.repositories)
                {
                    if (repository.id.Equals("nmaven.id"))
                    {
                        textBox1.Text = repository.url;
                    }
                }
            }
        }
    }
}