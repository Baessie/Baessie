package org.baessie.simulator.util;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

public class DocumentNamspaceContext implements NamespaceContext {
	private final Document document;

	public DocumentNamspaceContext(final Document document) {
		this.document = document;
	}

	@Override
	public String getPrefix(final String namespaceURI) {
		return document.lookupPrefix(namespaceURI);
	}

	@Override
	public String getNamespaceURI(final String prefix) {
		String namespaceURI;
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			namespaceURI = document.lookupNamespaceURI(null);
		} else {
			namespaceURI = document.lookupNamespaceURI(prefix);
		}
		return namespaceURI;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(final String namespaceURI) {
		return null;
	}

}
