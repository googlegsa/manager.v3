/**
 * SiteDataStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT Aug 23, 2006 (04:21:30 GMT+00:00)
 */
package com.google.enterprise.connector.sharepoint.gen;

/*
 * SiteDataStub java implementation
 */

public class SiteDataStub extends org.apache.axis2.client.Stub {
	// default axis home being null forces the system to pick up the mars from
	// the axis2 library
	public static final java.lang.String AXIS2_HOME = null;

	protected static org.apache.axis2.description.AxisOperation[] _operations;

	// hashmaps to keep the fault mapping
	private java.util.HashMap faultExeptionNameMap = new java.util.HashMap();

	private java.util.HashMap faultExeptionClassNameMap = new java.util.HashMap();

	private java.util.HashMap faultMessageMap = new java.util.HashMap();

	private void populateAxisService() {

		// creating the Service with a unique name
		_service = new org.apache.axis2.description.AxisService("SiteData"
				+ this.hashCode());

		// creating the operations
		org.apache.axis2.description.AxisOperation __operation;

		_operations = new org.apache.axis2.description.AxisOperation[9];

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("", "GetWeb"));

		_operations[0] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation
				.setName(new javax.xml.namespace.QName("", "GetAttachments"));

		_operations[1] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation
				.setName(new javax.xml.namespace.QName("", "EnumerateFolder"));

		_operations[2] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetListCollection"));

