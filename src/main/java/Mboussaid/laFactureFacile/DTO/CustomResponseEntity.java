package Mboussaid.laFactureFacile.DTO;

public record CustomResponseEntity<T>(Integer code, String message, T data, boolean hasError, boolean hasSuccess, boolean displayNotification) {

    public CustomResponseEntity{
        if(code < 200 || code > 599) {
            throw new IllegalArgumentException("Le code doit etre entre 200 et 599");
        }
    }

    public static <T> CustomResponseEntity<T> successWithoutDataDisplayed (Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, false,true, true);
    }
    public static <T> CustomResponseEntity<T> successWithoutDataHidden (Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, false,true, false);
    }

    public static <T> CustomResponseEntity<T> successWithDataDisplayed (Integer code, String message, T data) {
        return new CustomResponseEntity<>(code, message, data, false, true, true);
    }
    public static <T> CustomResponseEntity<T> successWithDataHidden (Integer code, String message, T data) {
        return new CustomResponseEntity<>(code, message, data, false, true, false);
    }

    public static <T> CustomResponseEntity<T> error (Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, true,false, true);
    }
    
    public boolean isError() {
        return code >= 400;
    }
}
