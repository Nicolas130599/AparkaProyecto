
package com.aparka.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter(urlPatterns = {
        "/admin/zonas",
        "/admin/cajas",
        "/admin/entradas",
        "/admin/salidas",
        "/perfil",
        "/perfil/foto",
        "/reserva"
})
public class CsrfFilter implements Filter {

    public static final String TOKEN_SESION = "csrfToken";
    public static final String TOKEN_PARAMETRO = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        if ("POST".equalsIgnoreCase(request.getMethod())) {

            String esperado = session == null
                    ? null
                    : (String) session.getAttribute(TOKEN_SESION);

            String recibido = request.getParameter(TOKEN_PARAMETRO);

            if (!coinciden(esperado, recibido)) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Solicitud rechazada por seguridad CSRF"
                );
                return;
            }

        } else if ("GET".equalsIgnoreCase(request.getMethod())
                && session != null
                && session.getAttribute("usuario") != null) {

            synchronized (session) {
                if (session.getAttribute(TOKEN_SESION) == null) {
                    byte[] bytes = new byte[32];
                    RANDOM.nextBytes(bytes);

                    String token = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(bytes);

                    session.setAttribute(TOKEN_SESION, token);
                }
            }
        }

        chain.doFilter(req, res);
    }

    private boolean coinciden(String esperado, String recibido) {
        if (esperado == null || recibido == null) {
            return false;
        }

        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8)
        );
    }
}
