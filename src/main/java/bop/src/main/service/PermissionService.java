package bop.src.main.service;

import bop.src.main.dao.PermissionDAO;

/**
 *
 * @author Ashraf.M.Fahmawi
 */
public class PermissionService {

    private final PermissionDAO permissionDAO;

    public PermissionService() {
        this.permissionDAO = new PermissionDAO();
    }

    public boolean hasPermission(
            String authenticatedUserId,
            String applicationName,
            String permissionCode) throws Exception {

        validateInputs(authenticatedUserId, applicationName, permissionCode);
        long userId = parseUserId(authenticatedUserId);
        return permissionDAO.hasPermission(userId, applicationName.trim(), permissionCode.trim()
        );
    }

    private void validateInputs(String authenticatedUserId, String applicationName, String permissionCode) {

        if (isBlank(authenticatedUserId)) {
            throw new IllegalArgumentException("Authenticated user ID is required");
        }
        if (isBlank(applicationName)) {
            throw new IllegalArgumentException("Application name is required");
        }
        if (isBlank(permissionCode)) {
            throw new IllegalArgumentException("Permission code is required");
        }
    }

    private long parseUserId(String authenticatedUserId) {

        try {
            return Long.parseLong(authenticatedUserId.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Authenticated user ID must be numeric", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null|| value.trim().isEmpty();
    }
    
}
