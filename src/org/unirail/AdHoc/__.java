package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//item array Field
@Target(ElementType.FIELD)
public @interface __ {
	long value();//item array length
}
	