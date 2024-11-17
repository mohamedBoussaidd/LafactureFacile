package Mboussaid.laFactureFacile.Services;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.MessageEntity;
import Mboussaid.laFactureFacile.DTO.Request.UserRequest;
import Mboussaid.laFactureFacile.Models.ERole;
import Mboussaid.laFactureFacile.Models.Role;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Validation;
import Mboussaid.laFactureFacile.Repository.RoleRepository;
import Mboussaid.laFactureFacile.Repository.UserRepository;
import Mboussaid.laFactureFacile.Repository.ValidationRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ValidationRepository validationRepository;
    private final ValidationService validationService;
    private final BCryptPasswordEncoder encoder;
    private boolean isValid = false;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
            ValidationRepository validationRepository,
            ValidationService validationService, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.validationRepository = validationRepository;
        this.validationService = validationService;
        this.encoder = encoder;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByName(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public ResponseEntity<?> save(UserRequest user) {
        /* Vérification avant enregistrement */
        if (userRepository.findByEmail(user.getEmail()).isPresent() ||
                user.getEmail().indexOf("@") == -1 ||
                user.getEmail().indexOf(".") == -1) {
            return ResponseEntity.badRequest().body("Votre email est déja utiliser !!");
        }
        /* creation de l'utilisateur a ajouter en bdd */
        User userForRegister = new User();
        userForRegister.setName(user.getName());
        userForRegister.setEmail(user.getEmail());
        /* encodage du mot de passe */
        userForRegister.setPassword(this.encoder.encode(user.getPassword()));
        /* Création de role */
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role non trouver !!"));
        roles.add(userRole);
        userForRegister.setRoles(roles);
        /* sauvegarde de l'utilisateur */
        userForRegister = userRepository.save(userForRegister);
        this.validationService.addValidation(userForRegister);

        return ResponseEntity.ok(new MessageEntity(HttpStatus.CREATED.value(),
                "Félicitation votre compte est créé. Un email vous a été envoyé. Veuillez vérifier votre boite de réception."));
    }

    // cette fonction ne met pas a jour le mot de passe ni l'email ni les roles
    public ResponseEntity<?> updateUser(UserRequest userRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User userImpl = (User) auth.getPrincipal();
        Optional<User> optionalUser = userRepository.findById(userImpl.getId());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Error: User not found.");
        }
        User user = optionalUser.get();
        user.setName(userRequest.getName());
        user.setFirstname(userRequest.getFirstname());
        user.setAdresse(userRequest.getAddress());
        user.setCity(userRequest.getCity());
        user.setPostalcode(userRequest.getPostalCode());
        user.setTelephone(userRequest.getPhone());
        user.setSiret(userRequest.getSiret());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageEntity(HttpStatus.CREATED.value(), "User updated successfully"));
    }

    public void deleteUser(User user) {
        userRepository.deleteById(user.getId());
    }

    public ResponseEntity<?> activation(Map<String, String> activation) {
        Validation validation = validationService.getValidationByCode(activation.get("code"));
        if (Instant.now().isAfter(validation.getExpired())) {
            throw new RuntimeException("Error: Validation code is expired.");
        }
        User UserForActivation = userRepository.findById(validation.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        UserForActivation.setActif(true);
        validation.setActivation(Instant.now());
        validationRepository.save(validation);
        userRepository.save(UserForActivation);
        return new ResponseEntity<>(
                new MessageEntity(HttpStatus.CREATED.value(), "Félicitation votre compte est activé"),
                HttpStatus.CREATED);
    }

    public boolean isValidUid(String uid) {
        this.validationRepository.findByUid(uid).ifPresent(validation -> {
            this.isValid = true;
        });
        return isValid;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public ResponseEntity<?> modifyPassword(Map<String, String> parameters) {
        User user = (User) this.loadUserByUsername(parameters.get("email"));
        return this.validationService.addValidation(user);
    }

    public ResponseEntity<?> newPassword(Map<String, String> parameters) {
        User user = (User) this.loadUserByUsername(parameters.get("email"));
        final Validation validation = validationService.getValidationByCode(parameters.get("code"));
        if (validation.getUser().getEmail().equals(user.getEmail())) {
            String passwordCrypte = this.encoder.encode(parameters.get("password"));
            user.setPassword(passwordCrypte);
            this.userRepository.save(user);
            validationService.deleteValidation(validation);
            return ResponseEntity.ok(
                    new MessageEntity(HttpStatus.CREATED.value(), "Félicitation votre mot de passe a été modifié"));
        }
        return new ResponseEntity<>(new MessageEntity(HttpStatus.BAD_REQUEST.value(), "Le code saisie est invalide."),
                HttpStatus.BAD_REQUEST);
    }
}
