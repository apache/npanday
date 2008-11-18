Imports MyLibrary
Partial Class _Default
    Inherits System.Web.UI.Page

    Protected Sub Page_Load(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Load
        Dim objClass1 As New Class1
        Dim sDate As String
        sDate = objClass1.CurrentDay
        lblDate.Text = sDate
    End Sub
End Class
