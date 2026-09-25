package com.aparka.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Validaciones reutilizables de formularios, hechas en el backend (no solo en el HTML),
 * usando librerías de apoyo en vez de reinventar estas comprobaciones a mano:
 * - Apache Commons Lang3 (StringUtils) para verificar textos vacíos/en blanco.
 * - Apache Commons Validator para validar el FORMATO de un correo electrónico.
 * - Google Guava (Preconditions) para validaciones defensivas tipo "fail fast".
 */
public class ValidacionUtil {

    /** Lanza IllegalArgumentException si el texto es null, vacío o solo espacios. */
    public static void requerirTextoNoVacio(String valor, String nombreCampo) {
        Preconditions.checkArgument(
                StringUtils.isNotBlank(valor),
                "El campo '%s' no puede estar vacío.", nombreCampo
        );
    }

    /** Verifica que el correo tenga un formato válido (usuario@dominio.com). */
    public static boolean esCorreoValido(String email) {
        if (StringUtils.isBlank(email)) return false;
        return EmailValidator.getInstance().isValid(email);
    }

    /** Verifica que la placa no esté vacía y tenga una longitud razonable (entre 5 y 8 caracteres). */
    public static boolean esPlacaValida(String placa) {
        if (StringUtils.isBlank(placa)) return false;
        String limpia = StringUtils.trim(placa);
        return limpia.length() >= 5 && limpia.length() <= 8;
    }

    /** Verifica que una contraseña tenga al menos el largo mínimo exigido. */
    public static boolean esPasswordValida(String password, int largoMinimo) {
        Preconditions.checkArgument(largoMinimo > 0, "El largo mínimo debe ser mayor a 0.");
        return StringUtils.isNotBlank(password) && password.length() >= largoMinimo;
    }
}
