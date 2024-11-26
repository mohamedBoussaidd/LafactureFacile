package Mboussaid.laFactureFacile.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    private Integer id;
    private String name;
    private String firstname;
    private String email;
    private String adresse;
    private String city;
    private String postalCode;
    private String siret;
    private String telephone;
}
