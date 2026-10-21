package com.aparka.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Manejo de fotos de perfil, solo con la librería estándar de Java (ImageIO),
 * sin dependencias nuevas.
 *
 * - Acepta únicamente JPG y PNG (se comprueba decodificando la imagen, no por la extensión).
 * - Límite de 2 MB y de dimensiones, para evitar archivos abusivos.
 * - Recorta al centro en formato cuadrado y redimensiona a 400x400, para que todas las
 *   fotos se vean uniformes y formales sin importar cómo se tomaron.
 * - Siempre guarda como JPEG con nombre propio ("u{id}.jpg"): el nombre del archivo
 *   subido por el usuario nunca se usa, así no hay riesgo de rutas maliciosas.
 * - Se guarda FUERA de la carpeta de la aplicación (por defecto ~/aparka-uploads/perfiles)
 *   para que no se pierda al volver a desplegar el proyecto. Se puede cambiar con
 *   -Daparka.uploads=/otra/ruta
 */
public final class FotoPerfilUtil {

    public static final int LADO = 400;
    public static final long MAX_BYTES = 2L * 1024 * 1024; // 2 MB
    private static final int MAX_DIMENSION_ORIGEN = 6000;   // px por lado
    private static final Set<String> FORMATOS_PERMITIDOS = Set.of("jpeg", "png");

    private FotoPerfilUtil() {}

    /** Carpeta donde se guardan las fotos (se calcula en cada llamada para poder cambiarla en pruebas). */
    public static Path directorio() {
        String base = System.getProperty("aparka.uploads",
                System.getProperty("user.home") + "/aparka-uploads");
        return Paths.get(base, "perfiles");
    }

    public static String nombreArchivo(int usuarioId) {
        return "u" + usuarioId + ".jpg";
    }

    public static Path ruta(int usuarioId) {
        return directorio().resolve(nombreArchivo(usuarioId));
    }

    /**
     * Valida la imagen recibida, la recorta/redimensiona y la guarda como foto del usuario.
     *
     * @return el nombre del archivo guardado (para guardarlo en la BD)
     * @throws IllegalArgumentException si el archivo no es válido (el mensaje se puede mostrar al usuario)
     * @throws IOException              si falla el guardado en disco
     */
    public static String guardar(int usuarioId, InputStream entrada) throws IOException {
        byte[] jpeg = procesar(entrada);

        Path destino = ruta(usuarioId);
        Files.createDirectories(destino.getParent());
        Path temporal = Files.createTempFile(destino.getParent(), "tmp-", ".jpg");
        try {
            Files.write(temporal, jpeg);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
        return nombreArchivo(usuarioId);
    }

    /** Borra la foto del usuario si existe. */
    public static void eliminar(int usuarioId) throws IOException {
        Files.deleteIfExists(ruta(usuarioId));
    }

    /** Valida + recorta + redimensiona. Devuelve los bytes del JPEG final de LADO x LADO. */
    static byte[] procesar(InputStream entrada) throws IOException {
        byte[] datos = leerConLimite(entrada);

        BufferedImage original;
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(iis);
            if (!lectores.hasNext()) {
                throw new IllegalArgumentException("El archivo no es una imagen válida. Usa un JPG o PNG.");
            }
            ImageReader lector = lectores.next();
            try {
                String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!FORMATOS_PERMITIDOS.contains(formato)) {
                    throw new IllegalArgumentException("Formato no permitido. Usa una imagen JPG o PNG.");
                }
                lector.setInput(iis);
                // Se revisan las dimensiones ANTES de decodificar (evita imágenes "bomba")
                if (lector.getWidth(0) > MAX_DIMENSION_ORIGEN || lector.getHeight(0) > MAX_DIMENSION_ORIGEN) {
                    throw new IllegalArgumentException("La imagen es demasiado grande en píxeles. Usa una de hasta "
                            + MAX_DIMENSION_ORIGEN + " px por lado.");
                }
                original = lector.read(0);
            } finally {
                lector.dispose();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("No se pudo leer la imagen. Prueba con otro archivo JPG o PNG.");
        }

        BufferedImage cuadrada = recortarYEscalar(original);
        return aJpeg(cuadrada);
    }

    private static byte[] leerConLimite(InputStream entrada) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int leidos;
        while ((leidos = entrada.read(buffer)) != -1) {
            total += leidos;
            if (total > MAX_BYTES) {
                throw new IllegalArgumentException("La imagen supera el máximo de 2 MB.");
            }
            salida.write(buffer, 0, leidos);
        }
        if (total == 0) {
            throw new IllegalArgumentException("No seleccionaste ninguna imagen.");
        }
        return salida.toByteArray();
    }

    /** Recorta el cuadrado central y lo escala a LADO x LADO (fondo blanco si el PNG tenía transparencia). */
    private static BufferedImage recortarYEscalar(BufferedImage origen) {
        int lado = Math.min(origen.getWidth(), origen.getHeight());
        int x = (origen.getWidth() - lado) / 2;
        int y = (origen.getHeight() - lado) / 2;

        BufferedImage destino = new BufferedImage(LADO, LADO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, LADO, LADO);
            g.drawImage(origen, 0, 0, LADO, LADO, x, y, x + lado, y + lado, null);
        } finally {
            g.dispose();
        }
        return destino;
    }

    private static byte[] aJpeg(BufferedImage imagen) throws IOException {
        Iterator<ImageWriter> escritores = ImageIO.getImageWritersByFormatName("jpeg");
        if (!escritores.hasNext()) {
            throw new IOException("Este servidor no puede generar imágenes JPEG.");
        }
        ImageWriter escritor = escritores.next();
        ImageWriteParam param = escritor.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.9f);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(salida)) {
            escritor.setOutput(ios);
            escritor.write(null, new IIOImage(imagen, null, null), param);
        } finally {
            escritor.dispose();
        }
        return salida.toByteArray();
    }
}
