/*Objetivo: conectar la UI con el Backend (Spring Boot) y mostrar resultados.*/

document.addEventListener("DOMContentLoaded", () => {
  /**
   * Base URL del backend público (Spring Boot).
   * Si en el futuro cambiamos el puerto, se cambia aquí.
   */
  const API_BASE_URL = "http://localhost:8080";

  /**
   * Endpoint que expone Spring Boot
   * Espera: { text: "..." }
   * Devuelve: { prediction: "positive"|"negative", probability: 0.xx }
   */
  const SENTIMENT_URL = `${API_BASE_URL}/sentiment`;

  /*Funcion para obtener elementos con el ID */
  const $ = (id) => document.getElementById(id);

  /** Estadísticas de la sesión (acumulado de positivos/negativos)*/
  const sessionStats = {
    positive: 0,
    negative: 0,
  };

  // Canvas donde se dibujan las gráficas
  const canvasProb = $("grafica1");
  const canvasCount = $("grafica2");

  // Elementos del form de análisis
  const textInput = $("text-input"); // -> textarea
  const analyzeBtn = $("analyze-btn");  // -> botón Analizar
  const resetBtn = $("reset-btn");  // -> botón Resetear Sesión

  // Si un error ocurre, se muestra en la consola y se detiene
  if (!canvasProb || !canvasCount) {
    console.error("❌ No se encontraron los canvas grafica1/grafica2 en el HTML.");
    return;
  }
  if (!textInput || !analyzeBtn) {
    console.error(
      "❌ No se encontró el formulario (text-input/analyze-btn). " +
      "Pega el bloque HTML del form en index.html."
    );
    return;
  }

  /**
   * Revisiones para asegurar el funcionamiento de Chart.js:
   *  - chart.js debe cargar **ANTES** que este script
   *  - canvas con id="grafica1" e id="grafica2" deben existir
   */

  // Gráfico 1: probabilidad positiva del último análisis (barra)
  const chartProb = new Chart(canvasProb, {
    type: "bar",
    data: {
      labels: ["Probabilidad de coincidencia"],
      datasets: [
        {
          label: "Último análisis",
          data: [0], // al inicio 0, luego se actualiza con la respuesta real
          borderWidth: 1,
        },
      ],
    },
    options: {
      scales: {
        y: {
          beginAtZero: true,
          suggestedMax: 100, 
        },
      },
    },
  });

  // Gráfico 2: conteo acumulado positivos vs negativos 
  const chartCount = new Chart(canvasCount, {
    type: "pie",
    data: {
      labels: ["positive", "negative"],
      datasets: [
        {
          label: "Conteo",
          data: [0, 0], // [positive, negative]
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
    },
  });


  function showLoading(isLoading) {
    // Muestra/oculta el texto “Analizando…”
    $("loading")?.classList.toggle("d-none", !isLoading);

    // Deshabilita el botón mientras trabaja
    analyzeBtn.disabled = isLoading;
  }
    // funcion para mostrar errores
  function showError(message) {
    const el = $("error-alert");
    if (!el) return;

    el.textContent = message;
    el.classList.remove("d-none");
  }

  function clearError() {
    const el = $("error-alert");
    if (!el) return;

    el.classList.add("d-none");
    el.textContent = "";
  }

  function showResult(prediction, posprobability, negprobability) {
    const badge = $("result-badge");// -> span donde va el resultado
    if (badge) {
      badge.textContent = prediction;

      // Cambiamos color según predicción
      badge.classList.remove("bg-secondary", "bg-success", "bg-danger");
      if (prediction === "positive") badge.classList.add("bg-success");
      else if (prediction === "negative") badge.classList.add("bg-danger");
      else badge.classList.add("bg-danger");
    }

    // Mostrar probabilidad como porcentaje bonito
    const probEl = $("result-probability");
    if (probEl) {
      const pct = Math.round(posprobability * 1000) / 10; // 1 decimal
      probEl.textContent = `${pct}%`;
    }

    const negEl = $("result-negprobability");
    if (negEl) {
      const pctNeg = Math.round(negprobability * 1000) / 10; // 1 decimal
      negEl.textContent = `${pctNeg}%`;
    }

    // Mostrar tarjeta de resultado
    $("result-card")?.classList.remove("d-none");
  }

  function updateCharts(probability, prediction) {
    // Actualiza el gráfico 1 de probabilidad
    const pct= Math.round(probability * 1000) / 10; // 0..100 con 1 decimal
    chartProb.data.datasets[0].data = [pct];
    chartProb.update();

    // Actualiza el gráfico 2 de conteo acumulado
    if (prediction === "positive") sessionStats.positive=Math.round(probability * 1000) / 10;
    else sessionStats.negative=Math.round(probability * 1000) / 10;

    if (sessionStats.positive == 0) sessionStats.positive=100-(Math.round(probability * 1000) / 10);
    if (sessionStats.negative == 0) sessionStats.negative=100-(Math.round(probability * 1000) / 10);

    
    chartCount.data.datasets[0].data = [
      sessionStats.positive,
      sessionStats.negative,
    ];
    chartCount.update();

    // Logs para debug
    console.log("prediction:", prediction);
    console.log("sessionStats:", sessionStats);
    console.log("pieData:", chartCount.data.datasets[0].data);
  }

  function resetSession() {
    sessionStats.positive = 0;
    sessionStats.negative = 0;

    chartCount.data.datasets[0].data = [0, 0];
    chartCount.update();

    // (Opcional) reset gráfico 1 también:
    chartProb.data.datasets[0].data = [0];
    chartProb.update();

    $("result-card")?.classList.add("d-none");
    clearError();
}

  resetBtn?.addEventListener("click", resetSession);


  async function analyzeText() {
    clearError();

    // Leemos el texto del textarea
    const text = textInput.value?.trim() ?? "";

    // Validación rápida del lado UI
    if (text.length < 3) {
      showError("El campo debe contener al menos 3 caracteres.");
      return;
    }

    showLoading(true);

    try {
      // Llamada al backend
      const resp = await fetch(SENTIMENT_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ text }),
      });

      // Si hubo un error HTTP, lo procesamos
      if (!resp.ok) {
        let msg = `Error ${resp.status}`;

        try {
          const err = await resp.json();
          // Si el backend envió un mensaje, lo usamos
          msg = err?.error ?? msg;

          // Si hay detalles, mostramos el primero
          if (err?.details && Object.keys(err.details).length > 0) {
            const firstKey = Object.keys(err.details)[0];
            msg += ` (${err.details[firstKey]})`;
          }
        } catch (_) {
          const t = await resp.text();
          if (t) msg = t;
        }

        throw new Error(msg);
      }

      // Si fue OK, parseamos JSON del response
      const data = await resp.json();
      
      // Obtener probabilidad positiva (adaptarse a ambas APIs)
      const pos = (typeof data.positiveProbability === "number") // -> Nueva API
      ? data.positiveProbability  
      : data.probability;

      const neg = (typeof data.negativeProbability === "number") // -> Nueva API
      ? data.negativeProbability  
      : (1 - pos);

      // Esperado: { prediction: "positive"|"negative", probability: number }
      showResult(data.prediction, pos, neg);
      updateCharts(pos, data.prediction);
    } catch (e) {
      showError(e.message ?? "Error desconocido");
    } finally {
      showLoading(false);
    }
  }

  // Evento click del botón Analizar
  analyzeBtn.addEventListener("click", analyzeText);

  // Evento Ctrl+Enter en el textarea
  textInput.addEventListener("keydown", (ev) => {
    if ((ev.ctrlKey || ev.metaKey) && ev.key === "Enter") {
      analyzeText();
    }
  });

  console.log("✅ UI lista. Esperando análisis...");
});
