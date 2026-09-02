package bop.src.main.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Marks an Admin Portal endpoint as protected.
 *
 * The permission value must match:
 * ADM_PERMISSIONS.PERMISSION_CODE
 *
 * @author ِAshraf.M.Fahmawi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AdminOperation {

    String permission();
}
