/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.version.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.Constants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.facet.VersioningDocument;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.core.versioning.CompatVersioningService;
import org.nuxeo.ecm.core.versioning.VersioningComponent;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.runtime.api.Framework;

public class TestVersioningService extends SQLRepositoryTestCase {

    protected VersioningComponent service;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        service = (VersioningComponent) Framework.getService(VersioningService.class);
        openSession();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    protected long getMajor(DocumentModel doc) throws ClientException {
        return getVersion(doc, VersioningService.MAJOR_VERSION_PROP);
    }

    protected long getMinor(DocumentModel doc) throws ClientException {
        return getVersion(doc, VersioningService.MINOR_VERSION_PROP);
    }

    protected long getVersion(DocumentModel doc, String prop) throws ClientException {
        Object propVal = doc.getPropertyValue(prop);
        if (propVal == null || !(propVal instanceof Long)) {
            return -1;
        } else {
            return ((Long) propVal).longValue();
        }
    }

    protected void assertVersion(String expected, DocumentModel doc) throws Exception {
        assertEquals(expected, getMajor(doc) + "." + getMinor(doc));
    }

    protected void assertLatestVersion(String expected, DocumentModel doc) throws Exception {
        DocumentModel ver = doc.getCoreSession().getLastDocumentVersion(doc.getRef());
        if (ver == null) {
            assertNull(expected);
        } else {
            assertVersion(expected, ver);
        }
    }

    protected void assertVersionLabel(String expected, DocumentModel doc) {
        assertEquals(expected, service.getVersionLabel(doc));
    }

    @Test
    public void testStandardVersioning() throws Exception {
        DocumentModel folder = session.createDocumentModel("/", "folder", "Folder");
        folder = session.createDocument(folder);
        DocumentModel doc = session.createDocumentModel("/", "testfile1", "File");
        doc = session.createDocument(doc);
        doc.setPropertyValue("dc:title", "A");
        doc = session.saveDocument(doc);
        DocumentRef docRef = doc.getRef();
        assertTrue(doc.isCheckedOut());
        assertVersion("0.0", doc);
        assertVersionLabel("0.0", doc);
        assertLatestVersion(null, doc);

        // save with no option, use default
        doc.setPropertyValue("dc:title", "B");
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("0.0", doc);
        assertVersionLabel("0.0", doc);
        assertLatestVersion(null, doc);

        // change and save with new minor
        doc.setPropertyValue("dc:title", "C");
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MINOR);
        doc = session.saveDocument(doc);
        assertFalse(doc.isCheckedOut());
        assertVersion("0.1", doc);
        assertVersionLabel("0.1", doc);
        assertLatestVersion("0.1", doc);
        DocumentModel v01 = session.getLastDocumentVersion(docRef);
        assertEquals(v01.getId(), session.getBaseVersion(docRef).reference());

        // checkout
        doc.checkOut();
        assertTrue(doc.isCheckedOut());
        assertVersion("0.1", doc);
        assertVersionLabel("0.1+", doc);
        assertLatestVersion("0.1", doc);

        // change and save with new major
        doc.setPropertyValue("dc:title", "D");
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        doc = session.saveDocument(doc);
        assertFalse(doc.isCheckedOut());
        assertVersion("1.0", doc);
        assertVersionLabel("1.0", doc);
        assertLatestVersion("1.0", doc);
        DocumentModel v10 = session.getLastDocumentVersion(docRef);
        assertEquals(v10.getId(), session.getBaseVersion(docRef).reference());

        // direct save for autocheckout
        doc.setPropertyValue("dc:title", "E");
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("1.0", doc);
        assertVersionLabel("1.0+", doc);
        assertLatestVersion("1.0", doc);

        // checkin
        DocumentRef v11ref = doc.checkIn(VersioningOption.MINOR, "foo");
        assertFalse(doc.isCheckedOut());
        assertVersion("1.1", doc);
        assertVersionLabel("1.1", doc);
        assertLatestVersion("1.1", doc);
        assertEquals(v11ref.reference(), session.getBaseVersion(docRef).reference());

