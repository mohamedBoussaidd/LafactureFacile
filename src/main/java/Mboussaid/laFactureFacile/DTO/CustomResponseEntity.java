package Mboussaid.laFactureFacile.DTO;

public record CustomResponseEntity<T>(Integer code, String message, T data, boolean hasError) {

    public CustomResponseEntity{
        if(code < 200 || code > 599) {
            throw new IllegalArgumentException("Le code doit etre entre 200 et 599");
        }
    }

    public static <T> CustomResponseEntity<T> success (Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, false);
    }

    public static <T> CustomResponseEntity<T> success (Integer code, String message, T data) {
        return new CustomResponseEntity<>(code, message, data, false);
    }

    public static <T> CustomResponseEntity<T> error (Integer code, String message) {
        return new CustomResponseEntity<>(code, message, null, true);
    }
    
    public boolean isError() {
        return code >= 400;
    }
}
