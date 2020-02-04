package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Optional multidimensional field. Dimensions are described in the form (3|-3|~ 4)
@Target(ElementType.FIELD)
public @interface D_ {
	long value();
}
	