// Gestión de zonas y cajeros: diálogos de crear/editar y selector de ubicación sobre el plano.
document.addEventListener("DOMContentLoaded", () => {
    const $ = (id) => document.getElementById(id);

    // Selector de ubicación: muestra el plano del nivel elegido y guarda el clic como % del ancho/alto
    function crearSelector(c) {
        const nivel = $(c.nivel), x = $(c.x), y = $(c.y), bloque = $(c.bloque);
        const img = $(c.img), marcador = $(c.marcador), picker = $(c.picker);

        function pintar() {
            const tiene = x.value !== "" && y.value !== "";
            marcador.hidden = !tiene;
            if (tiene) { marcador.style.left = x.value + "%"; marcador.style.top = y.value + "%"; }
        }
        function actualizar() {
            bloque.hidden = !nivel.value;
            if (nivel.value) img.src = img.dataset.base + nivel.value + ".png";
            pintar();
        }
        nivel.addEventListener("change", () => { x.value = ""; y.value = ""; actualizar(); });
        picker.addEventListener("click", (e) => {
            const r = img.getBoundingClientRect();
            if (!r.width || !r.height) return;
            x.value = (((e.clientX - r.left) / r.width) * 100).toFixed(2);
            y.value = (((e.clientY - r.top) / r.height) * 100).toFixed(2);
            pintar();
        });
        return { actualizar };
    }

    // ---------- Zonas ----------
    const dlgZona = $("dlgZona");
    if (dlgZona) {
        const selZona = crearSelector({ nivel: "fNivel", x: "fX", y: "fY", bloque: "pickerBloque", img: "pickerImg", marcador: "pickerMarcador", picker: "picker" });
        function abrirZona(d) {
            $("fAccion").value = d ? "editar" : "crear";
            $("dlgTitulo").textContent = d ? "Editar zona" : "Nueva zona";
            $("fId").value = d ? d.id : "";
            $("fNombre").value = d ? d.nombre : "";
            $("fUbicacion").value = d ? d.ubicacion : "";
            $("fCapacidad").value = d ? d.capacidad : "";
            $("fTarifa").value = d ? d.tarifa : "";
            $("fNivel").value = d ? (d.nivel || "") : "";
            $("fX").value = d ? (d.x || "") : "";
            $("fY").value = d ? (d.y || "") : "";
            selZona.actualizar();
            dlgZona.showModal();
            $("fNombre").focus();
        }
        $("btnNuevaZona").addEventListener("click", () => abrirZona(null));
        document.querySelectorAll(".gz-editar").forEach((b) => b.addEventListener("click", () => abrirZona(b.dataset)));
        $("btnCancelarZona").addEventListener("click", () => dlgZona.close());
    }

    // ---------- Cajeros ----------
    const dlgCaja = $("dlgCaja");
    if (dlgCaja) {
        const selCaja = crearSelector({ nivel: "cNivel", x: "cX", y: "cY", bloque: "cPickerBloque", img: "cPickerImg", marcador: "cMarcador", picker: "cPicker" });
        function abrirCaja(d) {
            $("cAccion").value = d ? "editar" : "crear";
            $("cajaTitulo").textContent = d ? "Editar cajero" : "Nuevo cajero";
            $("cId").value = d ? d.id : "";
            $("cId").readOnly = !!d; // el identificador no cambia: lo referencian las ventas
            $("cTipo").value = d ? d.tipo : "";
            $("cUbicacion").value = d ? d.ubicacion : "";
            $("cNivel").value = d ? (d.nivel || "") : "";
            $("cX").value = d ? (d.x || "") : "";
            $("cY").value = d ? (d.y || "") : "";
            selCaja.actualizar();
            dlgCaja.showModal();
            (d ? $("cTipo") : $("cId")).focus();
        }
        $("btnNuevaCaja").addEventListener("click", () => abrirCaja(null));
        document.querySelectorAll(".gz-editar-caja").forEach((b) => b.addEventListener("click", () => abrirCaja(b.dataset)));
        $("btnCancelarCaja").addEventListener("click", () => dlgCaja.close());
    }
});
