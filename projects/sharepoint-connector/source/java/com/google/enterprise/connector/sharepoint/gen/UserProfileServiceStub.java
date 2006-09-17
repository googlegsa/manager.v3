/**
 * UserProfileServiceStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT Aug 10, 2006 (05:21:52 GMT+00:00)
 */
package com.google.enterprise.connector.sharepoint.gen;

/*
 * UserProfileServiceStub java implementation
 */

public class UserProfileServiceStub extends org.apache.axis2.client.Stub {
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
		_service = new org.apache.axis2.description.AxisService(
				"UserProfileService" + this.hashCode());

		// creating the operations
		org.apache.axis2.description.AxisOperation __operation;

		_operations = new org.apache.axis2.description.AxisOperation[4];

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetUserProfileByGuid"));

		_operations[0] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetUserProfileSchema"));

		_operations[1] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetUserProfileByName"));

		_operations[2] = __operation;
		_service.addOperation(__operation);

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("",
				"GetUserProfileByIndex"));

		_operations[3] = __operation;
		_service.addOperation(__operation);

	}

	// populates the faults
	private void populateFaults() {

	}

	/**
	 * Constructor that takes in a configContext
	 */
	public UserProfileServiceStub(
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
	public UserProfileServiceStub() throws java.lang.Exception {

		this("http://172.25.234.129/_vti_bin/UserProfileService.asmx");

	}

	/**
	 * Constructor taking the target endpoint
	 */
	public UserProfileServiceStub(java.lang.String targetEndpoint)
			throws java.lang.Exception {
		this(null, targetEndpoint);
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see sharepointportalserver.UserProfileService#GetUserProfileByGuid
	 * @param param0
	 * 
	 */
	public UserProfileServiceStub.GetUserProfileByGuidResponse GetUserProfileByGuid(

	UserProfileServiceStub.GetUserProfileByGuid param0)
			throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[0].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService/GetUserProfileByGuid");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param0,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetUserProfileByGuid")));

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
					UserProfileServiceStub.GetUserProfileByGuidResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (UserProfileServiceStub.GetUserProfileByGuidResponse) object;

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
	 * @see UserProfileService#GetUserProfileSchema
	 * @param param2
	 * 
	 */
	public UserProfileServiceStub.GetUserProfileSchemaResponse GetUserProfileSchema(

	UserProfileServiceStub.GetUserProfileSchema param2)
			throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[1].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService/GetUserProfileSchema");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param2,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetUserProfileSchema")));

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
					UserProfileServiceStub.GetUserProfileSchemaResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (UserProfileServiceStub.GetUserProfileSchemaResponse) object;

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
	 * @see UserProfileService#GetUserProfileByName
	 * @param param4
	 * 
	 */
	public UserProfileServiceStub.GetUserProfileByNameResponse GetUserProfileByName(

	UserProfileServiceStub.GetUserProfileByName param4)
			throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[2].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService/GetUserProfileByName");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param4,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetUserProfileByName")));

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
					UserProfileServiceStub.GetUserProfileByNameResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (UserProfileServiceStub.GetUserProfileByNameResponse) object;

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
	 * @see UserProfileService#GetUserProfileByIndex
	 * @param param6
	 * 
	 */
	public UserProfileServiceStub.GetUserProfileByIndexResponse GetUserProfileByIndex(

	UserProfileServiceStub.GetUserProfileByIndex param6)
			throws java.rmi.RemoteException

	{
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[3].getName());
			_operationClient
					.getOptions()
					.setAction(
							"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService/GetUserProfileByIndex");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			// Style is Doc.

			env = toEnvelope(getFactory(_operationClient.getOptions()
					.getSoapVersionURI()), param6,
					optimizeContent(new javax.xml.namespace.QName("",
							"GetUserProfileByIndex")));

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
					UserProfileServiceStub.GetUserProfileByIndexResponse.class,
					getEnvelopeNamespaces(_returnEnv));
			_messageContext.getTransportOut().getSender().cleanup(
					_messageContext);
			return (UserProfileServiceStub.GetUserProfileByIndexResponse) object;

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

	// http://172.25.234.129/_vti_bin/UserProfileService.asmx
	public static class GetUserProfileByName implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileByName", "ns1");

		/**
		 * field for AccountName
		 */

		protected java.lang.String localAccountName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localAccountNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getAccountName() {
			return localAccountName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            AccountName
		 */
		public void setAccountName(java.lang.String param) {

			// update the setting tracker
			localAccountNameTracker = true;

			this.localAccountName = param;

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

					if (localAccountNameTracker) {
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"AccountName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"AccountName");
							}

						} else {
							xmlWriter.writeStartElement("AccountName");
						}

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","AccountName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localAccountName));
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
				private String createPrefix() {
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

			if (localAccountNameTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"AccountName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localAccountName));
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
			public static GetUserProfileByName parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByName object = new GetUserProfileByName();
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
							if (!"GetUserProfileByName".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByName) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"AccountName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();
						object
								.setAccountName(org.apache.axis2.databinding.utils.ConverterUtil
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

	public static class GetUserProfileByGuid implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileByGuid", "ns1");

		/**
		 * field for Guid
		 */

		protected java.lang.String localGuid;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getGuid() {
			return localGuid;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Guid
		 */
		public void setGuid(java.lang.String param) {

			this.localGuid = param;

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

					namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "guid",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "guid");
						}

					} else {
						xmlWriter.writeStartElement("guid");
					}

					// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","guid");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localGuid));
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
				private String createPrefix() {
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

			elementList
					.add(new javax.xml.namespace.QName(
							"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
							"guid"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localGuid));

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
			public static GetUserProfileByGuid parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByGuid object = new GetUserProfileByGuid();
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
							if (!"GetUserProfileByGuid".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByGuid) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"guid").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();
						object
								.setGuid(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

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

	public static class GetUserProfileSchemaResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileSchemaResponse", "ns1");

		/**
		 * field for GetUserProfileSchemaResult
		 */

		protected ArrayOfPropertyInfo localGetUserProfileSchemaResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetUserProfileSchemaResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfPropertyInfo
		 */
		public ArrayOfPropertyInfo getGetUserProfileSchemaResult() {
			return localGetUserProfileSchemaResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetUserProfileSchemaResult
		 */
		public void setGetUserProfileSchemaResult(ArrayOfPropertyInfo param) {

			// update the setting tracker
			localGetUserProfileSchemaResultTracker = true;

			this.localGetUserProfileSchemaResult = param;

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

					if (localGetUserProfileSchemaResultTracker) {
						if (localGetUserProfileSchemaResult == null) {
							throw new RuntimeException(
									"GetUserProfileSchemaResult cannot be null!!");
						}
						localGetUserProfileSchemaResult
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
												"GetUserProfileSchemaResult"),
										factory).serialize(xmlWriter);
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
				private String createPrefix() {
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

			if (localGetUserProfileSchemaResultTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"GetUserProfileSchemaResult"));

				if (localGetUserProfileSchemaResult == null) {
					throw new RuntimeException(
							"GetUserProfileSchemaResult cannot be null!!");
				}
				elementList.add(localGetUserProfileSchemaResult);
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
			public static GetUserProfileSchemaResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileSchemaResponse object = new GetUserProfileSchemaResponse();
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
							if (!"GetUserProfileSchemaResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileSchemaResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"GetUserProfileSchemaResult").equals(reader
									.getName())) {

						object
								.setGetUserProfileSchemaResult(ArrayOfPropertyInfo.Factory
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

	public static class ArrayOfPropertyData implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOfPropertyData Namespace URI =
		 * http://microsoft.com/webservices/SharePointPortalServer/UserProfileService
		 * Namespace Prefix = ns1
		 */

		/**
		 * field for PropertyData This was an Array!
		 */

		protected PropertyData[] localPropertyData;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPropertyDataTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return PropertyData[]
		 */
		public PropertyData[] getPropertyData() {
			return localPropertyData;
		}

		/**
		 * validate the array for PropertyData
		 */
		protected void validatePropertyData(PropertyData[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            PropertyData
		 */
		public void setPropertyData(PropertyData[] param) {

			validatePropertyData(param);

			// update the setting tracker
			localPropertyDataTracker = true;

			this.localPropertyData = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            PropertyData
		 */
		public void addPropertyData(PropertyData param) {
			if (localPropertyData == null) {
				localPropertyData = new PropertyData[] {};
			}

			// update the setting tracker
			localPropertyDataTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(localPropertyData);
			list.add(param);
			this.localPropertyData = (PropertyData[]) list
					.toArray(new PropertyData[list.size()]);

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

					if (localPropertyDataTracker) {
						// this property is nillable
						if (localPropertyData != null) {

							for (int i = 0; i < localPropertyData.length; i++) {
								localPropertyData[i]
										.getOMElement(
												new javax.xml.namespace.QName(
														"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
														"PropertyData"),
												factory).serialize(xmlWriter);

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
				private String createPrefix() {
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

			if (localPropertyDataTracker) {
				// this property is nillable
				if (localPropertyData != null) {

					for (int i = 0; i < localPropertyData.length; i++) {
						elementList
								.add(new javax.xml.namespace.QName(
										"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
										"PropertyData"));
						elementList.add(localPropertyData[i]);
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
			public static ArrayOfPropertyData parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOfPropertyData object = new ArrayOfPropertyData();
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
							if (!"ArrayOfPropertyData".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOfPropertyData) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"PropertyData").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(PropertyData.Factory.parse(reader));
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
										"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
										"PropertyData")
										.equals(reader.getName())) {
									list1.add(PropertyData.Factory
											.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.setPropertyData((PropertyData[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(PropertyData.class,
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

	public static class GetUserProfileByNameResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileByNameResponse", "ns1");

		/**
		 * field for GetUserProfileByNameResult
		 */

		protected ArrayOfPropertyData localGetUserProfileByNameResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetUserProfileByNameResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfPropertyData
		 */
		public ArrayOfPropertyData getGetUserProfileByNameResult() {
			return localGetUserProfileByNameResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetUserProfileByNameResult
		 */
		public void setGetUserProfileByNameResult(ArrayOfPropertyData param) {

			// update the setting tracker
			localGetUserProfileByNameResultTracker = true;

			this.localGetUserProfileByNameResult = param;

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

					if (localGetUserProfileByNameResultTracker) {
						if (localGetUserProfileByNameResult == null) {
							throw new RuntimeException(
									"GetUserProfileByNameResult cannot be null!!");
						}
						localGetUserProfileByNameResult
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
												"GetUserProfileByNameResult"),
										factory).serialize(xmlWriter);
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
				private String createPrefix() {
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

			if (localGetUserProfileByNameResultTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"GetUserProfileByNameResult"));

				if (localGetUserProfileByNameResult == null) {
					throw new RuntimeException(
							"GetUserProfileByNameResult cannot be null!!");
				}
				elementList.add(localGetUserProfileByNameResult);
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
			public static GetUserProfileByNameResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByNameResponse object = new GetUserProfileByNameResponse();
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
							if (!"GetUserProfileByNameResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByNameResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"GetUserProfileByNameResult").equals(reader
									.getName())) {

						object
								.setGetUserProfileByNameResult(ArrayOfPropertyData.Factory
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

	public static class ExtensionMapper {

		public static java.lang.Object getTypeObject(
				java.lang.String namespaceURI, java.lang.String typeName,
				javax.xml.stream.XMLStreamReader reader)
				throws java.lang.Exception {

			if ("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService"
					.equals(namespaceURI)
					&& "ArrayOfPropertyInfo".equals(typeName)) {

				return ArrayOfPropertyInfo.Factory.parse(reader);

			}

			else if ("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService"
					.equals(namespaceURI)
					&& "GetUserProfileByIndexResult".equals(typeName)) {

				return GetUserProfileByIndexResult.Factory.parse(reader);

			}

			else if ("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService"
					.equals(namespaceURI)
					&& "PropertyInfo".equals(typeName)) {

				return PropertyInfo.Factory.parse(reader);

			}

			else if ("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService"
					.equals(namespaceURI)
					&& "PropertyData".equals(typeName)) {

				return PropertyData.Factory.parse(reader);

			}

			else if ("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService"
					.equals(namespaceURI)
					&& "ArrayOfPropertyData".equals(typeName)) {

				return ArrayOfPropertyData.Factory.parse(reader);

			}

			throw new java.lang.RuntimeException("Unsupported type "
					+ namespaceURI + " " + typeName);
		}

	}

	public static class GetUserProfileSchema implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileSchema", "ns1");

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
				private String createPrefix() {
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
			public static GetUserProfileSchema parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileSchema object = new GetUserProfileSchema();
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
							if (!"GetUserProfileSchema".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileSchema) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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

	public static class GetUserProfileByIndexResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileByIndexResponse", "ns1");

		/**
		 * field for GetUserProfileByIndexResult
		 */

		protected GetUserProfileByIndexResult localGetUserProfileByIndexResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetUserProfileByIndexResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return GetUserProfileByIndexResult
		 */
		public GetUserProfileByIndexResult getGetUserProfileByIndexResult() {
			return localGetUserProfileByIndexResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetUserProfileByIndexResult
		 */
		public void setGetUserProfileByIndexResult(
				GetUserProfileByIndexResult param) {

			// update the setting tracker
			localGetUserProfileByIndexResultTracker = true;

			this.localGetUserProfileByIndexResult = param;

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

					if (localGetUserProfileByIndexResultTracker) {
						if (localGetUserProfileByIndexResult == null) {
							throw new RuntimeException(
									"GetUserProfileByIndexResult cannot be null!!");
						}
						localGetUserProfileByIndexResult
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
												"GetUserProfileByIndexResult"),
										factory).serialize(xmlWriter);
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
				private String createPrefix() {
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

			if (localGetUserProfileByIndexResultTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"GetUserProfileByIndexResult"));

				if (localGetUserProfileByIndexResult == null) {
					throw new RuntimeException(
							"GetUserProfileByIndexResult cannot be null!!");
				}
				elementList.add(localGetUserProfileByIndexResult);
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
			public static GetUserProfileByIndexResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByIndexResponse object = new GetUserProfileByIndexResponse();
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
							if (!"GetUserProfileByIndexResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByIndexResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"GetUserProfileByIndexResult")
									.equals(reader.getName())) {

						object
								.setGetUserProfileByIndexResult(GetUserProfileByIndexResult.Factory
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

	public static class GetUserProfileByIndexResult implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * GetUserProfileByIndexResult Namespace URI =
		 * http://microsoft.com/webservices/SharePointPortalServer/UserProfileService
		 * Namespace Prefix = ns1
		 */

		/**
		 * field for NextValue
		 */

		protected java.lang.String localNextValue;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localNextValueTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getNextValue() {
			return localNextValue;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            NextValue
		 */
		public void setNextValue(java.lang.String param) {

			// update the setting tracker
			localNextValueTracker = true;

			this.localNextValue = param;

		}

		/**
		 * field for UserProfile
		 */

		protected ArrayOfPropertyData localUserProfile;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localUserProfileTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfPropertyData
		 */
		public ArrayOfPropertyData getUserProfile() {
			return localUserProfile;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            UserProfile
		 */
		public void setUserProfile(ArrayOfPropertyData param) {

			// update the setting tracker
			localUserProfileTracker = true;

			this.localUserProfile = param;

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

					if (localNextValueTracker) {
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"NextValue", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"NextValue");
							}

						} else {
							xmlWriter.writeStartElement("NextValue");
						}

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","NextValue");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localNextValue));
						xmlWriter.writeEndElement();
					}
					if (localUserProfileTracker) {
						if (localUserProfile == null) {
							throw new RuntimeException(
									"UserProfile cannot be null!!");
						}
						localUserProfile
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
												"UserProfile"), factory)
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
				private String createPrefix() {
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

			if (localNextValueTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"NextValue"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localNextValue));
			}
			if (localUserProfileTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"UserProfile"));

				if (localUserProfile == null) {
					throw new RuntimeException("UserProfile cannot be null!!");
				}
				elementList.add(localUserProfile);
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
			public static GetUserProfileByIndexResult parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByIndexResult object = new GetUserProfileByIndexResult();
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
							if (!"GetUserProfileByIndexResult".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByIndexResult) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"NextValue").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();
						object
								.setNextValue(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"UserProfile").equals(reader.getName())) {

						object.setUserProfile(ArrayOfPropertyData.Factory
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

	public static class ArrayOfPropertyInfo implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * ArrayOfPropertyInfo Namespace URI =
		 * http://microsoft.com/webservices/SharePointPortalServer/UserProfileService
		 * Namespace Prefix = ns1
		 */

		/**
		 * field for PropertyInfo This was an Array!
		 */

		protected PropertyInfo[] localPropertyInfo;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localPropertyInfoTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return PropertyInfo[]
		 */
		public PropertyInfo[] getPropertyInfo() {
			return localPropertyInfo;
		}

		/**
		 * validate the array for PropertyInfo
		 */
		protected void validatePropertyInfo(PropertyInfo[] param) {

		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            PropertyInfo
		 */
		public void setPropertyInfo(PropertyInfo[] param) {

			validatePropertyInfo(param);

			// update the setting tracker
			localPropertyInfoTracker = true;

			this.localPropertyInfo = param;
		}

		/**
		 * Auto generated add method for the array for convenience
		 * 
		 * @param param
		 *            PropertyInfo
		 */
		public void addPropertyInfo(PropertyInfo param) {
			if (localPropertyInfo == null) {
				localPropertyInfo = new PropertyInfo[] {};
			}

			// update the setting tracker
			localPropertyInfoTracker = true;

			java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil
					.toList(localPropertyInfo);
			list.add(param);
			this.localPropertyInfo = (PropertyInfo[]) list
					.toArray(new PropertyInfo[list.size()]);

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

					if (localPropertyInfoTracker) {
						// this property is nillable
						if (localPropertyInfo != null) {

							for (int i = 0; i < localPropertyInfo.length; i++) {
								localPropertyInfo[i]
										.getOMElement(
												new javax.xml.namespace.QName(
														"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
														"PropertyInfo"),
												factory).serialize(xmlWriter);

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
				private String createPrefix() {
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

			if (localPropertyInfoTracker) {
				// this property is nillable
				if (localPropertyInfo != null) {

					for (int i = 0; i < localPropertyInfo.length; i++) {
						elementList
								.add(new javax.xml.namespace.QName(
										"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
										"PropertyInfo"));
						elementList.add(localPropertyInfo[i]);
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
			public static ArrayOfPropertyInfo parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				ArrayOfPropertyInfo object = new ArrayOfPropertyInfo();
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
							if (!"ArrayOfPropertyInfo".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (ArrayOfPropertyInfo) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"PropertyInfo").equals(reader.getName())) {

						// Process the array and step past its final element's
						// end.
						list1.add(PropertyInfo.Factory.parse(reader));
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
										"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
										"PropertyInfo")
										.equals(reader.getName())) {
									list1.add(PropertyInfo.Factory
											.parse(reader));
								} else {
									loopDone1 = true;
								}
							}
						}
						// call the converter utility to convert and set the
						// array
						object
								.setPropertyInfo((PropertyInfo[]) org.apache.axis2.databinding.utils.ConverterUtil
										.convertToArray(PropertyInfo.class,
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

	public static class GetUserProfileByIndex implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileByIndex", "ns1");

		/**
		 * field for Index
		 */

		protected int localIndex;

		/**
		 * Auto generated getter method
		 * 
		 * @return int
		 */
		public int getIndex() {
			return localIndex;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Index
		 */
		public void setIndex(int param) {

			this.localIndex = param;

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

					namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

					if (!namespace.equals("")) {
						prefix = xmlWriter.getPrefix(namespace);

						if (prefix == null) {
							prefix = org.apache.axis2.databinding.utils.BeanUtil
									.getUniquePrefix();

							xmlWriter.writeStartElement(prefix, "index",
									namespace);
							xmlWriter.writeNamespace(prefix, namespace);
							xmlWriter.setPrefix(prefix, namespace);

						} else {
							xmlWriter.writeStartElement(namespace, "index");
						}

					} else {
						xmlWriter.writeStartElement("index");
					}

					// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","index");
					xmlWriter
							.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
									.convertToString(localIndex));
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
				private String createPrefix() {
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

			elementList
					.add(new javax.xml.namespace.QName(
							"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
							"index"));

			elementList.add(org.apache.axis2.databinding.utils.ConverterUtil
					.convertToString(localIndex));

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
			public static GetUserProfileByIndex parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByIndex object = new GetUserProfileByIndex();
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
							if (!"GetUserProfileByIndex".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByIndex) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"index").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();
						object
								.setIndex(org.apache.axis2.databinding.utils.ConverterUtil
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

	public static class PropertyData implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * PropertyData Namespace URI =
		 * http://microsoft.com/webservices/SharePointPortalServer/UserProfileService
		 * Namespace Prefix = ns1
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
		 * field for Value
		 */

		protected java.lang.String localValue;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localValueTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getValue() {
			return localValue;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            Value
		 */
		public void setValue(java.lang.String param) {

			// update the setting tracker
			localValueTracker = true;

			this.localValue = param;

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
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

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

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","Name");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localName));
						xmlWriter.writeEndElement();
					}
					if (localValueTracker) {
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix, "Value",
										namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace, "Value");
							}

						} else {
							xmlWriter.writeStartElement("Value");
						}

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","Value");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localValue));
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
				private String createPrefix() {
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
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"Name"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localName));
			}
			if (localValueTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"Value"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localValue));
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
			public static PropertyData parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				PropertyData object = new PropertyData();
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
							if (!"PropertyData".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (PropertyData) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"Value").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();
						object
								.setValue(org.apache.axis2.databinding.utils.ConverterUtil
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

	public static class PropertyInfo implements
			org.apache.axis2.databinding.ADBBean {
		/*
		 * This type was generated from the piece of schema that had name =
		 * PropertyInfo Namespace URI =
		 * http://microsoft.com/webservices/SharePointPortalServer/UserProfileService
		 * Namespace Prefix = ns1
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
		 * field for DisplayName
		 */

		protected java.lang.String localDisplayName;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localDisplayNameTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return java.lang.String
		 */
		public java.lang.String getDisplayName() {
			return localDisplayName;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            DisplayName
		 */
		public void setDisplayName(java.lang.String param) {

			// update the setting tracker
			localDisplayNameTracker = true;

			this.localDisplayName = param;

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
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

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

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","Name");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localName));
						xmlWriter.writeEndElement();
					}
					if (localDisplayNameTracker) {
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

						if (!namespace.equals("")) {
							prefix = xmlWriter.getPrefix(namespace);

							if (prefix == null) {
								prefix = org.apache.axis2.databinding.utils.BeanUtil
										.getUniquePrefix();

								xmlWriter.writeStartElement(prefix,
										"DisplayName", namespace);
								xmlWriter.writeNamespace(prefix, namespace);
								xmlWriter.setPrefix(prefix, namespace);

							} else {
								xmlWriter.writeStartElement(namespace,
										"DisplayName");
							}

						} else {
							xmlWriter.writeStartElement("DisplayName");
						}

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","DisplayName");
						xmlWriter
								.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(localDisplayName));
						xmlWriter.writeEndElement();
					}
					if (localTypeTracker) {
						namespace = "http://microsoft.com/webservices/SharePointPortalServer/UserProfileService";

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

						// xmlWriter.writeStartElement("http://microsoft.com/webservices/SharePointPortalServer/UserProfileService","Type");
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
				private String createPrefix() {
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
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"Name"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localName));
			}
			if (localDisplayNameTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"DisplayName"));

				elementList
						.add(org.apache.axis2.databinding.utils.ConverterUtil
								.convertToString(localDisplayName));
			}
			if (localTypeTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
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
			public static PropertyInfo parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				PropertyInfo object = new PropertyInfo();
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
							if (!"PropertyInfo".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (PropertyInfo) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"DisplayName").equals(reader.getName())) {

						java.lang.String content = reader.getElementText();
						object
								.setDisplayName(org.apache.axis2.databinding.utils.ConverterUtil
										.convertToString(content));

						reader.next();

					} // End of if for expected property start element

					while (!reader.isStartElement() && !reader.isEndElement())
						reader.next();

					if (reader.isStartElement()
							&& new javax.xml.namespace.QName(
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
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

	public static class GetUserProfileByGuidResponse implements
			org.apache.axis2.databinding.ADBBean {

		public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
				"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
				"GetUserProfileByGuidResponse", "ns1");

		/**
		 * field for GetUserProfileByGuidResult
		 */

		protected ArrayOfPropertyData localGetUserProfileByGuidResult;

		/*
		 * This tracker boolean wil be used to detect whether the user called
		 * the set method for this attribute. It will be used to determine
		 * whether to include this field in the serialized XML
		 */
		protected boolean localGetUserProfileByGuidResultTracker = false;

		/**
		 * Auto generated getter method
		 * 
		 * @return ArrayOfPropertyData
		 */
		public ArrayOfPropertyData getGetUserProfileByGuidResult() {
			return localGetUserProfileByGuidResult;
		}

		/**
		 * Auto generated setter method
		 * 
		 * @param param
		 *            GetUserProfileByGuidResult
		 */
		public void setGetUserProfileByGuidResult(ArrayOfPropertyData param) {

			// update the setting tracker
			localGetUserProfileByGuidResultTracker = true;

			this.localGetUserProfileByGuidResult = param;

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

					if (localGetUserProfileByGuidResultTracker) {
						if (localGetUserProfileByGuidResult == null) {
							throw new RuntimeException(
									"GetUserProfileByGuidResult cannot be null!!");
						}
						localGetUserProfileByGuidResult
								.getOMElement(
										new javax.xml.namespace.QName(
												"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
												"GetUserProfileByGuidResult"),
										factory).serialize(xmlWriter);
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
				private String createPrefix() {
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

			if (localGetUserProfileByGuidResultTracker) {
				elementList
						.add(new javax.xml.namespace.QName(
								"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
								"GetUserProfileByGuidResult"));

				if (localGetUserProfileByGuidResult == null) {
					throw new RuntimeException(
							"GetUserProfileByGuidResult cannot be null!!");
				}
				elementList.add(localGetUserProfileByGuidResult);
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
			public static GetUserProfileByGuidResponse parse(
					javax.xml.stream.XMLStreamReader reader)
					throws java.lang.Exception {
				GetUserProfileByGuidResponse object = new GetUserProfileByGuidResponse();
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
							if (!"GetUserProfileByGuidResponse".equals(type)) {
								// find namespace for the prefix
								java.lang.String nsUri = reader
										.getNamespaceContext().getNamespaceURI(
												nsPrefix);
								return (GetUserProfileByGuidResponse) ExtensionMapper
										.getTypeObject(nsUri, type, reader);
							}

						}

					}

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
									"http://microsoft.com/webservices/SharePointPortalServer/UserProfileService",
									"GetUserProfileByGuidResult").equals(reader
									.getName())) {

						object
								.setGetUserProfileByGuidResult(ArrayOfPropertyData.Factory
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

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileByGuid param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileByGuid.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileByGuidResponse param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileByGuidResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileSchema param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileSchema.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileSchemaResponse param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileSchemaResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileByName param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileByName.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileByNameResponse param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileByNameResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileByIndex param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileByIndex.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.om.OMElement toOM(
			UserProfileServiceStub.GetUserProfileByIndexResponse param,
			boolean optimizeContent) {

		return param.getOMElement(
				UserProfileServiceStub.GetUserProfileByIndexResponse.MY_QNAME,
				org.apache.axiom.om.OMAbstractFactory.getOMFactory());

	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			UserProfileServiceStub.GetUserProfileByGuid param,
			boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(
						UserProfileServiceStub.GetUserProfileByGuid.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			UserProfileServiceStub.GetUserProfileSchema param,
			boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(
						UserProfileServiceStub.GetUserProfileSchema.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			UserProfileServiceStub.GetUserProfileByName param,
			boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(
						UserProfileServiceStub.GetUserProfileByName.MY_QNAME,
						factory));

		return emptyEnvelope;
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			UserProfileServiceStub.GetUserProfileByIndex param,
			boolean optimizeContent) {
		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory
				.getDefaultEnvelope();

		emptyEnvelope.getBody().addChild(
				param.getOMElement(
						UserProfileServiceStub.GetUserProfileByIndex.MY_QNAME,
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

			if (UserProfileServiceStub.GetUserProfileByGuid.class.equals(type)) {

				return UserProfileServiceStub.GetUserProfileByGuid.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileByGuidResponse.class
					.equals(type)) {

				return UserProfileServiceStub.GetUserProfileByGuidResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileSchema.class.equals(type)) {

				return UserProfileServiceStub.GetUserProfileSchema.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileSchemaResponse.class
					.equals(type)) {

				return UserProfileServiceStub.GetUserProfileSchemaResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileByName.class.equals(type)) {

				return UserProfileServiceStub.GetUserProfileByName.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileByNameResponse.class
					.equals(type)) {

				return UserProfileServiceStub.GetUserProfileByNameResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileByIndex.class.equals(type)) {

				return UserProfileServiceStub.GetUserProfileByIndex.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

			}

			if (UserProfileServiceStub.GetUserProfileByIndexResponse.class
					.equals(type)) {

				return UserProfileServiceStub.GetUserProfileByIndexResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());

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
