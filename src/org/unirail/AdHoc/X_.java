package org.unirail.AdHoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//Optional field that values have bi-direction dispersion gradient.
@Target(ElementType.FIELD)
public @interface X_ {
	double value() default 0;//Middle, most often value or value range (-7 | 78)
}