        // wait before doing a restore
        waitForAsyncCompletion();

        // restore 0.1
        doc = session.restoreToVersion(docRef, v01.getRef());
        assertFalse(doc.isCheckedOut());
        assertVersion("0.1", doc);
        assertVersionLabel("0.1", doc);
        assertLatestVersion("1.1", doc);
        assertEquals(v01.getId(), session.getBaseVersion(docRef).reference());

        // checkout restored version
        doc.checkOut();
        assertTrue(doc.isCheckedOut());
        assertVersion("1.1", doc);
        assertVersionLabel("1.1+", doc);
        assertLatestVersion("1.1", doc);

        // publish (checks in first)
        DocumentModel proxy = session.publishDocument(doc, folder);
        assertFalse(doc.isCheckedOut());
        assertVersion("1.2", doc);
        assertVersionLabel("1.2", doc);
        assertLatestVersion("1.2", doc);
        assertVersion("1.2", proxy);

        // republish, no new version
        proxy = session.publishDocument(doc, folder);
        assertFalse(doc.isCheckedOut());
        assertVersion("1.2", doc);
        assertVersionLabel("1.2", doc);
        assertLatestVersion("1.2", doc);
        assertVersion("1.2", proxy);

        // do a change (autocheckout), and republish
        doc.setPropertyValue("dc:title", "F");
        session.saveDocument(doc);
        proxy = session.publishDocument(doc, folder);
        assertFalse(doc.isCheckedOut());
        assertVersion("1.3", doc);
        assertVersionLabel("1.3", doc);
        assertLatestVersion("1.3", doc);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testOldNuxeoVersioning() throws Exception {
        deployContrib(Constants.CORE_TEST_TESTS_BUNDLE, "test-versioningservice-contrib.xml");
        service.service = new CompatVersioningService();

        DocumentModel folder = session.createDocumentModel("/", "folder", "Folder");
        folder = session.createDocument(folder);
        DocumentModel doc = session.createDocumentModel("/", "testfile1", "File");
        doc = session.createDocument(doc);
        doc.setPropertyValue("dc:title", "A");
        doc = session.saveDocument(doc);
        DocumentRef docRef = doc.getRef();
        assertTrue(doc.isCheckedOut());
        assertVersion("1.0", doc);
        assertLatestVersion(null, doc);

        // snapshot A=1.0 and save B
        doc.setPropertyValue("dc:title", "B");
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MINOR);
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("1.1", doc);
        assertLatestVersion("1.0", doc);

        // another snapshot for B=1.1, using major inc
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("2.0", doc);
        assertLatestVersion("1.1", doc);
        DocumentModel v11 = session.getLastDocumentVersion(docRef);
        assertVersion("1.1", v11);

        // another snapshot but no increment doesn't change anything, doc is
        // clean
        doc.putContextData(ScopeType.REQUEST, VersioningDocument.CREATE_SNAPSHOT_ON_SAVE_KEY, Boolean.TRUE);
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("2.0", doc);
        assertLatestVersion("1.1", doc);

