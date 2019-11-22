package org.unirail.BlackBox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Optional(nullable) bit field.
@Target(ElementType.FIELD)
public @interface B_ {
	long value();//How many bits field used to store its value or field values range (3|82) Bits amount will be count.
}
	