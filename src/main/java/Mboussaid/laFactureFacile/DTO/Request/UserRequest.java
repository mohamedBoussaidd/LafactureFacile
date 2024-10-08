package Mboussaid.laFactureFacile.DTO.Request;

public class UserRequest {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String password, String confirmPassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getconfirmPassword() {
        return this.confirmPassword;
    }

    public String getPassword() {
        return this.password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setconfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
