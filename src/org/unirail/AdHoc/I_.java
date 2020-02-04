package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Optional field with uniform values distribution in the datatype range or in the provided range (-3 | 82)
//in special form I_() make numeric field optional
//without any parameters make integer field unsigned and optional
@Target(ElementType.FIELD)
public @interface I_ {
	double value() default 0;
}

