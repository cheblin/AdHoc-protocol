package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Required bit field.
@Target(ElementType.FIELD)
public @interface B {
	long value();//How many bits field used to store its value or field values range (3|82) Bits amount will be count.
}
	