package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Field with array items
@Target(ElementType.FIELD)
public @interface __ {
	long value();//item array length
}
	