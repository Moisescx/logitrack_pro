// ==========================================
// 1. CHAT DEL CHOFER (COPILOTO IA)
// ==========================================
function toggleChat() {
    const chatWindow = document.getElementById('chat-window');
    if (chatWindow.classList.contains('hidden')) {
        chatWindow.classList.remove('hidden');
        chatWindow.classList.add('flex');
    } else {
        chatWindow.classList.add('hidden');
        chatWindow.classList.remove('flex');
    }
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

async function sendMessage() {
    const inputField = document.getElementById('chat-input');
    const messageText = inputField.value.trim();
    if (!messageText) return;

    const messagesContainer = document.getElementById('chat-messages');

    const userMessageHTML = `
        <div class="self-end bg-blue-600 text-white p-3 rounded-tl-xl rounded-br-xl rounded-bl-xl max-w-[85%] text-sm shadow-sm">
            ${messageText}
        </div>
    `;
    messagesContainer.insertAdjacentHTML('beforeend', userMessageHTML);
    inputField.value = '';
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    const typingId = 'typing-' + Date.now();
    const typingHTML = `
        <div id="${typingId}" class="self-start bg-gray-200 dark:bg-slate-700 text-gray-500 dark:text-gray-400 p-3 rounded-tr-xl rounded-br-xl rounded-bl-xl max-w-[85%] text-sm italic shadow-sm">
            Pensando...
        </div>
    `;
    messagesContainer.insertAdjacentHTML('beforeend', typingHTML);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    try {
        const response = await fetch('/api/chat/chofer', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({mensaje: messageText})
        });

        const data = await response.json();
        document.getElementById(typingId).remove();

        const aiMessageHTML = `
            <div class="self-start bg-gray-200 dark:bg-slate-700 text-gray-800 dark:text-gray-100 p-3 rounded-tr-xl rounded-br-xl rounded-bl-xl max-w-[85%] text-sm shadow-sm whitespace-pre-wrap">${data.respuesta.trim()}</div>
        `;
        messagesContainer.insertAdjacentHTML('beforeend', aiMessageHTML);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

    } catch (error) {
        document.getElementById(typingId).remove();
        const errorHTML = `
            <div class="self-start bg-red-100 text-red-700 p-3 rounded-tr-xl rounded-br-xl rounded-bl-xl max-w-[85%] text-sm shadow-sm">
                ⚠️ Error de conexión. El copiloto no está disponible.
            </div>
        `;
        messagesContainer.insertAdjacentHTML('beforeend', errorHTML);
        console.error('Error en el chat:', error);
    }
}

// ==========================================
// 2. LÓGICA DE VIAJES Y TEMPORIZADORES
// ==========================================
const preStartIntervals = {};
const tripIntervals = {};
const tripPaused = {};
const tripRemaining = {};
let rutaEnEmergenciaActual = null;

function startTrip(routeId) {
    const seconds = 5;
    const countdown = document.getElementById('countdown-' + routeId);
    const startBtn = document.getElementById('start-btn-' + routeId);
    if (startBtn) startBtn.disabled = true;

    let remaining = seconds;
    if (countdown) countdown.textContent = remaining + 's...';

    preStartIntervals[routeId] = setInterval(() => {
        remaining -= 1;
        if (remaining <= 0) {
            clearInterval(preStartIntervals[routeId]);
            if (countdown) countdown.textContent = 'Iniciando...';
            try {
                sessionStorage.setItem('visible_in_progress', routeId);
            } catch (e) {
            }

            const url = '/api/rutas/iniciar/' + routeId;
            fetch(url, {
                method: 'GET',
                headers: {'Content-Type': 'application/json'}
            }).then(resp => {
                if (resp.ok) {
                    window.location.reload();
                } else {
                    if (countdown) countdown.textContent = 'Error al iniciar';
                    if (startBtn) startBtn.disabled = false;
                }
            }).catch(err => {
                if (countdown) countdown.textContent = 'Error de red';
                if (startBtn) startBtn.disabled = false;
            });
        } else {
            if (countdown) countdown.textContent = remaining + 's...';
        }
    }, 1000);
}

