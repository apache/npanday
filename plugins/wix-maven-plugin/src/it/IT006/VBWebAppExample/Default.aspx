<%@ Page Language="vb" AutoEventWireup="false" Codebehind="Default.aspx.vb" Inherits="VBWebAppExample._Default" %>

<%@ Register Src="Controls/ctlTest.ascx" TagName="ctlTest" TagPrefix="uc1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Untitled Page</title>
</head>
<body>
    <form id="form1" runat="server">
        <div>
            <asp:Label runat="server" ID="lblTest"></asp:Label><br />
            <br />
            <uc1:ctlTest id="CtlTest1" runat="server">
            </uc1:ctlTest></div>
    </form>
</body>
</html>
