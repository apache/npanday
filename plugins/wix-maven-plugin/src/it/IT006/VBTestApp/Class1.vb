Imports NUnit.Framework
Imports VBAppDll

Namespace NUnitTest
    <TestFixture()> _
    Public Class Class1

        <Test()> _
        Public Sub RunTest()
            Dim oHello As New Hello
            Assert.IsTrue(True)
        End Sub

    End Class
End Namespace
