package Mboussaid.laFactureFacile.Controllers.ControllerAdvice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ApplicationControllerAdvice {

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ BadCredentialsException.class })
    public @ResponseBody ProblemDetail handelBadCredentialsException(BadCredentialsException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        problemDetail.setProperty("ErrorMessage", "Nous n'avons pas trouvé votre compte ");
        return problemDetail;
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ RuntimeException.class })
    public @ResponseBody ProblemDetail handleRuntimeException(RuntimeException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        problemDetail.setProperty("ErrorMessage",
                "Nous n'avons pas trouvé votre compte ou n'est pas activé ! vérifier votre boite de réception");
        problemDetail.setProperty("ErrorActivation", "Votre code d'activation est incorrect ou a expiré !");
        problemDetail.setProperty("ErrorValidation", "Les informations fournies sont incorrectes");
        problemDetail.setProperty("ErrorEvent", "Les informations fournies sont incorrectes !");
        return problemDetail;
    }
    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ ConstraintViolationException.class })
    public @ResponseBody ProblemDetail constraintViolationException(ConstraintViolationException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        problemDetail.setProperty("ErrorFieldMissing", "Les informations fournies sont incorrectes ou incomplètes ! Veuillez vérifier les champs obligatoires");
        return problemDetail;
    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler({ UsernameNotFoundException.class })
    public @ResponseBody ProblemDetail UsernameNotFoundException(UsernameNotFoundException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        problemDetail.setProperty("ErrorForgetPassword", "Nous n'avons pas trouvé votre compte !");
        return problemDetail;
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler({ AccessDeniedException.class })
    public @ResponseBody ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(FORBIDDEN, e.getMessage());
        problemDetail.setProperty("ErrorMessage", "Vous n'avez pas les droits pour accéder à cette ressource");
        return problemDetail;
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({ Exception.class })
    public @ResponseBody ProblemDetail ExceptionsHandler(Exception e) {
        ApplicationControllerAdvice.log.error(e.getMessage(), e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, e.getMessage());
        problemDetail.setProperty("ErrorMessage", e.getMessage());
        return problemDetail;
    }

}