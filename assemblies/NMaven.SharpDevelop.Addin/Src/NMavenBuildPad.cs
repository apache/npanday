using System;
using System.Windows.Forms;
using ICSharpCode.Core;
using ICSharpCode.SharpDevelop.Gui;

namespace NMaven.SharpDevelop.Addin
{
	/// <summary>
	/// Description of the pad content
	/// </summary>
	public class NMavenBuildPad : AbstractPadContent
	{
		NMavenBuildControl ctl;
		
		/// <summary>
		/// Creates a new TestPad object
		/// </summary>
		public NMavenBuildPad()
		{
			ctl = new NMavenBuildControl();
		}
		
		/// <summary>
		/// The <see cref="System.Windows.Forms.Control"/> representing the pad
		/// </summary>
		public override Control Control {
			get {
				return ctl;
			}
		}
		
		/// <summary>
		/// Refreshes the pad
		/// </summary>
		public override void RedrawContent()
		{
			// TODO: Refresh the whole pad control here, renew all resource strings whatever
			//       Note that you do not need to recreate the control.
		}
		
		/// <summary>
		/// Cleans up all used resources
		/// </summary>
		public override void Dispose()
		{
			ctl.Dispose();
		}
	}
}
