package Mboussaid.laFactureFacile.Controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Mboussaid.laFactureFacile.DTO.AuthenticationDTO;
import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.Security.JwtService;
import Mboussaid.laFactureFacile.Services.UserService;

@RestController
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationManager authenticateManager;
    private final JwtService jwtService;

    public AuthenticationController(UserService userService, AuthenticationManager authenticateManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticateManager = authenticateManager;
        this.jwtService = jwtService;
    }


    @PostMapping("activation")
    public CustomResponseEntity<?> activation(@RequestBody Map<String, String> activation) {
        return userService.activation(activation);
    }
    @GetMapping("activation/{uid}")
    public boolean isUidValid(@PathVariable String uid) {
        return userService.isValidUid(uid);
    }

    @PostMapping("/connexion")
    public CustomResponseEntity<Map<String, String>> connexion(@RequestBody AuthenticationDTO authenticationDTO) {
        final Authentication authentication = authenticateManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationDTO.username(),
                        authenticationDTO.password()));
        if (authentication.isAuthenticated()) {
            return CustomResponseEntity.successWithDataDisplayed(HttpStatus.ACCEPTED.value(), "Connexion réussie",jwtService.generate(authenticationDTO.username()));
        }
        return CustomResponseEntity.error(HttpStatus.UNAUTHORIZED.value(),"Erreur d'authentification");
    }

    @PostMapping("modifyPassword")
    public CustomResponseEntity<?> modifyPassword(@RequestBody Map<String, String> parameters) {
        return this.userService.modifyPassword(parameters);
    }

    @PostMapping("newPassword")
    public CustomResponseEntity<?> newPassword(@RequestBody Map<String, String> parameters) {
        return this.userService.newPassword(parameters);
    }
    @PostMapping("tokenValidation")
    public boolean tokenValidation(@RequestBody String token) {
        return this.jwtService.isTokenValid(token);
    }

    @PostMapping("disconnection")
    public void disconnection() {
        jwtService.disconnection();
    }
}