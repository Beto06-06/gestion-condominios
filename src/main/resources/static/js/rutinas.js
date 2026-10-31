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
    const modalFinalizar = document.getElementById('finalizarAlquilerModal');
    if (modalFinalizar) {
        modalFinalizar.addEventListener('show.bs.modal', function (evento) {
            const boton = evento.relatedTarget;
            const identificador = document.getElementById('finalizarId');
            const fechaSalida = document.getElementById('fechaSalida');
            if (identificador.value !== boton.getAttribute('data-bs-id')) {
                fechaSalida.value = fechaSalida.getAttribute('data-hoy');
            }
            identificador.value = boton.getAttribute('data-bs-id');
            document.getElementById('finalizarDescripcion').textContent = boton.getAttribute('data-bs-descripcion');
            fechaSalida.min = boton.getAttribute('data-bs-inicio');
            document.getElementById('finalizarRetorno').value = boton.getAttribute('data-bs-retorno') || '';
        });
    }
    const modalAsignar = document.getElementById('asignarParqueoModal');
    if (modalAsignar) {
        modalAsignar.addEventListener('show.bs.modal', function (evento) {
            const boton = evento.relatedTarget;
            const identificador = document.getElementById('asignarParqueoId');
            if (identificador.value !== boton.getAttribute('data-bs-id')) {
                document.getElementById('asignarFilial').value = boton.getAttribute('data-bs-filial') || '';
            }
            identificador.value = boton.getAttribute('data-bs-id');
            document.getElementById('asignarParqueoDescripcion').textContent = boton.getAttribute('data-bs-descripcion');
        });
    }
    const modalLiberar = document.getElementById('liberarParqueoModal');
    if (modalLiberar) {
        modalLiberar.addEventListener('show.bs.modal', function (evento) {
            const boton = evento.relatedTarget;
            document.getElementById('liberarParqueoId').value = boton.getAttribute('data-bs-id');
            document.getElementById('liberarParqueoDescripcion').textContent = boton.getAttribute('data-bs-descripcion');
        });
    }
    const reabrirFinalizacion = document.getElementById('reabrirFinalizacion');
    if (reabrirFinalizacion) {
        reabrirFinalizacion.click();
    }

});
