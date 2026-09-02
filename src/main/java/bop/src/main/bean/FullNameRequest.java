package bop.src.main.bean;

/**
 *
 * @author Ashraf.M.Fahmawi
 */
public class FullNameRequest {

    private String firstName;
    private String secondName;

    public FullNameRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(
            String firstName) {

        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(
            String secondName) {

        this.secondName = secondName;
    }
}
