Partial Public Class _Default
    Inherits System.Web.UI.Page

    Protected Sub Page_Load(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Load
        lblTest.Text = "This is a test"
        'lblTest = "This is a test"
    End Sub

End Class