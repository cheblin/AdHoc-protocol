package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Required field with uniform values distribution in the datatype range or in the provided range (-3 | 82)
//without parameter make field unsigned
@Target(ElementType.FIELD)
public @interface I {
	double value()default 0;
}
	