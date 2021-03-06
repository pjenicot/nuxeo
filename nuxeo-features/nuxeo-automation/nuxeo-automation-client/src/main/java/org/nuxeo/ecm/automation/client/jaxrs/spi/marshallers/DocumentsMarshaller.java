/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     matic
 */
package org.nuxeo.ecm.automation.client.jaxrs.spi.marshallers;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshaller;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PaginableDocuments;

/**
 * @author matic
 */
public class DocumentsMarshaller implements JsonMarshaller<Documents> {

    @Override
    public String getType() {
        return "documents";
    }

    @Override
    public Class<Documents> getJavaType() {
        return Documents.class;
    }

    @Override
    public void write(JsonGenerator jg, Object value) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void readDocumentEntries(JsonParser jp, Documents docs) throws IOException {
        JsonToken tok = jp.nextToken();
        while (tok != JsonToken.END_ARRAY) {
            docs.add(DocumentMarshaller.readDocument(jp));
            tok = jp.nextToken();
        }
    }

    protected Documents readDocuments(JsonParser jp) throws IOException {
        Documents docs = new Documents();
        JsonToken tok = jp.nextToken();
        while (tok != JsonToken.END_ARRAY) {
            String key = jp.getCurrentName();
            if ("entries".equals(key)) {
                readDocumentEntries(jp, docs);
                return docs;
            }
            tok = jp.nextToken();
        }
        return docs;
    }

    protected Documents readPaginableDocuments(JsonParser jp) throws IOException {
        PaginableDocuments docs = new PaginableDocuments();
        JsonToken tok = jp.getCurrentToken();
        while (tok != null && tok != JsonToken.END_OBJECT) {
            String key = jp.getCurrentName();
            jp.nextToken();
            if ("resultsCount".equals(key)) {
                docs.setResultsCount(jp.getIntValue());
            } else if ("totalSize".equals(key)) {
                docs.setResultsCount(jp.getIntValue());
            } else if ("pageSize".equals(key)) {
                docs.setPageSize(jp.getIntValue());
            } else if ("numberOfPages".equals(key)) {
                docs.setNumberOfPages(jp.getIntValue());
            } else if ("pageCount".equals(key)) {
                docs.setNumberOfPages(jp.getIntValue());
            } else if ("currentPageIndex".equals(key)) {
                docs.setCurrentPageIndex(jp.getIntValue());
            } else if ("pageIndex".equals(key)) {
                docs.setCurrentPageIndex(jp.getIntValue());
            } else if ("entries".equals(key)) {
                readDocumentEntries(jp, docs);
            }
            tok = jp.nextToken();
        }
        if (tok == null) {
            throw new IllegalArgumentException("Unexpected end of stream.");
        }
        return docs;
    }

    @Override
    public Documents read(JsonParser jp) throws IOException {
        jp.nextToken();
        String key = jp.getCurrentName();
        if ("isPaginable".equals(key)) {
            jp.nextToken();
            boolean isPaginable = jp.getBooleanValue();
            if (isPaginable) {
                jp.nextToken();
                return readPaginableDocuments(jp);
            }
        }
        return readDocuments(jp);
    }

}