		_operations[3] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("", "GetSite"));

		_operations[4] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation
				.setName(new javax.xml.namespace.QName("", "GetURLSegments"));

		_operations[5] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("", "GetListItems"));

		_operations[6] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("", "GetSiteAndWeb"));

		_operations[7] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("", "GetList"));

		_operations[8] = __operation;
		_service.addOperation(__operation);

	}

	// populates the faults
	private void populateFaults() {

	}

	/**
	 * Constructor that takes in a configContext
	 */
	public SiteDataStub(
			org.apache.axis2.context.ConfigurationContext configurationContext,
			java.lang.String targetEndpoint) throws java.lang.Exception {
		// To populate AxisService
		populateAxisService();
		populateFaults();

		_serviceClient = new org.apache.axis2.client.ServiceClient(
				configurationContext, _service);
		configurationContext = _serviceClient.getServiceContext()
				.getConfigurationContext();

		_serviceClient.getOptions().setTo(
				new org.apache.axis2.addressing.EndpointReference(
						targetEndpoint));

	}

	/**
	 * Default Constructor
	 */
	public SiteDataStub() throws java.lang.Exception {

		this("http://www.wssdemo.com/_vti_bin/SiteData.asmx");

	}

	/**
	 * Constructor taking the target endpoint
	 */
	public SiteDataStub(java.lang.String targetEndpoint)
			throws java.lang.Exception {
		this(null, targetEndpoint);
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetWeb
	 * @param param0
	 * 
	 */
	public SiteDataStub.GetWebResponse GetWeb(

	SiteDataStub.GetWeb param0) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[0].getName());
			_operationClient.getOptions().setAction(
					"http://schemas.microsoft.com/sharepoint/soap/GetWeb");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(
					getFactory(_operationClient.getOptions()
							.getSoapVersionURI()),
					param0,
					optimizeContent(new javax.xml.namespace.QName("", "GetWeb")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(), SiteDataStub.GetWebResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetWebResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetAttachments
	 * @param param2
	 * 
	 */
	public SiteDataStub.GetAttachmentsResponse GetAttachments(

	SiteDataStub.GetAttachments param2) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[1].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetAttachments");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param2,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetAttachments")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					SiteDataStub.GetAttachmentsResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetAttachmentsResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#EnumerateFolder
	 * @param param4
	 * 
	 */
	public SiteDataStub.EnumerateFolderResponse EnumerateFolder(

	SiteDataStub.EnumerateFolder param4) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[2].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/EnumerateFolder");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param4,
					optimizeContent(new javax.xml.namespace.QName("",
							"EnumerateFolder")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					SiteDataStub.EnumerateFolderResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.EnumerateFolderResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetListCollection
	 * @param param6
	 * 
	 */
	public SiteDataStub.GetListCollectionResponse GetListCollection(

	SiteDataStub.GetListCollection param6) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[3].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetListCollection");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param6,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetListCollection")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					SiteDataStub.GetListCollectionResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetListCollectionResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetSite
	 * @param param8
	 * 
	 */
	public SiteDataStub.GetSiteResponse GetSite(

	SiteDataStub.GetSite param8) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[4].getName());
			_operationClient.getOptions().setAction(
					"http://schemas.microsoft.com/sharepoint/soap/GetSite");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(
					getFactory(_operationClient.getOptions()
							.getSoapVersionURI()),
					param8,
					optimizeContent(new javax.xml.namespace.QName("", "GetSite")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(), SiteDataStub.GetSiteResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetSiteResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetURLSegments
	 * @param param10
	 * 
	 */
	public SiteDataStub.GetURLSegmentsResponse GetURLSegments(

	SiteDataStub.GetURLSegments param10) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[5].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetURLSegments");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param10,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetURLSegments")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					SiteDataStub.GetURLSegmentsResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetURLSegmentsResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetListItems
	 * @param param12
	 * 
	 */
	public SiteDataStub.GetListItemsResponse GetListItems(

	SiteDataStub.GetListItems param12) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[6].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetListItems");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param12,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetListItems")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					SiteDataStub.GetListItemsResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetListItemsResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetSiteAndWeb
	 * @param param14
	 * 
	 */
	public SiteDataStub.GetSiteAndWebResponse GetSiteAndWeb(

	SiteDataStub.GetSiteAndWeb param14) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[7].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetSiteAndWeb");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param14,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetSiteAndWeb")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					SiteDataStub.GetSiteAndWebResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetSiteAndWebResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see SiteData#GetList
	 * @param param16
	 * 
	 */
	public SiteDataStub.GetListResponse GetList(

	SiteDataStub.GetList param16) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[8].getName());
			_operationClient.getOptions().setAction(
					"http://schemas.microsoft.com/sharepoint/soap/GetList");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(
					getFactory(_operationClient.getOptions()
							.getSoapVersionURI()),
					param16,
					optimizeContent(new javax.xml.namespace.QName("", "GetList")));

			// create message context with that soap envelope
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(), SiteDataStub.GetListResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (SiteDataStub.GetListResponse) object;

		} catch (org.apache.axis2.AxisFault f) {
			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExeptionNameMap.containsKey(faultElt.getQName())) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExeptionClassNameMap
								.get(faultElt.getQName());
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.rmi.RemoteException ex = (java.rmi.RemoteException) exceptionClass
								.newInstance();
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(faultElt.getQName());
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw ex;
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		}
	}

	/**
	 * A utility method that copies the namepaces from the SOAPEnvelope
	 */
	private java.util.Map getEnvelopeNamespaces(
			org.apache.axiom.soap.SOAPEnvelope env) {
		java.util.Map returnMap = new java.util.HashMap();
		java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
		while (namespaceIterator.hasNext()) {
			org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator
					.next();
			returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
		}
		return returnMap;
	}

	private javax.xml.namespace.QName[] opNameArray = null;

	private boolean optimizeContent(javax.xml.namespace.QName opName) {

		if (opNameArray == null) {
			return false;
		}
		for (int i = 0; i < opNameArray.length; i++) {
			if (opName.equals(opNameArray[i])) {
				return true;
			}
		}
		return false;
	}

	// http://www.wssdemo.com/_vti_bin/SiteData.asmx
	public static class _sListWithTime implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sListWithTime Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for InternalName
		 */

		protected java.lang.String localInternalName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localInternalNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getInternalName() {
			return localInternalName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            InternalName
		 */
		public void setInternalName(java.lang.String param) {

			// update the setting tracker
			localInternalNameTracker = true;

			this.localInternalName = param;

		}

		/**
		 * field for LastModified
		 */

		protected java.util.Calendar localLastModified;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.util.Calendar param) {

			this.localLastModified = param;

		}

		/**
		 * field for IsEmpty
		 */

		protected boolean localIsEmpty;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getIsEmpty() {
			return localIsEmpty;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            IsEmpty
		 */
		public void setIsEmpty(boolean param) {

			this.localIsEmpty = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localInternalNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"InternalName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"InternalName");
							}

						} else {
							xmlWriter.writeStartElement("InternalName");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","InternalName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localInternalName));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "LastModified",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModified");
						}

					} else {
						xmlWriter.writeStartElement("LastModified");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModified));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "IsEmpty",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "IsEmpty");
						}

					} else {
						xmlWriter.writeStartElement("IsEmpty");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","IsEmpty");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localIsEmpty));
					xmlWriter.writeEndElement();

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localInternalNameTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"InternalName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localInternalName));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModified"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModified));

			elementList
					.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"IsEmpty"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localIsEmpty));

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sListWithTime parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sListWithTime object = new _sListWithTime();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sListWithTime".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sListWithTime) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"InternalName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setInternalName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"IsEmpty").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setIsEmpty(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sProperty implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sProperty Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for Name
		 */

		protected java.lang.String localName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getName() {
			return localName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Name
		 */
		public void setName(java.lang.String param) {

			// update the setting tracker
			localNameTracker = true;

			this.localName = param;

		}

		/**
		 * field for Title
		 */

		protected java.lang.String localTitle;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localTitleTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getTitle() {
			return localTitle;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Title
		 */
		public void setTitle(java.lang.String param) {

			// update the setting tracker
			localTitleTracker = true;

			this.localTitle = param;

		}

		/**
		 * field for Type
		 */

		protected java.lang.String localType;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localTypeTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getType() {
			return localType;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Type
		 */
		public void setType(java.lang.String param) {

			// update the setting tracker
			localTypeTracker = true;

			this.localType = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Name",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Name");
							}

						} else {
							xmlWriter.writeStartElement("Name");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Name");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localName));
						xmlWriter.writeEndElement();
					}
					if (localTitleTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Title",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Title");
							}

						} else {
							xmlWriter.writeStartElement("Title");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Title");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localTitle));
						xmlWriter.writeEndElement();
					}
					if (localTypeTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Type",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Type");
							}

						} else {
							xmlWriter.writeStartElement("Type");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Type");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localType));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localNameTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"Name"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localName));
			}
			if (localTitleTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Title"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localTitle));
			}
			if (localTypeTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"Type"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localType));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sProperty parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sProperty object = new _sProperty();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sProperty".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sProperty) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Name").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Title").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setTitle(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Type").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setType(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sSiteMetadata implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sSiteMetadata Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for LastModified
		 */

		protected java.util.Calendar localLastModified;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.util.Calendar param) {

			this.localLastModified = param;

		}

		/**
		 * field for LastModifiedForceRecrawl
		 */

		protected java.util.Calendar localLastModifiedForceRecrawl;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModifiedForceRecrawl() {
			return localLastModifiedForceRecrawl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModifiedForceRecrawl
		 */
		public void setLastModifiedForceRecrawl(java.util.Calendar param) {

			this.localLastModifiedForceRecrawl = param;

		}

		/**
		 * field for SmallSite
		 */

		protected boolean localSmallSite;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getSmallSite() {
			return localSmallSite;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            SmallSite
		 */
		public void setSmallSite(boolean param) {

			this.localSmallSite = param;

		}

		/**
		 * field for PortalUrl
		 */

		protected java.lang.String localPortalUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPortalUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPortalUrl() {
			return localPortalUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            PortalUrl
		 */
		public void setPortalUrl(java.lang.String param) {

			// update the setting tracker
			localPortalUrlTracker = true;

			this.localPortalUrl = param;

		}

		/**
		 * field for UserProfileGUID
		 */

		protected java.lang.String localUserProfileGUID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localUserProfileGUIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUserProfileGUID() {
			return localUserProfileGUID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            UserProfileGUID
		 */
		public void setUserProfileGUID(java.lang.String param) {

			// update the setting tracker
			localUserProfileGUIDTracker = true;

			this.localUserProfileGUID = param;

		}

		/**
		 * field for ValidSecurityInfo
		 */

		protected boolean localValidSecurityInfo;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getValidSecurityInfo() {
			return localValidSecurityInfo;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ValidSecurityInfo
		 */
		public void setValidSecurityInfo(boolean param) {

			this.localValidSecurityInfo = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "LastModified",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModified");
						}

					} else {
						xmlWriter.writeStartElement("LastModified");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModified));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"LastModifiedForceRecrawl", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModifiedForceRecrawl");
						}

					} else {
						xmlWriter.writeStartElement("LastModifiedForceRecrawl");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModifiedForceRecrawl");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModifiedForceRecrawl));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "SmallSite",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "SmallSite");
						}

					} else {
						xmlWriter.writeStartElement("SmallSite");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","SmallSite");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localSmallSite));
					xmlWriter.writeEndElement();
					if (localPortalUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"PortalUrl", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"PortalUrl");
							}

						} else {
							xmlWriter.writeStartElement("PortalUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","PortalUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localPortalUrl));
						xmlWriter.writeEndElement();
					}
					if (localUserProfileGUIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"UserProfileGUID", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"UserProfileGUID");
							}

						} else {
							xmlWriter.writeStartElement("UserProfileGUID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","UserProfileGUID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localUserProfileGUID));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"ValidSecurityInfo", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"ValidSecurityInfo");
						}

					} else {
						xmlWriter.writeStartElement("ValidSecurityInfo");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","ValidSecurityInfo");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localValidSecurityInfo));
					xmlWriter.writeEndElement();

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModified"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModified));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModifiedForceRecrawl"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModifiedForceRecrawl));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"SmallSite"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localSmallSite));
			if (localPortalUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"PortalUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localPortalUrl));
			}
			if (localUserProfileGUIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"UserProfileGUID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localUserProfileGUID));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"ValidSecurityInfo"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localValidSecurityInfo));

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sSiteMetadata parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sSiteMetadata object = new _sSiteMetadata();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sSiteMetadata".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sSiteMetadata) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModifiedForceRecrawl").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModifiedForceRecrawl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"SmallSite").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setSmallSite(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"PortalUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setPortalUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"UserProfileGUID").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setUserProfileGUID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"ValidSecurityInfo").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setValidSecurityInfo(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sListMetadata implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sListMetadata Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for Title
		 */

		protected java.lang.String localTitle;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localTitleTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getTitle() {
			return localTitle;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Title
		 */
		public void setTitle(java.lang.String param) {

			// update the setting tracker
			localTitleTracker = true;

			this.localTitle = param;

		}

		/**
		 * field for Description
		 */

		protected java.lang.String localDescription;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localDescriptionTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getDescription() {
			return localDescription;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Description
		 */
		public void setDescription(java.lang.String param) {

			// update the setting tracker
			localDescriptionTracker = true;

			this.localDescription = param;

		}

		/**
		 * field for BaseType
		 */

		protected java.lang.String localBaseType;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localBaseTypeTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getBaseType() {
			return localBaseType;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            BaseType
		 */
		public void setBaseType(java.lang.String param) {

			// update the setting tracker
			localBaseTypeTracker = true;

			this.localBaseType = param;

		}

		/**
		 * field for BaseTemplate
		 */

		protected java.lang.String localBaseTemplate;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localBaseTemplateTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getBaseTemplate() {
			return localBaseTemplate;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            BaseTemplate
		 */
		public void setBaseTemplate(java.lang.String param) {

			// update the setting tracker
			localBaseTemplateTracker = true;

			this.localBaseTemplate = param;

		}

		/**
		 * field for DefaultViewUrl
		 */

		protected java.lang.String localDefaultViewUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localDefaultViewUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getDefaultViewUrl() {
			return localDefaultViewUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            DefaultViewUrl
		 */
		public void setDefaultViewUrl(java.lang.String param) {

			// update the setting tracker
			localDefaultViewUrlTracker = true;

			this.localDefaultViewUrl = param;

		}

		/**
		 * field for LastModified
		 */

		protected java.util.Calendar localLastModified;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.util.Calendar param) {

			this.localLastModified = param;

		}

		/**
		 * field for LastModifiedForceRecrawl
		 */

		protected java.util.Calendar localLastModifiedForceRecrawl;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModifiedForceRecrawl() {
			return localLastModifiedForceRecrawl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModifiedForceRecrawl
		 */
		public void setLastModifiedForceRecrawl(java.util.Calendar param) {

			this.localLastModifiedForceRecrawl = param;

		}

		/**
		 * field for Author
		 */

		protected java.lang.String localAuthor;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localAuthorTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getAuthor() {
			return localAuthor;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Author
		 */
		public void setAuthor(java.lang.String param) {

			// update the setting tracker
			localAuthorTracker = true;

			this.localAuthor = param;

		}

		/**
		 * field for ValidSecurityInfo
		 */

		protected boolean localValidSecurityInfo;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getValidSecurityInfo() {
			return localValidSecurityInfo;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ValidSecurityInfo
		 */
		public void setValidSecurityInfo(boolean param) {

			this.localValidSecurityInfo = param;

		}

		/**
		 * field for InheritedSecurity
		 */

		protected boolean localInheritedSecurity;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getInheritedSecurity() {
			return localInheritedSecurity;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            InheritedSecurity
		 */
		public void setInheritedSecurity(boolean param) {

			this.localInheritedSecurity = param;

		}

		/**
		 * field for AllowAnonymousAccess
		 */

		protected boolean localAllowAnonymousAccess;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getAllowAnonymousAccess() {
			return localAllowAnonymousAccess;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AllowAnonymousAccess
		 */
		public void setAllowAnonymousAccess(boolean param) {

			this.localAllowAnonymousAccess = param;

		}

		/**
		 * field for AnonymousViewListItems
		 */

		protected boolean localAnonymousViewListItems;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getAnonymousViewListItems() {
			return localAnonymousViewListItems;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AnonymousViewListItems
		 */
		public void setAnonymousViewListItems(boolean param) {

			this.localAnonymousViewListItems = param;

		}

		/**
		 * field for ReadSecurity
		 */

		protected int localReadSecurity;

		/**
		 * Auto generated getter method
		 * 
		 * @return int
		 */
		public int getReadSecurity() {
			return localReadSecurity;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ReadSecurity
		 */
		public void setReadSecurity(int param) {

			this.localReadSecurity = param;

		}

		/**
		 * field for Permissions
		 */

		protected java.lang.String localPermissions;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPermissionsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPermissions() {
			return localPermissions;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Permissions
		 */
		public void setPermissions(java.lang.String param) {

			// update the setting tracker
			localPermissionsTracker = true;

			this.localPermissions = param;

		}

		/**
		 * field for MultipleDataList
		 */

		protected boolean localMultipleDataList;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getMultipleDataList() {
			return localMultipleDataList;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            MultipleDataList
		 */
		public void setMultipleDataList(boolean param) {

			this.localMultipleDataList = param;

		}

		/**
		 * field for RootFolder
		 */

		protected java.lang.String localRootFolder;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localRootFolderTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getRootFolder() {
			return localRootFolder;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            RootFolder
		 */
		public void setRootFolder(java.lang.String param) {

			// update the setting tracker
			localRootFolderTracker = true;

			this.localRootFolder = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localTitleTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Title",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Title");
							}

						} else {
							xmlWriter.writeStartElement("Title");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Title");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localTitle));
						xmlWriter.writeEndElement();
					}
					if (localDescriptionTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"Description", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"Description");
							}

						} else {
							xmlWriter.writeStartElement("Description");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Description");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localDescription));
						xmlWriter.writeEndElement();
					}
					if (localBaseTypeTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "BaseType",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"BaseType");
							}

						} else {
							xmlWriter.writeStartElement("BaseType");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","BaseType");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localBaseType));
						xmlWriter.writeEndElement();
					}
					if (localBaseTemplateTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"BaseTemplate", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"BaseTemplate");
							}

						} else {
							xmlWriter.writeStartElement("BaseTemplate");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","BaseTemplate");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localBaseTemplate));
						xmlWriter.writeEndElement();
					}
					if (localDefaultViewUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"DefaultViewUrl", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"DefaultViewUrl");
							}

						} else {
							xmlWriter.writeStartElement("DefaultViewUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","DefaultViewUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localDefaultViewUrl));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "LastModified",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModified");
						}

					} else {
						xmlWriter.writeStartElement("LastModified");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModified));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"LastModifiedForceRecrawl", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModifiedForceRecrawl");
						}

					} else {
						xmlWriter.writeStartElement("LastModifiedForceRecrawl");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModifiedForceRecrawl");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModifiedForceRecrawl));
					xmlWriter.writeEndElement();
					if (localAuthorTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Author",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "Author");
							}

						} else {
							xmlWriter.writeStartElement("Author");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Author");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localAuthor));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"ValidSecurityInfo", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"ValidSecurityInfo");
						}

					} else {
						xmlWriter.writeStartElement("ValidSecurityInfo");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","ValidSecurityInfo");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localValidSecurityInfo));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"InheritedSecurity", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"InheritedSecurity");
						}

					} else {
						xmlWriter.writeStartElement("InheritedSecurity");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","InheritedSecurity");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localInheritedSecurity));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"AllowAnonymousAccess", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"AllowAnonymousAccess");
						}

					} else {
						xmlWriter.writeStartElement("AllowAnonymousAccess");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","AllowAnonymousAccess");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localAllowAnonymousAccess));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"AnonymousViewListItems", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"AnonymousViewListItems");
						}

					} else {
						xmlWriter.writeStartElement("AnonymousViewListItems");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","AnonymousViewListItems");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localAnonymousViewListItems));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "ReadSecurity",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"ReadSecurity");
						}

					} else {
						xmlWriter.writeStartElement("ReadSecurity");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","ReadSecurity");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localReadSecurity));
					xmlWriter.writeEndElement();
					if (localPermissionsTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"Permissions", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"Permissions");
							}

						} else {
							xmlWriter.writeStartElement("Permissions");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Permissions");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localPermissions));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"MultipleDataList", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"MultipleDataList");
						}

					} else {
						xmlWriter.writeStartElement("MultipleDataList");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","MultipleDataList");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localMultipleDataList));
					xmlWriter.writeEndElement();
					if (localRootFolderTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"RootFolder", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"RootFolder");
							}

						} else {
							xmlWriter.writeStartElement("RootFolder");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","RootFolder");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localRootFolder));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localTitleTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Title"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localTitle));
			}
			if (localDescriptionTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Description"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localDescription));
			}
			if (localBaseTypeTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"BaseType"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localBaseType));
			}
			if (localBaseTemplateTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"BaseTemplate"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localBaseTemplate));
			}
			if (localDefaultViewUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"DefaultViewUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localDefaultViewUrl));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModified"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModified));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModifiedForceRecrawl"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModifiedForceRecrawl));
			if (localAuthorTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Author"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localAuthor));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"ValidSecurityInfo"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localValidSecurityInfo));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"InheritedSecurity"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localInheritedSecurity));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"AllowAnonymousAccess"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localAllowAnonymousAccess));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"AnonymousViewListItems"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localAnonymousViewListItems));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"ReadSecurity"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localReadSecurity));
			if (localPermissionsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Permissions"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localPermissions));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"MultipleDataList"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localMultipleDataList));
			if (localRootFolderTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"RootFolder"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localRootFolder));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sListMetadata parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sListMetadata object = new _sListMetadata();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sListMetadata".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sListMetadata) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Title").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setTitle(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Description").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setDescription(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"BaseType").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setBaseType(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"BaseTemplate").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setBaseTemplate(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"DefaultViewUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setDefaultViewUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModifiedForceRecrawl").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModifiedForceRecrawl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Author").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAuthor(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"ValidSecurityInfo").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setValidSecurityInfo(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"InheritedSecurity").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setInheritedSecurity(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"AllowAnonymousAccess").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAllowAnonymousAccess(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"AnonymousViewListItems").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAnonymousViewListItems(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"ReadSecurity").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setReadSecurity(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Permissions").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setPermissions(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"MultipleDataList")
									.equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setMultipleDataList(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"RootFolder").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setRootFolder(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetListItems implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListItems", "ns1");

		/**
		 * field for StrListName
		 */

		protected java.lang.String localStrListName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrListNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrListName() {
			return localStrListName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrListName
		 */
		public void setStrListName(java.lang.String param) {

			// update the setting tracker
			localStrListNameTracker = true;

			this.localStrListName = param;

		}

		/**
		 * field for StrQuery
		 */

		protected java.lang.String localStrQuery;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrQueryTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrQuery() {
			return localStrQuery;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrQuery
		 */
		public void setStrQuery(java.lang.String param) {

			// update the setting tracker
			localStrQueryTracker = true;

			this.localStrQuery = param;

		}

		/**
		 * field for StrViewFields
		 */

		protected java.lang.String localStrViewFields;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrViewFieldsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrViewFields() {
			return localStrViewFields;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrViewFields
		 */
		public void setStrViewFields(java.lang.String param) {

			// update the setting tracker
			localStrViewFieldsTracker = true;

			this.localStrViewFields = param;

		}

		/**
		 * field for URowLimit
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localURowLimit;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getURowLimit() {
			return localURowLimit;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            URowLimit
		 */
		public void setURowLimit(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localURowLimit = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStrListNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strListName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strListName");
							}

						} else {
							xmlWriter.writeStartElement("strListName");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strListName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrListName));
						xmlWriter.writeEndElement();
					}
					if (localStrQueryTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strQuery",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strQuery");
							}

						} else {
							xmlWriter.writeStartElement("strQuery");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strQuery");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrQuery));
						xmlWriter.writeEndElement();
					}
					if (localStrViewFieldsTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strViewFields", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strViewFields");
							}

						} else {
							xmlWriter.writeStartElement("strViewFields");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strViewFields");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrViewFields));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "uRowLimit",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "uRowLimit");
						}

					} else {
						xmlWriter.writeStartElement("uRowLimit");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","uRowLimit");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localURowLimit));
					xmlWriter.writeEndElement();

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStrListNameTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strListName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrListName));
			}
			if (localStrQueryTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strQuery"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrQuery));
			}
			if (localStrViewFieldsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strViewFields"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrViewFields));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"uRowLimit"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localURowLimit));

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetListItems parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListItems object = new GetListItems();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetListItems".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListItems) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strListName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrListName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strQuery").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrQuery(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strViewFields").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrViewFields(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"uRowLimit").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setURowLimit(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetSiteAndWebResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetSiteAndWebResponse", "ns1");

		/**
		 * field for GetSiteAndWebResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localGetSiteAndWebResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getGetSiteAndWebResult() {
			return localGetSiteAndWebResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetSiteAndWebResult
		 */
		public void setGetSiteAndWebResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localGetSiteAndWebResult = param;

		}

		/**
		 * field for StrSite
		 */

		protected java.lang.String localStrSite;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrSiteTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrSite() {
			return localStrSite;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrSite
		 */
		public void setStrSite(java.lang.String param) {

			// update the setting tracker
			localStrSiteTracker = true;

			this.localStrSite = param;

		}

		/**
		 * field for StrWeb
		 */

		protected java.lang.String localStrWeb;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrWebTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrWeb() {
			return localStrWeb;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrWeb
		 */
		public void setStrWeb(java.lang.String param) {

			// update the setting tracker
			localStrWebTracker = true;

			this.localStrWeb = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"GetSiteAndWebResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetSiteAndWebResult");
						}

					} else {
						xmlWriter.writeStartElement("GetSiteAndWebResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetSiteAndWebResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetSiteAndWebResult));
					xmlWriter.writeEndElement();
					if (localStrSiteTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strSite",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strSite");
							}

						} else {
							xmlWriter.writeStartElement("strSite");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strSite");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrSite));
						xmlWriter.writeEndElement();
					}
					if (localStrWebTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strWeb",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "strWeb");
							}

						} else {
							xmlWriter.writeStartElement("strWeb");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strWeb");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrWeb));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetSiteAndWebResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetSiteAndWebResult));
			if (localStrSiteTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strSite"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrSite));
			}
			if (localStrWebTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strWeb"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrWeb));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetSiteAndWebResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetSiteAndWebResponse object = new GetSiteAndWebResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetSiteAndWebResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetSiteAndWebResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetSiteAndWebResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetSiteAndWebResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strSite").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrSite(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strWeb").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrWeb(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArrayOf_sWebWithTime implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOf_sWebWithTime Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for _sWebWithTime This was an Array!
		 */

		protected _sWebWithTime[] local_sWebWithTime;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean local_sWebWithTimeTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sWebWithTime[]
		 */
		public _sWebWithTime[] get_sWebWithTime() {
			return local_sWebWithTime;
		}

		/**
		 * validate the array for _sWebWithTime
		 */
		protected void validate_sWebWithTime(_sWebWithTime[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            _sWebWithTime
		 */
		public void set_sWebWithTime(_sWebWithTime[] param) {

			validate_sWebWithTime(param);

			if (param != null) {
				// update the setting tracker
				local_sWebWithTimeTracker = true;
			}

			this.local_sWebWithTime = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            _sWebWithTime
		 */
		public void add_sWebWithTime(_sWebWithTime param) {
			if (local_sWebWithTime == null) {
				local_sWebWithTime = new _sWebWithTime[] {};
			}

			// update the setting tracker
			local_sWebWithTimeTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(local_sWebWithTime);
			list.add(param);
			this.local_sWebWithTime = (_sWebWithTime[]) list
					.toArray(new _sWebWithTime[list.size()]);

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (local_sWebWithTimeTracker) {
						if (local_sWebWithTime == null) {
							throw new RuntimeException(
									"_sWebWithTime cannot be null!!");
						}

						for (int i = 0; i < local_sWebWithTime.length; i++) {
							local_sWebWithTime[i]
									.getOMElement(
											new javax.xml.namespace.QName(
													"http://schemas.microsoft.com/sharepoint/soap/",
													"_sWebWithTime"), factory)
									.serialize(xmlWriter);

						}

					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (local_sWebWithTimeTracker) {
				if (local_sWebWithTime == null) {
					throw new RuntimeException("_sWebWithTime cannot be null!!");
				}

				for (int i = 0; i < local_sWebWithTime.length; i++) {
					elementList.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"_sWebWithTime"));
					elementList.add(local_sWebWithTime[i]);
				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static ArrayOf_sWebWithTime parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOf_sWebWithTime object = new ArrayOf_sWebWithTime();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"ArrayOf_sWebWithTime".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOf_sWebWithTime) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"_sWebWithTime").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(_sWebWithTime.Factory.parse(reader));
						// loop until we find a start element that is not part
						// of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement())
								reader.next();
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement()
									&& !reader.isEndElement())
								reader.next();
							if (reader.isEndElement()) {
								// two continuous end elements means we are
								// exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"_sWebWithTime").equals(reader
										.getName())) {
									list1.add(_sWebWithTime.Factory
											.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.set_sWebWithTime((_sWebWithTime[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(_sWebWithTime.class,
												list1));

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetAttachments implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetAttachments", "ns1");

		/**
		 * field for StrListName
		 */

		protected java.lang.String localStrListName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrListNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrListName() {
			return localStrListName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrListName
		 */
		public void setStrListName(java.lang.String param) {

			// update the setting tracker
			localStrListNameTracker = true;

			this.localStrListName = param;

		}

		/**
		 * field for StrItemId
		 */

		protected java.lang.String localStrItemId;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrItemIdTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrItemId() {
			return localStrItemId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrItemId
		 */
		public void setStrItemId(java.lang.String param) {

			// update the setting tracker
			localStrItemIdTracker = true;

			this.localStrItemId = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStrListNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strListName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strListName");
							}

						} else {
							xmlWriter.writeStartElement("strListName");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strListName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrListName));
						xmlWriter.writeEndElement();
					}
					if (localStrItemIdTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strItemId", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strItemId");
							}

						} else {
							xmlWriter.writeStartElement("strItemId");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strItemId");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrItemId));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStrListNameTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strListName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrListName));
			}
			if (localStrItemIdTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strItemId"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrItemId));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetAttachments parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetAttachments object = new GetAttachments();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetAttachments".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetAttachments) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strListName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrListName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strItemId").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrItemId(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sWebWithTime implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sWebWithTime Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for Url
		 */

		protected java.lang.String localUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUrl() {
			return localUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Url
		 */
		public void setUrl(java.lang.String param) {

			// update the setting tracker
			localUrlTracker = true;

			this.localUrl = param;

		}

		/**
		 * field for LastModified
		 */

		protected java.util.Calendar localLastModified;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.util.Calendar param) {

			this.localLastModified = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Url",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Url");
							}

						} else {
							xmlWriter.writeStartElement("Url");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Url");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localUrl));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "LastModified",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModified");
						}

					} else {
						xmlWriter.writeStartElement("LastModified");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModified));
					xmlWriter.writeEndElement();

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localUrlTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"Url"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localUrl));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModified"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModified));

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sWebWithTime parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sWebWithTime object = new _sWebWithTime();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sWebWithTime".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sWebWithTime) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Url").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArrayOf_sFPUrl implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOf_sFPUrl Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for _sFPUrl This was an Array!
		 */

		protected _sFPUrl[] local_sFPUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean local_sFPUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sFPUrl[]
		 */
		public _sFPUrl[] get_sFPUrl() {
			return local_sFPUrl;
		}

		/**
		 * validate the array for _sFPUrl
		 */
		protected void validate_sFPUrl(_sFPUrl[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            _sFPUrl
		 */
		public void set_sFPUrl(_sFPUrl[] param) {

			validate_sFPUrl(param);

			if (param != null) {
				// update the setting tracker
				local_sFPUrlTracker = true;
			}

			this.local_sFPUrl = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            _sFPUrl
		 */
		public void add_sFPUrl(_sFPUrl param) {
			if (local_sFPUrl == null) {
				local_sFPUrl = new _sFPUrl[] {};
			}

			// update the setting tracker
			local_sFPUrlTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(local_sFPUrl);
			list.add(param);
			this.local_sFPUrl = (_sFPUrl[]) list.toArray(new _sFPUrl[list
					.size()]);

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (local_sFPUrlTracker) {
						if (local_sFPUrl == null) {
							throw new RuntimeException(
									"_sFPUrl cannot be null!!");
						}

						for (int i = 0; i < local_sFPUrl.length; i++) {
							local_sFPUrl[i]
									.getOMElement(
											new javax.xml.namespace.QName(
													"http://schemas.microsoft.com/sharepoint/soap/",
													"_sFPUrl"), factory)
									.serialize(xmlWriter);

						}

					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (local_sFPUrlTracker) {
				if (local_sFPUrl == null) {
					throw new RuntimeException("_sFPUrl cannot be null!!");
				}

				for (int i = 0; i < local_sFPUrl.length; i++) {
					elementList.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"_sFPUrl"));
					elementList.add(local_sFPUrl[i]);
				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static ArrayOf_sFPUrl parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOf_sFPUrl object = new ArrayOf_sFPUrl();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"ArrayOf_sFPUrl".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOf_sFPUrl) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"_sFPUrl").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(_sFPUrl.Factory.parse(reader));
						// loop until we find a start element that is not part
						// of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement())
								reader.next();
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement()
									&& !reader.isEndElement())
								reader.next();
							if (reader.isEndElement()) {
								// two continuous end elements means we are
								// exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"_sFPUrl").equals(reader.getName())) {
									list1.add(_sFPUrl.Factory.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.set_sFPUrl((_sFPUrl[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(_sFPUrl.class, list1));

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sFPUrl implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sFPUrl Namespace URI = http://schemas.microsoft.com/sharepoint/soap/
		 * Namespace Prefix = ns1
		 */

		/**
		 * field for Url
		 */

		protected java.lang.String localUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getUrl() {
			return localUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Url
		 */
		public void setUrl(java.lang.String param) {

			// update the setting tracker
			localUrlTracker = true;

			this.localUrl = param;

		}

		/**
		 * field for LastModified
		 */

		protected java.util.Calendar localLastModified;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.util.Calendar param) {

			this.localLastModified = param;

		}

		/**
		 * field for IsFolder
		 */

		protected boolean localIsFolder;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getIsFolder() {
			return localIsFolder;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            IsFolder
		 */
		public void setIsFolder(boolean param) {

			this.localIsFolder = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Url",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Url");
							}

						} else {
							xmlWriter.writeStartElement("Url");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Url");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localUrl));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "LastModified",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModified");
						}

					} else {
						xmlWriter.writeStartElement("LastModified");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModified));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "IsFolder",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "IsFolder");
						}

					} else {
						xmlWriter.writeStartElement("IsFolder");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","IsFolder");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localIsFolder));
					xmlWriter.writeEndElement();

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localUrlTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"Url"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localUrl));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModified"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModified));

			elementList
					.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"IsFolder"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localIsFolder));

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sFPUrl parse(javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sFPUrl object = new _sFPUrl();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sFPUrl".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sFPUrl) ExtensionMapper.getTypeObject(
										nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Url").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"IsFolder").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setIsFolder(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetAttachmentsResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetAttachmentsResponse", "ns1");

		/**
		 * field for GetAttachmentsResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localGetAttachmentsResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getGetAttachmentsResult() {
			return localGetAttachmentsResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetAttachmentsResult
		 */
		public void setGetAttachmentsResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localGetAttachmentsResult = param;

		}

		/**
		 * field for VAttachments
		 */

		protected ArrayOfString localVAttachments;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVAttachmentsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfString
		 */
		public ArrayOfString getVAttachments() {
			return localVAttachments;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VAttachments
		 */
		public void setVAttachments(ArrayOfString param) {

			// update the setting tracker
			localVAttachmentsTracker = true;

			this.localVAttachments = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"GetAttachmentsResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetAttachmentsResult");
						}

					} else {
						xmlWriter.writeStartElement("GetAttachmentsResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetAttachmentsResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetAttachmentsResult));
					xmlWriter.writeEndElement();
					if (localVAttachmentsTracker) {
						if (localVAttachments == null) {
							throw new RuntimeException(
									"vAttachments cannot be null!!");
						}
						localVAttachments
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vAttachments"), factory)
								.serialize(xmlWriter);
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetAttachmentsResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetAttachmentsResult));
			if (localVAttachmentsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vAttachments"));

				if (localVAttachments == null) {
					throw new RuntimeException("vAttachments cannot be null!!");
				}
				elementList.add(localVAttachments);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetAttachmentsResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetAttachmentsResponse object = new GetAttachmentsResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetAttachmentsResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetAttachmentsResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetAttachmentsResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetAttachmentsResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vAttachments").equals(reader.getName())) {

						object.setVAttachments(ArrayOfString.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetListResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListResponse", "ns1");

		/**
		 * field for GetListResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localGetListResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getGetListResult() {
			return localGetListResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetListResult
		 */
		public void setGetListResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localGetListResult = param;

		}

		/**
		 * field for SListMetadata
		 */

		protected _sListMetadata localSListMetadata;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sListMetadata
		 */
		public _sListMetadata getSListMetadata() {
			return localSListMetadata;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            SListMetadata
		 */
		public void setSListMetadata(_sListMetadata param) {

			this.localSListMetadata = param;

		}

		/**
		 * field for VProperties
		 */

		protected ArrayOf_sProperty localVProperties;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVPropertiesTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sProperty
		 */
		public ArrayOf_sProperty getVProperties() {
			return localVProperties;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VProperties
		 */
		public void setVProperties(ArrayOf_sProperty param) {

			// update the setting tracker
			localVPropertiesTracker = true;

			this.localVProperties = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"GetListResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetListResult");
						}

					} else {
						xmlWriter.writeStartElement("GetListResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetListResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetListResult));
					xmlWriter.writeEndElement();

					if (localSListMetadata == null) {
						throw new RuntimeException(
								"sListMetadata cannot be null!!");
					}
					localSListMetadata
							.getOMElement(
									new javax.xml.namespace.QName(
											"http://schemas.microsoft.com/sharepoint/soap/",
											"sListMetadata"), factory)
							.serialize(xmlWriter);
					if (localVPropertiesTracker) {
						if (localVProperties == null) {
							throw new RuntimeException(
									"vProperties cannot be null!!");
						}
						localVProperties
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vProperties"), factory)
								.serialize(xmlWriter);
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetListResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetListResult));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"sListMetadata"));

			if (localSListMetadata == null) {
				throw new RuntimeException("sListMetadata cannot be null!!");
			}
			elementList.add(localSListMetadata);
			if (localVPropertiesTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vProperties"));

				if (localVProperties == null) {
					throw new RuntimeException("vProperties cannot be null!!");
				}
				elementList.add(localVProperties);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetListResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListResponse object = new GetListResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetListResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetListResult").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetListResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"sListMetadata").equals(reader.getName())) {

						object.setSListMetadata(_sListMetadata.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vProperties").equals(reader.getName())) {

						object.setVProperties(ArrayOf_sProperty.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetList implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/", "GetList",
				"ns1");

		/**
		 * field for StrListName
		 */

		protected java.lang.String localStrListName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrListNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrListName() {
			return localStrListName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrListName
		 */
		public void setStrListName(java.lang.String param) {

			// update the setting tracker
			localStrListNameTracker = true;

			this.localStrListName = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStrListNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strListName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strListName");
							}

						} else {
							xmlWriter.writeStartElement("strListName");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strListName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrListName));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStrListNameTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strListName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrListName));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetList parse(javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetList object = new GetList();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetList".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetList) ExtensionMapper.getTypeObject(
										nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strListName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrListName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArrayOf_sProperty implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOf_sProperty Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for _sProperty This was an Array!
		 */

		protected _sProperty[] local_sProperty;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean local_sPropertyTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sProperty[]
		 */
		public _sProperty[] get_sProperty() {
			return local_sProperty;
		}

		/**
		 * validate the array for _sProperty
		 */
		protected void validate_sProperty(_sProperty[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            _sProperty
		 */
		public void set_sProperty(_sProperty[] param) {

			validate_sProperty(param);

			if (param != null) {
				// update the setting tracker
				local_sPropertyTracker = true;
			}

			this.local_sProperty = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            _sProperty
		 */
		public void add_sProperty(_sProperty param) {
			if (local_sProperty == null) {
				local_sProperty = new _sProperty[] {};
			}

			// update the setting tracker
			local_sPropertyTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(local_sProperty);
			list.add(param);
			this.local_sProperty = (_sProperty[]) list
					.toArray(new _sProperty[list.size()]);

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (local_sPropertyTracker) {
						if (local_sProperty == null) {
							throw new RuntimeException(
									"_sProperty cannot be null!!");
						}

						for (int i = 0; i < local_sProperty.length; i++) {
							local_sProperty[i]
									.getOMElement(
											new javax.xml.namespace.QName(
													"http://schemas.microsoft.com/sharepoint/soap/",
													"_sProperty"), factory)
									.serialize(xmlWriter);

						}

					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (local_sPropertyTracker) {
				if (local_sProperty == null) {
					throw new RuntimeException("_sProperty cannot be null!!");
				}

				for (int i = 0; i < local_sProperty.length; i++) {
					elementList.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"_sProperty"));
					elementList.add(local_sProperty[i]);
				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static ArrayOf_sProperty parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOf_sProperty object = new ArrayOf_sProperty();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"ArrayOf_sProperty".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOf_sProperty) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"_sProperty").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(_sProperty.Factory.parse(reader));
						// loop until we find a start element that is not part
						// of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement())
								reader.next();
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement()
									&& !reader.isEndElement())
								reader.next();
							if (reader.isEndElement()) {
								// two continuous end elements means we are
								// exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"_sProperty").equals(reader.getName())) {
									list1.add(_sProperty.Factory.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.set_sProperty((_sProperty[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(_sProperty.class, list1));

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetSiteResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetSiteResponse", "ns1");

		/**
		 * field for GetSiteResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localGetSiteResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getGetSiteResult() {
			return localGetSiteResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetSiteResult
		 */
		public void setGetSiteResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localGetSiteResult = param;

		}

		/**
		 * field for SSiteMetadata
		 */

		protected _sSiteMetadata localSSiteMetadata;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sSiteMetadata
		 */
		public _sSiteMetadata getSSiteMetadata() {
			return localSSiteMetadata;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            SSiteMetadata
		 */
		public void setSSiteMetadata(_sSiteMetadata param) {

			this.localSSiteMetadata = param;

		}

		/**
		 * field for VWebs
		 */

		protected ArrayOf_sWebWithTime localVWebs;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVWebsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sWebWithTime
		 */
		public ArrayOf_sWebWithTime getVWebs() {
			return localVWebs;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VWebs
		 */
		public void setVWebs(ArrayOf_sWebWithTime param) {

			// update the setting tracker
			localVWebsTracker = true;

			this.localVWebs = param;

		}

		/**
		 * field for StrUsers
		 */

		protected java.lang.String localStrUsers;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrUsersTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrUsers() {
			return localStrUsers;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrUsers
		 */
		public void setStrUsers(java.lang.String param) {

			// update the setting tracker
			localStrUsersTracker = true;

			this.localStrUsers = param;

		}

		/**
		 * field for StrGroups
		 */

		protected java.lang.String localStrGroups;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrGroupsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrGroups() {
			return localStrGroups;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrGroups
		 */
		public void setStrGroups(java.lang.String param) {

			// update the setting tracker
			localStrGroupsTracker = true;

			this.localStrGroups = param;

		}

		/**
		 * field for VGroups
		 */

		protected ArrayOfString localVGroups;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVGroupsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfString
		 */
		public ArrayOfString getVGroups() {
			return localVGroups;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VGroups
		 */
		public void setVGroups(ArrayOfString param) {

			// update the setting tracker
			localVGroupsTracker = true;

			this.localVGroups = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"GetSiteResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetSiteResult");
						}

					} else {
						xmlWriter.writeStartElement("GetSiteResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetSiteResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetSiteResult));
					xmlWriter.writeEndElement();

					if (localSSiteMetadata == null) {
						throw new RuntimeException(
								"sSiteMetadata cannot be null!!");
					}
					localSSiteMetadata
							.getOMElement(
									new javax.xml.namespace.QName(
											"http://schemas.microsoft.com/sharepoint/soap/",
											"sSiteMetadata"), factory)
							.serialize(xmlWriter);
					if (localVWebsTracker) {
						if (localVWebs == null) {
							throw new RuntimeException("vWebs cannot be null!!");
						}
						localVWebs
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vWebs"), factory).serialize(
										xmlWriter);
					}
					if (localStrUsersTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strUsers",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strUsers");
							}

						} else {
							xmlWriter.writeStartElement("strUsers");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strUsers");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrUsers));
						xmlWriter.writeEndElement();
					}
					if (localStrGroupsTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strGroups", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strGroups");
							}

						} else {
							xmlWriter.writeStartElement("strGroups");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strGroups");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrGroups));
						xmlWriter.writeEndElement();
					}
					if (localVGroupsTracker) {
						if (localVGroups == null) {
							throw new RuntimeException(
									"vGroups cannot be null!!");
						}
						localVGroups
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vGroups"), factory).serialize(
										xmlWriter);
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetSiteResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetSiteResult));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"sSiteMetadata"));

			if (localSSiteMetadata == null) {
				throw new RuntimeException("sSiteMetadata cannot be null!!");
			}
			elementList.add(localSSiteMetadata);
			if (localVWebsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vWebs"));

				if (localVWebs == null) {
					throw new RuntimeException("vWebs cannot be null!!");
				}
				elementList.add(localVWebs);
			}
			if (localStrUsersTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strUsers"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrUsers));
			}
			if (localStrGroupsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strGroups"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrGroups));
			}
			if (localVGroupsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vGroups"));

				if (localVGroups == null) {
					throw new RuntimeException("vGroups cannot be null!!");
				}
				elementList.add(localVGroups);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetSiteResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetSiteResponse object = new GetSiteResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetSiteResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetSiteResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetSiteResult").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetSiteResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"sSiteMetadata").equals(reader.getName())) {

						object.setSSiteMetadata(_sSiteMetadata.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vWebs").equals(reader.getName())) {

						object.setVWebs(ArrayOf_sWebWithTime.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strUsers").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrUsers(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strGroups").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrGroups(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vGroups").equals(reader.getName())) {

						object.setVGroups(ArrayOfString.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetListCollectionResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListCollectionResponse", "ns1");

		/**
		 * field for GetListCollectionResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localGetListCollectionResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getGetListCollectionResult() {
			return localGetListCollectionResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetListCollectionResult
		 */
		public void setGetListCollectionResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localGetListCollectionResult = param;

		}

		/**
		 * field for VLists
		 */

		protected ArrayOf_sList localVLists;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVListsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sList
		 */
		public ArrayOf_sList getVLists() {
			return localVLists;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VLists
		 */
		public void setVLists(ArrayOf_sList param) {

			// update the setting tracker
			localVListsTracker = true;

			this.localVLists = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"GetListCollectionResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetListCollectionResult");
						}

					} else {
						xmlWriter.writeStartElement("GetListCollectionResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetListCollectionResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetListCollectionResult));
					xmlWriter.writeEndElement();
					if (localVListsTracker) {
						if (localVLists == null) {
							throw new RuntimeException(
									"vLists cannot be null!!");
						}
						localVLists
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vLists"), factory).serialize(
										xmlWriter);
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetListCollectionResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetListCollectionResult));
			if (localVListsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vLists"));

				if (localVLists == null) {
					throw new RuntimeException("vLists cannot be null!!");
				}
				elementList.add(localVLists);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetListCollectionResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListCollectionResponse object = new GetListCollectionResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetListCollectionResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListCollectionResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetListCollectionResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetListCollectionResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vLists").equals(reader.getName())) {

						object.setVLists(ArrayOf_sList.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArrayOfString implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOfString Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for String This was an Array!
		 */

		protected java.lang.String[] localString;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStringTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String[]
		 */
		public java.lang.String[] getString() {
			return localString;
		}

		/**
		 * validate the array for String
		 */
		protected void validateString(java.lang.String[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            String
		 */
		public void setString(java.lang.String[] param) {

			validateString(param);

			// update the setting tracker
			localStringTracker = true;

			this.localString = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            java.lang.String
		 */
		public void addString(java.lang.String param) {
			if (localString == null) {
				localString = new java.lang.String[] {};
			}

			// update the setting tracker
			localStringTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(localString);
			list.add(param);
			this.localString = (java.lang.String[]) list
					.toArray(new java.lang.String[list.size()]);

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStringTracker) {
						// this property is nillable
						if (localString != null) {

							for (int i = 0; i < localString.length; i++) {
								namespace = "http://schemas.microsoft.com/sharepoint/soap/";

								if (!namespace.equals("")) {
									prefix = xmlWriter.getPrefix(namespace);

									if (prefix == null) {
										prefix = org.apache.axis2.databinding.utils.BeanUtil
												.getUniquePrefix();

										xmlWriter.writeStartElement(prefix,
												"string", namespace);
										xmlWriter.writeNamespace(prefix,
												namespace);
										xmlWriter.setPrefix(prefix, namespace);

									} else {
										xmlWriter.writeStartElement(namespace,
												"string");
									}

								} else {
									xmlWriter.writeStartElement("string");
								}

								if (localString[i] == null) {
									// write the nil attribute
									writeAttribute(
											"xsi",
											"http://www.w3.org/2001/XMLSchema-instance",
											"nil", "true", xmlWriter);
								} else {
									xmlWriter
											.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
													.convertToString(localString[i]));
								}

								xmlWriter.writeEndElement();
							}

						}
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStringTracker) {
				// this property is nillable
				if (localString != null) {

					for (int i = 0; i < localString.length; i++) {
						elementList
								.add(new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"string"));
						elementList
								.add(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localString[i]));
					}

				}
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static ArrayOfString parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOfString object = new ArrayOfString();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"ArrayOfString".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOfString) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"string").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(reader.getElementText());
						// loop until we find a start element that is not part
						// of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// Ensure we are at the EndElement
							while (!reader.isEndElement()) {
								reader.next();
							}
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement()
									&& !reader.isEndElement())
								reader.next();
							if (reader.isEndElement()) {
								// two continuous end elements means we are
								// exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"string").equals(reader.getName())) {
									list1.add(reader.getElementText());
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.setString((java.lang.String[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(java.lang.String.class,
												list1));

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetListItemsResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListItemsResponse", "ns1");

		/**
		 * field for GetListItemsResult
		 */

		protected java.lang.String localGetListItemsResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetListItemsResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getGetListItemsResult() {
			return localGetListItemsResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetListItemsResult
		 */
		public void setGetListItemsResult(java.lang.String param) {

			// update the setting tracker
			localGetListItemsResultTracker = true;

			this.localGetListItemsResult = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localGetListItemsResultTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"GetListItemsResult", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"GetListItemsResult");
							}

						} else {
							xmlWriter.writeStartElement("GetListItemsResult");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetListItemsResult");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localGetListItemsResult));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localGetListItemsResultTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"GetListItemsResult"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localGetListItemsResult));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetListItemsResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListItemsResponse object = new GetListItemsResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetListItemsResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListItemsResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetListItemsResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetListItemsResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArrayOf_sList implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOf_sList Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for _sList This was an Array!
		 */

		protected _sList[] local_sList;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean local_sListTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sList[]
		 */
		public _sList[] get_sList() {
			return local_sList;
		}

		/**
		 * validate the array for _sList
		 */
		protected void validate_sList(_sList[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            _sList
		 */
		public void set_sList(_sList[] param) {

			validate_sList(param);

			if (param != null) {
				// update the setting tracker
				local_sListTracker = true;
			}

			this.local_sList = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            _sList
		 */
		public void add_sList(_sList param) {
			if (local_sList == null) {
				local_sList = new _sList[] {};
			}

			// update the setting tracker
			local_sListTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(local_sList);
			list.add(param);
			this.local_sList = (_sList[]) list.toArray(new _sList[list.size()]);

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (local_sListTracker) {
						if (local_sList == null) {
							throw new RuntimeException(
									"_sList cannot be null!!");
						}

						for (int i = 0; i < local_sList.length; i++) {
							local_sList[i]
									.getOMElement(
											new javax.xml.namespace.QName(
													"http://schemas.microsoft.com/sharepoint/soap/",
													"_sList"), factory)
									.serialize(xmlWriter);

						}

					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (local_sListTracker) {
				if (local_sList == null) {
					throw new RuntimeException("_sList cannot be null!!");
				}

				for (int i = 0; i < local_sList.length; i++) {
					elementList.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"_sList"));
					elementList.add(local_sList[i]);
				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static ArrayOf_sList parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOf_sList object = new ArrayOf_sList();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"ArrayOf_sList".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOf_sList) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"_sList").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(_sList.Factory.parse(reader));
						// loop until we find a start element that is not part
						// of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement())
								reader.next();
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement()
									&& !reader.isEndElement())
								reader.next();
							if (reader.isEndElement()) {
								// two continuous end elements means we are
								// exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"_sList").equals(reader.getName())) {
									list1.add(_sList.Factory.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.set_sList((_sList[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(_sList.class, list1));

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetURLSegmentsResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetURLSegmentsResponse", "ns1");

		/**
		 * field for GetURLSegmentsResult
		 */

		protected boolean localGetURLSegmentsResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getGetURLSegmentsResult() {
			return localGetURLSegmentsResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetURLSegmentsResult
		 */
		public void setGetURLSegmentsResult(boolean param) {

			this.localGetURLSegmentsResult = param;

		}

		/**
		 * field for StrWebID
		 */

		protected java.lang.String localStrWebID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrWebIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrWebID() {
			return localStrWebID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrWebID
		 */
		public void setStrWebID(java.lang.String param) {

			// update the setting tracker
			localStrWebIDTracker = true;

			this.localStrWebID = param;

		}

		/**
		 * field for StrBucketID
		 */

		protected java.lang.String localStrBucketID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrBucketIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrBucketID() {
			return localStrBucketID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrBucketID
		 */
		public void setStrBucketID(java.lang.String param) {

			// update the setting tracker
			localStrBucketIDTracker = true;

			this.localStrBucketID = param;

		}

		/**
		 * field for StrListID
		 */

		protected java.lang.String localStrListID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrListIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrListID() {
			return localStrListID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrListID
		 */
		public void setStrListID(java.lang.String param) {

			// update the setting tracker
			localStrListIDTracker = true;

			this.localStrListID = param;

		}

		/**
		 * field for StrItemID
		 */

		protected java.lang.String localStrItemID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrItemIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrItemID() {
			return localStrItemID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrItemID
		 */
		public void setStrItemID(java.lang.String param) {

			// update the setting tracker
			localStrItemIDTracker = true;

			this.localStrItemID = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"GetURLSegmentsResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetURLSegmentsResult");
						}

					} else {
						xmlWriter.writeStartElement("GetURLSegmentsResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetURLSegmentsResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetURLSegmentsResult));
					xmlWriter.writeEndElement();
					if (localStrWebIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strWebID",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strWebID");
							}

						} else {
							xmlWriter.writeStartElement("strWebID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strWebID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrWebID));
						xmlWriter.writeEndElement();
					}
					if (localStrBucketIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strBucketID", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strBucketID");
							}

						} else {
							xmlWriter.writeStartElement("strBucketID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strBucketID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrBucketID));
						xmlWriter.writeEndElement();
					}
					if (localStrListIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strListID", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strListID");
							}

						} else {
							xmlWriter.writeStartElement("strListID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strListID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrListID));
						xmlWriter.writeEndElement();
					}
					if (localStrItemIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strItemID", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strItemID");
							}

						} else {
							xmlWriter.writeStartElement("strItemID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strItemID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrItemID));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetURLSegmentsResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetURLSegmentsResult));
			if (localStrWebIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strWebID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrWebID));
			}
			if (localStrBucketIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strBucketID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrBucketID));
			}
			if (localStrListIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strListID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrListID));
			}
			if (localStrItemIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strItemID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrItemID));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetURLSegmentsResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetURLSegmentsResponse object = new GetURLSegmentsResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetURLSegmentsResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetURLSegmentsResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetURLSegmentsResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetURLSegmentsResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strWebID").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrWebID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strBucketID").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrBucketID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strListID").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrListID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strItemID").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrItemID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sList implements org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sList Namespace URI = http://schemas.microsoft.com/sharepoint/soap/
		 * Namespace Prefix = ns1
		 */

		/**
		 * field for InternalName
		 */

		protected java.lang.String localInternalName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localInternalNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getInternalName() {
			return localInternalName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            InternalName
		 */
		public void setInternalName(java.lang.String param) {

			// update the setting tracker
			localInternalNameTracker = true;

			this.localInternalName = param;

		}

		/**
		 * field for Title
		 */

		protected java.lang.String localTitle;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localTitleTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getTitle() {
			return localTitle;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Title
		 */
		public void setTitle(java.lang.String param) {

			// update the setting tracker
			localTitleTracker = true;

			this.localTitle = param;

		}

		/**
		 * field for Description
		 */

		protected java.lang.String localDescription;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localDescriptionTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getDescription() {
			return localDescription;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Description
		 */
		public void setDescription(java.lang.String param) {

			// update the setting tracker
			localDescriptionTracker = true;

			this.localDescription = param;

		}

		/**
		 * field for BaseType
		 */

		protected java.lang.String localBaseType;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localBaseTypeTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getBaseType() {
			return localBaseType;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            BaseType
		 */
		public void setBaseType(java.lang.String param) {

			// update the setting tracker
			localBaseTypeTracker = true;

			this.localBaseType = param;

		}

		/**
		 * field for BaseTemplate
		 */

		protected java.lang.String localBaseTemplate;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localBaseTemplateTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getBaseTemplate() {
			return localBaseTemplate;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            BaseTemplate
		 */
		public void setBaseTemplate(java.lang.String param) {

			// update the setting tracker
			localBaseTemplateTracker = true;

			this.localBaseTemplate = param;

		}

		/**
		 * field for DefaultViewUrl
		 */

		protected java.lang.String localDefaultViewUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localDefaultViewUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getDefaultViewUrl() {
			return localDefaultViewUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            DefaultViewUrl
		 */
		public void setDefaultViewUrl(java.lang.String param) {

			// update the setting tracker
			localDefaultViewUrlTracker = true;

			this.localDefaultViewUrl = param;

		}

		/**
		 * field for LastModified
		 */

		protected java.lang.String localLastModified;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localLastModifiedTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.lang.String param) {

			// update the setting tracker
			localLastModifiedTracker = true;

			this.localLastModified = param;

		}

		/**
		 * field for PermId
		 */

		protected java.lang.String localPermId;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPermIdTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPermId() {
			return localPermId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            PermId
		 */
		public void setPermId(java.lang.String param) {

			// update the setting tracker
			localPermIdTracker = true;

			this.localPermId = param;

		}

		/**
		 * field for InheritedSecurity
		 */

		protected boolean localInheritedSecurity;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getInheritedSecurity() {
			return localInheritedSecurity;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            InheritedSecurity
		 */
		public void setInheritedSecurity(boolean param) {

			this.localInheritedSecurity = param;

		}

		/**
		 * field for AllowAnonymousAccess
		 */

		protected boolean localAllowAnonymousAccess;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getAllowAnonymousAccess() {
			return localAllowAnonymousAccess;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AllowAnonymousAccess
		 */
		public void setAllowAnonymousAccess(boolean param) {

			this.localAllowAnonymousAccess = param;

		}

		/**
		 * field for AnonymousViewListItems
		 */

		protected boolean localAnonymousViewListItems;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getAnonymousViewListItems() {
			return localAnonymousViewListItems;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AnonymousViewListItems
		 */
		public void setAnonymousViewListItems(boolean param) {

			this.localAnonymousViewListItems = param;

		}

		/**
		 * field for ReadSecurity
		 */

		protected int localReadSecurity;

		/**
		 * Auto generated getter method
		 * 
		 * @return int
		 */
		public int getReadSecurity() {
			return localReadSecurity;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ReadSecurity
		 */
		public void setReadSecurity(int param) {

			this.localReadSecurity = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localInternalNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"InternalName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"InternalName");
							}

						} else {
							xmlWriter.writeStartElement("InternalName");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","InternalName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localInternalName));
						xmlWriter.writeEndElement();
					}
					if (localTitleTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Title",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Title");
							}

						} else {
							xmlWriter.writeStartElement("Title");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Title");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localTitle));
						xmlWriter.writeEndElement();
					}
					if (localDescriptionTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"Description", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"Description");
							}

						} else {
							xmlWriter.writeStartElement("Description");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Description");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localDescription));
						xmlWriter.writeEndElement();
					}
					if (localBaseTypeTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "BaseType",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"BaseType");
							}

						} else {
							xmlWriter.writeStartElement("BaseType");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","BaseType");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localBaseType));
						xmlWriter.writeEndElement();
					}
					if (localBaseTemplateTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"BaseTemplate", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"BaseTemplate");
							}

						} else {
							xmlWriter.writeStartElement("BaseTemplate");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","BaseTemplate");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localBaseTemplate));
						xmlWriter.writeEndElement();
					}
					if (localDefaultViewUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"DefaultViewUrl", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"DefaultViewUrl");
							}

						} else {
							xmlWriter.writeStartElement("DefaultViewUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","DefaultViewUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localDefaultViewUrl));
						xmlWriter.writeEndElement();
					}
					if (localLastModifiedTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"LastModified", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"LastModified");
							}

						} else {
							xmlWriter.writeStartElement("LastModified");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localLastModified));
						xmlWriter.writeEndElement();
					}
					if (localPermIdTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "PermId",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "PermId");
							}

						} else {
							xmlWriter.writeStartElement("PermId");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","PermId");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localPermId));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"InheritedSecurity", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"InheritedSecurity");
						}

					} else {
						xmlWriter.writeStartElement("InheritedSecurity");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","InheritedSecurity");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localInheritedSecurity));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"AllowAnonymousAccess", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"AllowAnonymousAccess");
						}

					} else {
						xmlWriter.writeStartElement("AllowAnonymousAccess");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","AllowAnonymousAccess");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localAllowAnonymousAccess));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"AnonymousViewListItems", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"AnonymousViewListItems");
						}

					} else {
						xmlWriter.writeStartElement("AnonymousViewListItems");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","AnonymousViewListItems");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localAnonymousViewListItems));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "ReadSecurity",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"ReadSecurity");
						}

					} else {
						xmlWriter.writeStartElement("ReadSecurity");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","ReadSecurity");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localReadSecurity));
					xmlWriter.writeEndElement();

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localInternalNameTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"InternalName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localInternalName));
			}
			if (localTitleTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Title"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localTitle));
			}
			if (localDescriptionTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Description"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localDescription));
			}
			if (localBaseTypeTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"BaseType"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localBaseType));
			}
			if (localBaseTemplateTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"BaseTemplate"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localBaseTemplate));
			}
			if (localDefaultViewUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"DefaultViewUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localDefaultViewUrl));
			}
			if (localLastModifiedTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"LastModified"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localLastModified));
			}
			if (localPermIdTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"PermId"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localPermId));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"InheritedSecurity"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localInheritedSecurity));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"AllowAnonymousAccess"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localAllowAnonymousAccess));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"AnonymousViewListItems"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localAnonymousViewListItems));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"ReadSecurity"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localReadSecurity));

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sList parse(javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sList object = new _sList();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sList".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sList) ExtensionMapper.getTypeObject(
										nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"InternalName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setInternalName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Title").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setTitle(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Description").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setDescription(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"BaseType").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setBaseType(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"BaseTemplate").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setBaseTemplate(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"DefaultViewUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setDefaultViewUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"PermId").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setPermId(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"InheritedSecurity").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setInheritedSecurity(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"AllowAnonymousAccess").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAllowAnonymousAccess(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"AnonymousViewListItems").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAnonymousViewListItems(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"ReadSecurity").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setReadSecurity(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetWeb implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/", "GetWeb",
				"ns1");

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetWeb parse(javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetWeb object = new GetWeb();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetWeb".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetWeb) ExtensionMapper.getTypeObject(
										nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ArrayOf_sListWithTime implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOf_sListWithTime Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for _sListWithTime This was an Array!
		 */

		protected _sListWithTime[] local_sListWithTime;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean local_sListWithTimeTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sListWithTime[]
		 */
		public _sListWithTime[] get_sListWithTime() {
			return local_sListWithTime;
		}

		/**
		 * validate the array for _sListWithTime
		 */
		protected void validate_sListWithTime(_sListWithTime[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            _sListWithTime
		 */
		public void set_sListWithTime(_sListWithTime[] param) {

			validate_sListWithTime(param);

			if (param != null) {
				// update the setting tracker
				local_sListWithTimeTracker = true;
			}

			this.local_sListWithTime = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            _sListWithTime
		 */
		public void add_sListWithTime(_sListWithTime param) {
			if (local_sListWithTime == null) {
				local_sListWithTime = new _sListWithTime[] {};
			}

			// update the setting tracker
			local_sListWithTimeTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(local_sListWithTime);
			list.add(param);
			this.local_sListWithTime = (_sListWithTime[]) list
					.toArray(new _sListWithTime[list.size()]);

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (local_sListWithTimeTracker) {
						if (local_sListWithTime == null) {
							throw new RuntimeException(
									"_sListWithTime cannot be null!!");
						}

						for (int i = 0; i < local_sListWithTime.length; i++) {
							local_sListWithTime[i]
									.getOMElement(
											new javax.xml.namespace.QName(
													"http://schemas.microsoft.com/sharepoint/soap/",
													"_sListWithTime"), factory)
									.serialize(xmlWriter);

						}

					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (local_sListWithTimeTracker) {
				if (local_sListWithTime == null) {
					throw new RuntimeException(
							"_sListWithTime cannot be null!!");
				}

				for (int i = 0; i < local_sListWithTime.length; i++) {
					elementList.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"_sListWithTime"));
					elementList.add(local_sListWithTime[i]);
				}

			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static ArrayOf_sListWithTime parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOf_sListWithTime object = new ArrayOf_sListWithTime();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"ArrayOf_sListWithTime".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOf_sListWithTime) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					java.util.ArrayList list1 = new java.util.ArrayList();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"_sListWithTime").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(_sListWithTime.Factory.parse(reader));
						// loop until we find a start element that is not part
						// of this array
						boolean loopDone1 = false;
						while (!loopDone1) {
							// We should be at the end element, but make sure
							while (!reader.isEndElement())
								reader.next();
							// Step out of this element
							reader.next();
							// Step to next element event.
							while (!reader.isStartElement()
									&& !reader.isEndElement())
								reader.next();
							if (reader.isEndElement()) {
								// two continuous end elements means we are
								// exiting the xml structure
								loopDone1 = true;
							} else {
								if (new javax.xml.namespace.QName(
										"http://schemas.microsoft.com/sharepoint/soap/",
										"_sListWithTime").equals(reader
										.getName())) {
									list1.add(_sListWithTime.Factory
											.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.set_sListWithTime((_sListWithTime[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(_sListWithTime.class,
												list1));

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetWebResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetWebResponse", "ns1");

		/**
		 * field for GetWebResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localGetWebResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getGetWebResult() {
			return localGetWebResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetWebResult
		 */
		public void setGetWebResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localGetWebResult = param;

		}

		/**
		 * field for SWebMetadata
		 */

		protected _sWebMetadata localSWebMetadata;

		/**
		 * Auto generated getter method
		 * 
		 * @return _sWebMetadata
		 */
		public _sWebMetadata getSWebMetadata() {
			return localSWebMetadata;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            SWebMetadata
		 */
		public void setSWebMetadata(_sWebMetadata param) {

			this.localSWebMetadata = param;

		}

		/**
		 * field for VWebs
		 */

		protected ArrayOf_sWebWithTime localVWebs;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVWebsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sWebWithTime
		 */
		public ArrayOf_sWebWithTime getVWebs() {
			return localVWebs;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VWebs
		 */
		public void setVWebs(ArrayOf_sWebWithTime param) {

			// update the setting tracker
			localVWebsTracker = true;

			this.localVWebs = param;

		}

		/**
		 * field for VLists
		 */

		protected ArrayOf_sListWithTime localVLists;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVListsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sListWithTime
		 */
		public ArrayOf_sListWithTime getVLists() {
			return localVLists;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VLists
		 */
		public void setVLists(ArrayOf_sListWithTime param) {

			// update the setting tracker
			localVListsTracker = true;

			this.localVLists = param;

		}

		/**
		 * field for VFPUrls
		 */

		protected ArrayOf_sFPUrl localVFPUrls;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVFPUrlsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sFPUrl
		 */
		public ArrayOf_sFPUrl getVFPUrls() {
			return localVFPUrls;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VFPUrls
		 */
		public void setVFPUrls(ArrayOf_sFPUrl param) {

			// update the setting tracker
			localVFPUrlsTracker = true;

			this.localVFPUrls = param;

		}

		/**
		 * field for StrRoles
		 */

		protected java.lang.String localStrRoles;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrRolesTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrRoles() {
			return localStrRoles;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrRoles
		 */
		public void setStrRoles(java.lang.String param) {

			// update the setting tracker
			localStrRolesTracker = true;

			this.localStrRoles = param;

		}

		/**
		 * field for VRolesUsers
		 */

		protected ArrayOfString localVRolesUsers;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVRolesUsersTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfString
		 */
		public ArrayOfString getVRolesUsers() {
			return localVRolesUsers;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VRolesUsers
		 */
		public void setVRolesUsers(ArrayOfString param) {

			// update the setting tracker
			localVRolesUsersTracker = true;

			this.localVRolesUsers = param;

		}

		/**
		 * field for VRolesGroups
		 */

		protected ArrayOfString localVRolesGroups;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVRolesGroupsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfString
		 */
		public ArrayOfString getVRolesGroups() {
			return localVRolesGroups;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VRolesGroups
		 */
		public void setVRolesGroups(ArrayOfString param) {

			// update the setting tracker
			localVRolesGroupsTracker = true;

			this.localVRolesGroups = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "GetWebResult",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"GetWebResult");
						}

					} else {
						xmlWriter.writeStartElement("GetWebResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","GetWebResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGetWebResult));
					xmlWriter.writeEndElement();

					if (localSWebMetadata == null) {
						throw new RuntimeException(
								"sWebMetadata cannot be null!!");
					}
					localSWebMetadata
							.getOMElement(
									new javax.xml.namespace.QName(
											"http://schemas.microsoft.com/sharepoint/soap/",
											"sWebMetadata"), factory)
							.serialize(xmlWriter);
					if (localVWebsTracker) {
						if (localVWebs == null) {
							throw new RuntimeException("vWebs cannot be null!!");
						}
						localVWebs
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vWebs"), factory).serialize(
										xmlWriter);
					}
					if (localVListsTracker) {
						if (localVLists == null) {
							throw new RuntimeException(
									"vLists cannot be null!!");
						}
						localVLists
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vLists"), factory).serialize(
										xmlWriter);
					}
					if (localVFPUrlsTracker) {
						if (localVFPUrls == null) {
							throw new RuntimeException(
									"vFPUrls cannot be null!!");
						}
						localVFPUrls
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vFPUrls"), factory).serialize(
										xmlWriter);
					}
					if (localStrRolesTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strRoles",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strRoles");
							}

						} else {
							xmlWriter.writeStartElement("strRoles");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strRoles");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrRoles));
						xmlWriter.writeEndElement();
					}
					if (localVRolesUsersTracker) {
						if (localVRolesUsers == null) {
							throw new RuntimeException(
									"vRolesUsers cannot be null!!");
						}
						localVRolesUsers
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vRolesUsers"), factory)
								.serialize(xmlWriter);
					}
					if (localVRolesGroupsTracker) {
						if (localVRolesGroups == null) {
							throw new RuntimeException(
									"vRolesGroups cannot be null!!");
						}
						localVRolesGroups
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vRolesGroups"), factory)
								.serialize(xmlWriter);
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"GetWebResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGetWebResult));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"sWebMetadata"));

			if (localSWebMetadata == null) {
				throw new RuntimeException("sWebMetadata cannot be null!!");
			}
			elementList.add(localSWebMetadata);
			if (localVWebsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vWebs"));

				if (localVWebs == null) {
					throw new RuntimeException("vWebs cannot be null!!");
				}
				elementList.add(localVWebs);
			}
			if (localVListsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vLists"));

				if (localVLists == null) {
					throw new RuntimeException("vLists cannot be null!!");
				}
				elementList.add(localVLists);
			}
			if (localVFPUrlsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vFPUrls"));

				if (localVFPUrls == null) {
					throw new RuntimeException("vFPUrls cannot be null!!");
				}
				elementList.add(localVFPUrls);
			}
			if (localStrRolesTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strRoles"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrRoles));
			}
			if (localVRolesUsersTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vRolesUsers"));

				if (localVRolesUsers == null) {
					throw new RuntimeException("vRolesUsers cannot be null!!");
				}
				elementList.add(localVRolesUsers);
			}
			if (localVRolesGroupsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vRolesGroups"));

				if (localVRolesGroups == null) {
					throw new RuntimeException("vRolesGroups cannot be null!!");
				}
				elementList.add(localVRolesGroups);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetWebResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetWebResponse object = new GetWebResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetWebResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetWebResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"GetWebResult").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setGetWebResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"sWebMetadata").equals(reader.getName())) {

						object.setSWebMetadata(_sWebMetadata.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vWebs").equals(reader.getName())) {

						object.setVWebs(ArrayOf_sWebWithTime.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vLists").equals(reader.getName())) {

						object.setVLists(ArrayOf_sListWithTime.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vFPUrls").equals(reader.getName())) {

						object.setVFPUrls(ArrayOf_sFPUrl.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strRoles").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrRoles(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vRolesUsers").equals(reader.getName())) {

						object.setVRolesUsers(ArrayOfString.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vRolesGroups").equals(reader.getName())) {

						object.setVRolesGroups(ArrayOfString.Factory
								.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class _sWebMetadata implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * _sWebMetadata Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for WebID
		 */

		protected java.lang.String localWebID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localWebIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getWebID() {
			return localWebID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            WebID
		 */
		public void setWebID(java.lang.String param) {

			// update the setting tracker
			localWebIDTracker = true;

			this.localWebID = param;

		}

		/**
		 * field for Title
		 */

		protected java.lang.String localTitle;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localTitleTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getTitle() {
			return localTitle;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Title
		 */
		public void setTitle(java.lang.String param) {

			// update the setting tracker
			localTitleTracker = true;

			this.localTitle = param;

		}

		/**
		 * field for Description
		 */

		protected java.lang.String localDescription;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localDescriptionTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getDescription() {
			return localDescription;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Description
		 */
		public void setDescription(java.lang.String param) {

			// update the setting tracker
			localDescriptionTracker = true;

			this.localDescription = param;

		}

		/**
		 * field for Author
		 */

		protected java.lang.String localAuthor;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localAuthorTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getAuthor() {
			return localAuthor;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Author
		 */
		public void setAuthor(java.lang.String param) {

			// update the setting tracker
			localAuthorTracker = true;

			this.localAuthor = param;

		}

		/**
		 * field for Language
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localLanguage;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getLanguage() {
			return localLanguage;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Language
		 */
		public void setLanguage(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localLanguage = param;

		}

		/**
		 * field for LastModified
		 */

		protected java.util.Calendar localLastModified;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModified() {
			return localLastModified;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModified
		 */
		public void setLastModified(java.util.Calendar param) {

			this.localLastModified = param;

		}

		/**
		 * field for LastModifiedForceRecrawl
		 */

		protected java.util.Calendar localLastModifiedForceRecrawl;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.util.Calendar
		 */
		public java.util.Calendar getLastModifiedForceRecrawl() {
			return localLastModifiedForceRecrawl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            LastModifiedForceRecrawl
		 */
		public void setLastModifiedForceRecrawl(java.util.Calendar param) {

			this.localLastModifiedForceRecrawl = param;

		}

		/**
		 * field for NoIndex
		 */

		protected java.lang.String localNoIndex;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localNoIndexTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getNoIndex() {
			return localNoIndex;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            NoIndex
		 */
		public void setNoIndex(java.lang.String param) {

			// update the setting tracker
			localNoIndexTracker = true;

			this.localNoIndex = param;

		}

		/**
		 * field for ValidSecurityInfo
		 */

		protected boolean localValidSecurityInfo;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getValidSecurityInfo() {
			return localValidSecurityInfo;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ValidSecurityInfo
		 */
		public void setValidSecurityInfo(boolean param) {

			this.localValidSecurityInfo = param;

		}

		/**
		 * field for InheritedSecurity
		 */

		protected boolean localInheritedSecurity;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getInheritedSecurity() {
			return localInheritedSecurity;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            InheritedSecurity
		 */
		public void setInheritedSecurity(boolean param) {

			this.localInheritedSecurity = param;

		}

		/**
		 * field for AllowAnonymousAccess
		 */

		protected boolean localAllowAnonymousAccess;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getAllowAnonymousAccess() {
			return localAllowAnonymousAccess;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AllowAnonymousAccess
		 */
		public void setAllowAnonymousAccess(boolean param) {

			this.localAllowAnonymousAccess = param;

		}

		/**
		 * field for AnonymousViewListItems
		 */

		protected boolean localAnonymousViewListItems;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getAnonymousViewListItems() {
			return localAnonymousViewListItems;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AnonymousViewListItems
		 */
		public void setAnonymousViewListItems(boolean param) {

			this.localAnonymousViewListItems = param;

		}

		/**
		 * field for Permissions
		 */

		protected java.lang.String localPermissions;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPermissionsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPermissions() {
			return localPermissions;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Permissions
		 */
		public void setPermissions(java.lang.String param) {

			// update the setting tracker
			localPermissionsTracker = true;

			this.localPermissions = param;

		}

		/**
		 * field for ExternalSecurity
		 */

		protected boolean localExternalSecurity;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getExternalSecurity() {
			return localExternalSecurity;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ExternalSecurity
		 */
		public void setExternalSecurity(boolean param) {

			this.localExternalSecurity = param;

		}

		/**
		 * field for CategoryId
		 */

		protected java.lang.String localCategoryId;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localCategoryIdTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getCategoryId() {
			return localCategoryId;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            CategoryId
		 */
		public void setCategoryId(java.lang.String param) {

			// update the setting tracker
			localCategoryIdTracker = true;

			this.localCategoryId = param;

		}

		/**
		 * field for CategoryName
		 */

		protected java.lang.String localCategoryName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localCategoryNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getCategoryName() {
			return localCategoryName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            CategoryName
		 */
		public void setCategoryName(java.lang.String param) {

			// update the setting tracker
			localCategoryNameTracker = true;

			this.localCategoryName = param;

		}

		/**
		 * field for CategoryIdPath
		 */

		protected java.lang.String localCategoryIdPath;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localCategoryIdPathTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getCategoryIdPath() {
			return localCategoryIdPath;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            CategoryIdPath
		 */
		public void setCategoryIdPath(java.lang.String param) {

			// update the setting tracker
			localCategoryIdPathTracker = true;

			this.localCategoryIdPath = param;

		}

		/**
		 * field for IsBucketWeb
		 */

		protected boolean localIsBucketWeb;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getIsBucketWeb() {
			return localIsBucketWeb;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            IsBucketWeb
		 */
		public void setIsBucketWeb(boolean param) {

			this.localIsBucketWeb = param;

		}

		/**
		 * field for UsedInAutocat
		 */

		protected boolean localUsedInAutocat;

		/**
		 * Auto generated getter method
		 * 
		 * @return boolean
		 */
		public boolean getUsedInAutocat() {
			return localUsedInAutocat;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            UsedInAutocat
		 */
		public void setUsedInAutocat(boolean param) {

			this.localUsedInAutocat = param;

		}

		/**
		 * field for CategoryBucketID
		 */

		protected java.lang.String localCategoryBucketID;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localCategoryBucketIDTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getCategoryBucketID() {
			return localCategoryBucketID;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            CategoryBucketID
		 */
		public void setCategoryBucketID(java.lang.String param) {

			// update the setting tracker
			localCategoryBucketIDTracker = true;

			this.localCategoryBucketID = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localWebIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "WebID",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "WebID");
							}

						} else {
							xmlWriter.writeStartElement("WebID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","WebID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localWebID));
						xmlWriter.writeEndElement();
					}
					if (localTitleTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Title",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Title");
							}

						} else {
							xmlWriter.writeStartElement("Title");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Title");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localTitle));
						xmlWriter.writeEndElement();
					}
					if (localDescriptionTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"Description", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"Description");
							}

						} else {
							xmlWriter.writeStartElement("Description");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Description");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localDescription));
						xmlWriter.writeEndElement();
					}
					if (localAuthorTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Author",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "Author");
							}

						} else {
							xmlWriter.writeStartElement("Author");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Author");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localAuthor));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "Language",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "Language");
						}

					} else {
						xmlWriter.writeStartElement("Language");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Language");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLanguage));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "LastModified",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModified");
						}

					} else {
						xmlWriter.writeStartElement("LastModified");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModified");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModified));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"LastModifiedForceRecrawl", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"LastModifiedForceRecrawl");
						}

					} else {
						xmlWriter.writeStartElement("LastModifiedForceRecrawl");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","LastModifiedForceRecrawl");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localLastModifiedForceRecrawl));
					xmlWriter.writeEndElement();
					if (localNoIndexTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "NoIndex",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"NoIndex");
							}

						} else {
							xmlWriter.writeStartElement("NoIndex");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","NoIndex");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localNoIndex));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"ValidSecurityInfo", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"ValidSecurityInfo");
						}

					} else {
						xmlWriter.writeStartElement("ValidSecurityInfo");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","ValidSecurityInfo");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localValidSecurityInfo));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"InheritedSecurity", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"InheritedSecurity");
						}

					} else {
						xmlWriter.writeStartElement("InheritedSecurity");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","InheritedSecurity");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localInheritedSecurity));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"AllowAnonymousAccess", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"AllowAnonymousAccess");
						}

					} else {
						xmlWriter.writeStartElement("AllowAnonymousAccess");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","AllowAnonymousAccess");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localAllowAnonymousAccess));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"AnonymousViewListItems", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"AnonymousViewListItems");
						}

					} else {
						xmlWriter.writeStartElement("AnonymousViewListItems");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","AnonymousViewListItems");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localAnonymousViewListItems));
					xmlWriter.writeEndElement();
					if (localPermissionsTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"Permissions", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"Permissions");
							}

						} else {
							xmlWriter.writeStartElement("Permissions");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","Permissions");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localPermissions));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"ExternalSecurity", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"ExternalSecurity");
						}

					} else {
						xmlWriter.writeStartElement("ExternalSecurity");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","ExternalSecurity");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localExternalSecurity));
					xmlWriter.writeEndElement();
					if (localCategoryIdTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"CategoryId", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"CategoryId");
							}

						} else {
							xmlWriter.writeStartElement("CategoryId");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","CategoryId");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localCategoryId));
						xmlWriter.writeEndElement();
					}
					if (localCategoryNameTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"CategoryName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"CategoryName");
							}

						} else {
							xmlWriter.writeStartElement("CategoryName");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","CategoryName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localCategoryName));
						xmlWriter.writeEndElement();
					}
					if (localCategoryIdPathTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"CategoryIdPath", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"CategoryIdPath");
							}

						} else {
							xmlWriter.writeStartElement("CategoryIdPath");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","CategoryIdPath");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localCategoryIdPath));
						xmlWriter.writeEndElement();
					}
					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "IsBucketWeb",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"IsBucketWeb");
						}

					} else {
						xmlWriter.writeStartElement("IsBucketWeb");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","IsBucketWeb");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localIsBucketWeb));
					xmlWriter.writeEndElement();

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"UsedInAutocat", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"UsedInAutocat");
						}

					} else {
						xmlWriter.writeStartElement("UsedInAutocat");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","UsedInAutocat");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localUsedInAutocat));
					xmlWriter.writeEndElement();
					if (localCategoryBucketIDTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"CategoryBucketID", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"CategoryBucketID");
							}

						} else {
							xmlWriter.writeStartElement("CategoryBucketID");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","CategoryBucketID");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localCategoryBucketID));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					parentQName, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localWebIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"WebID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localWebID));
			}
			if (localTitleTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Title"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localTitle));
			}
			if (localDescriptionTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Description"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localDescription));
			}
			if (localAuthorTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Author"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localAuthor));
			}
			elementList
					.add(new javax.xml.namespace.QName(
							"http://schemas.microsoft.com/sharepoint/soap/",
							"Language"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLanguage));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModified"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModified));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"LastModifiedForceRecrawl"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localLastModifiedForceRecrawl));
			if (localNoIndexTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"NoIndex"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localNoIndex));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"ValidSecurityInfo"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localValidSecurityInfo));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"InheritedSecurity"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localInheritedSecurity));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"AllowAnonymousAccess"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localAllowAnonymousAccess));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"AnonymousViewListItems"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localAnonymousViewListItems));
			if (localPermissionsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"Permissions"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localPermissions));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"ExternalSecurity"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localExternalSecurity));
			if (localCategoryIdTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"CategoryId"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localCategoryId));
			}
			if (localCategoryNameTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"CategoryName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localCategoryName));
			}
			if (localCategoryIdPathTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"CategoryIdPath"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localCategoryIdPath));
			}
			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"IsBucketWeb"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localIsBucketWeb));

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"UsedInAutocat"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localUsedInAutocat));
			if (localCategoryBucketIDTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"CategoryBucketID"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localCategoryBucketID));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static _sWebMetadata parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				_sWebMetadata object = new _sWebMetadata();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"_sWebMetadata".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (_sWebMetadata) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"WebID").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setWebID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Title").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setTitle(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Description").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setDescription(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Author").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAuthor(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Language").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLanguage(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModified").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModified(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"LastModifiedForceRecrawl").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setLastModifiedForceRecrawl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToDateTime(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"NoIndex").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setNoIndex(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"ValidSecurityInfo").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setValidSecurityInfo(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"InheritedSecurity").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setInheritedSecurity(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"AllowAnonymousAccess").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAllowAnonymousAccess(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"AnonymousViewListItems").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setAnonymousViewListItems(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"Permissions").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setPermissions(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"ExternalSecurity")
									.equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setExternalSecurity(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"CategoryId").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setCategoryId(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"CategoryName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setCategoryName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"CategoryIdPath").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setCategoryIdPath(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"IsBucketWeb").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setIsBucketWeb(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"UsedInAutocat").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setUsedInAutocat(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToBoolean(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"CategoryBucketID")
									.equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setCategoryBucketID(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class ExtensionMapper {

		public static java.lang.Object getTypeObject(
				java.lang.String namespaceURI, java.lang.String typeName,
				javax.xml.stream.XMLStreamReader reader)
				throws java.lang.Exception {

			if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "ArrayOf_sProperty".equals(typeName)) {

				return ArrayOf_sProperty.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sListWithTime".equals(typeName)) {

				return _sListWithTime.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "ArrayOfString".equals(typeName)) {

				return ArrayOfString.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "ArrayOf_sList".equals(typeName)) {

				return ArrayOf_sList.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sList".equals(typeName)) {

				return _sList.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sProperty".equals(typeName)) {

				return _sProperty.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "ArrayOf_sListWithTime".equals(typeName)) {

				return ArrayOf_sListWithTime.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sSiteMetadata".equals(typeName)) {

				return _sSiteMetadata.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sWebMetadata".equals(typeName)) {

				return _sWebMetadata.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sListMetadata".equals(typeName)) {

				return _sListMetadata.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "ArrayOf_sWebWithTime".equals(typeName)) {

				return ArrayOf_sWebWithTime.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sWebWithTime".equals(typeName)) {

				return _sWebWithTime.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "ArrayOf_sFPUrl".equals(typeName)) {

				return ArrayOf_sFPUrl.Factory.parse(reader);

			}

			else if ("http://schemas.microsoft.com/sharepoint/soap/"
					.equals(namespaceURI)
					&& "_sFPUrl".equals(typeName)) {

				return _sFPUrl.Factory.parse(reader);

			}

			throw new java.lang.RuntimeException("Unsupported type "
					+ namespaceURI + " " + typeName);
		}

	}

	public static class EnumerateFolderResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"EnumerateFolderResponse", "ns1");

		/**
		 * field for EnumerateFolderResult
		 */

		protected org.apache.axis2.databinding.types.UnsignedInt localEnumerateFolderResult;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axis2.databinding.types.UnsignedInt
		 */
		public org.apache.axis2.databinding.types.UnsignedInt getEnumerateFolderResult() {
			return localEnumerateFolderResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            EnumerateFolderResult
		 */
		public void setEnumerateFolderResult(
				org.apache.axis2.databinding.types.UnsignedInt param) {

			this.localEnumerateFolderResult = param;

		}

		/**
		 * field for VUrls
		 */

		protected ArrayOf_sFPUrl localVUrls;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localVUrlsTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOf_sFPUrl
		 */
		public ArrayOf_sFPUrl getVUrls() {
			return localVUrls;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            VUrls
		 */
		public void setVUrls(ArrayOf_sFPUrl param) {

			// update the setting tracker
			localVUrlsTracker = true;

			this.localVUrls = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					namespace = "http://schemas.microsoft.com/sharepoint/soap/";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix,
									"EnumerateFolderResult", namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace,
									"EnumerateFolderResult");
						}

					} else {
						xmlWriter.writeStartElement("EnumerateFolderResult");
					}

					// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","EnumerateFolderResult");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localEnumerateFolderResult));
					xmlWriter.writeEndElement();
					if (localVUrlsTracker) {
						if (localVUrls == null) {
							throw new RuntimeException("vUrls cannot be null!!");
						}
						localVUrls
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://schemas.microsoft.com/sharepoint/soap/",
												"vUrls"), factory).serialize(
										xmlWriter);
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			elementList.add(new javax.xml.namespace.QName(
					"http://schemas.microsoft.com/sharepoint/soap/",
					"EnumerateFolderResult"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localEnumerateFolderResult));
			if (localVUrlsTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"vUrls"));

				if (localVUrls == null) {
					throw new RuntimeException("vUrls cannot be null!!");
				}
				elementList.add(localVUrls);
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static EnumerateFolderResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				EnumerateFolderResponse object = new EnumerateFolderResponse();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"EnumerateFolderResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (EnumerateFolderResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"EnumerateFolderResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setEnumerateFolderResult(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToUnsignedInt(content));

						reader.next();

					} // End of if for expected property start element

					else {
						// A start element we are not expecting indicates an
						// invalid parameter was passed
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());
					}

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"vUrls").equals(reader.getName())) {

						object.setVUrls(ArrayOf_sFPUrl.Factory.parse(reader));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetListCollection implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListCollection", "ns1");

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetListCollection parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListCollection object = new GetListCollection();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetListCollection".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListCollection) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetSite implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/", "GetSite",
				"ns1");

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetSite parse(javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetSite object = new GetSite();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetSite".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetSite) ExtensionMapper.getTypeObject(
										nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class EnumerateFolder implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"EnumerateFolder", "ns1");

		/**
		 * field for StrFolderUrl
		 */

		protected java.lang.String localStrFolderUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrFolderUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrFolderUrl() {
			return localStrFolderUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrFolderUrl
		 */
		public void setStrFolderUrl(java.lang.String param) {

			// update the setting tracker
			localStrFolderUrlTracker = true;

			this.localStrFolderUrl = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStrFolderUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"strFolderUrl", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"strFolderUrl");
							}

						} else {
							xmlWriter.writeStartElement("strFolderUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strFolderUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrFolderUrl));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStrFolderUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strFolderUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrFolderUrl));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static EnumerateFolder parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				EnumerateFolder object = new EnumerateFolder();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"EnumerateFolder".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (EnumerateFolder) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strFolderUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrFolderUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetSiteAndWeb implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetSiteAndWeb", "ns1");

		/**
		 * field for StrUrl
		 */

		protected java.lang.String localStrUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrUrl() {
			return localStrUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrUrl
		 */
		public void setStrUrl(java.lang.String param) {

			// update the setting tracker
			localStrUrlTracker = true;

			this.localStrUrl = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStrUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strUrl",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "strUrl");
							}

						} else {
							xmlWriter.writeStartElement("strUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrUrl));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStrUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrUrl));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetSiteAndWeb parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetSiteAndWeb object = new GetSiteAndWeb();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetSiteAndWeb".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetSiteAndWeb) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrUrl(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	public static class GetURLSegments implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetURLSegments", "ns1");

		/**
		 * field for StrURL
		 */

		protected java.lang.String localStrURL;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localStrURLTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getStrURL() {
			return localStrURL;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            StrURL
		 */
		public void setStrURL(java.lang.String param) {

			// update the setting tracker
			localStrURLTracker = true;

			this.localStrURL = param;

		}

		/**
		 * 
		 * @param parentQName
		 * @param factory
		 * @return
		 */
		public org.apache.axiom.om.OMElement getOMElement(
				final javax.xml.namespace.QName parentQName,
				final org.apache.axiom.om.OMFactory factory) {

			org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(
					this, parentQName) {

				public void serialize(javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					java.lang.String prefix = parentQName.getPrefix();
					java.lang.String namespace = parentQName.getNamespaceURI();

					if (namespace != null) {
						java.lang.String writerPrefix = xmlWriter
								.getPrefix(namespace);
						if (writerPrefix != null) {
							xmlWriter.writeStartElement(namespace, parentQName
									.getLocalPart());
						} else {
							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();
							}

							xmlWriter.writeStartElement(prefix, parentQName
									.getLocalPart(), namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);
						}
					} else {
						xmlWriter.writeStartElement(parentQName.getLocalPart());
					}

					if (localStrURLTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "strURL",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "strURL");
							}

						} else {
							xmlWriter.writeStartElement("strURL");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","strURL");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localStrURL));
						xmlWriter.writeEndElement();
					}

					xmlWriter.writeEndElement();

				}

				/**
				 * Util method to write an attribute with the ns prefix
				 */
				private void writeAttribute(java.lang.String prefix,
						java.lang.String namespace, java.lang.String attName,
						java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {
					if (xmlWriter.getPrefix(namespace) == null) {
						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);

					}

					xmlWriter.writeAttribute(namespace, attName, attValue);

				}

				/**
				 * Util method to write an attribute without the ns prefix
				 */
				private void writeAttribute(java.lang.String namespace,
						java.lang.String attName, java.lang.String attValue,
						javax.xml.stream.XMLStreamWriter xmlWriter)
						throws javax.xml.stream.XMLStreamException {

					registerPrefix(xmlWriter, namespace);

					xmlWriter.writeAttribute(namespace, attName, attValue);
				}

				/**
				 * Register a namespace prefix
				 */
				private java.lang.String registerPrefix(
						javax.xml.stream.XMLStreamWriter xmlWriter,
						java.lang.String namespace)
						throws javax.xml.stream.XMLStreamException {
					java.lang.String prefix = xmlWriter.getPrefix(namespace);

					if (prefix == null) {
						prefix = createPrefix();

						while (xmlWriter.getNamespaceContext().getNamespaceURI(
								prefix) != null) {
							prefix = createPrefix();
						}

						xmlWriter.writeNamespace(prefix, namespace);
						xmlWriter.setPrefix(prefix, namespace);
					}

					return prefix;
				}

				/**
				 * Create a prefix
				 */
				private java.lang.String createPrefix() {
					return "ns" + (int) Math.random();
				}
			};

			// ignore the QName passed in - we send only OUR QName!
			return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
					MY_QNAME, factory, dataSource);

		}

		/**
		 * databinding method to get an XML representation of this object
		 * 
		 */
		public javax.xml.stream.XMLStreamReader getPullParser(
				javax.xml.namespace.QName qName) {

			java.util.ArrayList elementList = new java.util.ArrayList();
			java.util.ArrayList attribList = new java.util.ArrayList();

			if (localStrURLTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"strURL"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localStrURL));
			}

			return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(
					qName, elementList.toArray(), attribList.toArray());

		}

		/**
		 * Factory class that keeps the parse method
		 */
		public static class Factory {

			/**
			 * static method to create the object Precondition: If this object
			 * is an element, the current or next start element starts this
			 * object and any intervening reader events are ignorable If this
			 * object is not an element, it is a complex type and the reader is
			 * at the event just after the outer start element Postcondition: If
			 * this object is an element, the reader is positioned at its end
			 * element If this object is a complex type, the reader is
			 * positioned at the end element of its outer element
			 */
			public static GetURLSegments parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetURLSegments object = new GetURLSegments();
				int event;
				try {

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader
							.getAttributeValue(
									"http://www.w3.org/2001/XMLSchema-instance",
									"type") != null) {
						java.lang.String fullTypeName = reader
								.getAttributeValue(
										"http://www.w3.org/2001/XMLSchema-instance",
										"type");
						if (fullTypeName != null) {
							java.lang.String nsPrefix = fullTypeName.substring(
									0, fullTypeName.indexOf(":"));
							nsPrefix = nsPrefix == null ? "" : nsPrefix;

							java.lang.String type = fullTypeName
									.substring(fullTypeName.indexOf(":") + 1);
							if (!"GetURLSegments".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetURLSegments) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

					// Note all attributes that were handled. Used to differ
					// normal attributes
					// from anyAttributes.
					java.util.Vector handledAttributes = new java.util.Vector();

					boolean isReaderMTOMAware = false;

					try {
						isReaderMTOMAware = java.lang.Boolean.TRUE
								.equals(reader
										.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
					} catch (java.lang.IllegalArgumentException e) {
						isReaderMTOMAware = false;
					}

					reader.next();

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"strURL").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setStrURL(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();
					if (reader.isStartElement())
						// A start element we are not expecting indicates a
						// trailing invalid property
						throw new java.lang.RuntimeException(
								"Unexpected subelement "
										+ reader.getLocalName());

				} catch (javax.xml.stream.XMLStreamException e) {
					throw new java.lang.Exception(e);
				}

				return object;
			}

		}// end of factory class

	}

	private org.apache.axiom.om.OMElement toOM(SiteDataStub.GetWeb param,
			boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetWeb.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetWebResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetWebResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetAttachments param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetAttachments.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetAttachmentsResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetAttachmentsResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.EnumerateFolder param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.EnumerateFolder.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.EnumerateFolderResponse param, boolean optimizeContent) {

		return param.getOMElement(
				SiteDataStub.EnumerateFolderResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetListCollection param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetListCollection.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetListCollectionResponse param,
			boolean optimizeContent) {

		return param.getOMElement(
				SiteDataStub.GetListCollectionResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(SiteDataStub.GetSite param,
			boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetSite.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetSiteResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetSiteResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetURLSegments param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetURLSegments.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetURLSegmentsResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetURLSegmentsResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(SiteDataStub.GetListItems param,
			boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetListItems.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetListItemsResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetListItemsResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetSiteAndWeb param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetSiteAndWeb.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetSiteAndWebResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetSiteAndWebResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(SiteDataStub.GetList param,
			boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetList.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			SiteDataStub.GetListResponse param, boolean optimizeContent) {

		return param.getOMElement(SiteDataStub.GetListResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetWeb param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.GetWeb.MY_QNAME, factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetAttachments param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.GetAttachments.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.EnumerateFolder param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.EnumerateFolder.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetListCollection param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.GetListCollection.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetSite param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.GetSite.MY_QNAME, factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetURLSegments param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.GetURLSegments.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetListItems param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody()
				.addChild(
						param.getOMElement(SiteDataStub.GetListItems.MY_QNAME,
								factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetSiteAndWeb param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param
						.getOMElement(SiteDataStub.GetSiteAndWeb.MY_QNAME,
								factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			SiteDataStub.GetList param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(SiteDataStub.GetList.MY_QNAME, factory));

		return emptyEnvelope;
	}

	/**
	 * get the default envelope
	 */
	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory) {
		return factory.getDefaultEnvelope();
	}

	private java.lang.Object fromOM(org.apache.axiom.om.OMElement param,
			java.lang.Class type, java.util.Map extraNamespaces) {

		try {

			if (SiteDataStub.GetWeb.class.equals(type)) {

				return SiteDataStub.GetWeb.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetWebResponse.class.equals(type)) {

				return SiteDataStub.GetWebResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetAttachments.class.equals(type)) {

				return SiteDataStub.GetAttachments.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetAttachmentsResponse.class.equals(type)) {

				return SiteDataStub.GetAttachmentsResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.EnumerateFolder.class.equals(type)) {

				return SiteDataStub.EnumerateFolder.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.EnumerateFolderResponse.class.equals(type)) {

				return SiteDataStub.EnumerateFolderResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetListCollection.class.equals(type)) {

				return SiteDataStub.GetListCollection.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetListCollectionResponse.class.equals(type)) {

				return SiteDataStub.GetListCollectionResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetSite.class.equals(type)) {

				return SiteDataStub.GetSite.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetSiteResponse.class.equals(type)) {

				return SiteDataStub.GetSiteResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetURLSegments.class.equals(type)) {

				return SiteDataStub.GetURLSegments.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetURLSegmentsResponse.class.equals(type)) {

				return SiteDataStub.GetURLSegmentsResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetListItems.class.equals(type)) {

				return SiteDataStub.GetListItems.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetListItemsResponse.class.equals(type)) {

				return SiteDataStub.GetListItemsResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetSiteAndWeb.class.equals(type)) {

				return SiteDataStub.GetSiteAndWeb.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetSiteAndWebResponse.class.equals(type)) {

				return SiteDataStub.GetSiteAndWebResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetList.class.equals(type)) {

				return SiteDataStub.GetList.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (SiteDataStub.GetListResponse.class.equals(type)) {

				return SiteDataStub.GetListResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private void setOpNameArray() {
		opNameArray = null;
	}

}
