package Mboussaid.laFactureFacile.DTO;

/* 
 * CustomResponseEntity.java
 * Gerer les reponses de l'API
 * syncronisation avec le front-end
 * Se constuire avec des methodes statiques custom pour plus de clarté gere aussi l'affichage ou non des notifications
 * @param <T> : type de la data
 * @param code : code de la reponse
 * @param message : message de la reponse
 * @param data : data de la reponse
 * @param hasError : presence d'une erreur
 * @param hasSuccess : presence d'une reponse reussie
 * @param displayNotification : affichage de la notification
 */
public record CustomResponseEntity<T>(Integer code, String message, T data, boolean hasError, boolean hasSuccess,
        boolean displayNotification) {

    public CustomResponseEntity {
        if (code < 200 || code > 599) {
            throw new IllegalArgumentException("Le code doit etre entre 200 et 599");
        }
        if (message == null) {
            throw new IllegalArgumentException("Le message ne doit pas être null");
        }
        if (hasError && hasSuccess) {
            throw new IllegalArgumentException("Une réponse ne peut pas être une erreur et un succès en même temps");
        }
        if (hasError && (code < 400 || code > 599)) {
            throw new IllegalArgumentException("Une réponse d'erreur doit avoir un code entre 400 et 599");
        }
        if (hasSuccess && (code < 200 || code > 299)) {
            throw new IllegalArgumentException("Une réponse de succès doit avoir un code entre 200 et 299");
        }
    }

    /*
     * Methodes statiques custom
     * 
     * @param code : code de la reponse
     * 
     * @param message : message de la reponse
     * successWithoutDataDisplayed : reponse sans data et avec affichage de la
     * notification
     */
    public static <T> CustomResponseEntity<T> successWithoutDataDisplayed(Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, false, true, true);
    }

    /*
     * Methodes statiques custom
     * 
     * @param code : code de la reponse
     * 
     * @param message : message de la reponse
     * successWithoutDataHidden : reponse sans data et sans affichage de la
     * notification
     */
    public static <T> CustomResponseEntity<T> successWithoutDataHidden(Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, false, true, false);
    }

    /*
     * Methodes statiques custom
     * 
     * @param code : code de la reponse
     * 
     * @param message : message de la reponse
     * 
     * @param data : data de la reponse
     * successWithDataDisplayed : reponse avec data et avec affichage de la
     * notification
     */
    public static <T> CustomResponseEntity<T> successWithDataDisplayed(Integer code, String message, T data) {
        return new CustomResponseEntity<>(code, message, data, false, true, true);
    }

    /*
     * Methodes statiques custom
     * 
     * @param code : code de la reponse
     * 
     * @param message : message de la reponse
     * 
     * @param data : data de la reponse
     * successWithDataHidden : reponse avec data et sans affichage de la
     * notification
     */
    public static <T> CustomResponseEntity<T> successWithDataHidden(Integer code, String message, T data) {
        return new CustomResponseEntity<>(code, message, data, false, true, false);
    }

    /*
     * Methodes statiques custom
     * 
     * @param code : code de la reponse
     * 
     * @param message : message de la reponse
     * error : reponse d'erreur
     */
    public static <T> CustomResponseEntity<T> error(Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, true, false, true);
    }

    public boolean isError() {
        return code >= 400;
    }
}
