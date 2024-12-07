package Mboussaid.laFactureFacile.Models.Interface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Utilisation sur les méthodes
@Retention(RetentionPolicy.RUNTIME) // Disponible à l'exécution
public @interface CheckOwnerForData {
    Class<?> entity(); // Classe de l'entité (ex: Invoice.class)
    String idField() default "id"; // Champ contenant l'identifiant à vérifier (par défaut "id")
}
