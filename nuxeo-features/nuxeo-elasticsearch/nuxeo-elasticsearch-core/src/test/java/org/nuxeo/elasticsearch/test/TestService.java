/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     tiry
 */

package org.nuxeo.elasticsearch.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.nuxeo.elasticsearch.api.ElasticSearchAdmin;
import org.nuxeo.elasticsearch.api.ElasticSearchIndexing;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

@RunWith(FeaturesRunner.class)
@LocalDeploy("org.nuxeo.elasticsearch.core:elasticsearch-test-contrib.xml")
@Features({ RepositoryElasticSearchFeature.class })
public class TestService {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Inject
    ElasticSearchAdmin esa;

    @Inject
    ElasticSearchService ess;

    @Inject
    ElasticSearchIndexing esi;

    @Test
    public void checkDeclaredServices() throws Exception {
        Assert.assertNotNull(ess);
        Assert.assertNotNull(esi);
        Assert.assertNotNull(esa);

        Client client = esa.getClient();
        Assert.assertNotNull(client);

        Assert.assertEquals(0, esa.getPendingCommandCount());
        Assert.assertEquals(0, esa.getTotalCommandProcessed());
        Assert.assertEquals(0, esa.getPendingWorkerCount());
        Assert.assertEquals(0, esa.getRunningWorkerCount());
        Assert.assertFalse(esa.isIndexingInProgress());
        Assert.assertEquals(1, esa.getRepositoryNames().size());
        Assert.assertEquals("test", esa.getRepositoryNames().get(0));
    }

    @Test
    public void verifyNodeStartedWithConfig() throws Exception {

        ElasticSearchService ess = Framework.getLocalService(ElasticSearchService.class);
        Assert.assertNotNull(ess);

        ElasticSearchAdmin esa = Framework.getLocalService(ElasticSearchAdmin.class);
        Assert.assertNotNull(esa);

        NodesInfoResponse nodeInfoResponse = esa.getClient().admin().cluster().nodesInfo(new NodesInfoRequest()).actionGet();

        Assert.assertEquals(1, nodeInfoResponse.getNodes().length);
    }

    @Test
    public void verifyPrepareWaitForIndexing() throws Exception {
        ListenableFuture<Boolean> futureRet = esa.prepareWaitForIndexing();
        Assert.assertFalse(futureRet.isCancelled());
        Assert.assertTrue(futureRet.get());
        Assert.assertTrue(futureRet.isDone());
        Assert.assertTrue(futureRet.get());
    }

    @Test
    public void verifyPrepareWaitForIndexingTimeout() throws Exception {
        // when a worker is created it is pending
        Assert.assertFalse(esa.isIndexingInProgress());
        esi.runReindexingWorker("test", "select * from Document");
        Assert.assertTrue(esa.isIndexingInProgress());
        Assert.assertEquals(1, esa.getPendingWorkerCount());
        Assert.assertEquals(0, esa.getRunningWorkerCount());
        ListenableFuture<Boolean> futureRet = esa.prepareWaitForIndexing();
        try {
            futureRet.get(0, TimeUnit.MILLISECONDS);
            // sometime we don't timeout
            Assert.assertTrue(futureRet.isDone());
        } catch (TimeoutException e) {
            Assert.assertFalse(futureRet.isDone());
            Assert.assertTrue(futureRet.get());
        } finally {
            Assert.assertFalse(esa.isIndexingInProgress());
        }
    }

    @Test
    public void verifyPrepareWaitForIndexingListener() throws Exception {
        ListenableFuture<Boolean> futureRet = esa.prepareWaitForIndexing();
        final Boolean[] callbackRet = { false };
        Futures.addCallback(futureRet, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                callbackRet[0] = true;
                // System.out.println("Success");
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail("Fail");
            }
        });

        Assert.assertTrue(futureRet.get());
        // callback are executed in async, :/
        Thread.sleep(200);
        Assert.assertTrue(callbackRet[0]);
    }

}
