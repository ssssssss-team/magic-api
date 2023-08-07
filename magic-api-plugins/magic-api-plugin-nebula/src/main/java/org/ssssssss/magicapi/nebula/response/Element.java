package org.ssssssss.magicapi.nebula.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = EdgeElement.class, name = "edge"),
        @JsonSubTypes.Type(value = Vertex.class, name = "vertex")
})
public abstract class Element {

    protected String type;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
