package org.ssssssss.magicapi.component.model;

import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.model.PathMagicEntity;

import java.util.Objects;

public class ComponentInfo extends PathMagicEntity {

    /**
     * 组件描述
     */
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComponentInfo copy() {
        ComponentInfo info = new ComponentInfo();
        super.copyTo(info);
        info.setDescription(this.description);
        return info;
    }

    @Override
    public MagicEntity simple() {
        ComponentInfo info = new ComponentInfo();
        super.simple(info);
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ComponentInfo componentInfo = (ComponentInfo) o;
        return Objects.equals(id, componentInfo.id) &&
                Objects.equals(path, componentInfo.path) &&
                Objects.equals(script, componentInfo.script) &&
                Objects.equals(name, componentInfo.name) &&
                Objects.equals(description, componentInfo.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, script, name, groupId, description);
    }

}
