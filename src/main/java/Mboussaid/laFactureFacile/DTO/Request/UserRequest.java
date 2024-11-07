package Mboussaid.laFactureFacile.DTO.Request;

import java.util.Set;

import Mboussaid.laFactureFacile.Models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private Integer id;
    private String name;
    private String firstname;
    private String email;
    private String password;
    private String confirmPassword;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    private String siret;
    private Set<Role> roles;

   
}
