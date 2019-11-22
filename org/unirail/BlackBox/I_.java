package org.unirail.BlackBox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Optional field with uniform values distribution in the datatype range or in the provided range (-3 | 82)
@Target(ElementType.FIELD)
public @interface I_ {
	double value() default 0;
}

