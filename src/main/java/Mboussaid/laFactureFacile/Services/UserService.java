package Mboussaid.laFactureFacile.Services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.DTO.Request.UserRequest;
import Mboussaid.laFactureFacile.DTO.Response.UserDTO;
import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Models.Role;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Validation;
import Mboussaid.laFactureFacile.Models.ENUM.ERole;
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

    public CustomResponseEntity<?> getUserById(Integer id) {

        Optional<User> userBdd = userRepository.findById(id);
        if (userBdd.isPresent()) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userBdd.get().getId());
            userDTO.setName(userBdd.get().getName());
            userDTO.setFirstname(userBdd.get().getFirstname());
            userDTO.setEmail(userBdd.get().getEmail());
            userDTO.setAdresse(userBdd.get().getAdresse());
            userDTO.setCity(userBdd.get().getCity());
            userDTO.setPostalCode(userBdd.get().getPostalcode());
            userDTO.setSiret(userBdd.get().getSiret());
            userDTO.setTelephone(userBdd.get().getTelephone());
            return CustomResponseEntity.successWithDataHidden(HttpStatus.ACCEPTED.value(), "Utilisateur trouvé", userDTO);
        }
        return CustomResponseEntity.error(HttpStatus.FORBIDDEN.value(), "Utilisateur non trouvé");
    }

    public CustomResponseEntity<?> saveExistingUser(User userBdd, UserRequest user) {
        if (userBdd.isActif()) {
            return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),"Votre email est déja utiliser !!");
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
                    return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),"Votre email est déja utiliser.Vérifier vos email pour activer votre compte !!");
                }
            }
            return this.validationService.addValidation(userBdd);
        }
    }

    public CustomResponseEntity<?> save(UserRequest user) {
        Optional<User> optionUserBdd = this.userRepository.findByEmail(user.getEmail());
        /* Vérification avant enregistrement */
        if (optionUserBdd.isPresent()) {
            User userBdd = optionUserBdd.get();
            return this.saveExistingUser(userBdd, user);
        }
        /* creation de l'utilisateur a ajouter en bdd */
        User userForRegister = new User();
        userForRegister.setName(StringUtils.capitalizeFirstLetter(user.getName()));
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

        return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.CREATED.value(),
                "Félicitation votre compte est créé. Un email vous a été envoyé. Veuillez vérifier votre boite de réception.");
    }

    // cette fonction ne met pas a jour le mot de passe ni l'email ni les roles
    public CustomResponseEntity<?> updateUser(UserRequest userRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User userImpl = (User) auth.getPrincipal();
        Optional<User> optionalUser = userRepository.findById(userImpl.getId());
        if (optionalUser.isEmpty()) {
            return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),"Un probleme est survenu lors de la mise à jour de votre profil !!");
        }
        User user = optionalUser.get();
        user.setName(StringUtils.capitalizeFirstLetter(userRequest.getName()));
        user.setFirstname(StringUtils.capitalizeFirstLetter(userRequest.getFirstname()));
        user.setAdresse(userRequest.getAddress());
        user.setCity(userRequest.getCity());
        user.setPostalcode(userRequest.getPostalCode());
        user.setTelephone(userRequest.getPhone());
        user.setSiret(userRequest.getSiret());
        userRepository.save(user);
        return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.CREATED.value(), "Votre profil a été mis à jour");
    }

    public void deleteUser(User user) {
        userRepository.deleteById(user.getId());
    }

    public CustomResponseEntity<?> activation(Map<String, String> activation) {
        Validation validation = validationService.getValidationByCode(activation.get("code"));
        if(validation.getActivation() != null) {
            CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),"Votre compte est déja activé !!");
        }
        if (GetDate.getNow().isAfter(validation.getExpired())) {
            CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),"Le code de validation a expirer. Veuillez refaire votre inscription");
        }
        User UserForActivation = userRepository.findById(validation.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        UserForActivation.setActif(true);
        validation.setActivation(GetDate.getNow());
        validationRepository.save(validation);
        userRepository.save(UserForActivation);
        return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.CREATED.value(), "Félicitation votre compte est activé");
    }

    public boolean isValidUid(String uid) {
        return this.validationRepository.findByUid(uid).isPresent();
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public CustomResponseEntity<?> modifyPassword(Map<String, String> parameters) {
        User user = (User) this.loadUserByUsername(parameters.get("email"));
        return this.validationService.addValidation(user);
    }

    public CustomResponseEntity<?> newPassword(Map<String, String> parameters) {
        User user = (User) this.loadUserByUsername(parameters.get("email"));
        final Validation validation = validationService.getValidationByCode(parameters.get("code"));
        if (validation.getUser().getEmail().equals(user.getEmail())) {
            String passwordCrypte = this.encoder.encode(parameters.get("password"));
            user.setPassword(passwordCrypte);
            this.userRepository.save(user);
            validationService.deleteValidation(validation);
            return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.CREATED.value(),
                            "Félicitation votre mot de passe a été modifié");
        }
        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(), "Le code saisie est invalide.");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
