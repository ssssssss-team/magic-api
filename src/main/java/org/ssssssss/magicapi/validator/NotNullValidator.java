package org.ssssssss.magicapi.validator;

import org.w3c.dom.Node;

public class NotNullValidator implements IValidator {
    @Override
    public String support() {
        return "not-null";
    }

    @Override
    public boolean validate(Object input, Node node) {
        return input != null;
    }
}