function finalizeTrip(routeId) {
    if (!(typeof tripRemaining[routeId] !== 'undefined' && tripRemaining[routeId] <= 0)) return;
    try {
        sessionStorage.removeItem('visible_in_progress');
    } catch (e) {
    }

    const url = '/api/rutas/finalizar/' + routeId;
    fetch(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    }).then(resp => {
        if (resp.ok) {
            window.location.reload();
        } else {
            console.error('Finalize failed', resp.status);
        }
    }).catch(err => console.error(err));
}

function startTripCountdown(routeId) {
    // 1. Conectamos con el HTML
    const countdown = document.getElementById('countdown-' + routeId); // El de la tarjeta pendiente
    const barraProgreso = document.getElementById('barra-progreso-' + routeId); // La nueva barra
    const textoEta = document.getElementById('texto-eta-' + routeId); // El nuevo texto de ETA

    const TIEMPO_TOTAL_VIAJE = 60; // Segundos que dura el viaje (Cámbialo para probar)

    if (typeof tripRemaining[routeId] === 'undefined') {
        tripRemaining[routeId] = TIEMPO_TOTAL_VIAJE;
    }

    // Si ya terminó o recargó la página y el tiempo era 0
    if (tripRemaining[routeId] <= 0) {
        activarBotonFinalizar(routeId);
        if (textoEta) textoEta.textContent = 'Llegada a destino ✅';
        if (barraProgreso) {
            barraProgreso.style.width = '100%';
            barraProgreso.classList.replace('bg-indigo-600', 'bg-emerald-500'); // Se pone verde al llegar
        }
        return;
    }

    if (tripIntervals[routeId]) clearInterval(tripIntervals[routeId]);

    // 2. El motor del tiempo
    tripIntervals[routeId] = setInterval(() => {
        if (tripPaused[routeId]) return; // Si hay emergencia, se congela la barra

        tripRemaining[routeId] = Math.max(0, tripRemaining[routeId] - 1);

        // 3. Matemáticas visuales
        if (barraProgreso && textoEta) {
            // Calcular porcentaje: (Tiempo transcurrido / Tiempo Total) * 100
            let porcentaje = ((TIEMPO_TOTAL_VIAJE - tripRemaining[routeId]) / TIEMPO_TOTAL_VIAJE) * 100;
            barraProgreso.style.width = porcentaje + '%';

            // Formatear texto a "1m 15s"
            let minutos = Math.floor(tripRemaining[routeId] / 60);
            let segundos = tripRemaining[routeId] % 60;
            textoEta.textContent = `Llegada en: ${minutos}m ${segundos}s`;

            // Efecto visual: si falta menos del 15%, la barra se pone verde
            if (porcentaje > 85) {
                barraProgreso.classList.replace('bg-indigo-600', 'bg-emerald-500');
            }
        }

        // 4. ¿Llegamos al destino?
        if (tripRemaining[routeId] <= 0) {
            clearInterval(tripIntervals[routeId]);
            tripIntervals[routeId] = null;
            if (textoEta) textoEta.textContent = 'Llegada a destino ✅';
            activarBotonFinalizar(routeId);
        }
    }, 1000);
}

// Función auxiliar para mantener el código limpio
function activarBotonFinalizar(routeId) {
    const finalizeBtn = document.getElementById('finalize-btn-' + routeId);
    if (finalizeBtn) {
        finalizeBtn.disabled = false;
        finalizeBtn.classList.remove('opacity-50', 'cursor-not-allowed', 'bg-gray-400');
        finalizeBtn.classList.add('bg-green-600', 'hover:bg-green-700'); // Botón verde brillante listo para tocar
    }
}

// ==========================================
// 3. LÓGICA DE EMERGENCIAS (WebSockets + Modal)
// ==========================================
let stompClient = null;

