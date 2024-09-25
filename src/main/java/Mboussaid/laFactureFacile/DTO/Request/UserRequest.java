package Mboussaid.laFactureFacile.DTO.Request;

public class UserRequest {
    private String name;
    private String email;
    private String pass;
    private String passwordConfirm;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String pass, String passwordConfirm) {
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.passwordConfirm = passwordConfirm;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPasswordConfirm() {
        return this.passwordConfirm;
    }

    public String getPass() {
        return this.pass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