        // now dirty doc and snapshot+inc
        doc.setPropertyValue("dc:title", "C");
        doc = session.saveDocument(doc);
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MINOR);
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("2.1", doc);
        assertLatestVersion("2.0", doc);

        // another save+inc, no snapshot
        doc.setPropertyValue("dc:title", "D");
        doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        doc = session.saveDocument(doc);
        assertTrue(doc.isCheckedOut());
        assertVersion("3.0", doc);
        assertLatestVersion("2.1", doc);

        // checkin/checkout (old style)
        session.checkIn(docRef, null);
        session.checkOut(docRef);
        doc = session.getDocument(docRef);
        assertTrue(doc.isCheckedOut());
        assertVersion("3.1", doc);
        assertLatestVersion("3.0", doc);

        // wait before doing a restore
        waitForAsyncCompletion();

        // restore 1.1 -> 3.2 (snapshots 3.1)
        doc = session.restoreToVersion(docRef, v11.getRef());
        assertFalse(doc.isCheckedOut());
        assertVersion("1.1", doc);
        assertVersionLabel("1.1", doc);
        assertLatestVersion("3.1", doc);

        // checkout restored version
        doc.checkOut();
        assertTrue(doc.isCheckedOut());
        assertVersion("3.2", doc);
        assertVersionLabel("3.2", doc);
        assertLatestVersion("3.1", doc);
    }

    @Test
    public void testNoOptions() throws Exception {
        deployContrib(Constants.CORE_TEST_TESTS_BUNDLE, "test-versioning-nooptions.xml");
        DocumentModel doc = session.createDocumentModel("/", "doc", "File");
        doc = session.createDocument(doc);

        // no options according to config
        List<VersioningOption> opts = service.getSaveOptions(doc);
        assertEquals(0, opts.size());

        doc.setPropertyValue("dc:title", "A");
        doc = session.saveDocument(doc);

        assertVersion("0.0", doc);
        assertVersionLabel("0.0", doc);
        assertLatestVersion(null, doc);
    }

    @Test
    public void testVersioningOnLiveProxy() throws Exception {
        DocumentModel folder = session.createDocumentModel("/", "folder",
                "Folder");
        folder = session.createDocument(folder);
        DocumentModel section = session.createDocumentModel("/", "section",
                "Folder");
        section = session.createDocument(section);
        DocumentModel doc = session.createDocumentModel("/", "testfile1",
                "File");
        doc = session.createDocument(doc);
        doc.setPropertyValue("dc:title", "A");
        doc = session.saveDocument(doc);
        DocumentRef docRef = doc.getRef();
        assertTrue(doc.isCheckedOut());
        assertVersion("0.0", doc);
        assertVersionLabel("0.0", doc);
        assertLatestVersion(null, doc);

        // create a live proxy
        DocumentModel proxy = session.createProxy(doc.getRef(), section.getRef());
        assertTrue(proxy.isCheckedOut());
        assertVersion("0.0", proxy);
        assertVersionLabel("0.0", proxy);
        assertLatestVersion(null, proxy);

        // save live proxy with no option, use default
        proxy.setPropertyValue("dc:title", "B");
        proxy = session.saveDocument(proxy);
        assertTrue(proxy.isCheckedOut());
        assertVersion("0.0", proxy);
        assertVersionLabel("0.0", proxy);
        assertLatestVersion(null, proxy);

        // change live proxy and save with minor increment
        proxy.setPropertyValue("dc:title", "C");
        proxy.putContextData(VersioningService.VERSIONING_OPTION,
                VersioningOption.MINOR);
        proxy = session.saveDocument(proxy);
        assertFalse(proxy.isCheckedOut());
        assertVersion("0.1", proxy);
        assertVersionLabel("0.1", proxy);
        assertLatestVersion("0.1", proxy);

        // check the source document is also changed
        doc = session.getDocument(docRef);
        assertFalse(doc.isCheckedOut());
        assertVersion("0.1", doc);
        assertVersionLabel("0.1", doc);
        assertLatestVersion("0.1", doc);
        DocumentModel v01 = session.getLastDocumentVersion(docRef);
        assertEquals(v01.getId(), session.getBaseVersion(docRef).reference());

        // change with no increment, the proxy is checked out
        proxy.setPropertyValue("dc:title", "D");
        proxy = session.saveDocument(proxy);
        assertTrue(proxy.isCheckedOut());
        assertVersion("0.1", proxy);
        assertVersionLabel("0.1+", proxy);

        // check source doc
        doc = session.getDocument(docRef);
        assertEquals("D", doc.getPropertyValue("dc:title"));
        assertTrue(doc.isCheckedOut());
        assertVersion("0.1", doc);
        assertVersionLabel("0.1+", doc);

    }
}
