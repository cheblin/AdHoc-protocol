package org.unirail.BlackBox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

//Required multidimensional field. Dimensions are described in the form (3|3|4)
@Target(ElementType.FIELD)
public @interface D {
	long value();
	
}
	