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
package org.nuxeo.ecm.web.resources.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.web.resources.api.Resource;
import org.nuxeo.ecm.web.resources.api.ResourceType;

/**
 * @since 7.2
 */
@XObject("resource")
public class ResourceImpl implements Resource {

    private static final long serialVersionUID = 1L;

    @XNode("@name")
    public String name;

    @XNode("@type")
    public String type;

    @XNode("path")
    public String path;

    @XNodeList(value = "require", type = ArrayList.class, componentType = String.class)
    public List<String> dependencies;

    @XNode("shrinkable")
    public boolean shrinkable = true;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ResourceType getType() {
        if (StringUtils.isBlank(type)) {
            // try to infer it from name for easier declaration
            return ResourceType.parse(FileUtils.getFileExtension(name));
        }
        return ResourceType.parse(type);
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isShrinkable() {
        return shrinkable;
    }

}