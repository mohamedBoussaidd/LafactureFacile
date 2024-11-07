package Mboussaid.laFactureFacile.Controllers;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import Mboussaid.laFactureFacile.DTO.Request.UserRequest;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Services.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

    private UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
   @GetMapping("allUsers")
    public List<User> getUserList() {
        return userService.getAll();
    };

    @GetMapping("user")
    public Optional<User> getUserByUsername(String usernameOrEmail) {
        if (usernameOrEmail.contains("@")) {
            return userService.getUserByEmail(usernameOrEmail);
        } else {
            return userService.getUserByUsername(usernameOrEmail);
        }
    };

    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @PostMapping("addUser")
    public ResponseEntity<?> addUser(@RequestBody UserRequest user) {
        return userService.save(user);
    }

    // update user
    @PostMapping("updateUser")
    public ResponseEntity<?> updateUser(@RequestBody UserRequest userRequest) {
        return userService.updateUser(userRequest);
    }

    // delete user
    @PostMapping("deleteUser")
    public String deleteUser(@RequestBody User user) {
        userService.deleteUser(user);
        return "User deleted";
    }
    
}
