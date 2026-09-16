// Anima los elementos .animar-entrada con un pequeño retraso escalonado
document.addEventListener("DOMContentLoaded", () => {
    const elementos = document.querySelectorAll(".animar-entrada");
    elementos.forEach((el, index) => {
        el.style.animationDelay = `${index * 0.08}s`;
    });

    // Resalta el enlace de navegación activo según la URL actual
    const enlaces = document.querySelectorAll(".navbar-links a");
    enlaces.forEach((enlace) => {
        if (enlace.href === window.location.href) {
            enlace.style.color = "var(--acento)";
        }
    });

    // Cierra alertas automáticamente con un fundido suave
    const alertas = document.querySelectorAll(".alerta");
    alertas.forEach((alerta) => {
        setTimeout(() => {
            alerta.style.transition = "opacity 0.6s ease";
            alerta.style.opacity = "0";
        }, 4000);
    });
});
