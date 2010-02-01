Imports VBAppDll
Partial Public Class ctlTest
    Inherits System.Web.UI.UserControl

    Protected Sub Page_Load(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Load
        Dim oHello As New Hello
        lblTestCtl.Text = oHello.SayHello()
    End Sub

End Class