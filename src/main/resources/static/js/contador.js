const preStartIntervals = {};
const tripIntervals = {};
const tripPaused = {};
const tripRemaining = {};

function startTrip(routeId) {
  const seconds = 5;
  const countdown = document.getElementById('countdown-' + routeId);
  const startBtn = document.getElementById('start-btn-' + routeId);
  if (startBtn) startBtn.disabled = true;

  let remaining = seconds;
  countdown.textContent = remaining + 's...';
  preStartIntervals[routeId] = setInterval(() => {
    remaining -= 1;
    if (remaining <= 0) {
      clearInterval(preStartIntervals[routeId]);
      countdown.textContent = 'Iniciando...';
      try { sessionStorage.setItem('visible_in_progress', routeId); } catch (e) { }

      const url = '/api/rutas/iniciar/' + routeId;
      fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(resp => {
        if (resp.ok) {
          window.location.reload();
        } else {
          countdown.textContent = 'Error al iniciar';
          if (startBtn) startBtn.disabled = false;
        }
      }).catch(err => {
        countdown.textContent = 'Error de red';
        if (startBtn) startBtn.disabled = false;
      });
    } else {
      countdown.textContent = remaining + 's...';
    }
  }, 1000);
}

function finalizeTrip(routeId) {
  if (!(typeof tripRemaining[routeId] !== 'undefined' && tripRemaining[routeId] <= 0)) return;
  try { sessionStorage.removeItem('visible_in_progress'); } catch (e) { }

  const url = '/api/rutas/finalizar/' + routeId;
  fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(resp => {
    if (resp.ok) {
      window.location.reload();
    } else {
      console.error('Finalize failed', resp.status);
    }
  }).catch(err => console.error(err));
}

function toggleEmergency(routeId) {
  const btn = document.getElementById('emergency-btn-' + routeId);
  if (!tripPaused[routeId]) {
    tripPaused[routeId] = true;
    if (tripIntervals[routeId]) {
      clearInterval(tripIntervals[routeId]);
      tripIntervals[routeId] = null;
    }
    btn.textContent = 'Reanudar';
    btn.classList.remove('bg-red-600');
    btn.classList.add('bg-yellow-500');
  } else {
    tripPaused[routeId] = false;
    startTripCountdown(routeId);
    btn.textContent = 'Emergencia';
    btn.classList.remove('bg-yellow-500');
    btn.classList.add('bg-red-600');
  }
}

function startTripCountdown(routeId) {
  const countdown = document.getElementById('countdown-' + routeId);
  if (typeof tripRemaining[routeId] === 'undefined') tripRemaining[routeId] = 10;
  if (tripRemaining[routeId] <= 0) {
    const finalizeBtn = document.getElementById('finalize-btn-' + routeId);
    if (finalizeBtn) { finalizeBtn.disabled = false; finalizeBtn.classList.remove('opacity-50', 'cursor-not-allowed'); }
    if (countdown) countdown.textContent = 'Tiempo restante: 0s';
    return;
  }

  if (countdown) countdown.textContent = 'Tiempo restante: ' + tripRemaining[routeId] + 's';

  if (tripIntervals[routeId]) clearInterval(tripIntervals[routeId]);
  tripIntervals[routeId] = setInterval(() => {
    if (tripPaused[routeId]) return;
    tripRemaining[routeId] = Math.max(0, tripRemaining[routeId] - 1);
    if (countdown) countdown.textContent = 'Tiempo restante: ' + tripRemaining[routeId] + 's';
    if (tripRemaining[routeId] <= 0) {
      clearInterval(tripIntervals[routeId]);
      tripIntervals[routeId] = null;
      const finalizeBtn = document.getElementById('finalize-btn-' + routeId);
      if (finalizeBtn) { finalizeBtn.disabled = false; finalizeBtn.classList.remove('opacity-50', 'cursor-not-allowed'); }
    }
  }, 1000);
}

window.addEventListener('DOMContentLoaded', () => {
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
        tripRemaining[id] = Math.max(0, Math.ceil(10 - elapsedFloat));
      } else {
        tripRemaining[id] = 10;
      }
    } else {
      tripRemaining[id] = 10;
    }

    if (tripRemaining[id] > 0) {
      btn.disabled = true; btn.classList.add('opacity-50', 'cursor-not-allowed');
    } else {
      btn.disabled = false; btn.classList.remove('opacity-50', 'cursor-not-allowed');
    }
    startTripCountdown(id);
  });

  document.querySelectorAll('.start-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = btn.getAttribute('data-route-id');
      btn.disabled = true;
      try { sessionStorage.setItem('visible_in_progress', id); } catch (e) { }
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
    btn.addEventListener('click', (e) => {
      const id = btn.getAttribute('data-route-id');
      toggleEmergency(id);
    });
  });

  try {
    const visibleId = sessionStorage.getItem('visible_in_progress');
    if (visibleId) {
      const card = document.getElementById('route-card-' + visibleId);
      if (card) card.style.display = '';
    }
  } catch (e) { }
});