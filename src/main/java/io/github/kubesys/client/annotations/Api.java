/**
 * Copyrigt (2019, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author  wuheng@iscas.ac.cn
 * @since   2023/07/25
 * @version 1.0.0
 *
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR}) 
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {

	/**
	 * @return api description
	 */
	String desc() default "";
	
	/**
	 * @return all exceptions
	 */
	Class<?>[] catches() default {};
}
