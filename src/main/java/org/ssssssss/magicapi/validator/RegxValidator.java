package org.ssssssss.magicapi.validator;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.utils.DomUtils;
import org.w3c.dom.Node;

import java.util.regex.Pattern;

public class RegxValidator implements IValidator {

    @Override
    public String support() {
        return "regx";
    }

    @Override
    public boolean validate(Object input, Node node) {
        if (input instanceof String) {
            String regx = DomUtils.getNodeAttributeValue(node, "value");
            if (StringUtils.isNotBlank(regx)) {
                return Pattern.compile(regx.trim()).matcher(input.toString()).matches();
            }
        }
        return false;
    }
}
