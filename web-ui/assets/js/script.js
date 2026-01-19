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

  // ======================================================
  // CARGA DE CSV + RENDER EN DATATABLES (DataTables 2.x)
  // - Parseo CSV con PapaParse (en Web Worker para rendimiento)
  // - Renderizado eficiente con DataTables (paginación/orden/búsqueda)
  // ======================================================

  // -----------------------------
  // Referencias a la tabla HTML
  // -----------------------------
  const csvTable = document.getElementById("csv_table");
  // Variable global para almacenar la instancia de DataTable
  let dataTablecsv = null;

  // ======================================================
  // Seguridad: escapeHtml
  // ======================================================

  /**
   * Convierte caracteres especiales a entidades HTML.
   * 
   * ¿Por qué?
   * - Si insertas texto externo (por ejemplo, nombres de columnas o datos del CSV)
   *   usando innerHTML, existe el riesgo de inyectar HTML/JS (XSS).
   * - Esta función asegura que lo que se inserta se renderice como texto.
   *
   * Ejemplo:
   *   "<script>alert(1)</script>" => "&lt;script&gt;alert(1)&lt;/script&gt;"
   *
   * @param {any} value - Valor a convertir a texto y escapar
   * @returns {string} - Texto seguro para insertar en HTML
   */
  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  // ======================================================
  // Render / Update DataTable
  // ======================================================

  /**
   * Renderiza los datos del CSV en DataTables.
   * 
   * Flujo:
   * 1) Construye el <thead> con los nombres de columnas
   * 2) Mapea columnas para DataTables usando { title, data }
   * 3) Si DataTable ya existe: reemplaza data (clear + add + draw)
   * 4) Si no existe: crea la instancia con opciones optimizadas
   *
   * @param {Array<Object>} datos - Arreglo de objetos; cada objeto es una fila del CSV.
   * @param {Array<string>} columnas - Lista de nombres de columnas detectadas.
   */

  function renderCsvInDataTable(datos, columnas) {
    // -----------------------------
    // 1) Crear/actualizar encabezado (thead)
    // -----------------------------
    // Nota: DataTables usa el encabezado para ordenar, mostrar títulos, etc.
    const headerElement = document.getElementById("tabla-header");
    if (headerElement) {
      headerElement.innerHTML =
        `<tr>${columnas.map(c => `<th>${escapeHtml(c)}</th>`).join("")}</tr>`;
    }

    // -----------------------------
    // 2) Definir columnas para DataTables
    // -----------------------------
    // Cada columna indica:
    // - title: texto que muestra en header
    // - data: nombre de propiedad en el objeto (fila)
    // - defaultContent: valor por defecto si falta el dato

    const dtColumns = columnas.map((c) => ({
      title: c,
      data: c,
      defaultContent: "-",
    }));

    // -----------------------------
    // 3) Si ya existe el DataTable, solo actualizamos data
    // -----------------------------
    // IMPORTANTE:
    // - No existe "reload()" en DataTables cuando trabajas con data local.
    // - El patrón correcto es clear() + rows.add() + draw()

    if (dataTablecsv) {
      dataTablecsv.clear().rows.add(datos).draw();
      return;
    }

    // -----------------------------
    // 4) Crear DataTable por primera vez
    // -----------------------------
    dataTablecsv = new DataTable(csvTable, {
      data: datos,
      columns: dtColumns,
      
      deferRender: true,
      processing: true,
      searchDelay: 300,

      pageLength: 10,
      lengthMenu: [10, 25, 50, 100, 250],
      order: [],
      scrollX: true,
    });
  }

  // ======================================================
  // Carga de CSV desde URL con PapaParse
  // ======================================================

  /**
  * Carga un archivo CSV desde una URL y lo procesa con PapaParse.
  *
  * Puntos clave:
  * - Se convierte la URL a absoluta (evita errores de "Invalid URL" en Web Workers)
  * - download: true => PapaParse descarga el CSV por XHR/fetch
  * - header: true => devuelve cada fila como objeto {col1: val1, col2: val2...}
  * - worker: true => parseo en background (no congela la UI)
  *
  * @param {string} urlArchivoRel - Ruta relativa o URL del CSV
  */

  function loadcsv(urlArchivoRel) {
    
    // Convertimos a URL absoluta para que el Worker no “pierda” el contexto
    // cuando el script corre desde una URL tipo blob:...
    const urlArchivo = new URL(urlArchivoRel, window.location.href).href;

    Papa.parse(urlArchivo, {
      download: true,         // descarga el CSV desde la URL
      header: true,           // interpreta primera fila como headers
      skipEmptyLines: true,   // ignora filas vacías
      worker: true,           // parseo en Web Worker (mejor performance)
      complete: function (results) {
        const datos = results.data || [];

        // Validación rápida de contenido
        if (!datos.length) {
          console.warn("No hay datos en el archivo CSV");
          return;
        }

        // Obtiene columnas desde meta.fields si existe, sino desde el primer objeto
        const columnas = results?.meta?.fields ?? Object.keys(datos[0] || {});
        console.log(`CSV cargado: ${datos.length} filas`, { columnas });

        // Renderiza o actualiza la tabla
        renderCsvInDataTable(datos, columnas);
      },
      error: function (err) {
        console.error("Error al cargar el CSV:", err);
      }
    });
  }

  // ======================================================
  // Ejecución inicial
  // ======================================================

  // Ruta del CSV (relativa al sitio)
  const urlArchivo = 'assets/documents/df_core_clean.csv';
  // Dispara la carga al iniciar la página
  loadcsv(urlArchivo);

});
