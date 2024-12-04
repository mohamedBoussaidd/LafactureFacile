package Mboussaid.laFactureFacile.Controllers.ControllerAdvice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ApplicationControllerAdvice {

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public @ResponseBody CustomResponseEntity<?> handelBadCredentialsException(BadCredentialsException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        CustomResponseEntity<?> customResponseEntity = CustomResponseEntity.error(UNAUTHORIZED.value(),
                "Nous n'avons pas trouvé votre compte !!!");
        return customResponseEntity;
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ RuntimeException.class, ConstraintViolationException.class })
    public @ResponseBody CustomResponseEntity<?> handleRuntimeException(RuntimeException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        CustomResponseEntity<?> customResponseEntity = CustomResponseEntity.error(UNAUTHORIZED.value(),
                "Les informations fournies sont incorrectes");
        return customResponseEntity;
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ Exception.class ,io.jsonwebtoken.ExpiredJwtException.class, JwtException.class, IllegalArgumentException.class})
    public @ResponseBody CustomResponseEntity<?> ExpiredJwtException(IllegalArgumentException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        CustomResponseEntity<?> customResponseEntity = CustomResponseEntity.error(UNAUTHORIZED.value(),
                "Votre session a expiré, veuillez vous reconnecter");
        return customResponseEntity;
    }

}
