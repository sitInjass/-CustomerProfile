/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.filter;

import bop.src.main.annotation.AdminOperation;

import java.lang.reflect.Method;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Ashraf.M.Fahmawi
 */

/*
 * Connects @AdminOperation with AdminOperationFilter.
 *
 * Jersey executes this class during application startup
 * for every REST resource method.
 */
@Provider
public class AdminOperationFeature
        implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        if (resourceInfo == null || resourceInfo.getResourceMethod() == null) {
            return;
        }

        Method resourceMethod = resourceInfo.getResourceMethod();

        AdminOperation adminOperation = resourceMethod.getAnnotation(AdminOperation.class);

        /*
         * Public endpoints such as health checks do not have
         * @AdminOperation, so no security filter is registered.
         */
        if (adminOperation == null) {
            return;
        }

        String requiredPermission = adminOperation.permission();

        validatePermissionConfiguration(resourceMethod, requiredPermission);

        String serviceName = resourceMethod.getName();

        featureContext.register(new AdminOperationFilter(requiredPermission.trim(), serviceName));
    }

    private void validatePermissionConfiguration(Method resourceMethod, String requiredPermission) {

        if (requiredPermission == null || requiredPermission.trim().isEmpty()) {

            throw new IllegalStateException("Missing permission configuration on resource method: "
                    + resourceMethod.getDeclaringClass().getName()
                    + "."
                    + resourceMethod.getName()
            );
        }
    }
}
