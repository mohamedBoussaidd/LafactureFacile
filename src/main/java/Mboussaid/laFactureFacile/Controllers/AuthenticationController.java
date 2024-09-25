package Mboussaid.laFactureFacile.Controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Mboussaid.laFactureFacile.DTO.AuthenticationDTO;
import Mboussaid.laFactureFacile.Security.JwtService;
import Mboussaid.laFactureFacile.Services.UserService;

@RestController
public class AuthenticationController {

    private UserService userService;
    private AuthenticationManager authenticateManager;
    private JwtService jwtService;

    public AuthenticationController(UserService userService, AuthenticationManager authenticateManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticateManager = authenticateManager;
        this.jwtService = jwtService;
    }

    @PostMapping("activation")
    public ResponseEntity<?> activation(@RequestBody Map<String, String> activation) {
        return userService.activation(activation);
    }

    @PostMapping("connexion")
    public Map<String, String> connexion(@RequestBody AuthenticationDTO authenticationDTO) {
        final Authentication authentication = authenticateManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationDTO.username(),
                        authenticationDTO.password()));
        if (authentication.isAuthenticated()) {
            return jwtService.generate(authenticationDTO.username());
        }
        return null;
    }

    @PostMapping("modifyPassword")
    public ResponseEntity<?> modifyPassword(@RequestBody Map<String, String> parameters) {
        return this.userService.modifyPassword(parameters);
    }

    @PostMapping("newPassword")
    public ResponseEntity<?> newPassword(@RequestBody Map<String, String> parameters) {
        return this.userService.newPassword(parameters);
    }

    @PostMapping("disconnection")
    public void disconnection() {
        jwtService.disconnection();
    }

    @GetMapping("/test1")
     public String test() {
         return "Hello World";
     }
}