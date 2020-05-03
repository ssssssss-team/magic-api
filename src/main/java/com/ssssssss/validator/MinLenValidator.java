package com.ssssssss.validator;

import com.ssssssss.utils.DomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Node;

public class MinLenValidator implements IValidator {
    @Override
    public String support() {
        return "min-len";
    }

    @Override
    public boolean validate(Object input, Node node) {
        if (input instanceof String) {
            int len = NumberUtils.toInt(DomUtils.getNodeAttributeValue(node, "value"), 0);
            return len <= 0 || input.toString().length() >= len;
        }
        return false;
    }
}
