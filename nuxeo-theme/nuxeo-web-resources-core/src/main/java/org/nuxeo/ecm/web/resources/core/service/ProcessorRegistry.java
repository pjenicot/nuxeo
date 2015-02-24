/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.web.resources.core.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.web.resources.api.Processor;
import org.nuxeo.ecm.web.resources.core.ProcessorDescriptor;
import org.nuxeo.runtime.model.SimpleContributionRegistry;

/**
 * Registry for resource elements. Does not handle merge.
 *
 * @since 5.5
 */
public class ProcessorRegistry extends SimpleContributionRegistry<ProcessorDescriptor> {

    private static final Log log = LogFactory.getLog(ProcessorRegistry.class);

    protected Map<String, Processor> instances = new HashMap<String, Processor>();

    @Override
    public String getContributionId(ProcessorDescriptor contrib) {
        return contrib.getName();
    }

    @Override
    public void contributionUpdated(String id, ProcessorDescriptor contrib, ProcessorDescriptor newOrigContrib) {
        Class<?> pClass;
        try {
            pClass = ProcessorRegistry.class.getClassLoader().loadClass(contrib.getClassName());
            Processor processor = (Processor) pClass.newInstance();
            instances.put(id, processor);
        } catch (ClassCastException | ReflectiveOperationException e) {
            log.error("Caught error when instantiating processor", e);
        }
    }

    @Override
    public void contributionRemoved(String id, ProcessorDescriptor origContrib) {
        instances.remove(id);
    }

    // custom API

    public Processor getProcessor(String id) {
        return instances.get(id);
    }

}
