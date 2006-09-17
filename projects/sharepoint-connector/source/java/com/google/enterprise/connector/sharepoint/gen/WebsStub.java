/**
 * WebsStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT Aug 23, 2006 (04:21:30 GMT+00:00)
 */
package com.google.enterprise.connector.sharepoint.gen;

/*
 * WebsStub java implementation
 */

public class WebsStub extends org.apache.axis2.client.Stub {
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
		_service = new org.apache.axis2.description.AxisService("Webs"
				+ this.hashCode());

		// creating the operations
		org.apache.axis2.description.AxisOperation __operation;

		_operations = new org.apache.axis2.description.AxisOperation[5];

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("", "GetWeb"));

		_operations[0] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetWebCollection"));

		_operations[1] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"WebUrlFromPageUrl"));

		_operations[2] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetAllSubWebCollection"));

		_operations[3] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetListTemplates"));

		_operations[4] = __operation;
		_service.addOperation(__operation);

	}

	// populates the faults
	private void populateFaults() {

	}

	/**
	 * Constructor that takes in a configContext
	 */
	public WebsStub(
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
	public WebsStub() throws java.lang.Exception {

		this("http://172.25.234.129/_vti_bin/Webs.asmx");

	}

	/**
	 * Constructor taking the target endpoint
	 */
	public WebsStub(java.lang.String targetEndpoint) throws java.lang.Exception {
		this(null, targetEndpoint);
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see sharepoint.Webs#GetWeb
	 * @param param0
	 * 
	 */
	public WebsStub.GetWebResponse GetWeb(

	WebsStub.GetWeb param0) throws java.rmi.RemoteException

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
					.getFirstElement(), WebsStub.GetWebResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (WebsStub.GetWebResponse) object;

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
	 * @see Webs#GetWebCollection
	 * @param param2
	 * 
	 */
	public WebsStub.GetWebCollectionResponse GetWebCollection(

	WebsStub.GetWebCollection param2) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[1].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetWebCollection");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param2,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetWebCollection")));

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
					WebsStub.GetWebCollectionResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (WebsStub.GetWebCollectionResponse) object;

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
	 * @see Webs#WebUrlFromPageUrl
	 * @param param4
	 * 
	 */
	public WebsStub.WebUrlFromPageUrlResponse WebUrlFromPageUrl(

	WebsStub.WebUrlFromPageUrl param4) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[2].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/WebUrlFromPageUrl");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param4,
					optimizeContent(new javax.xml.namespace.QName("",
							"WebUrlFromPageUrl")));

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
					WebsStub.WebUrlFromPageUrlResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (WebsStub.WebUrlFromPageUrlResponse) object;

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
	 * @see Webs#GetAllSubWebCollection
	 * @param param6
	 * 
	 */
	public WebsStub.GetAllSubWebCollectionResponse GetAllSubWebCollection(

	WebsStub.GetAllSubWebCollection param6) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[3].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetAllSubWebCollection");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param6,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetAllSubWebCollection")));

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
					WebsStub.GetAllSubWebCollectionResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (WebsStub.GetAllSubWebCollectionResponse) object;

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
	 * @see Webs#GetListTemplates
	 * @param param8
	 * 
	 */
	public WebsStub.GetListTemplatesResponse GetListTemplates(

	WebsStub.GetListTemplates param8) throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[4].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://schemas.microsoft.com/sharepoint/soap/GetListTemplates");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param8,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetListTemplates")));

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
					WebsStub.GetListTemplatesResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (WebsStub.GetListTemplatesResponse) object;

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

	// http://172.25.234.129/_vti_bin/Webs.asmx
	public static class GetListTemplatesResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListTemplatesResponse", "ns1");

		/**
		 * field for GetListTemplatesResult
		 */

		protected org.apache.axiom.om.OMElement localGetListTemplatesResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetListTemplatesResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getGetListTemplatesResult() {
			return localGetListTemplatesResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetListTemplatesResult
		 */
		public void setGetListTemplatesResult(
				org.apache.axiom.om.OMElement param) {

			// update the setting tracker
			localGetListTemplatesResultTracker = true;

			this.localGetListTemplatesResult = param;

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

					if (localGetListTemplatesResultTracker) {
						if (localGetListTemplatesResult == null) {
							throw new RuntimeException(
									"GetListTemplatesResult cannot be null!!");
						}
						localGetListTemplatesResult.serialize(xmlWriter);
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

			if (localGetListTemplatesResultTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"GetListTemplatesResult"));

				if (localGetListTemplatesResult == null) {
					throw new RuntimeException(
							"GetListTemplatesResult cannot be null!!");
				}
				elementList.add(localGetListTemplatesResult);
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
			public static GetListTemplatesResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListTemplatesResponse object = new GetListTemplatesResponse();
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
							if (!"GetListTemplatesResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListTemplatesResponse) ExtensionMapper
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
									"GetListTemplatesResult").equals(reader
									.getName())) {

						boolean loopDone1 = false;
						javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"GetListTemplatesResult");

						while (!loopDone1) {
							if (reader.isStartElement()
									&& startQname1.equals(reader.getName())) {
								loopDone1 = true;
							} else {
								reader.next();
							}
						}

						// We need to wrap the reader so that it produces a fake
						// START_DOCUEMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setGetListTemplatesResult(builder1
								.getOMElement());

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

	public static class GetAllSubWebCollectionResult_type1 implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * GetAllSubWebCollectionResult_type1 Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for ExtraElement
		 */

		protected org.apache.axiom.om.OMElement localExtraElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getExtraElement() {
			return localExtraElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ExtraElement
		 */
		public void setExtraElement(org.apache.axiom.om.OMElement param) {

			this.localExtraElement = param;

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

					if (localExtraElement == null) {
						throw new RuntimeException(
								"extraElement cannot be null!!");
					}
					localExtraElement.serialize(xmlWriter);

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

			elementList.add(new javax.xml.namespace.QName("", "extraElement"));

			if (localExtraElement == null) {
				throw new RuntimeException("extraElement cannot be null!!");
			}
			elementList.add(localExtraElement);

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
			public static GetAllSubWebCollectionResult_type1 parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetAllSubWebCollectionResult_type1 object = new GetAllSubWebCollectionResult_type1();
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
							if (!"GetAllSubWebCollectionResult_type1"
									.equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetAllSubWebCollectionResult_type1) ExtensionMapper
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
							&& new javax.xml.namespace.QName("", "extraElement")
									.equals(reader.getName())) {

						// use the QName from the parser as the name for the
						// builder
						javax.xml.namespace.QName startQname1 = reader
								.getName();

						// We need to wrap the reader so that it produces a fake
						// START_DOCUMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setExtraElement(builder1.getOMElement());

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

	public static class WebUrlFromPageUrl implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"WebUrlFromPageUrl", "ns1");

		/**
		 * field for PageUrl
		 */

		protected java.lang.String localPageUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPageUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getPageUrl() {
			return localPageUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            PageUrl
		 */
		public void setPageUrl(java.lang.String param) {

			// update the setting tracker
			localPageUrlTracker = true;

			this.localPageUrl = param;

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

					if (localPageUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "pageUrl",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"pageUrl");
							}

						} else {
							xmlWriter.writeStartElement("pageUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","pageUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localPageUrl));
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

			if (localPageUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"pageUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localPageUrl));
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
			public static WebUrlFromPageUrl parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				WebUrlFromPageUrl object = new WebUrlFromPageUrl();
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
							if (!"WebUrlFromPageUrl".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (WebUrlFromPageUrl) ExtensionMapper
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
									"pageUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setPageUrl(org.apache.axis2.databinding.utils.ConverterUtil
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

	public static class GetWebCollectionResult_type3 implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * GetWebCollectionResult_type3 Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for ExtraElement
		 */

		protected org.apache.axiom.om.OMElement localExtraElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getExtraElement() {
			return localExtraElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ExtraElement
		 */
		public void setExtraElement(org.apache.axiom.om.OMElement param) {

			this.localExtraElement = param;

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

					if (localExtraElement == null) {
						throw new RuntimeException(
								"extraElement cannot be null!!");
					}
					localExtraElement.serialize(xmlWriter);

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

			elementList.add(new javax.xml.namespace.QName("", "extraElement"));

			if (localExtraElement == null) {
				throw new RuntimeException("extraElement cannot be null!!");
			}
			elementList.add(localExtraElement);

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
			public static GetWebCollectionResult_type3 parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetWebCollectionResult_type3 object = new GetWebCollectionResult_type3();
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
							if (!"GetWebCollectionResult_type3".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetWebCollectionResult_type3) ExtensionMapper
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
							&& new javax.xml.namespace.QName("", "extraElement")
									.equals(reader.getName())) {

						// use the QName from the parser as the name for the
						// builder
						javax.xml.namespace.QName startQname1 = reader
								.getName();

						// We need to wrap the reader so that it produces a fake
						// START_DOCUMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setExtraElement(builder1.getOMElement());

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

	public static class GetAllSubWebCollection implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetAllSubWebCollection", "ns1");

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
			public static GetAllSubWebCollection parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetAllSubWebCollection object = new GetAllSubWebCollection();
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
							if (!"GetAllSubWebCollection".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetAllSubWebCollection) ExtensionMapper
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

	public static class GetWeb implements org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/", "GetWeb",
				"ns1");

		/**
		 * field for WebUrl
		 */

		protected java.lang.String localWebUrl;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localWebUrlTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getWebUrl() {
			return localWebUrl;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            WebUrl
		 */
		public void setWebUrl(java.lang.String param) {

			// update the setting tracker
			localWebUrlTracker = true;

			this.localWebUrl = param;

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

					if (localWebUrlTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "webUrl",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter
										.writeStartElement(namespace, "webUrl");
							}

						} else {
							xmlWriter.writeStartElement("webUrl");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","webUrl");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localWebUrl));
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

			if (localWebUrlTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"webUrl"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localWebUrl));
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

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://schemas.microsoft.com/sharepoint/soap/",
									"webUrl").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setWebUrl(org.apache.axis2.databinding.utils.ConverterUtil
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

	public static class GetWebResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetWebResponse", "ns1");

		/**
		 * field for GetWebResult
		 */

		protected org.apache.axiom.om.OMElement localGetWebResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetWebResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getGetWebResult() {
			return localGetWebResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetWebResult
		 */
		public void setGetWebResult(org.apache.axiom.om.OMElement param) {

			// update the setting tracker
			localGetWebResultTracker = true;

			this.localGetWebResult = param;

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

					if (localGetWebResultTracker) {
						if (localGetWebResult == null) {
							throw new RuntimeException(
									"GetWebResult cannot be null!!");
						}
						localGetWebResult.serialize(xmlWriter);
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

			if (localGetWebResultTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"GetWebResult"));

				if (localGetWebResult == null) {
					throw new RuntimeException("GetWebResult cannot be null!!");
				}
				elementList.add(localGetWebResult);
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

						boolean loopDone1 = false;
						javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"GetWebResult");

						while (!loopDone1) {
							if (reader.isStartElement()
									&& startQname1.equals(reader.getName())) {
								loopDone1 = true;
							} else {
								reader.next();
							}
						}

						// We need to wrap the reader so that it produces a fake
						// START_DOCUEMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setGetWebResult(builder1.getOMElement());

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

	public static class WebUrlFromPageUrlResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"WebUrlFromPageUrlResponse", "ns1");

		/**
		 * field for WebUrlFromPageUrlResult
		 */

		protected java.lang.String localWebUrlFromPageUrlResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localWebUrlFromPageUrlResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getWebUrlFromPageUrlResult() {
			return localWebUrlFromPageUrlResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            WebUrlFromPageUrlResult
		 */
		public void setWebUrlFromPageUrlResult(java.lang.String param) {

			// update the setting tracker
			localWebUrlFromPageUrlResultTracker = true;

			this.localWebUrlFromPageUrlResult = param;

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

					if (localWebUrlFromPageUrlResultTracker) {
						namespace = "http://schemas.microsoft.com/sharepoint/soap/";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"WebUrlFromPageUrlResult", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"WebUrlFromPageUrlResult");
							}

						} else {
							xmlWriter
									.writeStartElement("WebUrlFromPageUrlResult");
						}

						// xmlWriter.writeStartElement("http://schemas.microsoft.com/sharepoint/soap/","WebUrlFromPageUrlResult");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localWebUrlFromPageUrlResult));
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

			if (localWebUrlFromPageUrlResultTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"WebUrlFromPageUrlResult"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localWebUrlFromPageUrlResult));
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
			public static WebUrlFromPageUrlResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				WebUrlFromPageUrlResponse object = new WebUrlFromPageUrlResponse();
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
							if (!"WebUrlFromPageUrlResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (WebUrlFromPageUrlResponse) ExtensionMapper
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
									"WebUrlFromPageUrlResult").equals(reader
									.getName())) {

						java.lang.String content = reader.getElementText();

						object
								.setWebUrlFromPageUrlResult(org.apache.axis2.databinding.utils.ConverterUtil
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

	public static class GetListTemplates implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetListTemplates", "ns1");

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
			public static GetListTemplates parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListTemplates object = new GetListTemplates();
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
							if (!"GetListTemplates".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListTemplates) ExtensionMapper
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

	public static class GetWebCollectionResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetWebCollectionResponse", "ns1");

		/**
		 * field for GetWebCollectionResult
		 */

		protected org.apache.axiom.om.OMElement localGetWebCollectionResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetWebCollectionResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getGetWebCollectionResult() {
			return localGetWebCollectionResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetWebCollectionResult
		 */
		public void setGetWebCollectionResult(
				org.apache.axiom.om.OMElement param) {

			// update the setting tracker
			localGetWebCollectionResultTracker = true;

			this.localGetWebCollectionResult = param;

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

					if (localGetWebCollectionResultTracker) {
						if (localGetWebCollectionResult == null) {
							throw new RuntimeException(
									"GetWebCollectionResult cannot be null!!");
						}
						localGetWebCollectionResult.serialize(xmlWriter);
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

			if (localGetWebCollectionResultTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"GetWebCollectionResult"));

				if (localGetWebCollectionResult == null) {
					throw new RuntimeException(
							"GetWebCollectionResult cannot be null!!");
				}
				elementList.add(localGetWebCollectionResult);
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
			public static GetWebCollectionResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetWebCollectionResponse object = new GetWebCollectionResponse();
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
							if (!"GetWebCollectionResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetWebCollectionResponse) ExtensionMapper
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
									"GetWebCollectionResult").equals(reader
									.getName())) {

						boolean loopDone1 = false;
						javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"GetWebCollectionResult");

						while (!loopDone1) {
							if (reader.isStartElement()
									&& startQname1.equals(reader.getName())) {
								loopDone1 = true;
							} else {
								reader.next();
							}
						}

						// We need to wrap the reader so that it produces a fake
						// START_DOCUEMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setGetWebCollectionResult(builder1
								.getOMElement());

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

			throw new java.lang.RuntimeException("Unsupported type "
					+ namespaceURI + " " + typeName);
		}

	}

	public static class GetWebCollection implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetWebCollection", "ns1");

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
			public static GetWebCollection parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetWebCollection object = new GetWebCollection();
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
							if (!"GetWebCollection".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetWebCollection) ExtensionMapper
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

	public static class GetAllSubWebCollectionResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://schemas.microsoft.com/sharepoint/soap/",
				"GetAllSubWebCollectionResponse", "ns1");

		/**
		 * field for GetAllSubWebCollectionResult
		 */

		protected org.apache.axiom.om.OMElement localGetAllSubWebCollectionResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetAllSubWebCollectionResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getGetAllSubWebCollectionResult() {
			return localGetAllSubWebCollectionResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetAllSubWebCollectionResult
		 */
		public void setGetAllSubWebCollectionResult(
				org.apache.axiom.om.OMElement param) {

			// update the setting tracker
			localGetAllSubWebCollectionResultTracker = true;

			this.localGetAllSubWebCollectionResult = param;

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

					if (localGetAllSubWebCollectionResultTracker) {
						if (localGetAllSubWebCollectionResult == null) {
							throw new RuntimeException(
									"GetAllSubWebCollectionResult cannot be null!!");
						}
						localGetAllSubWebCollectionResult.serialize(xmlWriter);
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

			if (localGetAllSubWebCollectionResultTracker) {
				elementList.add(new javax.xml.namespace.QName(
						"http://schemas.microsoft.com/sharepoint/soap/",
						"GetAllSubWebCollectionResult"));

				if (localGetAllSubWebCollectionResult == null) {
					throw new RuntimeException(
							"GetAllSubWebCollectionResult cannot be null!!");
				}
				elementList.add(localGetAllSubWebCollectionResult);
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
			public static GetAllSubWebCollectionResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetAllSubWebCollectionResponse object = new GetAllSubWebCollectionResponse();
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
							if (!"GetAllSubWebCollectionResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetAllSubWebCollectionResponse) ExtensionMapper
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
									"GetAllSubWebCollectionResult")
									.equals(reader.getName())) {

						boolean loopDone1 = false;
						javax.xml.namespace.QName startQname1 = new javax.xml.namespace.QName(
								"http://schemas.microsoft.com/sharepoint/soap/",
								"GetAllSubWebCollectionResult");

						while (!loopDone1) {
							if (reader.isStartElement()
									&& startQname1.equals(reader.getName())) {
								loopDone1 = true;
							} else {
								reader.next();
							}
						}

						// We need to wrap the reader so that it produces a fake
						// START_DOCUEMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setGetAllSubWebCollectionResult(builder1
								.getOMElement());

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

	public static class GetListTemplatesResult_type0 implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * GetListTemplatesResult_type0 Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for ExtraElement
		 */

		protected org.apache.axiom.om.OMElement localExtraElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getExtraElement() {
			return localExtraElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ExtraElement
		 */
		public void setExtraElement(org.apache.axiom.om.OMElement param) {

			this.localExtraElement = param;

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

					if (localExtraElement == null) {
						throw new RuntimeException(
								"extraElement cannot be null!!");
					}
					localExtraElement.serialize(xmlWriter);

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

			elementList.add(new javax.xml.namespace.QName("", "extraElement"));

			if (localExtraElement == null) {
				throw new RuntimeException("extraElement cannot be null!!");
			}
			elementList.add(localExtraElement);

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
			public static GetListTemplatesResult_type0 parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetListTemplatesResult_type0 object = new GetListTemplatesResult_type0();
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
							if (!"GetListTemplatesResult_type0".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetListTemplatesResult_type0) ExtensionMapper
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
							&& new javax.xml.namespace.QName("", "extraElement")
									.equals(reader.getName())) {

						// use the QName from the parser as the name for the
						// builder
						javax.xml.namespace.QName startQname1 = reader
								.getName();

						// We need to wrap the reader so that it produces a fake
						// START_DOCUMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setExtraElement(builder1.getOMElement());

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

	public static class GetWebResult_type2 implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * GetWebResult_type2 Namespace URI =
		 * http://schemas.microsoft.com/sharepoint/soap/ Namespace Prefix = ns1
		 */

		/**
		 * field for ExtraElement
		 */

		protected org.apache.axiom.om.OMElement localExtraElement;

		/**
		 * Auto generated getter method
		 * 
		 * @return org.apache.axiom.om.OMElement
		 */
		public org.apache.axiom.om.OMElement getExtraElement() {
			return localExtraElement;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            ExtraElement
		 */
		public void setExtraElement(org.apache.axiom.om.OMElement param) {

			this.localExtraElement = param;

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

					if (localExtraElement == null) {
						throw new RuntimeException(
								"extraElement cannot be null!!");
					}
					localExtraElement.serialize(xmlWriter);

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

			elementList.add(new javax.xml.namespace.QName("", "extraElement"));

			if (localExtraElement == null) {
				throw new RuntimeException("extraElement cannot be null!!");
			}
			elementList.add(localExtraElement);

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
			public static GetWebResult_type2 parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetWebResult_type2 object = new GetWebResult_type2();
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
							if (!"GetWebResult_type2".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetWebResult_type2) ExtensionMapper
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
							&& new javax.xml.namespace.QName("", "extraElement")
									.equals(reader.getName())) {

						// use the QName from the parser as the name for the
						// builder
						javax.xml.namespace.QName startQname1 = reader
								.getName();

						// We need to wrap the reader so that it produces a fake
						// START_DOCUMENT event
						// this is needed by the builder classes
						org.apache.axis2.databinding.utils.NamedStaxOMBuilder builder1 = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
								new org.apache.axis2.util.StreamWrapper(reader),
								startQname1);
						object.setExtraElement(builder1.getOMElement());

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

	private org.apache.axiom.om.OMElement toOM(WebsStub.GetWeb param,
			boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetWeb.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(WebsStub.GetWebResponse param,
			boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetWebResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(WebsStub.GetWebCollection param,
			boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetWebCollection.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			WebsStub.GetWebCollectionResponse param, boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetWebCollectionResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			WebsStub.WebUrlFromPageUrl param, boolean optimizeContent) {

		return param.getOMElement(WebsStub.WebUrlFromPageUrl.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			WebsStub.WebUrlFromPageUrlResponse param, boolean optimizeContent) {

		return param.getOMElement(WebsStub.WebUrlFromPageUrlResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			WebsStub.GetAllSubWebCollection param, boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetAllSubWebCollection.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			WebsStub.GetAllSubWebCollectionResponse param,
			boolean optimizeContent) {

		return param.getOMElement(
				WebsStub.GetAllSubWebCollectionResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(WebsStub.GetListTemplates param,
			boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetListTemplates.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			WebsStub.GetListTemplatesResponse param, boolean optimizeContent) {

		return param.getOMElement(WebsStub.GetListTemplatesResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory, WebsStub.GetWeb param,
			boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(WebsStub.GetWeb.MY_QNAME, factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			WebsStub.GetWebCollection param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody()
				.addChild(
						param.getOMElement(WebsStub.GetWebCollection.MY_QNAME,
								factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			WebsStub.WebUrlFromPageUrl param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param
						.getOMElement(WebsStub.WebUrlFromPageUrl.MY_QNAME,
								factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			WebsStub.GetAllSubWebCollection param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(WebsStub.GetAllSubWebCollection.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			WebsStub.GetListTemplates param, boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody()
				.addChild(
						param.getOMElement(WebsStub.GetListTemplates.MY_QNAME,
								factory));

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

			if (WebsStub.GetWeb.class.equals(type)) {

				return WebsStub.GetWeb.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetWebResponse.class.equals(type)) {

				return WebsStub.GetWebResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetWebCollection.class.equals(type)) {

				return WebsStub.GetWebCollection.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetWebCollectionResponse.class.equals(type)) {

				return WebsStub.GetWebCollectionResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.WebUrlFromPageUrl.class.equals(type)) {

				return WebsStub.WebUrlFromPageUrl.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.WebUrlFromPageUrlResponse.class.equals(type)) {

				return WebsStub.WebUrlFromPageUrlResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetAllSubWebCollection.class.equals(type)) {

				return WebsStub.GetAllSubWebCollection.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetAllSubWebCollectionResponse.class.equals(type)) {

				return WebsStub.GetAllSubWebCollectionResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetListTemplates.class.equals(type)) {

				return WebsStub.GetListTemplates.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());

			}

			if (WebsStub.GetListTemplatesResponse.class.equals(type)) {

				return WebsStub.GetListTemplatesResponse.Factory.parse(param
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