function conectarWebSocket() {
    let socket = new SockJS('/ws/location');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function (frame) {
        console.log('Túnel conectado para emergencias');
    });
}

function abrirModalEmergencia(routeId) {
    tripPaused[routeId] = true;
    rutaEnEmergenciaActual = routeId;
    document.getElementById('modal-emergencia').classList.remove('hidden');
}

function cerrarModalEmergencia() {
    document.getElementById('modal-emergencia').classList.add('hidden');
    if (rutaEnEmergenciaActual) {
        tripPaused[rutaEnEmergenciaActual] = false;
        rutaEnEmergenciaActual = null;
    }
}

function reanudarRecorrido(routeId) {
    tripPaused[routeId] = false;
    const btn = document.getElementById('emergency-btn-' + routeId);
    if (btn) {
        btn.textContent = 'Emergencia';
        btn.classList.remove('bg-yellow-500');
        btn.classList.add('bg-red-600');
        btn.disabled = false;
    }
}

function enviarAlerta(tipoProblema) {
    if (stompClient && rutaEnEmergenciaActual) {
        const alerta = {
            chofer: "Chofer",
            patente: "Ruta-" + rutaEnEmergenciaActual,
            tipoProblema: tipoProblema
        };

        stompClient.send("/app/alerta", {}, JSON.stringify(alerta));

        const btn = document.getElementById('emergency-btn-' + rutaEnEmergenciaActual);
        if (btn) {
            btn.textContent = 'Reanudar Recorrido';
            btn.classList.remove('bg-red-600');
            btn.classList.add('bg-yellow-500');
            btn.disabled = false;
        }

        document.getElementById('modal-emergencia').classList.add('hidden');
        alert("🚨 Alerta enviada a la central.");
    } else {
        alert("Error de conexión. Intentando reconectar...");
        conectarWebSocket();
    }
}

// ==========================================
// 4. INICIALIZACIÓN DE LA PÁGINA
// ==========================================
window.addEventListener('DOMContentLoaded', () => {
    conectarWebSocket();

    document.querySelectorAll('.finalize-btn').forEach(btn => {
        const id = btn.getAttribute('data-route-id');
        tripPaused[id] = false;
        const startIso = btn.getAttribute('data-start-time');

        if (startIso) {
            let isoToParse = startIso;
            const hasTimezone = isoToParse.endsWith('Z') || isoToParse.includes('+') || isoToParse.substring(10).includes('-');
            if (!hasTimezone) isoToParse += 'Z';

            const startMs = Date.parse(isoToParse);
            if (!isNaN(startMs)) {
                const elapsedFloat = (Date.now() - startMs) / 1000;
                tripRemaining[id] = Math.max(0, Math.ceil(60 - elapsedFloat));
            } else {
                tripRemaining[id] = 10;
            }
        } else {
            tripRemaining[id] = 10;
        }

        if (tripRemaining[id] > 0) {
            btn.disabled = true;
            btn.classList.add('opacity-50', 'cursor-not-allowed');
        } else {
            btn.disabled = false;
            btn.classList.remove('opacity-50', 'cursor-not-allowed');
        }
        startTripCountdown(id);
    });

    document.querySelectorAll('.start-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = btn.getAttribute('data-route-id');
            startTrip(id);
        });
    });

    document.querySelectorAll('.finalize-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = btn.getAttribute('data-route-id');
            finalizeTrip(id);
        });
    });

    document.querySelectorAll('.emergency-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const id = this.getAttribute('data-route-id');
            
            // Si el botón dice "Reanudar Recorrido", reanuda
            if (this.textContent.includes('Reanudar')) {
                reanudarRecorrido(id);
            } else {
                // Si no, abre el modal de emergencia
                abrirModalEmergencia(id);
            }
        });
    });

    try {
        const visibleId = sessionStorage.getItem('visible_in_progress');
        if (visibleId) {
            const card = document.getElementById('route-card-' + visibleId);
            if (card) card.style.display = '';
        }
    } catch (e) {
    }
});