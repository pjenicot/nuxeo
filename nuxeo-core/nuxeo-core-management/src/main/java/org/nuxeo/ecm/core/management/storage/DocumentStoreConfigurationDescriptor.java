/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     "Stephane Lacoin [aka matic] <slacoin at nuxeo.com>"
 */
package org.nuxeo.ecm.core.management.storage;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Configure the repository to be used
 *
 * @author "Stephane Lacoin [aka matic] <slacoin at nuxeo.com>"
 */
@XObject("configuration")
public class DocumentStoreConfigurationDescriptor {

    @XNode("@repository")
    protected String repositoryName = "default";

    @XNode("@group")
    protected String groupName = "Administrators";

}
