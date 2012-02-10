<%@ Page Language="C#" Inherits="System.Web.UI.Page"%>
<%@ Assembly Name="Microsoft.SharePoint, Version=11.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> <%@ Import Namespace="Microsoft.SharePoint.Utilities" %> <%@ Import Namespace="Microsoft.SharePoint" %>
<% Response.ContentType = "text/xml"; %>
<discovery xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="http://schemas.xmlsoap.org/disco/">
	<contractRef ref=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request) + "?wsdl", '"'); %>  docRef=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %>  xmlns="http://schemas.xmlsoap.org/disco/scl/" />
	<soap address=<% SPEncode.WriteHtmlEncodeWithQuote(Response, SPWeb.OriginalBaseUrl(Request), '"'); %>  xmlns:q1="http://tempuri.org/" binding="q1:SPFilesSoap" xmlns="http://schemas.xmlsoap.org/disco/soap/" />
</discovery>