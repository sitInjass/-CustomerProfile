package bop.src.main.service;

import bop.src.main.bean.FullNameRequest;
import bop.src.main.bean.FullNameRespons;
import bop.src.main.dao.FullNameDAO;

/**
 * 
 * @author ِAshraf.M.Fahmawi
 */
public class FullNameService {

    private final FullNameDAO fullNameDAO;

    public FullNameService() {
        this.fullNameDAO
                = new FullNameDAO();
    }

    public FullNameRespons buildFullName(
            FullNameRequest request)
            throws Exception {

        validateRequest(request);

        String firstName
                = request.getFirstName()
                        .trim();

        String secondName
                = request.getSecondName()
                        .trim();

        String fullName
                = fullNameDAO.buildFullName(
                        firstName,
                        secondName
                );

        if (isBlank(fullName)) {
            throw new Exception(
                    "Full name was not generated"
            );
        }

        return new FullNameRespons(
                fullName
        );
    }

    private void validateRequest(
            FullNameRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }

        if (isBlank(request.getFirstName())) {
            throw new IllegalArgumentException(
                    "First name is required"
            );
        }

        if (isBlank(request.getSecondName())) {
            throw new IllegalArgumentException(
                    "Second name is required"
            );
        }

        if (request.getFirstName()
                .trim()
                .length() > 100) {

            throw new IllegalArgumentException(
                    "First name must not exceed 100 characters"
            );
        }

        if (request.getSecondName()
                .trim()
                .length() > 100) {

            throw new IllegalArgumentException(
                    "Second name must not exceed 100 characters"
            );
        }
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}
