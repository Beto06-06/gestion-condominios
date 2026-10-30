// Cargo los datos del registro antes de confirmar la accion.
document.addEventListener('DOMContentLoaded', function () {
    const modalConfirmacion = document.getElementById('confirmModal');
    if (modalConfirmacion) {
        modalConfirmacion.addEventListener('show.bs.modal', function (evento) {
            const boton = evento.relatedTarget;
            document.getElementById('modalId').value = boton.getAttribute('data-bs-id');
            document.getElementById('modalDescripcion').textContent = boton.getAttribute('data-bs-descripcion');
        });
    }
});
