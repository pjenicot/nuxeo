/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 */
package org.nuxeo.theme.styling.service.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * A flavor represents the set of information that can be used to switch the theme styling on a given page.
 * <p>
 * It holds presets that can be referenced in CSS files, as well as logo information. It can extend another flavor, in
 * case it will its logo and presets. The name and label are not inherited.
 * <p>
 * At registration, presets and log information are merged of a previous contribution with the same name already held
 * that kind of information. When emptying the list of presets.
 *
 * @since 5.5
 */
@XObject("flavor")
public class FlavorDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @XNode("@name")
    String name;

    @XNode("label")
    String label;

    @XNode("@extends")
    String extendsFlavor;

    @XNode("logo")
    LogoDescriptor logo;

    @XNode("palettePreview")
    PalettePreview palettePreview;

    @XNode("presetsList@append")
    boolean appendPresets;

    @XNodeList(value = "sassVariables/variable", type = ArrayList.class, componentType = SassVariable.class)
    List<SassVariable> sassVariables;

    @XNodeList(value = "presetsList/presets", type = ArrayList.class, componentType = FlavorPresets.class)
    List<FlavorPresets> presets;

    /**
     * @since 7.4
     */
    @XNodeList(value = "links/icon", type = ArrayList.class, componentType = IconDescriptor.class)
    List<IconDescriptor> favicons;

    @Override
    public FlavorDescriptor clone() {
        FlavorDescriptor clone = new FlavorDescriptor();
        clone.setName(getName());
        clone.setLabel(getLabel());
        LogoDescriptor logo = getLogo();
        if (logo != null) {
            clone.setLogo(logo.clone());
        }
        PalettePreview pp = getPalettePreview();
        if (pp != null) {
            clone.setPalettePreview(pp.clone());
        }
        clone.setExtendsFlavor(getExtendsFlavor());
        clone.setAppendPresets(getAppendPresets());
        List<FlavorPresets> presets = getPresets();
        if (presets != null) {
            List<FlavorPresets> newPresets = new ArrayList<FlavorPresets>();
            for (FlavorPresets item : presets) {
                newPresets.add(item.clone());
            }
            clone.setPresets(newPresets);
        }
        List<SassVariable> sassVariables = getSassVariables();
        if (sassVariables != null) {
            List<SassVariable> cSassVariables = new ArrayList<SassVariable>();
            for (SassVariable var : sassVariables) {
                cSassVariables.add(var.clone());
            }
            clone.setSassVariables(cSassVariables);
        }
        List<IconDescriptor> favicons = getFavicons();
        if (favicons != null) {
            List<IconDescriptor> icons = new ArrayList<IconDescriptor>();
            for (IconDescriptor icon : favicons) {
                icons.add(icon.clone());
            }
            clone.setFavicons(icons);
        }
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FlavorDescriptor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        FlavorDescriptor f = (FlavorDescriptor) obj;
        return new EqualsBuilder().append(name, f.name).append(label, f.label).append(extendsFlavor,
                f.extendsFlavor).append(logo, f.logo).append(palettePreview, f.palettePreview).append(appendPresets,
                        f.appendPresets).append(presets, f.presets).append(favicons, f.favicons).isEquals();
    }

    public boolean getAppendPresets() {
        return appendPresets;
    }

    public String getExtendsFlavor() {
        return extendsFlavor;
    }

    /**
     * @since 7.4
     */
    public List<IconDescriptor> getFavicons() {
        return favicons;
    }

    public String getLabel() {
        return label;
    }

    public LogoDescriptor getLogo() {
        return logo;
    }

    public String getName() {
        return name;
    }

    public PalettePreview getPalettePreview() {
        return palettePreview;
    }

    public List<FlavorPresets> getPresets() {
        return presets;
    }

    /**
     * @since 7.4
     */
    public List<SassVariable> getSassVariables() {
        return sassVariables;
    }

    public void merge(FlavorDescriptor src) {
        String newExtend = src.getExtendsFlavor();
        if (newExtend != null) {
            setExtendsFlavor(newExtend);
        }
        String newLabel = src.getLabel();
        if (newLabel != null) {
            setLabel(newLabel);
        }
        LogoDescriptor logo = src.getLogo();
        if (logo != null) {
            LogoDescriptor newLogo = getLogo();
            if (newLogo == null) {
                newLogo = logo.clone();
            } else {
                // merge logo info
                if (logo.getHeight() != null) {
                    newLogo.setHeight(logo.getHeight());
                }
                if (logo.getWidth() != null) {
                    newLogo.setWidth(logo.getWidth());
                }
                if (logo.getTitle() != null) {
                    newLogo.setTitle(logo.getTitle());
                }
                if (logo.getPath() != null) {
                    newLogo.setPath(logo.getPath());
                }
            }
            setLogo(newLogo);
        }
        PalettePreview pp = src.getPalettePreview();
        if (pp != null) {
            setPalettePreview(pp);
        }

        List<FlavorPresets> newPresets = src.getPresets();
        if (newPresets != null) {
            List<FlavorPresets> merged = new ArrayList<FlavorPresets>();
            merged.addAll(newPresets);
            boolean keepOld = src.getAppendPresets() || (newPresets.isEmpty() && !src.getAppendPresets());
            if (keepOld) {
                // add back old contributions
                List<FlavorPresets> oldPresets = getPresets();
                if (oldPresets != null) {
                    merged.addAll(0, oldPresets);
                }
            }
            setPresets(merged);
        }

        List<IconDescriptor> newFavicons = src.getFavicons();
        if (newFavicons != null && !newFavicons.isEmpty()) {
            setFavicons(newFavicons);
        }

    }

    public void setAppendPresets(boolean appendPresets) {
        this.appendPresets = appendPresets;
    }

    public void setExtendsFlavor(String extendsFlavor) {
        this.extendsFlavor = extendsFlavor;
    }

    /**
     * @since 7.4
     */
    public void setFavicons(List<IconDescriptor> favicons) {
        this.favicons = favicons;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLogo(LogoDescriptor logo) {
        this.logo = logo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPalettePreview(PalettePreview palettePreview) {
        this.palettePreview = palettePreview;
    }

    public void setPresets(List<FlavorPresets> presets) {
        this.presets = presets;
    }

    /**
     * @since 7.4
     */
    public void setSassVariables(List<SassVariable> sassVariables) {
        this.sassVariables = sassVariables;
    }

}