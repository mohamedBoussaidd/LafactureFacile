package Mboussaid.laFactureFacile.Security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import Mboussaid.laFactureFacile.DTO.Request.InvoiceForSendEmailRequest;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Interface.CheckOwnerForData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.reflect.Field;

@Aspect
@Component
public class OwnerAspect {

    @PersistenceContext
    private EntityManager entityManager; // Utilisé pour charger dynamiquement l'entité

    @Before("@annotation(checkOwnerForData) && args(resource,..)")
    public void verifyOwnership(CheckOwnerForData checkOwnerForData, Object resource) throws Throwable {
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new SecurityException("Utilisateur non authentifié");
        }
        User authenticatedUser = (User) auth.getPrincipal();

        Object entity = null;
        if (resource instanceof Integer) {
            Class<?> entityClass = checkOwnerForData.entity();
            Integer resourceId = (Integer) resource;
            entity = entityManager.find(entityClass, (Integer) resourceId);
        }
        if (resource instanceof InvoiceForSendEmailRequest) {
            Class<?> entityClass = checkOwnerForData.entity();
            Integer resourceId = (Integer) ((InvoiceForSendEmailRequest) resource).id();
            entity = entityManager.find(entityClass, (Integer) resourceId);
        } else {
            entity = resource;
        }
        if (entity == null) {
            throw new IllegalArgumentException("Ressource introuvable");
        }

        // Vérifier la propriété de l'utilisateur
        Field ownerField = checkOwnerForData.entity().getDeclaredField("user");
        ownerField.setAccessible(true); // Accéder à la propriété privée
        Object owner = ownerField.get(entity);

        if (!authenticatedUser.getId().equals(((User) owner).getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à cette ressource");
        }
    }
}
