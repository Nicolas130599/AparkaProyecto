
package com.aparka.util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Locale;

/**
 * Validaciones reutilizables de formularios realizadas en el backend.
 *
 * Bibliotecas utilizadas:
 * - Apache Commons Lang3: validaciones de cadenas.
 * - Apache Commons Validator: validación de correos electrónicos.
 * - Google Guava: validaciones defensivas.
 */
public class ValidacionUtil {

    /**
     * Verifica que un texto no sea nulo, vacío o solo espacios.
     */
    public static void requerirTextoNoVacio(
            String valor, String nombreCampo) {

        Preconditions.checkArgument(
                StringUtils.isNotBlank(valor),
                "El campo '%s' no puede estar vacío.",
                nombreCampo
        );
    }

    /**
     * Comprueba el formato de un correo electrónico.
     */
    public static boolean esCorreoValido(String email) {

        if (StringUtils.isBlank(email)) {
            return false;
        }

        return EmailValidator.getInstance().isValid(email);
    }

    /**
     * Valida placas alfanuméricas flexibles.
     *
     * Reglas:
     * - Entre 5 y 7 caracteres.
     * - Letras y números.
     * - Un guion opcional entre grupos.
     * - No permite espacios internos ni símbolos especiales.
     * - Compatible con Entrada.Placa CHAR(7).
     *
     * Ejemplos válidos:
     * ABC-123, ABC123, 1234AB, A1B234.
     */
    public static boolean esPlacaValida(String placa) {

        if (StringUtils.isBlank(placa)) {
            return false;
        }

        String limpia = placa.trim().toUpperCase(Locale.ROOT);

        return limpia.matches(
                "(?=.{5,7}$)[A-Z0-9]+(?:-[A-Z0-9]+)?"
        );
    }

    /**
     * Comprueba que una contraseña tenga la longitud mínima.
     */
    public static boolean esPasswordValida(
            String password, int largoMinimo) {

        Preconditions.checkArgument(
                largoMinimo > 0,
                "El largo mínimo debe ser mayor a 0."
        );

        return StringUtils.isNotBlank(password)
                && password.length() >= largoMinimo;
    }

    /**
     * Valida números telefónicos.
     *
     * Permite:
     * - Dígitos.
     * - Espacios.
     * - Signo +.
     * - Guiones.
     * - Paréntesis.
     *
     * Requiere entre 7 y 15 dígitos.
     */
    public static boolean esTelefonoValido(String telefono) {

        if (StringUtils.isBlank(telefono)) {
            return false;
        }

        String t = telefono.trim();

        if (t.length() > 20
                || !t.matches("[+0-9 ()\\-]+")) {
            return false;
        }

        long digitos = t.chars()
                .filter(Character::isDigit)
                .count();

        return digitos >= 7 && digitos <= 15;
    }
}
