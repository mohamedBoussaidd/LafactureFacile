package Mboussaid.laFactureFacile.Services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import Mboussaid.laFactureFacile.Models.GetDate;
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

    public ResponseEntity<?> saveExistingUser(User userBdd, UserRequest user) {
        if (userBdd.isActif()) {
            return ResponseEntity.badRequest().body("Votre email est déja utiliser !!");
        } else {
            userBdd.setName(user.getName());
            userBdd.setPassword(this.encoder.encode(user.getPassword()));
            this.userRepository.save(userBdd);
            Optional<Validation> OptionalValidation = this.validationRepository.findByUser(userBdd);
            if (OptionalValidation.isPresent()) {
                Validation validation = OptionalValidation.get();
                if (GetDate.getNow().isAfter(validation.getExpired())) {
                    return this.validationService.addValidation(userBdd);
                } else {
                    return ResponseEntity.badRequest()
                            .body("Votre email est déja utiliser.Vérifier vos email pour activer votre compte !!");
                }
            }
            return this.validationService.addValidation(userBdd);
        }
    }

    public ResponseEntity<?> save(UserRequest user) {
        Optional<User> optionUserBdd = this.userRepository.findByEmail(user.getEmail());
        /* Vérification avant enregistrement */
        if (optionUserBdd.isPresent()) {
            User userBdd = optionUserBdd.get();
            return this.saveExistingUser(userBdd, user);
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
        if(validation.getActivation() != null) {
            throw new RuntimeException("Votre compte est déja activé !!");
        }
        if (GetDate.getNow().isAfter(validation.getExpired())) {
            throw new RuntimeException("Le code de validation a expirer. Veuillez refaire votre inscription");
        }
        User UserForActivation = userRepository.findById(validation.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        UserForActivation.setActif(true);
        validation.setActivation(GetDate.getNow());
        validationRepository.save(validation);
        userRepository.save(UserForActivation);
        return new ResponseEntity<>(
                new MessageEntity(HttpStatus.CREATED.value(), "Félicitation votre compte est activé"),
                HttpStatus.CREATED);
    }

    public boolean isValidUid(String uid) {
        return this.validationRepository.findByUid(uid).isPresent();
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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
