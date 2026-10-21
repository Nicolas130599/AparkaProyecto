package com.aparka.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la validación, recorte y guardado de fotos de perfil. */
class FotoPerfilUtilTest {

    @TempDir
    Path carpeta;

    @BeforeEach
    void usarCarpetaTemporal() {
        System.setProperty("aparka.uploads", carpeta.toString());
    }

    private static byte[] imagen(int ancho, int alto, String formato) throws IOException {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, formato, out);
        return out.toByteArray();
    }

    @Test
    void guardar_conPngRectangular_generaJpegCuadradoDe400() throws IOException {
        String nombre = FotoPerfilUtil.guardar(7, new ByteArrayInputStream(imagen(1200, 600, "png")));

        assertEquals("u7.jpg", nombre);
        BufferedImage guardada = ImageIO.read(FotoPerfilUtil.ruta(7).toFile());
        assertEquals(FotoPerfilUtil.LADO, guardada.getWidth());
        assertEquals(FotoPerfilUtil.LADO, guardada.getHeight());
    }

    @Test
    void guardar_reemplazaLaFotoAnterior() throws IOException {
        FotoPerfilUtil.guardar(3, new ByteArrayInputStream(imagen(500, 500, "png")));
        FotoPerfilUtil.guardar(3, new ByteArrayInputStream(imagen(800, 900, "jpg")));

        try (var archivos = Files.list(FotoPerfilUtil.directorio())) {
            assertEquals(1, archivos.count(), "No deben quedar archivos temporales ni duplicados");
        }
    }

    @Test
    void guardar_conArchivoQueNoEsImagen_lanzaExcepcion() {
        byte[] texto = "esto no es una imagen".getBytes();
        assertThrows(IllegalArgumentException.class,
                () -> FotoPerfilUtil.guardar(1, new ByteArrayInputStream(texto)));
    }

    @Test
    void guardar_conFormatoNoPermitido_lanzaExcepcion() throws IOException {
        byte[] gif = imagen(100, 100, "gif");
        assertThrows(IllegalArgumentException.class,
                () -> FotoPerfilUtil.guardar(1, new ByteArrayInputStream(gif)));
    }

    @Test
    void guardar_conArchivoVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> FotoPerfilUtil.guardar(1, new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void guardar_conArchivoMayorA2MB_lanzaExcepcion() {
        byte[] grande = new byte[(int) FotoPerfilUtil.MAX_BYTES + 1];
        assertThrows(IllegalArgumentException.class,
                () -> FotoPerfilUtil.guardar(1, new ByteArrayInputStream(grande)));
    }

    @Test
    void eliminar_borraLaFotoSiExiste() throws IOException {
        FotoPerfilUtil.guardar(5, new ByteArrayInputStream(imagen(400, 400, "png")));
        assertTrue(Files.exists(FotoPerfilUtil.ruta(5)));

        FotoPerfilUtil.eliminar(5);

        assertFalse(Files.exists(FotoPerfilUtil.ruta(5)));
        assertDoesNotThrow(() -> FotoPerfilUtil.eliminar(5)); // borrar dos veces no falla
    }
}
