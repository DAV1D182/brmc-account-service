package com.brmc.account;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador web que entrega las paginas HTML de la aplicacion BRMC.
 *
 * <p>Este proyecto no usa plantillas externas para la UI: cada metodo retorna HTML estatico con
 * JavaScript que consume las APIs REST del sistema. El controlador actua como capa de navegacion y
 * no ejecuta reglas de negocio directamente.</p>
 */
@RestController
class HomeController {

    private final UserContextService userContextService;

    /**
     * Constructor por defecto usado por Spring para exponer las paginas web.
     */
    HomeController(UserContextService userContextService) {
        this.userContextService = userContextService;
    }

    private static final String STYLES = """
            <style>
                :root {
                    color-scheme: light;
                    font-family: Arial, sans-serif;
                    color: #1f2937;
                    background: #f4f7fb;
                }
                body {
                    margin: 0;
                    padding: 32px;
                }
                main {
                    max-width: 1000px;
                    margin: 0 auto;
                    background: #ffffff;
                    border: 1px solid #dbe3ea;
                    border-radius: 8px;
                    padding: 28px;
                    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
                }
                h1 {
                    margin: 0 0 8px;
                    color: #164e63;
                    font-size: 30px;
                }
                h2 {
                    margin: 24px 0 8px;
                    color: #164e63;
                    font-size: 20px;
                }
                p {
                    margin: 0 0 18px;
                    color: #4b5563;
                }
                a,
                button {
                    border: 0;
                    border-radius: 6px;
                    background: #0f766e;
                    color: #ffffff;
                    cursor: pointer;
                    display: inline-block;
                    font-size: 15px;
                    font-weight: 700;
                    padding: 11px 16px;
                    text-decoration: none;
                }
                a:hover,
                button:hover {
                    background: #115e59;
                }
                .toolbar,
                .actions {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 10px;
                    margin-bottom: 10px;
                }
                .action-board {
                    display: grid;
                    gap: 12px;
                    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                    margin: 18px 0;
                }
                .action-group {
                    background: #f8fafc;
                    border: 1px solid #e5e7eb;
                    border-radius: 8px;
                    padding: 12px;
                }
                .action-group h3 {
                    color: #164e63;
                    font-size: 14px;
                    margin: 0 0 10px;
                }
                .action-group .actions {
                    margin-bottom: 0;
                }
                .summary-grid {
                    display: grid;
                    gap: 12px;
                    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                    margin: 12px 0 18px;
                }
                .summary-item {
                    background: #f8fafc;
                    border: 1px solid #e5e7eb;
                    border-radius: 8px;
                    padding: 12px;
                }
                .summary-item span {
                    color: #64748b;
                    display: block;
                    font-size: 12px;
                    font-weight: 700;
                    margin-bottom: 6px;
                }
                .summary-item strong {
                    color: #111827;
                    display: block;
                    font-size: 16px;
                }
                form {
                    display: grid;
                    grid-template-columns: repeat(3, minmax(160px, 1fr)) auto;
                    gap: 10px;
                    align-items: end;
                    margin: 22px 0 18px;
                    padding: 16px;
                    background: #f8fafc;
                    border: 1px solid #e5e7eb;
                    border-radius: 8px;
                }
                .movement-form {
                    grid-template-columns: minmax(320px, 2fr) minmax(150px, 0.7fr);
                }
                .filter-form {
                    grid-template-columns: repeat(3, minmax(160px, 1fr));
                }
                .account-create-form {
                    grid-template-columns: 1fr;
                    gap: 14px;
                }
                .form-section {
                    display: grid;
                    gap: 12px;
                    grid-template-columns: repeat(3, minmax(160px, 1fr));
                }
                .form-section h2 {
                    grid-column: 1 / -1;
                    margin: 0;
                }
                .form-section .wide-field {
                    grid-column: span 2;
                }
                .movement-form .description-field {
                    grid-column: 1 / -1;
                }
                .movement-form button {
                    grid-column: 1 / -1;
                    justify-self: start;
                }
                label {
                    display: grid;
                    gap: 6px;
                    color: #334155;
                    font-size: 13px;
                    font-weight: 700;
                }
                input,
                select {
                    border: 1px solid #cbd5e1;
                    border-radius: 6px;
                    color: #111827;
                    font-size: 15px;
                    padding: 10px;
                }
                input:focus,
                select:focus {
                    border-color: #0f766e;
                    outline: 2px solid #ccfbf1;
                }
                .secondary {
                    background: #2563eb;
                }
                .secondary:hover {
                    background: #1d4ed8;
                }
                .refund {
                    background: #9333ea;
                }
                .refund:hover {
                    background: #7e22ce;
                }
                .muted {
                    background: #475569;
                }
                .muted:hover {
                    background: #334155;
                }
                .danger {
                    background: #dc2626;
                }
                .danger:hover {
                    background: #b91c1c;
                }
                .logout {
                    background: #64748b;
                    margin-left: auto;
                }
                .logout:hover {
                    background: #475569;
                }
                .status {
                    border-radius: 999px;
                    display: inline-block;
                    font-size: 12px;
                    font-weight: 700;
                    padding: 4px 8px;
                }
                .active {
                    background: #dcfce7;
                    color: #166534;
                }
                .closed {
                    background: #fee2e2;
                    color: #991b1b;
                }
                .pending {
                    background: #fef3c7;
                    color: #92400e;
                }
                .approved {
                    background: #dcfce7;
                    color: #166534;
                }
                .rejected {
                    background: #fee2e2;
                    color: #991b1b;
                }
                .settled {
                    background: #dbeafe;
                    color: #1e40af;
                }
                .suspended,
                .inactive,
                .cancelled,
                .failed {
                    background: #fffbeb;
                    color: #92400e;
                }
                .terminated {
                    background: #fee2e2;
                    color: #991b1b;
                }
                .completed {
                    background: #dcfce7;
                    color: #166534;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 18px;
                    font-size: 14px;
                }
                th, td {
                    border-bottom: 1px solid #e5e7eb;
                    padding: 12px 10px;
                    text-align: left;
                    vertical-align: middle;
                }
                th {
                    background: #ecfeff;
                    color: #164e63;
                }
                .message {
                    margin-top: 18px;
                    padding: 12px;
                    border-radius: 6px;
                    border: 1px solid;
                }
                .info {
                    background: #f8fafc;
                    border: 1px solid #e5e7eb;
                    color: #334155;
                }
                .success {
                    background: #f0fdf4;
                    border-color: #bbf7d0;
                    color: #166534;
                }
                .warning {
                    background: #fffbeb;
                    border-color: #fde68a;
                    color: #92400e;
                }
                .error {
                    background: #fef2f2;
                    border-color: #fecaca;
                    color: #991b1b;
                }
                .module-grid {
                    display: grid;
                    gap: 16px;
                    grid-template-columns: repeat(2, minmax(260px, 1fr));
                    margin-top: 18px;
                }
                .module {
                    background: #f8fafc;
                    border: 1px solid #dbe3ea;
                    border-radius: 8px;
                    padding: 16px;
                }
                .module h2 {
                    margin-top: 0;
                }
                .module p {
                    min-height: 42px;
                }
                .money {
                    font-variant-numeric: tabular-nums;
                    white-space: nowrap;
                }
                .trm-box {
                    background: #eef2ff;
                    border: 1px solid #c7d2fe;
                    border-radius: 8px;
                    color: #3730a3;
                    margin: 0 0 16px;
                    padding: 12px;
                }
                @media (max-width: 820px) {
                    body {
                        padding: 16px;
                    }
                    form {
                        grid-template-columns: 1fr;
                    }
                    .module-grid {
                        grid-template-columns: 1fr;
                    }
                    .movement-form .description-field,
                    .movement-form button {
                        grid-column: auto;
                    }
                    .form-section {
                        grid-template-columns: 1fr;
                    }
                    .form-section .wide-field {
                        grid-column: auto;
                    }
                }
            </style>
            """;

    /**
     * Renderiza el tablero principal con acceso a los modulos funcionales.
     *
     * @return pagina HTML de inicio.
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    String home() {
        return page("Dashboard", """
                <section class="bc-hero">
                    <div>
                        <span class="bc-module-meta">Customer operations workspace</span>
                        <h2>BRMC Billing Care</h2>
                        <p>Portal operativo para cuentas, servicios, billing, pagos, disputas, reportes y eventos. Inspirado en flujos de atencion tipo BRM/Billing Care, sin usar marcas oficiales de terceros.</p>
                    </div>
                    <div class="bc-quick-actions">
                        <a class="secondary" href="/accounts/new">Crear cuenta</a>
                        <a href="/accounts">Buscar cuenta</a>
                        <a class="secondary" href="/payments">Registrar pago</a>
                        <a href="/products">Crear producto</a>
                        <a class="secondary" href="/billing">Ejecutar billing</a>
                        <a href="/entel">ENTEL</a>
                        <a class="muted" href="/events">Ver eventos</a>
                    </div>
                </section>
                <section class="bc-dashboard-grid">
                    <article class="module">
                        <span class="bc-module-meta">Customer management</span>
                        <h2>Clientes y cuentas</h2>
                        <p>Alta y consulta de cuentas. La moneda base de las cuentas es COP.</p>
                        <div class="actions">
                            <a class="secondary" href="/accounts/new">Crear cuenta</a>
                            <a href="/accounts">Ver cuentas</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Financial operations</span>
                        <h2>Pagos y reembolsos</h2>
                        <p>Registra pagos en COP o USD; los pagos USD se convierten con TRM del dia.</p>
                        <div class="actions">
                            <a class="secondary" href="/payments">Crear pago</a>
                            <a class="refund" href="/refunds">Crear reembolso</a>
                            <a class="danger" href="/write-offs">Write-off</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Case management</span>
                        <h2>Disputas</h2>
                        <p>Crea casos pendientes y cierralos por aprobacion, rechazo o settlement.</p>
                        <div class="actions">
                            <a class="secondary" href="/disputes">Gestionar disputas</a>
                            <a href="/disputes?status=PENDING">Settlement</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Reports workspace</span>
                        <h2>Reportes</h2>
                        <p>Consulta transacciones y disputas en una misma vista operativa.</p>
                        <div class="actions">
                            <a class="secondary" href="/reports">Ver reportes</a>
                            <a class="muted" href="/transactions">Historial completo</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Audit trail</span>
                        <h2>Eventos</h2>
                        <p>Auditoria completa de cuentas, pagos, reembolsos, cierres y disputas.</p>
                        <div class="actions">
                            <a class="muted" href="/events">Ver eventos</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Time control</span>
                        <h2>Virtual Time</h2>
                        <p>Administra la fecha logica usada por billing y pruebas de ciclo.</p>
                        <div class="actions">
                            <a class="secondary" href="/virtual-time">Ver reloj virtual</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Catalog</span>
                        <h2>Products</h2>
                        <p>Catalogo comercial de cargos unicos y recurrentes.</p>
                        <div class="actions">
                            <a class="secondary" href="/products">Gestionar productos</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Assets</span>
                        <h2>Services</h2>
                        <p>Consulta los services creados y los productos asociados a cada cuenta.</p>
                        <div class="actions">
                            <a class="secondary" href="/services">Consultar services creados</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Billing cycle</span>
                        <h2>Billing</h2>
                        <p>Ejecuta billing manual usando la fecha virtual y productos activos.</p>
                        <div class="actions">
                            <a class="secondary" href="/billing">Ejecutar billing</a>
                            <a href="/bills">Ver Bills</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Invoice documents</span>
                        <h2>Invoices</h2>
                        <p>Consulta facturas generadas por billing, pagos aplicados, estados y exportacion CSV.</p>
                        <div class="actions">
                            <a class="secondary" href="/invoices">Ver facturas generadas</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">Operations inventory</span>
                        <h2>Inventario</h2>
                        <p>Gestiona existencias, valor de inventario, disponibilidad y niveles de nuevo pedido.</p>
                        <div class="actions">
                            <a class="secondary" href="/inventory">Gestionar inventario</a>
                        </div>
                    </article>
                    <article class="module">
                        <span class="bc-module-meta">ENTEL operations</span>
                        <h2>ENTEL</h2>
                        <p>Genera archivos TXT para cambio de numero por NAP o carga ILE.</p>
                        <div class="actions">
                            <a class="secondary" href="/entel">Generar TXT ENTEL</a>
                        </div>
                    </article>
                    %s
                </section>
                <h2>Account workspace</h2>
                <section id="result"></section>
                <script>
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    async function loadAccounts() {
                        const result = document.getElementById("result");
                        result.innerHTML = message("info", "Consultando cuentas...");

                        try {
                            const response = await fetch("/api/accounts");
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar las cuentas."));
                            }

                            const accounts = await response.json();
                            if (accounts.length === 0) {
                                result.innerHTML = message("info", "No hay cuentas creadas todavia.");
                                return;
                            }

                            const rows = accounts.map(account => `
                                <tr>
                                    <td>${account.id}</td>
                                    <td>${account.ownerName}</td>
                                    <td>${account.phoneNumber || ""}</td>
                                    <td>${account.email || ""}</td>
                                    <td>${money(account.balance, account.currency)}</td>
                                    <td><span class="status ${account.status === "ACTIVE" ? "active" : "closed"}">${account.status}</span></td>
                                    <td>${account.createdAt}</td>
                                    <td>
                                        <div class="actions">
                                            <a class="muted" href="/accounts/${account.id}" target="_blank" rel="noopener">Ver detalle</a>
                                        </div>
                                    </td>
                                </tr>
                            `).join("");

                            result.innerHTML = `
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Id</th>
                                            <th>Titular</th>
                                            <th>Numero</th>
                                            <th>Correo</th>
                                            <th>Saldo</th>
                                            <th>Estado</th>
                                            <th>Fecha de creacion</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }
                </script>
                """.formatted(adminDashboardCard()));
    }

    /**
     * Renderiza la lista de cuentas.
     *
     * @return pagina HTML para consultar cuentas y abrir detalle.
     */
    @GetMapping(value = "/accounts", produces = MediaType.TEXT_HTML_VALUE)
    String accounts() {
        return page("Ver cuentas", """
                <p>Consulta las cuentas creadas y abre el detalle en una nueva pestana.</p>
                <div class="toolbar">
                    <a class="secondary" href="/accounts/new">Crear cuenta</a>
                    <a class="muted" href="/">Volver a la principal</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="accountFilterForm" class="bc-filter-panel">
                    <label>
                        Buscar cuenta
                        <input id="accountSearch" name="q" type="search" placeholder="ID, titular, telefono o correo">
                    </label>
                    <label>
                        Estado
                        <select id="statusFilter" name="status">
                            <option value="">Todos</option>
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="CLOSED">CLOSED</option>
                        </select>
                    </label>
                    <label>
                        Ciclo facturacion
                        <select id="cycleFilter" name="cycle">
                            <option value="">Todos</option>
                            <option value="MONTHLY">MONTHLY</option>
                            <option value="BIMONTHLY">BIMONTHLY</option>
                            <option value="QUARTERLY">QUARTERLY</option>
                            <option value="ANNUAL">ANNUAL</option>
                        </select>
                    </label>
                    <label>
                        Saldo minimo
                        <input id="minBalanceFilter" name="minBalance" type="number" step="0.01" placeholder="0">
                    </label>
                    <label>
                        Saldo maximo
                        <input id="maxBalanceFilter" name="maxBalance" type="number" step="0.01" placeholder="100000">
                    </label>
                    <label>
                        Ordenar por
                        <select id="sortFilter" name="sort">
                            <option value="createdDesc">Mas recientes</option>
                            <option value="createdAsc">Mas antiguas</option>
                            <option value="ownerAsc">Titular A-Z</option>
                            <option value="balanceDesc">Mayor saldo</option>
                            <option value="balanceAsc">Menor saldo</option>
                        </select>
                    </label>
                    <div class="bc-filter-actions">
                        <button type="submit">Filtrar cuentas</button>
                        <button class="muted" type="button" onclick="clearAccountFilters()">Limpiar filtros</button>
                    </div>
                </form>
                <section id="result"></section>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    let currentAccounts = [];

                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    function normalize(value) {
                        return (value || "").toString().trim().toUpperCase();
                    }

                    function applyInitialFilters() {
                        document.getElementById("accountSearch").value = params.get("q") || "";
                        document.getElementById("statusFilter").value = params.get("status") || "";
                        document.getElementById("cycleFilter").value = params.get("cycle") || "";
                        document.getElementById("minBalanceFilter").value = params.get("minBalance") || "";
                        document.getElementById("maxBalanceFilter").value = params.get("maxBalance") || "";
                        document.getElementById("sortFilter").value = params.get("sort") || "createdDesc";
                    }

                    function filteredAccounts() {
                        const search = normalize(document.getElementById("accountSearch").value);
                        const status = document.getElementById("statusFilter").value;
                        const cycle = document.getElementById("cycleFilter").value;
                        const minBalance = document.getElementById("minBalanceFilter").value;
                        const maxBalance = document.getElementById("maxBalanceFilter").value;
                        const sort = document.getElementById("sortFilter").value;
                        const minValue = minBalance === "" ? null : Number(minBalance);
                        const maxValue = maxBalance === "" ? null : Number(maxBalance);

                        return currentAccounts
                            .filter(account => {
                                const haystack = [
                                    account.id,
                                    account.ownerName,
                                    account.phoneNumber,
                                    account.email,
                                    account.billNo
                                ].map(normalize).join(" ");
                                return !search || haystack.includes(search);
                            })
                            .filter(account => !status || account.status === status)
                            .filter(account => !cycle || account.billingCycle === cycle)
                            .filter(account => minValue === null || Number(account.balance) >= minValue)
                            .filter(account => maxValue === null || Number(account.balance) <= maxValue)
                            .sort((left, right) => {
                                if (sort === "createdAsc") {
                                    return new Date(left.createdAt) - new Date(right.createdAt);
                                }
                                if (sort === "ownerAsc") {
                                    return (left.ownerName || "").localeCompare(right.ownerName || "", "es");
                                }
                                if (sort === "balanceDesc") {
                                    return Number(right.balance) - Number(left.balance);
                                }
                                if (sort === "balanceAsc") {
                                    return Number(left.balance) - Number(right.balance);
                                }
                                return new Date(right.createdAt) - new Date(left.createdAt);
                            });
                    }

                    function renderAccounts(accounts) {
                        const result = document.getElementById("result");
                        if (currentAccounts.length === 0) {
                            result.innerHTML = message("info", "No hay cuentas creadas todavia.");
                            return;
                        }
                        if (accounts.length === 0) {
                            result.innerHTML = message("info", "No hay cuentas que coincidan con los filtros seleccionados.");
                            return;
                        }

                        const rows = accounts.map(account => `
                            <tr>
                                <td>${account.id}</td>
                                <td>${account.ownerName}</td>
                                <td>${account.phoneNumber || ""}</td>
                                <td>${account.email || ""}</td>
                                <td>${money(account.balance, account.currency)}</td>
                                <td><span class="status ${account.status === "ACTIVE" ? "active" : "closed"}">${account.status}</span></td>
                                <td>${account.billingDom || ""}</td>
                                <td>${account.billingCycle || ""}</td>
                                <td>${account.billNo || ""}</td>
                                <td>${account.createdAt}</td>
                                <td>
                                    <div class="actions">
                                        <a class="muted" href="/accounts/${account.id}" target="_blank" rel="noopener">Ver detalle</a>
                                    </div>
                                </td>
                            </tr>
                        `).join("");

                        result.innerHTML = `
                            <div class="bc-result-meta">
                                <strong>${accounts.length}</strong> de ${currentAccounts.length} cuentas encontradas
                            </div>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Id</th>
                                        <th>Titular</th>
                                        <th>Numero</th>
                                        <th>Correo</th>
                                        <th>Saldo</th>
                                        <th>Estado</th>
                                        <th>DOM</th>
                                        <th>Ciclo</th>
                                        <th>Bill No</th>
                                        <th>Fecha de creacion</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function clearAccountFilters() {
                        document.getElementById("accountFilterForm").reset();
                        document.getElementById("sortFilter").value = "createdDesc";
                        renderAccounts(filteredAccounts());
                    }

                    async function loadAccounts() {
                        const result = document.getElementById("result");
                        result.innerHTML = message("info", "Consultando cuentas...");

                        try {
                            const response = await fetch("/api/accounts");
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar las cuentas."));
                            }

                            currentAccounts = await response.json();
                            renderAccounts(filteredAccounts());
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }

                    document.getElementById("accountFilterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        renderAccounts(filteredAccounts());
                    });
                    ["accountSearch", "statusFilter", "cycleFilter", "minBalanceFilter", "maxBalanceFilter", "sortFilter"]
                        .forEach(id => document.getElementById(id).addEventListener("input", function () {
                            renderAccounts(filteredAccounts());
                        }));

                    applyInitialFilters();
                    loadAccounts();
                </script>
                """);
    }

    /**
     * Renderiza el formulario de creacion de cuentas.
     *
     * @return pagina HTML de alta de cuenta.
     */
    @GetMapping(value = "/accounts/new", produces = MediaType.TEXT_HTML_VALUE)
    String newAccount() {
        return page("Crear cuenta", """
                <p>Alta de cuenta con datos de cliente y configuracion inicial de facturacion.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="createAccountForm" class="account-create-form">
                    <section class="form-section">
                        <h2>Cuenta</h2>
                        <label class="wide-field">
                            Titular
                            <input id="ownerName" name="ownerName" type="text" maxlength="120" value="Cliente BRMC" required>
                        </label>
                        <label>
                            Estado inicial
                            <input type="text" value="ACTIVE" disabled>
                        </label>
                        <label>
                            Moneda base
                            <input type="text" value="COP" disabled>
                        </label>
                        <label>
                            Saldo inicial COP
                            <input id="initialBalance" name="initialBalance" type="number" min="0" step="0.01" value="100.00" required>
                        </label>
                        <label>
                            Bill No
                            <input type="text" value="Automatico" disabled>
                        </label>
                    </section>
                    <section class="form-section">
                        <h2>Cliente</h2>
                        <label>
                            Numero
                            <input id="phoneNumber" name="phoneNumber" type="text" maxlength="40" value="3001234567" required>
                        </label>
                        <label class="wide-field">
                            Correo
                            <input id="email" name="email" type="email" maxlength="160" value="cliente@brmc.com" required>
                        </label>
                    </section>
                    <section class="form-section">
                        <h2>Billinfo</h2>
                        <label>
                            DOM
                            <input id="billingDom" name="billingDom" type="number" min="1" max="31" step="1" value="1" required>
                        </label>
                        <label>
                            Ciclo de facturacion
                            <select id="billingCycle" name="billingCycle">
                                <option value="MONTHLY">MONTHLY</option>
                                <option value="BIMONTHLY">BIMONTHLY</option>
                                <option value="QUARTERLY">QUARTERLY</option>
                                <option value="ANNUAL">ANNUAL</option>
                            </select>
                        </label>
                        <label>
                            Pay type
                            <input type="text" value="Invoice" disabled>
                        </label>
                    </section>
                    <button type="submit">Crear cuenta</button>
                </form>
                <section id="result"></section>
                <script>
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    document.getElementById("createAccountForm").addEventListener("submit", async function (event) {
                        event.preventDefault();

                        const result = document.getElementById("result");
                        const ownerName = document.getElementById("ownerName").value.trim();
                        const phoneNumber = document.getElementById("phoneNumber").value.trim();
                        const email = document.getElementById("email").value.trim();
                        const initialBalance = Number(document.getElementById("initialBalance").value);
                        const billingDom = Number(document.getElementById("billingDom").value);
                        const billingCycle = document.getElementById("billingCycle").value;

                        if (!ownerName) {
                            result.innerHTML = message("warning", "Debe ingresar el titular de la cuenta.");
                            return;
                        }

                        if (!phoneNumber) {
                            result.innerHTML = message("warning", "Debe ingresar el numero.");
                            return;
                        }

                        if (!email) {
                            result.innerHTML = message("warning", "Debe ingresar el correo.");
                            return;
                        }

                        if (Number.isNaN(initialBalance) || initialBalance < 0) {
                            result.innerHTML = message("warning", "El saldo inicial debe ser cero o mayor.");
                            return;
                        }

                        if (!Number.isInteger(billingDom) || billingDom < 1 || billingDom > 31) {
                            result.innerHTML = message("warning", "El DOM debe estar entre 1 y 31.");
                            return;
                        }

                        result.innerHTML = message("info", "Creando cuenta...");

                        try {
                            const response = await fetch("/api/accounts", {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify({
                                    ownerName,
                                    phoneNumber,
                                    email,
                                    initialBalance,
                                    billingDom,
                                    billingCycle
                                })
                            });

                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible crear la cuenta."));
                            }

                            const account = await response.json();
                            result.innerHTML = `
                                ${message("success", `Cuenta creada correctamente. Id: <strong>${account.id}</strong> - Bill No: <strong>${account.billNo}</strong>`)}
                                <div class="summary-grid">
                                    <div class="summary-item"><span>DOM</span><strong>${account.billingDom}</strong></div>
                                    <div class="summary-item"><span>Ciclo</span><strong>${account.billingCycle}</strong></div>
                                    <div class="summary-item"><span>Moneda</span><strong>${account.currency}</strong></div>
                                </div>
                                <div class="toolbar">
                                    <a class="muted" href="/accounts/${account.id}">Ver detalle</a>
                                    <a href="/">Volver al inicio</a>
                                </div>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    });
                </script>
                """);
    }

    /**
     * Renderiza el detalle operativo de una cuenta.
     *
     * @param accountId cuenta cuyo detalle se abrira desde la UI.
     * @return pagina HTML con saldo, transacciones, servicios, billing y acciones rapidas.
     */
    @GetMapping(value = "/accounts/{accountId}", produces = MediaType.TEXT_HTML_VALUE)
    String accountDetail(@PathVariable String accountId) {
        return page("Detalle de cuenta - Account Workspace", """
                <div class="toolbar">
                    <a class="muted" href="/accounts">Accounts</a>
                    <a class="muted" href="/">Dashboard</a>
                </div>
                <section id="closedWarning"></section>
                <section id="accountBanner" class="bc-account-banner"></section>
                <section id="accountCards" class="bc-info-grid"></section>
                <nav class="bc-tabs" aria-label="Account workspace tabs">
                    <button class="bc-tab active" type="button" data-tab="home" onclick="switchTab('home')">Home</button>
                    <button class="bc-tab" type="button" data-tab="bills" onclick="switchTab('bills')">Bills</button>
                    <button class="bc-tab" type="button" data-tab="invoices" onclick="switchTab('invoices')">Invoices</button>
                    <button class="bc-tab" type="button" data-tab="payments" onclick="switchTab('payments')">Payments</button>
                    <button class="bc-tab" type="button" data-tab="services" onclick="switchTab('services')">Services</button>
                    <button class="bc-tab" type="button" data-tab="products" onclick="switchTab('products')">Products</button>
                    <button class="bc-tab" type="button" data-tab="disputes" onclick="switchTab('disputes')">Disputes</button>
                    <button class="bc-tab" type="button" data-tab="transactions" onclick="switchTab('transactions')">Transactions</button>
                    <button class="bc-tab" type="button" data-tab="events" onclick="switchTab('events')">Events</button>
                </nav>
                <section id="tab-home" class="bc-tab-panel active">
                    <div class="bc-section-grid">
                        <section id="accountSummary"></section>
                        <section id="homeAssets"></section>
                        <section id="homeBills"></section>
                        <section id="homeActivity"></section>
                    </div>
                </section>
                <section id="tab-bills" class="bc-tab-panel">
                    <section id="billingCharges"></section>
                </section>
                <section id="tab-invoices" class="bc-tab-panel">
                    <section id="invoices"></section>
                </section>
                <section id="tab-payments" class="bc-tab-panel">
                    <section id="payments"></section>
                    <section id="refunds"></section>
                </section>
                <section id="tab-services" class="bc-tab-panel">
                    <section id="services"></section>
                </section>
                <section id="tab-products" class="bc-tab-panel">
                    <section id="assignedProducts"></section>
                </section>
                <section id="tab-disputes" class="bc-tab-panel">
                    <section id="disputes"></section>
                </section>
                <section id="tab-transactions" class="bc-tab-panel">
                    <section id="transactions"></section>
                </section>
                <section id="tab-events" class="bc-tab-panel">
                    <section id="events"></section>
                </section>
                <script>
                    const accountId = "%s";
                    let currentAccount = null;
                    let currentServices = [];
                    let editingServiceId = null;

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }

                    async function fetchJson(url, options) {
                        const response = await fetch(url, options);
                        if (!response.ok) {
                            const error = await response.json().catch(() => ({}));
                            throw new Error(error.detail || "No fue posible completar la operacion.");
                        }
                        return response.json();
                    }

                    function switchTab(tabName) {
                        document.querySelectorAll(".bc-tab").forEach(tab => tab.classList.toggle("active", tab.dataset.tab === tabName));
                        document.querySelectorAll(".bc-tab-panel").forEach(panel => panel.classList.toggle("active", panel.id === `tab-${tabName}`));
                    }

                    function isClosed() {
                        return currentAccount && currentAccount.status === "CLOSED";
                    }

                    function disabledClass() {
                        return isClosed() ? " disabled" : "";
                    }

                    function disabledAttr() {
                        return isClosed() ? " disabled" : "";
                    }

                    function cycleMonths(cycle) {
                        if (cycle === "BIMONTHLY") {
                            return 2;
                        }
                        if (cycle === "QUARTERLY") {
                            return 3;
                        }
                        if (cycle === "ANNUAL") {
                            return 12;
                        }
                        return 1;
                    }

                    function daysInMonth(year, monthIndex) {
                        return new Date(year, monthIndex + 1, 0).getDate();
                    }

                    function addMonthsClamped(value, months) {
                        const targetYear = value.getFullYear();
                        const targetMonth = value.getMonth() + months;
                        const candidate = new Date(targetYear, targetMonth, 1, value.getHours(), value.getMinutes(), value.getSeconds());
                        candidate.setDate(Math.min(value.getDate(), daysInMonth(candidate.getFullYear(), candidate.getMonth())));
                        return candidate;
                    }

                    function calculateBillingCycle(account, billInfo, virtualTime) {
                        const currentVirtualTime = virtualTime?.currentVirtualTime || account.pinVirtualTimeT || new Date().toISOString();
                        const virtualDate = new Date(currentVirtualTime);
                        const cycle = billInfo?.billingCycle || account.billingCycle || "MONTHLY";
                        const months = cycleMonths(cycle);
                        const dom = Math.max(1, Math.min(31, Number(billInfo?.billingDom || account.billingDom || virtualDate.getDate())));
                        let start = new Date(
                            virtualDate.getFullYear(),
                            virtualDate.getMonth(),
                            Math.min(dom, daysInMonth(virtualDate.getFullYear(), virtualDate.getMonth())),
                            0,
                            0,
                            0
                        );
                        if (start > virtualDate) {
                            const previousBase = new Date(virtualDate.getFullYear(), virtualDate.getMonth() - months, 1, 0, 0, 0);
                            start = new Date(
                                previousBase.getFullYear(),
                                previousBase.getMonth(),
                                Math.min(dom, daysInMonth(previousBase.getFullYear(), previousBase.getMonth())),
                                0,
                                0,
                                0
                            );
                        }
                        const nextStart = addMonthsClamped(start, months);
                        const end = new Date(nextStart.getTime() - 1000);
                        return {
                            cycle,
                            dom,
                            virtualDate,
                            start,
                            end,
                            nextStart,
                            status: virtualDate >= start && virtualDate <= end ? "CURRENT" : "OUT_OF_CYCLE",
                            lastBillAt: billInfo?.lastBillAt || null,
                            nextBillAt: billInfo?.nextBillAt || nextStart.toISOString(),
                            billInfoNo: billInfo?.billInfoNo || account.billNo || "N/A"
                        };
                    }

                    function renderAccount(account, virtualTime, billInfo) {
                        currentAccount = account;
                        const cycleInfo = calculateBillingCycle(account, billInfo, virtualTime);
                        const cyclePeriod = `${formatDateOnly(cycleInfo.start)} - ${formatDateOnly(cycleInfo.end)}`;
                        document.getElementById("closedWarning").innerHTML = account.status === "CLOSED"
                            ? `<div class="bc-closed-warning">This account is closed. Financial operations are disabled.</div>`
                            : "";
                        document.getElementById("accountBanner").innerHTML = `
                            <h2>${account.ownerName}</h2>
                            <div class="bc-account-meta">
                                <span>Account Number <strong>${account.id}</strong></span>
                                <span>Company Name <strong>N/A</strong></span>
                                <span>Contact <strong>${account.phoneNumber || "N/A"}</strong></span>
                                <span>Email <strong>${account.email || "N/A"}</strong></span>
                                <span>Status <strong><span class="status ${account.status === "ACTIVE" ? "active" : "closed"}">${account.status}</span></strong></span>
                                <span>Balance <strong>${money(account.balance, account.currency)}</strong></span>
                            </div>
                            <div class="bc-banner-actions">
                                <a class="secondary${disabledClass()}" href="/payments?accountId=${account.id}">Make Payment / Pagar</a>
                                <a class="refund${disabledClass()}" href="/refunds?accountId=${account.id}">Create Refund / Reembolsar</a>
                                <a class="danger${disabledClass()}" href="/write-offs?accountId=${account.id}">Apply Write-Off</a>
                                <a class="secondary${disabledClass()}" href="/disputes?accountId=${account.id}">Create Dispute</a>
                                <a class="secondary" href="/invoices?accountId=${account.id}">Invoices</a>
                                <button type="button"${disabledAttr()} onclick="runAccountBilling()">Run Billing for Account</button>
                                <button type="button"${disabledAttr()} onclick="focusServiceCreation()">Create Service</button>
                                <button class="danger" type="button"${disabledAttr()} onclick="closeAccount()">Close Account / Cerrar cuenta</button>
                            </div>
                        `;
                        document.getElementById("accountCards").innerHTML = `
                            <article class="bc-info-card">
                                <h3>Contact Details</h3>
                                <span class="bc-field">Address</span><span class="bc-value">N/A</span>
                                <span class="bc-field">Email</span><span class="bc-value">${account.email || "N/A"}</span>
                                <span class="bc-field">Phone</span><span class="bc-value">${account.phoneNumber || "N/A"}</span>
                            </article>
                            <article class="bc-info-card">
                                <h3>Account Details</h3>
                                <span class="bc-field">Account ID</span><span class="bc-value">${account.id}</span>
                                <span class="bc-field">Currency</span><span class="bc-value">${account.currency}</span>
                                <span class="bc-field">Created At</span><span class="bc-value">${account.createdAt}</span>
                            </article>
                            <article class="bc-info-card">
                                <h3>Billing Cycle</h3>
                                <span class="bc-field">Current cycle</span><span class="bc-value bc-cycle-period">${cyclePeriod}</span>
                                <span class="bc-field">DOM / Cycle</span><span class="bc-value">Dia ${cycleInfo.dom} - ${cycleInfo.cycle}</span>
                                <span class="bc-field">Next billing date</span><span class="bc-value">${formatDate(cycleInfo.nextStart)}</span>
                                <span class="bc-field">Billinfo</span><span class="bc-value">${cycleInfo.billInfoNo}</span>
                            </article>
                            <article class="bc-info-card">
                                <h3>Hierarchy Details</h3>
                                <span class="bc-field">Parent account</span><span class="bc-value">Not set</span>
                                <span class="bc-field">Child accounts</span><span class="bc-value">0</span>
                                <span class="bc-field">Bill unit</span><span class="bc-value">default</span>
                            </article>
                        `;
                        document.getElementById("accountSummary").innerHTML = `
                            <h2>Balance Summary</h2>
                            <div class="bc-cycle-panel">
                                <div>
                                    <span>Ciclo de facturacion actual</span>
                                    <strong>${cyclePeriod}</strong>
                                </div>
                                <div>
                                    <span>Pin Virtual Time</span>
                                    <strong>${formatDate(cycleInfo.virtualDate)}</strong>
                                </div>
                                <div>
                                    <span>Proxima fecha de billing</span>
                                    <strong>${formatDate(cycleInfo.nextStart)}</strong>
                                </div>
                                <div>
                                    <span>Estado de ciclo</span>
                                    <strong><span class="status ${cycleInfo.status === "CURRENT" ? "active" : "pending"}">${cycleInfo.status}</span></strong>
                                </div>
                            </div>
                            <div class="summary-grid">
                                <div class="summary-item"><span>Saldo actual</span><strong>${money(account.balance, account.currency)}</strong></div>
                                <div class="summary-item"><span>Estado</span><strong>${account.status}</strong></div>
                                <div class="summary-item"><span>DOM</span><strong>${cycleInfo.dom}</strong></div>
                                <div class="summary-item"><span>Ciclo facturacion</span><strong>${cycleInfo.cycle}</strong></div>
                                <div class="summary-item"><span>Bill No</span><strong>${account.billNo || ""}</strong></div>
                                <div class="summary-item"><span>Ultimo billing</span><strong>${cycleInfo.lastBillAt ? formatDate(cycleInfo.lastBillAt) : "N/A"}</strong></div>
                            </div>
                        `;
                    }

                    function renderDisputes(disputes) {
                        const target = document.getElementById("disputes");
                        if (disputes.length === 0) {
                            target.innerHTML = `<h2>Disputas</h2>${message("info", "No hay disputas registradas para esta cuenta.")}`;
                            return;
                        }

                        const rows = disputes.map(dispute => `
                            <tr>
                                <td>${dispute.id}</td>
                                <td>${money(dispute.amount, dispute.currency)}</td>
                                <td>${dispute.reason}</td>
                                <td><span class="status ${dispute.status.toLowerCase()}">${dispute.status}</span></td>
                                <td>${dispute.resolutionNote || ""}</td>
                                <td>${formatDate(dispute.pinVirtualTimeT)}</td>
                                <td>${formatDate(dispute.createdT || dispute.createdAt)}</td>
                                <td>${dispute.resolvedPinVirtualTimeT ? formatDate(dispute.resolvedPinVirtualTimeT) : ""}</td>
                                <td>
                                    ${dispute.status === "PENDING" ? `
                                        <div class="actions">
                                            <button type="button" onclick="createSettlement('${dispute.id}')">Settlement</button>
                                            <button class="secondary" type="button" onclick="resolveDispute('${dispute.id}', 'approve')">Aprobar</button>
                                            <button class="danger" type="button" onclick="resolveDispute('${dispute.id}', 'reject')">Rechazar</button>
                                        </div>
                                    ` : ""}
                                </td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>Disputas</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Disputa</th>
                                        <th>Monto</th>
                                        <th>Motivo</th>
                                        <th>Estado</th>
                                        <th>Nota</th>
                                        <th>PVT apertura</th>
                                        <th>Fecha real</th>
                                        <th>PVT resolucion</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function renderMovements(targetId, title, movements) {
                        const target = document.getElementById(targetId);
                        if (movements.length === 0) {
                            target.innerHTML = `<h2>${title}</h2>${message("info", "No hay registros.")}`;
                            return;
                        }

                        const rows = movements.map(movement => `
                            <tr>
                                <td>${movement.id}</td>
                                <td>${movement.type}</td>
                                <td>${money(movement.amount, movement.currency)}</td>
                                <td>${money(movement.originalAmount, movement.originalCurrency)} / TRM ${Number(movement.exchangeRate || 1).toFixed(2)}</td>
                                <td>${movement.paymentMethod || ""}</td>
                                <td>${movement.description || ""}</td>
                                <td>${formatDate(movement.pinVirtualTimeT)}</td>
                                <td>${formatDate(movement.createdT || movement.createdAt)}</td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>${title}</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Id</th>
                                        <th>Tipo</th>
                                        <th>Monto COP</th>
                                        <th>Original / TRM</th>
                                        <th>Metodo pago</th>
                                        <th>Descripcion</th>
                                        <th>PVT negocio</th>
                                        <th>Fecha real</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function renderServices(services, products, catalogServices) {
                        const target = document.getElementById("services");
                        const activeProducts = products.filter(product => product.status === "ACTIVE");
                        const activeCatalogServices = catalogServices.filter(service => service.status === "ACTIVE");
                        const availableServices = services.filter(service => service.status !== "TERMINATED");
                        const catalogOptions = activeCatalogServices
                            .map(service => `<option value="${service.id}">${service.id} - ${service.name} - ${service.serviceType}</option>`)
                            .join("");
                        const serviceOptions = availableServices
                            .map(service => `<option value="${service.id}">${service.serviceName || service.serviceCode} - ${service.serviceType} - ${service.status}</option>`)
                            .join("");
                        const productOptions = activeProducts
                            .map(product => `<option value="${product.id}">${product.displayId || product.id} - ${product.code} - ${product.name} - ${Number(product.price).toFixed(2)} ${product.currency}</option>`)
                            .join("");
                        const createAction = `
                            <div class="bc-operation-panel">
                                <form id="serviceCreateForm" class="bc-inline-form" onsubmit="activateCatalogService(event)">
                                    <label>
                                        Servicio general
                                        <select id="newCatalogServiceId" name="newCatalogServiceId"${disabledAttr()} ${activeCatalogServices.length === 0 ? "disabled" : ""} required>
                                            <option value="">Seleccione un servicio general</option>
                                            ${catalogOptions}
                                        </select>
                                    </label>
                                    <button id="serviceSubmitButton" type="submit"${disabledAttr()} ${activeCatalogServices.length === 0 ? "disabled" : ""}>Activar servicio</button>
                                    <a class="secondary" href="/services" target="_blank" rel="noopener">Crear servicio general</a>
                                </form>
                                <form id="serviceEditForm" class="bc-inline-form" onsubmit="updateService(event)" style="display:none;">
                                    <label>
                                        Nombre del servicio
                                        <input id="editServiceName" name="editServiceName" type="text"${disabledAttr()} required>
                                    </label>
                                    <label>
                                        Tipo de servicio
                                        <select id="editServiceType" name="editServiceType"${disabledAttr()} required>
                                            <option value="MOBILE">MOBILE</option>
                                            <option value="INTERNET">INTERNET</option>
                                            <option value="TV">TV</option>
                                            <option value="GENERIC">GENERIC</option>
                                        </select>
                                    </label>
                                    <button type="submit"${disabledAttr()}>Guardar servicio</button>
                                    <button id="cancelServiceEditButton" class="muted" type="button"${disabledAttr()} onclick="cancelServiceEdit()">Cancelar edicion</button>
                                </form>
                                <form id="serviceProductForm" class="bc-inline-form" onsubmit="assignProductFromForm(event)">
                                    <label>
                                        Servicio
                                        <select id="assignServiceId" name="assignServiceId"${disabledAttr()} ${availableServices.length === 0 ? "disabled" : ""} required>
                                            <option value="">Seleccione un servicio</option>
                                            ${serviceOptions}
                                        </select>
                                    </label>
                                    <label>
                                        Producto activo
                                        <select id="assignProductId" name="assignProductId"${disabledAttr()} ${activeProducts.length === 0 ? "disabled" : ""} required>
                                            <option value="">Seleccione un producto</option>
                                            ${productOptions}
                                        </select>
                                    </label>
                                    <button type="submit"${disabledAttr()} ${availableServices.length === 0 || activeProducts.length === 0 ? "disabled" : ""}>Asociar producto</button>
                                </form>
                                <div class="toolbar bc-operation-actions">
                                    <a class="secondary" href="/products">Gestionar productos</a>
                                    <button type="button"${disabledAttr()} onclick="runAccountBilling()">Ejecutar billing de la cuenta</button>
                                </div>
                            </div>`;
                        if (services.length === 0) {
                            target.innerHTML = `<h2>Servicios</h2>${createAction}${message("info", "No hay servicios asociados a esta cuenta.")}`;
                            return;
                        }

                        const rows = services.map(service => `
                            <tr>
                                <td>${service.id}</td>
                                <td>${service.serviceName || ""}</td>
                                <td>${service.serviceCode}</td>
                                <td>${service.serviceType}</td>
                                <td><span class="status ${service.status.toLowerCase()}">${service.status}</span></td>
                                <td>${service.products.map(product => `${product.productCode} (${product.status})<br><span class="bc-time-note">PVT compra: ${formatDate(product.pinVirtualTimeT || product.assignedAt)}</span>`).join("<br>") || ""}</td>
                                <td>${formatDate(service.pinVirtualTimeT || service.activationDate || service.createdAt)}</td>
                                <td>${formatDate(service.createdT)}</td>
                                <td>${formatDate(service.updatedAt)}</td>
                                <td>
                                    <div class="actions">
                                        <button type="button"${disabledAttr()} onclick="startServiceEdit('${service.id}')">Editar</button>
                                        <button type="button"${disabledAttr()} onclick="prepareAssignProduct('${service.id}')">Assign Product</button>
                                        ${service.status === "ACTIVE" ? `<button class="muted" type="button"${disabledAttr()} onclick="serviceAction('${service.id}', 'suspend')">Suspender</button>` : ""}
                                        ${service.status === "SUSPENDED" ? `<button class="secondary" type="button"${disabledAttr()} onclick="serviceAction('${service.id}', 'reactivate')">Reactivar</button>` : ""}
                                        ${service.status !== "TERMINATED" ? `<button class="danger" type="button"${disabledAttr()} onclick="serviceAction('${service.id}', 'terminate')">Terminar</button>` : ""}
                                    </div>
                                </td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>Servicios</h2>
                            ${createAction}
                            <table>
                                <thead>
                                    <tr>
                                        <th>Id</th>
                                        <th>Nombre</th>
                                        <th>Codigo</th>
                                        <th>Tipo</th>
                                        <th>Estado</th>
                                        <th>Productos</th>
                                        <th>PVT alta</th>
                                        <th>Fecha real</th>
                                        <th>PVT actualizacion</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function focusServiceCreation() {
                        switchTab("services");
                        setTimeout(() => document.getElementById("newCatalogServiceId")?.focus(), 0);
                    }

                    function prepareAssignProduct(serviceId) {
                        switchTab("services");
                        const serviceSelect = document.getElementById("assignServiceId");
                        if (serviceSelect) {
                            serviceSelect.value = serviceId;
                        }
                        document.getElementById("assignProductId")?.focus();
                    }

                    function startServiceEdit(serviceId) {
                        const service = currentServices.find(item => item.id === serviceId);
                        if (!service) {
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("warning", "No fue posible encontrar el servicio seleccionado."));
                            return;
                        }
                        switchTab("services");
                        editingServiceId = service.id;
                        document.getElementById("serviceEditForm").style.display = "";
                        document.getElementById("editServiceName").value = service.serviceName || service.serviceCode;
                        document.getElementById("editServiceType").value = service.serviceType;
                        document.getElementById("editServiceName").focus();
                    }

                    function cancelServiceEdit() {
                        editingServiceId = null;
                        const form = document.getElementById("serviceEditForm");
                        if (form) {
                            form.reset();
                            form.style.display = "none";
                        }
                    }

                    function formatDate(value) {
                        if (!value) {
                            return "N/A";
                        }
                        return new Date(value).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" });
                    }

                    function formatDateOnly(value) {
                        if (!value) {
                            return "N/A";
                        }
                        return new Date(value).toLocaleDateString("es-CO", { year: "numeric", month: "short", day: "2-digit" });
                    }

                    function renderBillingCharges(charges, bills, invoices) {
                        const target = document.getElementById("billingCharges");
                        const billRows = bills.map(bill => {
                            const invoice = invoices.find(item => item.billingRunId === bill.billingRunId);
                            return `
                                <tr>
                                    <td><a class="muted" href="/bills/${bill.id}" target="_blank" rel="noopener">${bill.billNo}</a></td>
                                    <td>${formatDateOnly(bill.periodStart)} - ${formatDateOnly(bill.periodEnd)}</td>
                                    <td>${formatDate(bill.billDate)}</td>
                                    <td>${formatDate(bill.dueDate)}</td>
                                    <td><span class="status ${bill.status.toLowerCase()}">${bill.status}</span></td>
                                    <td>${money(bill.totalAmount, bill.currency)}</td>
                                    <td>${money(bill.paidAmount, bill.currency)}</td>
                                    <td>${money(bill.dueAmount, bill.currency)}</td>
                                    <td>${invoice ? `<a href="/invoices/${invoice.id}" target="_blank" rel="noopener">${invoice.invoiceNumber}</a>` : "No invoice"}</td>
                                    <td><a class="secondary" href="/api/bills/${bill.id}/csv">Export CSV</a></td>
                                </tr>
                            `;
                        }).join("");
                        const rows = charges.map(charge => `
                            <tr>
                                <td>${charge.billNo ? `<a href="/bills/${charge.billId}" target="_blank" rel="noopener">${charge.billNo}</a>` : ""}</td>
                                <td>${charge.billingDom || ""}</td>
                                <td>${charge.billingCycle || ""}</td>
                                <td>${formatDateOnly(charge.billPeriodStart)} - ${formatDateOnly(charge.billPeriodEnd)}</td>
                                <td>${charge.productCode}</td>
                                <td>${charge.chargeType}</td>
                                <td>${money(charge.amount, charge.currency)}</td>
                                <td>${charge.chargeDate}</td>
                                <td>${charge.transactionId || ""}</td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>Bills</h2>
                            <div class="toolbar"><button type="button"${disabledAttr()} onclick="runAccountBilling()">Run Billing for Account</button></div>
                            ${bills.length === 0 ? message("info", "No hay bills generados para esta cuenta.") : `
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Bill Number</th>
                                            <th>Periodo</th>
                                            <th>Emision</th>
                                            <th>Vencimiento</th>
                                            <th>Estado</th>
                                            <th>Total</th>
                                            <th>Paid</th>
                                            <th>Due</th>
                                            <th>Invoice</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>${billRows}</tbody>
                                </table>
                            `}
                            <h2>Billing charges</h2>
                            ${charges.length === 0 ? message("info", "No hay cargos de billing para esta cuenta.") : `
                            <table>
                                <thead>
                                    <tr>
                                        <th>Bill No</th>
                                        <th>DOM</th>
                                        <th>Ciclo</th>
                                        <th>Desde - Hasta</th>
                                        <th>Producto</th>
                                        <th>Tipo cargo</th>
                                        <th>Monto</th>
                                        <th>Fecha cargo</th>
                                        <th>Transaccion</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                            `}
                        `;
                    }

                    function renderInvoices(invoices) {
                        const target = document.getElementById("invoices");
                        if (invoices.length === 0) {
                            target.innerHTML = `<h2>Invoices</h2>${message("info", "No hay invoices generadas para esta cuenta.")}`;
                            return;
                        }

                        const rows = invoices.map(invoice => `
                            <tr>
                                <td>${invoice.invoiceNumber}</td>
                                <td>${invoice.issueDate}</td>
                                <td>${invoice.dueDate}</td>
                                <td><span class="status ${invoice.status.toLowerCase()}">${invoice.status}</span></td>
                                <td>${money(invoice.subtotal, invoice.currency)}</td>
                                <td>${money(invoice.taxAmount, invoice.currency)}</td>
                                <td>${money(invoice.totalAmount, invoice.currency)}</td>
                                <td>${money(invoice.amountPaid, invoice.currency)}</td>
                                <td>${money(invoice.creditAmount || 0, invoice.currency)}</td>
                                <td>${money(invoice.amountDue, invoice.currency)}</td>
                                <td>
                                    <div class="actions">
                                        <a class="muted" href="/invoices/${invoice.id}" target="_blank" rel="noopener">View</a>
                                        <a class="secondary" href="/api/invoices/${invoice.id}/csv">Export CSV</a>
                                        ${invoice.status !== "CANCELLED" && Number(invoice.amountDue) > 0 ? `<button class="secondary" type="button" onclick="createCreditNote('${invoice.id}')">Nota credito</button>` : ""}
                                        ${invoice.status !== "CANCELLED" && invoice.status !== "PAID" ? `<button type="button" onclick="markInvoiceSent('${invoice.id}')">Mark as sent</button>` : ""}
                                        ${invoice.status !== "CANCELLED" && invoice.status !== "PAID" ? `<button class="danger" type="button" onclick="cancelInvoice('${invoice.id}')">Cancel</button>` : ""}
                                    </div>
                                </td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>Invoices</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Invoice Number</th>
                                        <th>Issue Date</th>
                                        <th>Due Date</th>
                                        <th>Status</th>
                                        <th>Subtotal</th>
                                        <th>Tax</th>
                                        <th>Total</th>
                                        <th>Amount Paid</th>
                                        <th>Credit Notes</th>
                                        <th>Amount Due</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function renderHomePanels(account, payments, disputes, services, billingCharges, invoices) {
                        const activeServices = services.filter(service => service.status === "ACTIVE");
                        const lastCharge = billingCharges[billingCharges.length - 1];
                        const openInvoices = invoices.filter(invoice => invoice.amountDue > 0 && invoice.status !== "CANCELLED");
                        document.getElementById("homeAssets").innerHTML = `
                            <h2>Assets / Services activos</h2>
                            <div class="summary-grid">
                                <div class="summary-item"><span>Servicios activos</span><strong>${activeServices.length}</strong></div>
                                <div class="summary-item"><span>Productos asignados</span><strong>${services.flatMap(service => service.products).length}</strong></div>
                            </div>
                            ${activeServices.length === 0 ? `<div class="bc-empty">No services assigned</div>` : activeServices.slice(0, 4).map(service => `<p><strong>${service.serviceCode}</strong> - ${service.serviceType}</p>`).join("")}
                        `;
                        document.getElementById("homeBills").innerHTML = `
                            <h2>Bills summary</h2>
                            <div class="summary-grid">
                                <div class="summary-item"><span>Ultimos cargos</span><strong>${billingCharges.length}</strong></div>
                                <div class="summary-item"><span>Invoices abiertas</span><strong>${openInvoices.length}</strong></div>
                                <div class="summary-item"><span>Ultimo periodo</span><strong>${lastCharge ? lastCharge.billingPeriodLabel : "N/A"}</strong></div>
                                <div class="summary-item"><span>Balance</span><strong>${money(account.balance, account.currency)}</strong></div>
                            </div>
                        `;
                        document.getElementById("homeActivity").innerHTML = `
                            <h2>Recent activity</h2>
                            <p><strong>Recent payments:</strong> ${payments.length || "No payments found"}</p>
                            <p><strong>Recent disputes:</strong> ${disputes.length || "No disputes found"}</p>
                            <p><strong>Notes:</strong> No recent notes</p>
                        `;
                    }

                    function renderAssignedProducts(services) {
                        const products = services.flatMap(service => service.products.map(product => ({ ...product, serviceCode: service.serviceCode })));
                        const target = document.getElementById("assignedProducts");
                        if (products.length === 0) {
                            target.innerHTML = `<h2>Products</h2><p class="bc-time-note">PVT compra muestra la fecha de negocio tomada desde pin_virtual_time_t. Fecha real muestra created_t.</p>${message("info", "No products assigned")}`;
                            return;
                        }
                        const rows = products.map(product => `
                            <tr>
                                <td>${product.serviceCode}</td>
                                <td>${product.productCode}</td>
                                <td>${product.productName || ""}</td>
                                <td><span class="status ${product.status.toLowerCase()}">${product.status}</span></td>
                                <td>${formatDate(product.pinVirtualTimeT || product.assignedAt)}</td>
                                <td>${formatDate(product.createdT)}</td>
                                <td>${product.lastBilledAt ? formatDate(product.lastBilledAt) : ""}</td>
                                <td>${product.nextBillAt ? formatDate(product.nextBillAt) : ""}</td>
                                <td><a class="secondary" href="/products?editId=${product.productId}" target="_blank" rel="noopener">Editar producto</a></td>
                            </tr>
                        `).join("");
                        target.innerHTML = `
                            <h2>Products assigned</h2>
                            <p class="bc-time-note">PVT compra muestra la fecha de negocio tomada desde pin_virtual_time_t. Fecha real muestra created_t.</p>
                            <table>
                                <thead><tr><th>Service</th><th>Product</th><th>Name</th><th>Status</th><th>PVT compra</th><th>Fecha real</th><th>Ultimo billing PVT</th><th>Next bill PVT</th><th>Actions</th></tr></thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function renderEvents(events) {
                        const target = document.getElementById("events");
                        if (events.length === 0) {
                            target.innerHTML = `<h2>Events</h2>${message("info", "No events found")}`;
                            return;
                        }
                        const rows = events.slice().reverse().map(event => `
                            <tr>
                                <td>${event.type}</td>
                                <td>${event.entityType || ""}</td>
                                <td>${event.entityId || ""}</td>
                                <td>${event.description || ""}</td>
                                <td>${event.pinVirtualTimeT || event.createdAt}</td>
                            </tr>
                        `).join("");
                        target.innerHTML = `
                            <h2>Events</h2>
                            <table>
                                <thead><tr><th>Type</th><th>Entity</th><th>Entity ID</th><th>Description</th><th>Time</th></tr></thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    async function loadDetail() {
                        try {
                            const [account, virtualTime, billInfo, payments, refunds, transactions, disputes, services, products, serviceCatalog, billingCharges, bills, invoices, events] = await Promise.all([
                                fetchJson(`/api/accounts/${accountId}`),
                                fetchJson("/api/virtual-time"),
                                fetchJson(`/api/accounts/${accountId}/billinfo`),
                                fetchJson(`/api/accounts/${accountId}/payments`),
                                fetchJson(`/api/accounts/${accountId}/refunds`),
                                fetchJson(`/api/accounts/${accountId}/transactions`),
                                fetchJson(`/api/disputes?accountId=${accountId}`),
                                fetchJson(`/api/accounts/${accountId}/services`),
                                fetchJson(`/api/products?accountId=${accountId}`),
                                fetchJson("/api/service-catalog"),
                                fetchJson(`/api/accounts/${accountId}/billing-charges`),
                                fetchJson(`/api/accounts/${accountId}/bills`),
                                fetchJson(`/api/accounts/${accountId}/invoices`),
                                fetchJson(`/api/events?accountId=${accountId}`)
                            ]);

                            currentServices = services;
                            renderAccount(account, virtualTime, billInfo);
                            renderHomePanels(account, payments, disputes, services, billingCharges, invoices);
                            renderMovements("payments", "Pagos", payments);
                            renderMovements("refunds", "Reembolsos", refunds);
                            renderMovements("transactions", "Transacciones completas", transactions);
                            renderDisputes(disputes);
                            renderServices(services, products, serviceCatalog);
                            renderAssignedProducts(services);
                            renderBillingCharges(billingCharges, bills, invoices);
                            renderInvoices(invoices);
                            renderEvents(events);
                        } catch (error) {
                            document.getElementById("accountSummary").innerHTML = message("error", error.message);
                        }
                    }

                    async function activateCatalogService(event) {
                        if (event) {
                            event.preventDefault();
                        }
                        if (isClosed()) {
                            document.getElementById("services").innerHTML = message("warning", "This account is closed. Service operations are disabled.");
                            return;
                        }
                        const catalogServiceId = document.getElementById("newCatalogServiceId")?.value || "";
                        if (!catalogServiceId) {
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("warning", "Seleccione un servicio general para activar."));
                            return;
                        }
                        try {
                            await fetchJson(`/api/accounts/${accountId}/services/catalog/${catalogServiceId}/activate`, { method: "POST" });
                            await loadDetail();
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("success", "Servicio activado correctamente."));
                        } catch (error) {
                            document.getElementById("services").innerHTML = message("error", error.message);
                        }
                    }

                    async function updateService(event) {
                        if (event) {
                            event.preventDefault();
                        }
                        if (!editingServiceId) {
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("warning", "Seleccione un servicio para editar."));
                            return;
                        }
                        const service = currentServices.find(item => item.id === editingServiceId);
                        try {
                            await fetchJson(`/api/services/${editingServiceId}`, {
                                method: "PUT",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({
                                    serviceName: document.getElementById("editServiceName").value,
                                    serviceType: document.getElementById("editServiceType").value,
                                    serviceCode: service ? service.serviceCode : ""
                                })
                            });
                            cancelServiceEdit();
                            await loadDetail();
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("success", "Servicio actualizado correctamente."));
                        } catch (error) {
                            document.getElementById("services").innerHTML = message("error", error.message);
                        }
                    }

                    async function assignProductFromForm(event) {
                        if (event) {
                            event.preventDefault();
                        }
                        if (isClosed()) {
                            document.getElementById("services").innerHTML = message("warning", "This account is closed. Product assignment is disabled.");
                            return;
                        }
                        const serviceId = document.getElementById("assignServiceId")?.value;
                        const productId = document.getElementById("assignProductId")?.value;
                        if (!serviceId || !productId) {
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("warning", "Seleccione un servicio y un producto activo."));
                            return;
                        }
                        try {
                            await fetchJson(`/api/services/${serviceId}/products/${productId}`, { method: "POST" });
                            await loadDetail();
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("success", "Producto asignado correctamente."));
                        } catch (error) {
                            document.getElementById("services").innerHTML = message("error", error.message);
                        }
                    }

                    async function serviceAction(serviceId, action) {
                        if (isClosed()) {
                            document.getElementById("services").innerHTML = message("warning", "This account is closed. Service actions are disabled.");
                            return;
                        }
                        if (action === "terminate" && !confirm("Are you sure you want to terminate this service?")) {
                            return;
                        }
                        try {
                            await fetchJson(`/api/services/${serviceId}/${action}`, { method: "POST" });
                            await loadDetail();
                            document.getElementById("services").insertAdjacentHTML("afterbegin", message("success", "Servicio actualizado correctamente."));
                        } catch (error) {
                            document.getElementById("services").innerHTML = message("error", error.message);
                        }
                    }

                    async function runAccountBilling() {
                        if (isClosed()) {
                            document.getElementById("billingCharges").innerHTML = message("warning", "This account is closed. Billing is disabled.");
                            return;
                        }
                        if (!confirm("Run billing using current virtual time?")) {
                            return;
                        }
                        try {
                            const run = await fetchJson(`/api/billing/accounts/${accountId}/run`, { method: "POST" });
                            await loadDetail();
                            document.getElementById("billingCharges").insertAdjacentHTML("afterbegin", message("success", `Billing ejecutado. Cargos creados: ${run.chargesCreated}.`));
                        } catch (error) {
                            document.getElementById("billingCharges").innerHTML = message("error", error.message);
                        }
                    }

                    async function markInvoiceSent(invoiceId) {
                        try {
                            await fetchJson(`/api/invoices/${invoiceId}/sent`, { method: "POST" });
                            await loadDetail();
                            document.getElementById("invoices").insertAdjacentHTML("afterbegin", message("success", "Invoice marcada como enviada."));
                        } catch (error) {
                            document.getElementById("invoices").innerHTML = message("error", error.message);
                        }
                    }

                    async function cancelInvoice(invoiceId) {
                        const reason = window.prompt("Motivo de cancelacion de la invoice", "Cancelacion manual");
                        if (reason === null) {
                            return;
                        }
                        try {
                            await fetchJson(`/api/invoices/${invoiceId}/cancel`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ reason })
                            });
                            await loadDetail();
                            document.getElementById("invoices").insertAdjacentHTML("afterbegin", message("success", "Invoice cancelada correctamente."));
                        } catch (error) {
                            document.getElementById("invoices").innerHTML = message("error", error.message);
                        }
                    }

                    async function createCreditNote(invoiceId) {
                        const amountText = window.prompt("Monto de la nota de credito", "");
                        if (amountText === null) {
                            return;
                        }
                        const amount = Number(amountText);
                        const reason = window.prompt("Motivo de la nota de credito", "Ajuste comercial");
                        if (reason === null) {
                            return;
                        }
                        try {
                            await fetchJson(`/api/invoices/${invoiceId}/credit-notes`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({
                                    amount,
                                    reason,
                                    description: reason
                                })
                            });
                            await loadDetail();
                            document.getElementById("invoices").insertAdjacentHTML("afterbegin", message("success", "Nota de credito aplicada correctamente."));
                        } catch (error) {
                            document.getElementById("invoices").innerHTML = message("error", error.message);
                        }
                    }

                    async function resolveDispute(disputeId, action) {
                        const actionText = action === "approve" ? "aprobar" : "rechazar";
                        const resolutionNote = window.prompt(`Nota para ${actionText} la disputa`, "");
                        if (resolutionNote === null) {
                            return;
                        }

                        try {
                            await fetchJson(`/api/disputes/${disputeId}/${action}`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ resolutionNote })
                            });
                            await loadDetail();
                            document.getElementById("disputes").insertAdjacentHTML("afterbegin", message("success", "Disputa actualizada correctamente."));
                        } catch (error) {
                            document.getElementById("disputes").innerHTML = message("error", error.message);
                        }
                    }

                    async function createSettlement(disputeId) {
                        const amountText = window.prompt("Monto del settlement en COP", "");
                        if (amountText === null) {
                            return;
                        }

                        const amount = Number(amountText);
                        if (Number.isNaN(amount) || amount <= 0) {
                            document.getElementById("disputes").insertAdjacentHTML("afterbegin", message("warning", "El monto del settlement debe ser mayor a cero."));
                            return;
                        }

                        const note = window.prompt("Nota del settlement", "");
                        if (note === null) {
                            return;
                        }

                        try {
                            await fetchJson(`/api/disputes/${disputeId}/settlements`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ amount, note })
                            });
                            await loadDetail();
                            document.getElementById("disputes").insertAdjacentHTML("afterbegin", message("success", "Settlement creado correctamente. La disputa quedo cerrada como SETTLED."));
                        } catch (error) {
                            document.getElementById("disputes").innerHTML = message("error", error.message);
                        }
                    }

                    async function closeAccount() {
                        if (!confirm("Are you sure you want to close this account? Payments, refunds, write-offs and billing will be disabled.")) {
                            return;
                        }

                        try {
                            await fetchJson(`/api/accounts/${accountId}/close`, { method: "POST" });
                            await loadDetail();
                            document.getElementById("accountSummary").insertAdjacentHTML("afterbegin", message("success", "Cuenta cerrada correctamente."));
                        } catch (error) {
                            document.getElementById("accountSummary").innerHTML = message("error", error.message);
                        }
                    }

                    loadDetail();
                </script>
                """.formatted(accountId));
    }

    /**
     * Renderiza la lista de bills generados.
     *
     * @return pagina HTML de consulta de bills.
     */
    @GetMapping(value = "/bills", produces = MediaType.TEXT_HTML_VALUE)
    String bills() {
        return page("Bills", """
                <p>Consulta bills generados por billing con periodos, vencimientos, totales e invoices asociadas.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Dashboard</a>
                    <a class="secondary" href="/billing">Billing</a>
                    <a href="/invoices">Invoices</a>
                </div>
                <section id="result"></section>
                <script>
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    function formatDate(value) {
                        return value ? new Date(value).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" }) : "N/A";
                    }
                    function formatDateOnly(value) {
                        return value ? new Date(value).toLocaleDateString("es-CO", { year: "numeric", month: "short", day: "2-digit" }) : "N/A";
                    }
                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }
                    async function loadBills() {
                        document.getElementById("result").innerHTML = message("info", "Consultando bills...");
                        try {
                            const [billsResponse, invoicesResponse] = await Promise.all([fetch("/api/bills"), fetch("/api/invoices")]);
                            if (!billsResponse.ok) throw new Error(await readError(billsResponse, "No fue posible consultar bills."));
                            if (!invoicesResponse.ok) throw new Error(await readError(invoicesResponse, "No fue posible consultar invoices."));
                            const bills = await billsResponse.json();
                            const invoices = await invoicesResponse.json();
                            if (bills.length === 0) {
                                document.getElementById("result").innerHTML = message("info", "No hay bills generados.");
                                return;
                            }
                            const rows = bills.map(bill => {
                                const invoice = invoices.find(item => item.billingRunId === bill.billingRunId && item.accountId === bill.accountId);
                                return `
                                    <tr>
                                        <td><a class="muted" href="/bills/${bill.id}" target="_blank" rel="noopener">${bill.billNo}</a></td>
                                        <td>${bill.accountId}</td>
                                        <td>${formatDateOnly(bill.periodStart)} - ${formatDateOnly(bill.periodEnd)}</td>
                                        <td>${formatDate(bill.billDate)}</td>
                                        <td>${formatDate(bill.dueDate)}</td>
                                        <td><span class="status ${bill.status.toLowerCase()}">${bill.status}</span></td>
                                        <td>${money(bill.totalAmount, bill.currency)}</td>
                                        <td>${money(bill.paidAmount, bill.currency)}</td>
                                        <td>${money(bill.dueAmount, bill.currency)}</td>
                                        <td>${invoice ? `<a href="/invoices/${invoice.id}" target="_blank" rel="noopener">${invoice.invoiceNumber}</a>` : "No invoice"}</td>
                                        <td><a class="secondary" href="/api/bills/${bill.id}/csv">CSV</a></td>
                                    </tr>
                                `;
                            }).join("");
                            document.getElementById("result").innerHTML = `
                                <h2>Bills generados</h2>
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Bill Number</th>
                                            <th>Account</th>
                                            <th>Periodo</th>
                                            <th>Emision</th>
                                            <th>Vencimiento</th>
                                            <th>Estado</th>
                                            <th>Total</th>
                                            <th>Paid</th>
                                            <th>Due</th>
                                            <th>Invoice</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    loadBills();
                </script>
                """);
    }

    /**
     * Renderiza el detalle operativo de un bill.
     *
     * @param billId bill consultado.
     * @return pagina HTML con tabs de items, movimientos, invoice y eventos.
     */
    @GetMapping(value = "/bills/{billId}", produces = MediaType.TEXT_HTML_VALUE)
    String billDetail(@PathVariable String billId) {
        return page("Bill Detail", """
                <section id="result"></section>
                <script>
                    const billId = "%s";
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    function formatDate(value) {
                        return value ? new Date(value).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" }) : "N/A";
                    }
                    function formatDateOnly(value) {
                        return value ? new Date(value).toLocaleDateString("es-CO", { year: "numeric", month: "short", day: "2-digit" }) : "N/A";
                    }
                    async function fetchJson(url) {
                        const response = await fetch(url);
                        if (!response.ok) {
                            const error = await response.json().catch(() => ({}));
                            throw new Error(error.detail || "No fue posible completar la consulta.");
                        }
                        return response.json();
                    }
                    function switchBillTab(tabName) {
                        document.querySelectorAll(".bc-tab").forEach(tab => tab.classList.toggle("active", tab.dataset.tab === tabName));
                        document.querySelectorAll(".bc-tab-panel").forEach(panel => panel.classList.toggle("active", panel.id === `bill-tab-${tabName}`));
                    }
                    function movementRows(movements) {
                        if (movements.length === 0) return `<tr><td colspan="5">Sin movimientos en el periodo del bill.</td></tr>`;
                        return movements.map(item => `<tr><td>${item.id}</td><td>${item.type}</td><td>${money(item.amount, item.currency)}</td><td>${item.description || ""}</td><td>${formatDate(item.createdAt)}</td></tr>`).join("");
                    }
                    function inPeriod(item, start, end) {
                        const value = new Date(item.createdAt || item.chargeDate);
                        return value >= new Date(start) && value <= new Date(end);
                    }
                    async function loadBill() {
                        try {
                            const bill = await fetchJson(`/api/bills/${billId}`);
                            const [account, payments, refunds, writeOffs, disputes, invoices, events] = await Promise.all([
                                fetchJson(`/api/accounts/${bill.accountId}`),
                                fetchJson(`/api/accounts/${bill.accountId}/payments`),
                                fetchJson(`/api/accounts/${bill.accountId}/refunds`),
                                fetchJson(`/api/accounts/${bill.accountId}/write-offs`),
                                fetchJson(`/api/disputes?accountId=${bill.accountId}`),
                                fetchJson(`/api/accounts/${bill.accountId}/invoices`),
                                fetchJson(`/api/events?accountId=${bill.accountId}`)
                            ]);
                            const invoice = invoices.find(item => item.billingRunId === bill.billingRunId);
                            const charges = bill.items || [];
                            const products = [...new Map(charges.filter(item => item.productCode).map(item => [item.productId, item])).values()];
                            const periodPayments = payments.filter(item => inPeriod(item, bill.periodStart, bill.periodEnd));
                            const periodRefunds = refunds.filter(item => inPeriod(item, bill.periodStart, bill.periodEnd));
                            const periodWriteOffs = writeOffs.filter(item => inPeriod(item, bill.periodStart, bill.periodEnd));
                            const periodDisputes = disputes.filter(item => inPeriod(item, bill.periodStart, bill.periodEnd));
                            const relatedEvents = events.filter(event => event.entityId === bill.id || event.entityId === bill.billingRunId || (invoice && event.entityId === invoice.id));
                            document.getElementById("result").innerHTML = `
                                <section class="bc-bill-header">
                                    <div>
                                        <span class="bc-module-meta">Bill workspace</span>
                                        <h2>Bill Number: ${bill.billNo}</h2>
                                        <p>Account Number ${bill.accountId} - ${account.ownerName}</p>
                                        <p><strong>Period:</strong> ${formatDateOnly(bill.periodStart)} - ${formatDateOnly(bill.periodEnd)}</p>
                                    </div>
                                    <div>
                                        <span class="status ${bill.status.toLowerCase()}">${bill.status}</span>
                                        <p><strong>Issue Date:</strong> ${formatDate(bill.billDate)}</p>
                                        <p><strong>Due Date:</strong> ${formatDate(bill.dueDate)}</p>
                                    </div>
                                </section>
                                <section class="summary-grid">
                                    <div class="summary-item"><span>Total Charges</span><strong>${money(bill.totalAmount, bill.currency)}</strong></div>
                                    <div class="summary-item"><span>Payments</span><strong>${money(periodPayments.reduce((sum, item) => sum + Number(item.amount), 0), bill.currency)}</strong></div>
                                    <div class="summary-item"><span>Refunds</span><strong>${money(periodRefunds.reduce((sum, item) => sum + Number(item.amount), 0), bill.currency)}</strong></div>
                                    <div class="summary-item"><span>Write-Offs / Adjustments</span><strong>${money(periodWriteOffs.reduce((sum, item) => sum + Number(item.amount), 0), bill.currency)}</strong></div>
                                    <div class="summary-item"><span>Amount Due</span><strong>${money(invoice ? invoice.amountDue : bill.dueAmount, bill.currency)}</strong></div>
                                </section>
                                <div class="toolbar bc-no-print">
                                    <a class="muted" href="/accounts/${bill.accountId}">Back to Account</a>
                                    ${invoice ? `<a class="secondary" href="/invoices/${invoice.id}">View Invoice</a>` : ""}
                                    <a href="/api/bills/${bill.id}/csv">Export CSV</a>
                                    <button class="muted" type="button" onclick="window.print()">Print</button>
                                </div>
                                <nav class="bc-tabs">
                                    <button class="bc-tab active" data-tab="overview" type="button" onclick="switchBillTab('overview')">Overview</button>
                                    <button class="bc-tab" data-tab="charges" type="button" onclick="switchBillTab('charges')">Charges</button>
                                    <button class="bc-tab" data-tab="assets" type="button" onclick="switchBillTab('assets')">Services & Products</button>
                                    <button class="bc-tab" data-tab="payments" type="button" onclick="switchBillTab('payments')">Payments</button>
                                    <button class="bc-tab" data-tab="refunds" type="button" onclick="switchBillTab('refunds')">Refunds</button>
                                    <button class="bc-tab" data-tab="writeoffs" type="button" onclick="switchBillTab('writeoffs')">Write-Offs</button>
                                    <button class="bc-tab" data-tab="disputes" type="button" onclick="switchBillTab('disputes')">Disputes</button>
                                    <button class="bc-tab" data-tab="invoice" type="button" onclick="switchBillTab('invoice')">Invoice</button>
                                    <button class="bc-tab" data-tab="events" type="button" onclick="switchBillTab('events')">Events</button>
                                </nav>
                                <section id="bill-tab-overview" class="bc-tab-panel active"><section><h2>Overview</h2><p>Billing run ${bill.billingRunId}</p><p>Invoice: ${invoice ? invoice.invoiceNumber : "No invoice generated for this bill yet"}</p></section></section>
                                <section id="bill-tab-charges" class="bc-tab-panel"><section><h2>Charges</h2><table><thead><tr><th>Date</th><th>Service</th><th>Product</th><th>Description</th><th>Amount</th><th>Currency</th></tr></thead><tbody>${charges.map(item => `<tr><td>${formatDate(item.itemDate)}</td><td>${item.serviceCode || ""}</td><td>${item.productCode}</td><td>${item.description}</td><td>${money(item.amount, item.currency)}</td><td>${item.currency}</td></tr>`).join("")}</tbody></table></section></section>
                                <section id="bill-tab-assets" class="bc-tab-panel"><section><h2>Services & Products</h2><table><thead><tr><th>Service</th><th>Product</th><th>Name</th><th>Amount</th></tr></thead><tbody>${products.map(item => `<tr><td>${item.serviceCode || ""}</td><td>${item.productCode}</td><td>${item.productName}</td><td>${money(item.amount, item.currency)}</td></tr>`).join("")}</tbody></table></section></section>
                                <section id="bill-tab-payments" class="bc-tab-panel"><section><h2>Payments</h2><table><thead><tr><th>Payment ID</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th></tr></thead><tbody>${movementRows(periodPayments)}</tbody></table></section></section>
                                <section id="bill-tab-refunds" class="bc-tab-panel"><section><h2>Refunds</h2><table><thead><tr><th>Refund ID</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th></tr></thead><tbody>${movementRows(periodRefunds)}</tbody></table></section></section>
                                <section id="bill-tab-writeoffs" class="bc-tab-panel"><section><h2>Write-Offs</h2><table><thead><tr><th>Write-Off ID</th><th>Type</th><th>Amount</th><th>Description</th><th>Date</th></tr></thead><tbody>${movementRows(periodWriteOffs)}</tbody></table></section></section>
                                <section id="bill-tab-disputes" class="bc-tab-panel"><section><h2>Disputes</h2><table><thead><tr><th>Dispute</th><th>Status</th><th>Amount</th><th>Reason</th><th>Date</th></tr></thead><tbody>${periodDisputes.map(item => `<tr><td>${item.id}</td><td><span class="status ${item.status.toLowerCase()}">${item.status}</span></td><td>${money(item.amount, item.currency)}</td><td>${item.reason}</td><td>${formatDate(item.createdAt)}</td></tr>`).join("") || `<tr><td colspan="5">Sin disputas en el periodo del bill.</td></tr>`}</tbody></table></section></section>
                                <section id="bill-tab-invoice" class="bc-tab-panel"><section><h2>Invoice</h2>${invoice ? `<div class="summary-grid"><div class="summary-item"><span>Invoice Number</span><strong>${invoice.invoiceNumber}</strong></div><div class="summary-item"><span>Status</span><strong>${invoice.status}</strong></div><div class="summary-item"><span>Total</span><strong>${money(invoice.totalAmount, invoice.currency)}</strong></div><div class="summary-item"><span>Amount Due</span><strong>${money(invoice.amountDue, invoice.currency)}</strong></div></div><a class="secondary" href="/invoices/${invoice.id}">View Invoice</a>` : `<div class="bc-empty">No invoice generated for this bill yet</div>`}</section></section>
                                <section id="bill-tab-events" class="bc-tab-panel"><section><h2>Events</h2><table><thead><tr><th>Type</th><th>Entity</th><th>Description</th><th>Time</th></tr></thead><tbody>${relatedEvents.map(event => `<tr><td>${event.type}</td><td>${event.entityType}</td><td>${event.description}</td><td>${formatDate(event.pinVirtualTimeT || event.createdAt)}</td></tr>`).join("") || `<tr><td colspan="4">Sin eventos directos del bill.</td></tr>`}</tbody></table></section></section>
                            `;
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    loadBill();
                </script>
                """.formatted(billId));
    }

    /**
     * Renderiza la lista de invoices generadas por billing.
     *
     * @return pagina HTML de consulta de facturas.
     */
    @GetMapping(value = "/invoices", produces = MediaType.TEXT_HTML_VALUE)
    String invoices() {
        return page("Invoices", """
                <p>Consulta facturas generadas desde billing, filtra por cuenta o estado y exporta CSV.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Dashboard</a>
                    <a class="secondary" href="/billing">Billing</a>
                </div>
                <form id="invoiceFilterForm" class="filter-form">
                    <label>
                        Account Number
                        <input id="accountId" name="accountId" type="text" placeholder="Cuenta">
                    </label>
                    <label>
                        Status
                        <select id="status" name="status">
                            <option value="">Todos</option>
                            <option value="ISSUED">ISSUED</option>
                            <option value="SENT">SENT</option>
                            <option value="PARTIALLY_PAID">PARTIALLY_PAID</option>
                            <option value="PAID">PAID</option>
                            <option value="PARTIALLY_CREDITED">PARTIALLY_CREDITED</option>
                            <option value="CREDITED">CREDITED</option>
                            <option value="CANCELLED">CANCELLED</option>
                        </select>
                    </label>
                    <label>
                        Fecha desde
                        <input id="from" name="from" type="datetime-local">
                    </label>
                    <label>
                        Fecha hasta
                        <input id="to" name="to" type="datetime-local">
                    </label>
                    <button type="submit">Consultar invoices</button>
                </form>
                <section id="result"></section>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }
                    function buildQuery() {
                        const query = new URLSearchParams();
                        const accountId = document.getElementById("accountId").value.trim();
                        const status = document.getElementById("status").value;
                        const from = document.getElementById("from").value;
                        const to = document.getElementById("to").value;
                        if (accountId) query.set("accountId", accountId);
                        if (status) query.set("status", status);
                        if (from) query.set("from", from);
                        if (to) query.set("to", to);
                        const value = query.toString();
                        return value ? `?${value}` : "";
                    }
                    async function loadInvoices() {
                        document.getElementById("result").innerHTML = message("info", "Consultando invoices...");
                        try {
                            const response = await fetch(`/api/invoices${buildQuery()}`);
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar invoices."));
                            }
                            const invoices = await response.json();
                            if (invoices.length === 0) {
                                document.getElementById("result").innerHTML = message("info", "No hay invoices para los filtros seleccionados.");
                                return;
                            }
                            const rows = invoices.map(invoice => `
                                <tr>
                                    <td>${invoice.invoiceNumber}</td>
                                    <td>${invoice.accountId}<br>${invoice.accountOwnerName || ""}</td>
                                    <td>${invoice.issueDate}</td>
                                    <td>${invoice.dueDate}</td>
                                    <td><span class="status ${invoice.status.toLowerCase()}">${invoice.status}</span></td>
                                    <td>${money(invoice.totalAmount, invoice.currency)}</td>
                                    <td>${money(invoice.creditAmount || 0, invoice.currency)}</td>
                                    <td>${money(invoice.amountDue, invoice.currency)}</td>
                                    <td>
                                        <div class="actions">
                                            <a class="muted" href="/invoices/${invoice.id}" target="_blank" rel="noopener">View</a>
                                            <a class="secondary" href="/accounts/${invoice.accountId}" target="_blank" rel="noopener">Account</a>
                                            <a href="/api/invoices/${invoice.id}/csv">CSV</a>
                                        </div>
                                    </td>
                                </tr>
                            `).join("");
                            document.getElementById("result").innerHTML = `
                                <h2>Facturas generadas</h2>
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Invoice Number</th>
                                            <th>Account / Customer</th>
                                            <th>Issue Date</th>
                                            <th>Due Date</th>
                                            <th>Status</th>
                                            <th>Total</th>
                                            <th>Credit Notes</th>
                                            <th>Amount Due</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    document.getElementById("invoiceFilterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        loadInvoices();
                    });
                    if (params.get("accountId")) {
                        document.getElementById("accountId").value = params.get("accountId");
                    }
                    loadInvoices();
                </script>
                """);
    }

    /**
     * Renderiza el detalle imprimible de una invoice.
     *
     * @param invoiceId factura consultada.
     * @return pagina HTML de detalle.
     */
    @GetMapping(value = "/invoices/{invoiceId}", produces = MediaType.TEXT_HTML_VALUE)
    String invoiceDetail(@PathVariable String invoiceId) {
        return page("Invoice Detail", """
                <section id="result"></section>
                <script>
                    const invoiceId = "%s";
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    async function fetchJson(url, options) {
                        const response = await fetch(url, options);
                        if (!response.ok) {
                            const error = await response.json().catch(() => ({}));
                            throw new Error(error.detail || "No fue posible completar la operacion.");
                        }
                        return response.json();
                    }
                    async function loadInvoice() {
                        try {
                            const [invoice, creditNotes] = await Promise.all([
                                fetchJson(`/api/invoices/${invoiceId}`),
                                fetchJson(`/api/invoices/${invoiceId}/credit-notes`)
                            ]);
                            const rows = invoice.lines.map(line => `
                                <tr>
                                    <td>${line.chargeDate || ""}</td>
                                    <td>${line.serviceCode || ""}</td>
                                    <td>${line.productCode || ""}</td>
                                    <td>${line.description || ""}</td>
                                    <td>${line.lineType}</td>
                                    <td>${Number(line.quantity).toFixed(2)}</td>
                                    <td>${money(line.unitAmount, line.currency)}</td>
                                    <td>${money(line.totalAmount, line.currency)}</td>
                                </tr>
                            `).join("");
                            const creditRows = creditNotes.map(note => `
                                <tr>
                                    <td>${note.creditNoteNumber}</td>
                                    <td>${note.issueDate}</td>
                                    <td><span class="status ${note.status.toLowerCase()}">${note.status}</span></td>
                                    <td>${note.reason || ""}</td>
                                    <td>${money(note.totalAmount, note.currency)}</td>
                                    <td>
                                        ${note.status !== "CANCELLED" ? `<button class="danger" type="button" onclick="cancelCreditNote('${note.id}')">Cancelar nota</button>` : ""}
                                    </td>
                                </tr>
                            `).join("");
                            document.getElementById("result").innerHTML = `
                                <section class="bc-invoice-document">
                                    <div class="toolbar bc-no-print">
                                        <a class="muted" href="/invoices">Back to Invoices</a>
                                        <a class="secondary" href="/accounts/${invoice.accountId}">Back to Account</a>
                                        <a href="/api/invoices/${invoice.id}/csv">Export CSV</a>
                                        ${invoice.status !== "PAID" && invoice.status !== "CANCELLED" ? `<button type="button" onclick="markAsSent()">Mark as Sent</button>` : ""}
                                        ${invoice.status !== "PAID" && invoice.status !== "CANCELLED" ? `<button class="secondary" type="button" onclick="applyPayment()">Apply Payment</button>` : ""}
                                        ${invoice.status !== "CANCELLED" && Number(invoice.amountDue) > 0 ? `<button class="secondary" type="button" onclick="createCreditNote()">Crear nota de credito</button>` : ""}
                                        ${invoice.status !== "PAID" && invoice.status !== "CANCELLED" ? `<button class="danger" type="button" onclick="cancelInvoice()">Cancel Invoice</button>` : ""}
                                        <button class="muted" type="button" onclick="window.print()">Print</button>
                                    </div>
                                    <header class="bc-invoice-header">
                                        <div>
                                            <h2>BRMC Billing Care</h2>
                                            <p>Invoice document</p>
                                        </div>
                                        <div>
                                            <strong>${invoice.invoiceNumber}</strong><br>
                                            <span class="status ${invoice.status.toLowerCase()}">${invoice.status}</span>
                                        </div>
                                    </header>
                                    <section class="bc-invoice-summary">
                                        <div><span>Account Number</span><strong>${invoice.accountId}</strong></div>
                                        <div><span>Account Holder</span><strong>${invoice.accountOwnerName}</strong></div>
                                        <div><span>Email</span><strong>${invoice.accountEmail || "N/A"}</strong></div>
                                        <div><span>Currency</span><strong>${invoice.currency}</strong></div>
                                        <div><span>Issue Date</span><strong>${invoice.issueDate}</strong></div>
                                        <div><span>Due Date</span><strong>${invoice.dueDate}</strong></div>
                                    </section>
                                    <section class="summary-grid">
                                        <div class="summary-item"><span>Subtotal</span><strong>${money(invoice.subtotal, invoice.currency)}</strong></div>
                                        <div class="summary-item"><span>Tax</span><strong>${money(invoice.taxAmount, invoice.currency)}</strong></div>
                                        <div class="summary-item"><span>Total</span><strong>${money(invoice.totalAmount, invoice.currency)}</strong></div>
                                        <div class="summary-item"><span>Amount Paid</span><strong>${money(invoice.amountPaid, invoice.currency)}</strong></div>
                                        <div class="summary-item"><span>Credit Notes</span><strong>${money(invoice.creditAmount || 0, invoice.currency)}</strong></div>
                                        <div class="summary-item"><span>Amount Due</span><strong>${money(invoice.amountDue, invoice.currency)}</strong></div>
                                    </section>
                                    <h2>Notas de credito</h2>
                                    ${creditNotes.length === 0 ? message("info", "No hay notas de credito para esta invoice.") : `
                                    <table>
                                        <thead>
                                            <tr><th>Numero</th><th>Fecha</th><th>Estado</th><th>Motivo</th><th>Total</th><th>Acciones</th></tr>
                                        </thead>
                                        <tbody>${creditRows}</tbody>
                                    </table>
                                    `}
                                    <h2>Detalle de cargos</h2>
                                    <table>
                                        <thead>
                                            <tr><th>Charge Date</th><th>Service</th><th>Product</th><th>Description</th><th>Line Type</th><th>Quantity</th><th>Unit Amount</th><th>Total</th></tr>
                                        </thead>
                                        <tbody>${rows}</tbody>
                                    </table>
                                </section>
                            `;
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    async function markAsSent() {
                        await fetchJson(`/api/invoices/${invoiceId}/sent`, { method: "POST" });
                        await loadInvoice();
                    }
                    async function cancelInvoice() {
                        const reason = window.prompt("Motivo de cancelacion", "Cancelacion manual");
                        if (reason === null) return;
                        await fetchJson(`/api/invoices/${invoiceId}/cancel`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ reason })
                        });
                        await loadInvoice();
                    }
                    async function applyPayment() {
                        const amountText = window.prompt("Monto a aplicar a la invoice", "");
                        if (amountText === null) return;
                        const amount = Number(amountText);
                        await fetchJson(`/api/invoices/${invoiceId}/payment`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ amount })
                        });
                        await loadInvoice();
                    }
                    async function createCreditNote() {
                        const amountText = window.prompt("Monto de la nota de credito", "");
                        if (amountText === null) return;
                        const amount = Number(amountText);
                        const reason = window.prompt("Motivo de la nota de credito", "Ajuste comercial");
                        if (reason === null) return;
                        await fetchJson(`/api/invoices/${invoiceId}/credit-notes`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({
                                amount,
                                reason,
                                description: reason
                            })
                        });
                        await loadInvoice();
                    }
                    async function cancelCreditNote(creditNoteId) {
                        const reason = window.prompt("Motivo de cancelacion de la nota de credito", "Cancelacion manual");
                        if (reason === null) return;
                        await fetchJson(`/api/credit-notes/${creditNoteId}/cancel`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ reason })
                        });
                        await loadInvoice();
                    }
                    loadInvoice();
                </script>
                """.formatted(invoiceId));
    }

    /**
     * Renderiza la pagina de administracion de fecha virtual.
     *
     * @return pagina HTML para consultar, actualizar o resetear la fecha virtual.
     */
    @GetMapping(value = "/virtual-time", produces = MediaType.TEXT_HTML_VALUE)
    String virtualTime() {
        return page("Virtual Time", """
                <p>Simulacion de pin_virtual_time para controlar la fecha logica usada por billing.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                </div>
                <form id="virtualTimeForm">
                    <label>
                        Fecha virtual
                        <input id="currentVirtualTime" name="currentVirtualTime" type="datetime-local" required>
                    </label>
                    <label>
                        Actualizado por
                        <input id="updatedBy" name="updatedBy" type="text" value="admin">
                    </label>
                    <button type="submit">Actualizar</button>
                    <button class="muted" type="button" onclick="resetVirtualTime()">Reset fecha real</button>
                </form>
                <section id="result"></section>
                <script>
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    function toInputValue(value) {
                        return value ? value.substring(0, 16) : "";
                    }

                    async function loadVirtualTime() {
                        const response = await fetch("/api/virtual-time");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible consultar la fecha virtual."));
                        }
                        const data = await response.json();
                        document.getElementById("currentVirtualTime").value = toInputValue(data.currentVirtualTime);
                        document.getElementById("result").innerHTML = `
                            <h2>Fecha virtual actual</h2>
                            <table>
                                <tr><th>Fecha virtual</th><td>${data.currentVirtualTime}</td></tr>
                                <tr><th>Configurada</th><td>${data.configured ? "SI" : "NO, usando fecha real"}</td></tr>
                                <tr><th>Actualizado por</th><td>${data.updatedBy || ""}</td></tr>
                            </table>
                        `;
                    }

                    document.getElementById("virtualTimeForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const currentVirtualTime = document.getElementById("currentVirtualTime").value;
                        const updatedBy = document.getElementById("updatedBy").value;
                        try {
                            const response = await fetch("/api/virtual-time", {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ currentVirtualTime, updatedBy })
                            });
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible actualizar la fecha virtual."));
                            }
                            await loadVirtualTime();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Fecha virtual actualizada correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });

                    async function resetVirtualTime() {
                        try {
                            const response = await fetch("/api/virtual-time/reset", {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ updatedBy: document.getElementById("updatedBy").value })
                            });
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible reiniciar la fecha virtual."));
                            }
                            await loadVirtualTime();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Fecha virtual reiniciada correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }

                    loadVirtualTime().catch(error => document.getElementById("result").innerHTML = message("error", error.message));
                </script>
                """);
    }

    /**
     * Renderiza el gestor administrativo de usuarios.
     *
     * @return pagina HTML de administracion de usuarios.
     */
    @GetMapping(value = "/users", produces = MediaType.TEXT_HTML_VALUE)
    String users() {
        return page("Gestor de usuarios", """
                <p>Solo ADMIN puede crear usuarios, cambiar roles y activar o desactivar accesos.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver al dashboard</a>
                </div>
                <form id="userForm" class="bc-inline-form">
                    <label>
                        Username
                        <input id="username" name="username" type="text" maxlength="60" required>
                    </label>
                    <label>
                        Nombre
                        <input id="fullName" name="fullName" type="text" maxlength="120" required>
                    </label>
                    <label>
                        Email
                        <input id="email" name="email" type="email" maxlength="160">
                    </label>
                    <label>
                        Password
                        <input id="password" name="password" type="password" maxlength="120" placeholder="Obligatoria al crear">
                    </label>
                    <label>
                        Rol
                        <select id="role" name="role">
                            <option value="USER">USER</option>
                            <option value="ADMIN">ADMIN</option>
                        </select>
                    </label>
                    <label>
                        Estado
                        <select id="status" name="status">
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </label>
                    <button id="userSubmitButton" type="submit">Crear usuario</button>
                    <button id="cancelEditButton" class="muted" type="button" onclick="cancelEdit()" style="display:none;">Cancelar edicion</button>
                </form>
                <section id="result"></section>
                <script>
                    let currentUsers = [];
                    let editingUsername = null;

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    async function fetchJson(url, options) {
                        const response = await fetch(url, options);
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible completar la operacion."));
                        }
                        return response.json();
                    }

                    async function loadUsers() {
                        const result = document.getElementById("result");
                        result.innerHTML = message("info", "Consultando usuarios...");
                        try {
                            currentUsers = await fetchJson("/api/users");
                            const rows = currentUsers.map(user => `
                                <tr>
                                    <td>${user.username}</td>
                                    <td>${user.fullName}</td>
                                    <td>${user.email || ""}</td>
                                    <td><span class="status ${user.role === "ADMIN" ? "settled" : "active"}">${user.role}</span></td>
                                    <td><span class="status ${user.status === "ACTIVE" ? "active" : "closed"}">${user.status}</span></td>
                                    <td>${user.createdAt}</td>
                                    <td>
                                        <div class="actions">
                                            <button type="button" onclick="startEdit('${user.username}')">Editar</button>
                                            ${user.status === "ACTIVE"
                                                ? `<button class="danger" type="button" onclick="changeStatus('${user.username}', 'deactivate')">Desactivar</button>`
                                                : `<button class="secondary" type="button" onclick="changeStatus('${user.username}', 'activate')">Activar</button>`}
                                        </div>
                                    </td>
                                </tr>
                            `).join("");
                            result.innerHTML = `
                                <h2>Usuarios creados</h2>
                                <table>
                                    <thead>
                                        <tr><th>Username</th><th>Nombre</th><th>Email</th><th>Rol</th><th>Estado</th><th>Creado</th><th>Acciones</th></tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }

                    document.getElementById("userForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const payload = {
                            username: document.getElementById("username").value,
                            password: document.getElementById("password").value,
                            fullName: document.getElementById("fullName").value,
                            email: document.getElementById("email").value,
                            role: document.getElementById("role").value,
                            status: document.getElementById("status").value
                        };
                        try {
                            await fetchJson(editingUsername ? `/api/users/${editingUsername}` : "/api/users", {
                                method: editingUsername ? "PUT" : "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify(payload)
                            });
                            const wasEditing = editingUsername !== null;
                            cancelEdit();
                            await loadUsers();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", wasEditing ? "Usuario actualizado correctamente." : "Usuario creado correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });

                    function startEdit(username) {
                        const user = currentUsers.find(item => item.username === username);
                        if (!user) {
                            return;
                        }
                        editingUsername = username;
                        document.getElementById("username").value = user.username;
                        document.getElementById("username").disabled = true;
                        document.getElementById("fullName").value = user.fullName;
                        document.getElementById("email").value = user.email || "";
                        document.getElementById("password").value = "";
                        document.getElementById("role").value = user.role;
                        document.getElementById("status").value = user.status;
                        document.getElementById("userSubmitButton").textContent = "Guardar usuario";
                        document.getElementById("cancelEditButton").style.display = "";
                        document.getElementById("fullName").focus();
                    }

                    function cancelEdit() {
                        editingUsername = null;
                        document.getElementById("userForm").reset();
                        document.getElementById("username").disabled = false;
                        document.getElementById("role").value = "USER";
                        document.getElementById("status").value = "ACTIVE";
                        document.getElementById("userSubmitButton").textContent = "Crear usuario";
                        document.getElementById("cancelEditButton").style.display = "none";
                    }

                    async function changeStatus(username, action) {
                        try {
                            await fetchJson(`/api/users/${username}/${action}`, { method: "POST" });
                            await loadUsers();
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }

                    loadUsers();
                </script>
                """);
    }

    /**
     * Renderiza la pagina de catalogo de productos.
     *
     * @return pagina HTML para listar, crear, editar, activar e inactivar productos.
     */
    @GetMapping(value = "/products", produces = MediaType.TEXT_HTML_VALUE)
    String products() {
        return page("Products", """
                <p>Product Catalog telco/BRM: ofertas comerciales facturables por codigo funcional y Product ID visible.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                </div>
                <form id="productFilterForm" class="filter-form">
                    <label>
                        Codigo o nombre
                        <input id="productSearch" name="productSearch" type="text" placeholder="PLAN_MOVIL_10GB">
                    </label>
                    <label>
                        Tipo
                        <select id="productFilterType" name="productFilterType">
                            <option value="">Todos</option>
                            <option value="RECURRING">RECURRING</option>
                            <option value="ONE_TIME">ONE_TIME</option>
                        </select>
                    </label>
                    <label>
                        Estado
                        <select id="productFilterStatus" name="productFilterStatus">
                            <option value="">Todos</option>
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </label>
                    <label>
                        Cuenta
                        <select id="productFilterAccount" name="productFilterAccount">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <button type="submit">Consultar productos creados</button>
                </form>
                <form id="productForm">
                    <label>
                        Cuenta propietaria
                        <select id="productAccount" name="productAccount">
                            <option value="">Catalogo global</option>
                        </select>
                    </label>
                    <label>
                        Codigo
                        <input id="code" name="code" type="text" value="PLAN_MOVIL_10GB" required>
                    </label>
                    <label>
                        Nombre
                        <input id="name" name="name" type="text" value="Plan Movil 10GB" required>
                    </label>
                    <label>
                        Tipo
                        <select id="productType" name="productType">
                            <option value="RECURRING">RECURRING</option>
                            <option value="ONE_TIME">ONE_TIME</option>
                        </select>
                    </label>
                    <label>
                        Precio
                        <input id="price" name="price" type="number" min="0" step="0.01" value="50000" required>
                    </label>
                    <label>
                        Frecuencia
                        <select id="billingFrequency" name="billingFrequency">
                            <option value="MONTHLY">MONTHLY</option>
                            <option value="NONE">NONE</option>
                        </select>
                    </label>
                    <label>
                        Estado
                        <select id="status" name="status">
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </label>
                    <label class="description-field">
                        Descripcion
                        <input id="description" name="description" type="text" value="Producto comercial BRMC">
                    </label>
                    <button id="productSubmitButton" type="submit">Crear producto</button>
                    <button id="cancelProductEditButton" class="muted" type="button" onclick="cancelProductEdit()" style="display:none;">Cancelar edicion</button>
                </form>
                <section id="result"></section>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    let editingProductId = null;
                    let appliedProductEditParam = false;
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }
                    let currentProducts = [];
                    let currentAccounts = [];
                    async function loadAccountsForProducts() {
                        currentAccounts = await fetchJson("/api/accounts");
                        const options = currentAccounts.map(account => `<option value="${account.id}">${account.id} - ${account.ownerName}</option>`).join("");
                        document.getElementById("productFilterAccount").innerHTML = `<option value="">Todas las cuentas</option><option value="GLOBAL">Catalogo global</option>${options}`;
                        document.getElementById("productAccount").innerHTML = `<option value="">Catalogo global</option>${options}`;
                    }
                    async function loadProducts() {
                        const response = await fetch("/api/products");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible consultar productos."));
                        }
                        currentProducts = await response.json();
                        renderProducts(currentProducts);
                        const editId = params.get("editId");
                        if (editId && !editingProductId && !appliedProductEditParam) {
                            const product = currentProducts.find(item => item.id === editId);
                            if (product) {
                                appliedProductEditParam = true;
                                startProductEdit(editId);
                            }
                        }
                    }
                    function filteredProducts() {
                        const search = document.getElementById("productSearch").value.trim().toUpperCase();
                        const type = document.getElementById("productFilterType").value;
                        const status = document.getElementById("productFilterStatus").value;
                        const account = document.getElementById("productFilterAccount").value;
                        return currentProducts
                            .filter(product => !search || product.code.includes(search) || product.name.toUpperCase().includes(search))
                            .filter(product => !type || product.productType === type)
                            .filter(product => !status || product.status === status)
                            .filter(product => !account || (account === "GLOBAL" ? !product.accountId : product.accountId === account));
                    }
                    function productAccountLabel(product) {
                        return product.accountId ? `${product.accountNumber} - ${product.accountOwnerName}` : "Catalogo global";
                    }
                    function renderProducts(products) {
                        if (products.length === 0) {
                            document.getElementById("result").innerHTML = message("info", "No hay productos creados.");
                            return;
                        }
                        const rows = products.map(product => `
                            <tr>
                                <td>${product.displayId || product.id}</td>
                                <td>${productAccountLabel(product)}</td>
                                <td>${product.code}</td>
                                <td>${product.name}</td>
                                <td>${product.description || ""}</td>
                                <td>${product.productType}</td>
                                <td>${product.billingFrequency}</td>
                                <td>${money(product.price, product.currency)}</td>
                                <td><span class="status ${product.status.toLowerCase()}">${product.status}</span></td>
                                <td>${product.createdAt}</td>
                                <td>
                                    <div class="actions">
                                        <button type="button" onclick="startProductEdit('${product.id}')">Editar</button>
                                        ${product.status === "ACTIVE"
                                            ? `<button class="muted" type="button" onclick="productAction('${product.id}', 'deactivate')">Desactivar</button>`
                                            : `<button class="secondary" type="button" onclick="productAction('${product.id}', 'activate')">Activar</button>`}
                                    </div>
                                </td>
                            </tr>
                        `).join("");
                        document.getElementById("result").innerHTML = `
                            <h2>Productos</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Product ID</th>
                                        <th>Cuenta</th>
                                        <th>Code</th>
                                        <th>Name</th>
                                        <th>Description</th>
                                        <th>Type</th>
                                        <th>Billing Frequency</th>
                                        <th>Price</th>
                                        <th>Status</th>
                                        <th>Created At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }
                    document.getElementById("productFilterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        renderProducts(filteredProducts());
                    });
                    function productPayload() {
                        return {
                            code: document.getElementById("code").value,
                            name: document.getElementById("name").value,
                            description: document.getElementById("description").value,
                            productType: document.getElementById("productType").value,
                            price: Number(document.getElementById("price").value),
                            currency: "COP",
                            billingFrequency: document.getElementById("billingFrequency").value,
                            status: document.getElementById("status").value,
                            accountId: document.getElementById("productAccount").value || null
                        };
                    }
                    function startProductEdit(productId) {
                        const product = currentProducts.find(item => item.id === productId);
                        if (!product) {
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("warning", "No fue posible encontrar el producto seleccionado."));
                            return;
                        }
                        editingProductId = product.id;
                        document.getElementById("code").value = product.code;
                        document.getElementById("name").value = product.name;
                        document.getElementById("description").value = product.description || "";
                        document.getElementById("productType").value = product.productType;
                        document.getElementById("price").value = product.price;
                        document.getElementById("billingFrequency").value = product.billingFrequency;
                        document.getElementById("status").value = product.status;
                        document.getElementById("productAccount").value = product.accountId || "";
                        document.getElementById("productSubmitButton").textContent = "Guardar producto";
                        document.getElementById("cancelProductEditButton").style.display = "";
                        document.getElementById("code").focus();
                    }
                    function cancelProductEdit() {
                        editingProductId = null;
                        document.getElementById("productForm").reset();
                        document.getElementById("productSubmitButton").textContent = "Crear producto";
                        document.getElementById("cancelProductEditButton").style.display = "none";
                    }
                    document.getElementById("productForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const payload = productPayload();
                        try {
                            const response = await fetch(editingProductId ? `/api/products/${editingProductId}` : "/api/products", {
                                method: editingProductId ? "PUT" : "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify(payload)
                            });
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible guardar el producto."));
                            }
                            const wasEditing = editingProductId !== null;
                            cancelProductEdit();
                            await loadProducts();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", wasEditing ? "Producto actualizado correctamente." : "Producto creado correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });
                    async function productAction(productId, action) {
                        try {
                            const response = await fetch(`/api/products/${productId}/${action}`, { method: "POST" });
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible actualizar el producto."));
                            }
                            await loadProducts();
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    Promise.all([loadAccountsForProducts(), loadProducts()])
                        .catch(error => document.getElementById("result").innerHTML = message("error", error.message));
                </script>
                """);
    }

    /**
     * Renderiza la pagina de administracion de inventario.
     *
     * @return pagina HTML para crear, consultar y cambiar disponibilidad de items.
     */
    @GetMapping(value = "/inventory", produces = MediaType.TEXT_HTML_VALUE)
    String inventory() {
        return page("Inventario", """
                <p>Control operativo de items, existencias, valor de inventario y parametros de nuevo pedido.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <a class="secondary" href="/events?type=INVENTORY_ITEM_CREATED">Eventos de inventario</a>
                </div>
                <form id="inventoryFilterForm" class="filter-form">
                    <label>
                        ID o nombre
                        <input id="inventorySearch" name="inventorySearch" type="text" placeholder="INV o SIM">
                    </label>
                    <label>
                        Disponible
                        <select id="filterAvailable" name="filterAvailable">
                            <option value="">Todos</option>
                            <option value="true">SI</option>
                            <option value="false">NO</option>
                        </select>
                    </label>
                    <label>
                        Nuevo pedido
                        <select id="filterReorder" name="filterReorder">
                            <option value="">Todos</option>
                            <option value="true">Requiere nuevo pedido</option>
                            <option value="false">Sin alerta</option>
                        </select>
                    </label>
                    <label>
                        Cuenta
                        <select id="inventoryFilterAccount" name="inventoryFilterAccount">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <button type="submit">Consultar inventario</button>
                </form>
                <form id="inventoryForm">
                    <label>
                        Cuenta propietaria
                        <select id="inventoryAccount" name="inventoryAccount">
                            <option value="">Inventario global</option>
                        </select>
                    </label>
                    <label>
                        Nombre
                        <input id="name" name="name" type="text" value="SIM Card" required>
                    </label>
                    <label>
                        Precio por unidad
                        <input id="unitPrice" name="unitPrice" type="number" min="0" step="0.01" value="5000" required>
                    </label>
                    <label>
                        Cantidad en existencias
                        <input id="stockQuantity" name="stockQuantity" type="number" min="0" step="1" value="100" required>
                    </label>
                    <label>
                        Nivel del nuevo pedido
                        <input id="reorderLevel" name="reorderLevel" type="number" min="0" step="1" value="20" required>
                    </label>
                    <label>
                        Tiempo del nuevo pedido en dias
                        <input id="reorderTimeDays" name="reorderTimeDays" type="number" min="0" step="1" value="5" required>
                    </label>
                    <label>
                        Cantidad del nuevo pedido
                        <input id="reorderQuantity" name="reorderQuantity" type="number" min="0" step="1" value="50" required>
                    </label>
                    <label>
                        Aun se encuentra disponible
                        <select id="available" name="available">
                            <option value="true">SI</option>
                            <option value="false">NO</option>
                        </select>
                    </label>
                    <label class="description-field">
                        Descripcion
                        <input id="description" name="description" type="text" value="Inventario operativo BRMC">
                    </label>
                    <button type="submit">Crear item de inventario</button>
                </form>
                <section id="result"></section>
                <script>
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }
                    let currentInventory = [];
                    let currentAccounts = [];
                    async function loadAccountsForInventory() {
                        currentAccounts = await fetchJson("/api/accounts");
                        const options = currentAccounts.map(account => `<option value="${account.id}">${account.id} - ${account.ownerName}</option>`).join("");
                        document.getElementById("inventoryFilterAccount").innerHTML = `<option value="">Todas las cuentas</option><option value="GLOBAL">Inventario global</option>${options}`;
                        document.getElementById("inventoryAccount").innerHTML = `<option value="">Inventario global</option>${options}`;
                    }
                    async function loadInventory() {
                        const response = await fetch("/api/inventory");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible consultar inventario."));
                        }
                        currentInventory = await response.json();
                        renderInventory(filteredInventory());
                    }
                    function filteredInventory() {
                        const search = document.getElementById("inventorySearch").value.trim().toUpperCase();
                        const available = document.getElementById("filterAvailable").value;
                        const reorder = document.getElementById("filterReorder").value;
                        const account = document.getElementById("inventoryFilterAccount").value;
                        return currentInventory
                            .filter(item => !search || item.id.toUpperCase().includes(search) || item.name.toUpperCase().includes(search))
                            .filter(item => available === "" || String(item.available) === available)
                            .filter(item => reorder === "" || String(item.needsReorder) === reorder)
                            .filter(item => !account || (account === "GLOBAL" ? !item.accountId : item.accountId === account));
                    }
                    function inventoryAccountLabel(item) {
                        return item.accountId ? `${item.accountNumber} - ${item.accountOwnerName}` : "Inventario global";
                    }
                    function inventoryStatus(item) {
                        return item.available
                            ? `<span class="status active">SI</span>`
                            : `<span class="status inactive">NO</span>`;
                    }
                    function reorderStatus(item) {
                        return item.needsReorder
                            ? `<span class="status pending">Reordenar</span>`
                            : `<span class="status completed">OK</span>`;
                    }
                    function renderInventory(items) {
                        if (items.length === 0) {
                            document.getElementById("result").innerHTML = message("info", "No hay items de inventario para mostrar.");
                            return;
                        }
                        const rows = items.map(item => `
                            <tr>
                                <td>${item.id}</td>
                                <td>${inventoryAccountLabel(item)}</td>
                                <td>${item.name}</td>
                                <td>${item.description || ""}</td>
                                <td>${money(item.unitPrice)}</td>
                                <td>${item.stockQuantity}</td>
                                <td>${money(item.inventoryValue)}</td>
                                <td>${item.reorderLevel}</td>
                                <td>${item.reorderTimeDays}</td>
                                <td>${item.reorderQuantity}</td>
                                <td>${inventoryStatus(item)}</td>
                                <td>${reorderStatus(item)}</td>
                                <td>${item.pinVirtualTimeT || ""}</td>
                                <td>
                                    ${item.available
                                        ? `<button class="muted" type="button" onclick="changeAvailability('${item.id}', false)">No disponible</button>`
                                        : `<button class="secondary" type="button" onclick="changeAvailability('${item.id}', true)">Disponible</button>`}
                                </td>
                            </tr>
                        `).join("");
                        document.getElementById("result").innerHTML = `
                            <h2>Items de inventario</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID de inventario</th>
                                        <th>Cuenta</th>
                                        <th>Nombre</th>
                                        <th>Descripcion</th>
                                        <th>Precio por unidad</th>
                                        <th>Cantidad en existencias</th>
                                        <th>Valor de inventario</th>
                                        <th>Nivel nuevo pedido</th>
                                        <th>Tiempo nuevo pedido dias</th>
                                        <th>Cantidad nuevo pedido</th>
                                        <th>Disponible</th>
                                        <th>Alerta</th>
                                        <th>Pin virtual time</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }
                    function buildPayload() {
                        return {
                            name: document.getElementById("name").value.trim(),
                            description: document.getElementById("description").value.trim(),
                            unitPrice: Number(document.getElementById("unitPrice").value),
                            stockQuantity: Number(document.getElementById("stockQuantity").value),
                            reorderLevel: Number(document.getElementById("reorderLevel").value),
                            reorderTimeDays: Number(document.getElementById("reorderTimeDays").value),
                            reorderQuantity: Number(document.getElementById("reorderQuantity").value),
                            available: document.getElementById("available").value === "true",
                            accountId: document.getElementById("inventoryAccount").value || null
                        };
                    }
                    document.getElementById("inventoryFilterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        renderInventory(filteredInventory());
                    });
                    document.getElementById("inventoryForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const payload = buildPayload();
                        if (!payload.name) {
                            document.getElementById("result").innerHTML = message("warning", "Debe ingresar el nombre del item.");
                            return;
                        }
                        try {
                            const response = await fetch("/api/inventory", {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify(payload)
                            });
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible crear el item de inventario."));
                            }
                            await loadInventory();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Item de inventario creado correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });
                    async function changeAvailability(inventoryId, available) {
                        const action = available ? "available" : "unavailable";
                        try {
                            const response = await fetch(`/api/inventory/${inventoryId}/${action}`, { method: "POST" });
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible cambiar la disponibilidad."));
                            }
                            await loadInventory();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Disponibilidad actualizada correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    Promise.all([loadAccountsForInventory(), loadInventory()])
                        .catch(error => document.getElementById("result").innerHTML = message("error", error.message));
                </script>
                """);
    }

    /**
     * Renderiza el generador de archivos ENTEL para cambio de numero.
     *
     * @return pagina HTML con formulario y descarga local de TXT.
     */
    @GetMapping(value = "/entel", produces = MediaType.TEXT_HTML_VALUE)
    String entel() {
        return page("ENTEL", """
                <p>Central de utilidades ENTEL para solicitudes operativas y generacion de artefactos BRM.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver al dashboard</a>
                </div>
                <div class="bc-operation-switch" role="tablist" aria-label="Operaciones ENTEL">
                    <button type="button" class="bc-operation-tab active" data-panel="changeNumberPanel">EXT_OP_CUST_POL_CHANGE_NUMBER</button>
                    <button type="button" class="bc-operation-tab" data-panel="addAssetPanel">EXT_OP_CUST_POL_ADD_ASSET</button>
                    <button type="button" class="bc-operation-tab" data-panel="createCaPanel">EXT_OP_CUST_POL_CREATE_CA</button>
                    <button type="button" class="bc-operation-tab" data-panel="createBaPanel">EXT_OP_CUST_POL_CREATE_BA</button>
                    <button type="button" class="bc-operation-tab" data-panel="podlPanel">Generar PODL</button>
                </div>
                <section id="changeNumberPanel" class="entel-operation-panel active">
                    <div class="bc-operation-panel">
                        <h2>EXT_OP_CUST_POL_CHANGE_NUMBER</h2>
                        <p class="bc-time-note">Genera un archivo NAP o IEL para solicitud de cambio de numero. Selecciona una sola salida por descarga.</p>
                <form id="entelForm" class="bc-inline-form entel-compact-form">
                    <label>
                        Tipo de archivo
                        <select id="changeNumberOutputType" name="outputType" required>
                            <option value="NAP">NAP</option>
                            <option value="IEL">IEL</option>
                        </select>
                    </label>
                    <label>
                        CUENTA_PAGADORA
                        <input id="accountNo" name="accountNo" type="text" placeholder="02049903" required autofocus>
                    </label>
                    <label>
                        PIN_FLD_PROGRAM_NAME / Nombre del programa solicitante
                        <input id="programName" name="programName" type="text" value="EXT_OP_CUST_POL_CHANGE_NUMBER">
                    </label>
                    <label>
                        NUMERO_ACTUAL
                        <input id="currentNumber" name="currentNumber" type="text" placeholder="02049903_NUMERO" required>
                    </label>
                    <label>
                        NUMERO_NUEVO_LIBRE
                        <input id="newNumber" name="newNumber" type="text" placeholder="02049903_NUMERO2" required>
                    </label>
                    <label>
                        IMEI para ILE (opcional)
                        <input id="imei" name="imei" type="text" placeholder="Si queda vacio se genera CUENTA_PAGADORA_imei">
                    </label>
                    <button type="submit">Descargar NAP o IEL</button>
                </form>
                        <ul class="bc-helper-list">
                            <li>NAP genera solo el flist con xop 20005 para cambio de numero.</li>
                            <li>IEL genera solo la linea ACCOUNT_NO;PROGRAM_NAME;NUMERO_ACTUAL;NUMERO_NUEVO;IMEI.</li>
                            <li>Si IMEI queda vacio en IEL, se usa CUENTA_PAGADORA_imei.</li>
                        </ul>
                    </div>
                    <section id="changeNumberResult"></section>
                </section>
                <section id="addAssetPanel" class="entel-operation-panel">
                    <div class="bc-operation-panel">
                        <h2>EXT_OP_CUST_POL_ADD_ASSET</h2>
                        <p class="bc-time-note">Genera un archivo NAP o IEL para comprar/crear un asset de servicio. Selecciona una sola salida por descarga.</p>
                        <form id="addAssetForm" class="bc-inline-form entel-compact-form">
                            <label>
                                Tipo de archivo
                                <select id="addAssetOutputType" name="outputType" required>
                                    <option value="NAP">NAP</option>
                                    <option value="IEL">IEL</option>
                                </select>
                            </label>
                            <label>
                                ACCOUNT_NO / Cuenta pagadora
                                <input id="addAssetAccountNo" name="accountNo" type="text" placeholder=".700003.3" required>
                            </label>
                            <label>
                                SERVICE_TYPE
                                <select id="addAssetServiceType" name="serviceType" required>
                                    <option value="/service/device">/service/device</option>
                                    <option value="/service/telco/gsm">/service/telco/gsm</option>
                                    <option value="/service/telephony">/service/telephony</option>
                                    <option value="/service/other">/service/other</option>
                                    <option value="/service/pcm_client">/service/pcm_client</option>
                                    <option value="/service/admin_client">/service/admin_client</option>
                                    <option value="/service/device_installment">/service/device_installment</option>
                                </select>
                            </label>
                            <label>
                                PIN_FLD_PROGRAM_NAME / PROGRAM_NAME
                                <input id="addAssetProgramName" name="programName" type="text" value="Testnap" required>
                            </label>
                            <label>
                                PIN_FLD_LOGIN / LOGIN
                                <input id="addAssetLogin" name="login" type="text" placeholder="56912345678" required>
                            </label>
                            <label>
                                IMEI (opcional)
                                <input id="addAssetImei" name="imei" type="text" placeholder="123456789012345">
                            </label>
                            <label>
                                PLAN_MIGRABLE / PIN_FLD_AAC_PACKAGE
                                <select id="addAssetPlanMigrable" name="planMigrable">
                                    <option value="0">0 - No migrable</option>
                                    <option value="1">1 - Migrable</option>
                                </select>
                            </label>
                            <button type="submit">Descargar NAP o IEL</button>
                        </form>
                        <ul class="bc-helper-list">
                            <li>NAP usa el opcode EXT_OP_CUST_POL_ADD_ASSET con POID del tipo de servicio seleccionado.</li>
                            <li>IEL genera una sola linea con ACCOUNT_NO;SERVICE_TYPE;PROGRAM_NAME;LOGIN;IMEI;PLAN_MIGRABLE.</li>
                            <li>Si IMEI queda vacio, el NAP omite PIN_FLD_IMEI y el IEL deja la columna vacia.</li>
                        </ul>
                    </div>
                    <section id="addAssetResult"></section>
                </section>
                <section id="createCaPanel" class="entel-operation-panel">
                    <div class="bc-operation-panel">
                        <h2>EXT_OP_CUST_POL_CREATE_CA</h2>
                        <p class="bc-time-note">Cuenta cliente. El NAP queda precargado y editable antes de descargar.</p>
                        <p class="bc-important-note">Importante: cambia PIN_FLD_ACCOUNT_NO antes de ejecutar este opcode.</p>
                        <form id="createCaForm" class="entel-account-form">
                            <div class="entel-key-grid">
                            <label class="entel-critical-field">
                                PIN_FLD_ACCOUNT_NO
                                <input id="createCaAccountNo" name="accountNo" type="text" value="02049901" required>
                            </label>
                            <div class="entel-key-note">
                                <span>Tipo</span>
                                <strong>Cuenta Cliente</strong>
                                <small>Opcode: EXT_OP_CUST_POL_CREATE_CA</small>
                            </div>
                            </div>
                            <div class="entel-template-card">
                                <div class="entel-template-header">
                                    <div>
                                        <span>NAP editable</span>
                                        <strong>Revisa y ajusta el flist antes de descargar</strong>
                                    </div>
                                </div>
                                <textarea id="createCaTemplate" class="bc-template-area" spellcheck="false">r << EOF 1
0 PIN_FLD_POID           POID [0] 0.0.0.1 /account -1 0
0 PIN_FLD_PROGRAM_NAME    STR [0] "Create Account API REST"
0 PIN_FLD_USER_NAME       STR [0] "RAV NAP"
0 PIN_FLD_ACCTINFO      ARRAY [0] allocated 20, used 13
1   PIN_FLD_CURRENCY        INT [0] 152
1   PIN_FLD_ACCOUNT_NO      STR [0] "02049901"
1   PIN_FLD_AAC_SOURCE      STR [0] "CTA-CLI"
1   PIN_FLD_AAC_VENDOR      STR [0] "Cuenta Cliente"
1   PIN_FLD_LOCALE          STR [0] "es_CL"
1   EXT_FLD_BUSINESS_ACTIVITY           STR [0] "0115"
1   EXT_FLD_SEGMENT                     STR [0] "C"
1   EXT_FLD_CUST_CLASSIFICATION         STR [0] "Corporate"
1   EXT_FLD_CUST_SUB_CLASSIFICATION     STR [0] "Corporate-001"
1   EXT_FLD_DOC_TYPE                    STR [0] "RUT"
1   EXT_FLD_DOC_NO                      STR [0] "700001"
1   EXT_FLD_RISK_LEVEL                  STR [0] "Bajo"
1   EXT_FLD_VIP_FLAG                    ENUM [0] 0
1   EXT_FLD_HOLDING                     STR [0] "D001"
1   EXT_FLD_INTERCOMPANY                STR [0] "ENTEL"
1   EXT_FLD_CLIENT_CHURN_STATUS         ENUM [0] 0
0 PIN_FLD_NAMEINFO      ARRAY [0] allocated 20, used 11
1   PIN_FLD_CONTACT_TYPE    STR [0] "101"
1   PIN_FLD_FIRST_NAME      STR [0] "Robert"
1   PIN_FLD_LAST_NAME       STR [0] "Altamirano"
1   PIN_FLD_COMPANY         STR [0] "Robert Altamirano S.A."
1   PIN_FLD_ADDRESS         STR [0] "Av. Libertador Bernardo O'Higgins 1500, Depto 501"
1   PIN_FLD_COUNTRY         STR [0] "CL"
1   PIN_FLD_STATE           STR [0] "Metropolitana"
1   PIN_FLD_CITY            STR [0] "Santiago"
1   PIN_FLD_ZIP             STR [0] "3570000"
1   PIN_FLD_EMAIL_ADDR      STR [0] "roberto.altamirano@example.com"
1   PIN_FLD_PHONES       ARRAY [0] allocated 5, used 1
2     PIN_FLD_PHONE          STR [0] "+56911112222"
0 PIN_FLD_NAMEINFO      ARRAY [1] allocated 20, used 11
1   PIN_FLD_CONTACT_TYPE    STR [0] "103"
1   PIN_FLD_FIRST_NAME      STR [0] "Robert"
1   PIN_FLD_LAST_NAME       STR [0] "Altamirano"
1   PIN_FLD_COMPANY         STR [0] "Robert Altamirano S.A."
1   PIN_FLD_ADDRESS         STR [0] "Av. Libertador Bernardo O'Higgins 1500, Depto 502"
1   PIN_FLD_COUNTRY         STR [0] "CL"
1   PIN_FLD_STATE           STR [0] "Metropolitana"
1   PIN_FLD_CITY            STR [0] "Santiago"
1   PIN_FLD_ZIP             STR [0] "3580000"
1   PIN_FLD_EMAIL_ADDR      STR [0] "roberto.altamirano@example.com"
1   PIN_FLD_PHONES       ARRAY [0] allocated 5, used 1
2     PIN_FLD_PHONE          STR [0] "+56933334444"
EOF
xop EXT_OP_CUST_POL_CREATE_CA 0 1</textarea>
                            </div>
                            <div class="entel-form-actions">
                                <button type="submit">Descargar NAP CREATE_CA</button>
                            </div>
                        </form>
                    </div>
                    <section id="createCaResult"></section>
                </section>
                <section id="createBaPanel" class="entel-operation-panel">
                    <div class="bc-operation-panel">
                        <h2>EXT_OP_CUST_POL_CREATE_BA</h2>
                        <p class="bc-time-note">Cuenta pagadora. El NAP queda precargado y editable antes de descargar.</p>
                        <p class="bc-important-note">Importante: cambia PIN_FLD_ACCOUNT_NO y PIN_FLD_PARENT_NAME. PIN_FLD_PARENT_NAME es la cuenta cliente.</p>
                        <form id="createBaForm" class="entel-account-form">
                            <div class="entel-key-grid">
                            <label class="entel-critical-field">
                                PIN_FLD_ACCOUNT_NO
                                <input id="createBaAccountNo" name="accountNo" type="text" value="02049908" required>
                            </label>
                            <label class="entel-critical-field">
                                PIN_FLD_PARENT_NAME
                                <input id="createBaParentName" name="parentName" type="text" value="02049901" required>
                            </label>
                            <div class="entel-key-note">
                                <span>Tipo</span>
                                <strong>Cuenta Pagadora</strong>
                                <small>Parent Name debe ser la cuenta cliente</small>
                            </div>
                            </div>
                            <div class="entel-template-card">
                                <div class="entel-template-header">
                                    <div>
                                        <span>NAP editable</span>
                                        <strong>Revisa y ajusta el flist antes de descargar</strong>
                                    </div>
                                </div>
                                <textarea id="createBaTemplate" class="bc-template-area" spellcheck="false">r << EOF 1
0 PIN_FLD_POID           POID [0] 0.0.0.1 /account -1 0
0 PIN_FLD_PROGRAM_NAME    STR [0] "Create Paying Account API REST"
0 PIN_FLD_USER_NAME       STR [0] "RAV"
0 PIN_FLD_ACCTINFO      ARRAY [0] allocated 30, used 20
1   PIN_FLD_CURRENCY                      INT [0] 152
1   PIN_FLD_ACCOUNT_NO                    STR [0] "02049908"
1   PIN_FLD_AAC_SOURCE                    STR [0] "CTA-GEN"
1   PIN_FLD_AAC_VENDOR                    STR [0] "Cuenta Generica"
1   PIN_FLD_PARENT_NAME                   STR [0] "02049901"
1   PIN_FLD_LOCALE                        STR [0] "es_CL"
1   PIN_FLD_GL_SEGMENT                    STR [0] "ENT"
1   EXT_FLD_SUB_SEGMENT_DESCR         STR [0] "Convergente"
1   PIN_FLD_GROUP_INFO               SUBSTRUCT [0] allocated 20, used 0
1   EXT_FLD_SUB_SEGMENT                   STR [0] "P002"
1   EXT_FLD_SOCIETY                       STR [0] "ENT"
1   EXT_FLD_BILLING_CYCLE                 STR [0] "CICLO_01"
1   EXT_FLD_NOTIFICATION_TYPE            ENUM [0] 1
1   EXT_FLD_SERVICE_NAME                  STR [0] "001"
1   EXT_FLD_GL_ACCOUNT                    STR [0] "GL-1000"
1   EXT_FLD_DIGIT_ACCOUNT                 STR [0] "7"
1   EXT_FLD_INTEREST_FLAG                ENUM [0] 0
1   EXT_FLD_BUREAU_FLAG                  ENUM [0] 0
1   EXT_FLD_EXT_AGENCY_FLAG              ENUM [0] 0
1   EXT_FLD_COLL_EXPENSES_FLAG           ENUM [0] 0
1   EXT_FLD_RECONNECTION_FEE_FLAG        ENUM [0] 0
1   EXT_FLD_PENALTY_FLAG                 ENUM [0] 0
1   EXT_FLD_PAYING_CHURN_STATUS          ENUM [0] 0
0 PIN_FLD_NAMEINFO      ARRAY [0] allocated 20, used 11
1   PIN_FLD_CONTACT_TYPE                  STR [0] "102"
1   PIN_FLD_FIRST_NAME                    STR [0] "Juan"
1   PIN_FLD_LAST_NAME                     STR [0] "Perez"
1   PIN_FLD_COMPANY                       STR [0] "Cliente Pagador S.A."
1   PIN_FLD_ADDRESS                       STR [0] "Av. Principal 123"
1   PIN_FLD_COUNTRY                       STR [0] "CL"
1   PIN_FLD_STATE                         STR [0] "Metropolitana"
1   PIN_FLD_CITY                          STR [0] "Santiago"
1   PIN_FLD_ZIP                           STR [0] "8320000"
1   PIN_FLD_EMAIL_ADDR                    STR [0] "juan.perez@example.com"
1   PIN_FLD_PHONES       ARRAY [0] allocated 5, used 1
2     PIN_FLD_PHONE                       STR [0] "+56977778888"
0 PIN_FLD_NAMEINFO      ARRAY [1] allocated 20, used 11
1   PIN_FLD_CONTACT_TYPE                  STR [0] "104"
1   PIN_FLD_FIRST_NAME                    STR [0] "Juan"
1   PIN_FLD_LAST_NAME                     STR [0] "Perez"
1   PIN_FLD_COMPANY                       STR [0] "Cliente Pagador S.A."
1   PIN_FLD_ADDRESS                       STR [0] "Av. Principal 123"
1   PIN_FLD_COUNTRY                       STR [0] "CL"
1   PIN_FLD_STATE                         STR [0] "Metropolitana"
1   PIN_FLD_CITY                          STR [0] "Santiago"
1   PIN_FLD_ZIP                           STR [0] "8320000"
1   PIN_FLD_EMAIL_ADDR                    STR [0] "juan.perez@example.com"
1   PIN_FLD_PHONES       ARRAY [0] allocated 5, used 1
2     PIN_FLD_PHONE                       STR [0] "+56977778888"
EOF
xop EXT_OP_CUST_POL_CREATE_BA 0 1</textarea>
                            </div>
                            <div class="entel-form-actions">
                                <button type="submit">Descargar NAP CREATE_BA</button>
                            </div>
                        </form>
                    </div>
                    <section id="createBaResult"></section>
                </section>
                <section id="podlPanel" class="entel-operation-panel">
                    <div class="bc-operation-panel">
                        <h2>Generar PODL desde Excel</h2>
                        <p class="bc-time-note">Descarga la plantilla, diligencia la hoja PODL_INPUT y carga el .xlsx para generar el archivo .podl.</p>
                        <div class="summary-grid">
                            <div class="summary-item">
                                <span>Hoja requerida</span>
                                <strong>PODL_INPUT</strong>
                            </div>
                            <div class="summary-item">
                                <span>Salida</span>
                                <strong>Archivo .podl</strong>
                            </div>
                            <div class="summary-item">
                                <span>Validacion</span>
                                <strong>Campos obligatorios y tipos BRM</strong>
                            </div>
                        </div>
                        <div class="toolbar">
                            <a class="secondary" href="/templates/PODL_template_BRMC.xlsx" download>Descargar Excel template PODL</a>
                        </div>
                        <form id="podlForm" class="bc-inline-form entel-compact-form" enctype="multipart/form-data">
                            <label class="wide-field">
                                Excel PODL_INPUT (.xlsx)
                                <input id="podlFile" name="file" type="file" accept=".xlsx" required>
                            </label>
                            <button type="submit">Generar PODL</button>
                        </form>
                        <ul class="bc-helper-list">
                            <li>storable_class, sql_table, field_type, field_name, field_description, create_rule, modify_rule y sql_column son obligatorios.</li>
                            <li>Para field_type STRING debes diligenciar field_length.</li>
                            <li>Si una clase no trae PIN_FLD_POID, el generador lo agrega como campo tecnico.</li>
                        </ul>
                    </div>
                    <section id="podlResult"></section>
                </section>
                <script>
                    const DEFAULT_PROGRAM_NAME = "EXT_OP_CUST_POL_CHANGE_NUMBER";
                    const ADD_ASSET_OPCODE = "EXT_OP_CUST_POL_ADD_ASSET";

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    function valueOf(id) {
                        return document.getElementById(id).value.trim();
                    }

                    function sanitizedFilePart(value) {
                        return value.replace(/[^a-zA-Z0-9_-]+/g, "_").replace(/^_+|_+$/g, "") || "entel";
                    }

                    function escapeNap(value) {
                        return value.replace(/\\\\/g, "\\\\\\\\").replace(/"/g, '\\\\"');
                    }

                    function escapeHtml(value) {
                        return value.replace(/[&<>]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;" }[char]));
                    }

                    function buildTxt() {
                        const outputType = valueOf("changeNumberOutputType");
                        const accountNo = valueOf("accountNo");
                        const programName = valueOf("programName") || DEFAULT_PROGRAM_NAME;
                        const currentNumber = valueOf("currentNumber");
                        const newNumber = valueOf("newNumber");
                        const imei = valueOf("imei") || `${accountNo}_imei`;

                        if (!accountNo || !currentNumber || !newNumber) {
                            throw new Error("Debe diligenciar CUENTA_PAGADORA, NUMERO_ACTUAL y NUMERO_NUEVO_LIBRE.");
                        }

                        if (outputType === "IEL") {
                            return {
                                outputType,
                                accountNo,
                                currentNumber,
                                newNumber,
                                content: [
                                    "#ACCOUNT_NO;Nombre_del_programa_solicitante;Numero_actual;Nuevo_numero;IMEI",
                                    `${accountNo};${programName};${currentNumber};${newNumber};${imei}`
                                ].join("\\r\\n") + "\\r\\n"
                            };
                        }

                        const nap = [
                            "r << XXX 1",
                            "0 PIN_FLD_POID           POID [0] 0.0.0.1 /service/telco/gsm -1 0",
                            `0 PIN_FLD_ACCOUNT_NO      STR [0] "${escapeNap(accountNo)}"`,
                            `0 PIN_FLD_PROGRAM_NAME    STR [0] "${escapeNap(programName)}"`,
                            `0 PIN_FLD_LOGIN           STR [0] "${escapeNap(currentNumber)}"`,
                            `0 PIN_FLD_NEW_LOGIN       STR [0] "${escapeNap(newNumber)}"`,
                            "XXX",
                            "xop 20005 0 1"
                        ].join("\\r\\n");

                        const ile = [
                            "2. ILE:",
                            "",
                            "#ACCOUNT_NO;Nombre_del_programa_solicitante;Número_actual;Nuevo_número;IMEI",
                            `${accountNo};${programName};${currentNumber};${newNumber};${imei}`
                        ].join("\\r\\n");

                        return {
                            outputType,
                            accountNo,
                            currentNumber,
                            newNumber,
                            content: `${nap}\\r\\n`
                        };
                    }

                    function buildAddAsset() {
                        const outputType = valueOf("addAssetOutputType");
                        const accountNo = valueOf("addAssetAccountNo");
                        const serviceType = valueOf("addAssetServiceType");
                        const programName = valueOf("addAssetProgramName");
                        const login = valueOf("addAssetLogin");
                        const imei = valueOf("addAssetImei");
                        const planMigrable = valueOf("addAssetPlanMigrable") || "0";

                        if (!accountNo || !serviceType || !programName || !login) {
                            throw new Error("Debe diligenciar ACCOUNT_NO, SERVICE_TYPE, PROGRAM_NAME y LOGIN.");
                        }

                        if (outputType === "IEL") {
                            return {
                                outputType,
                                accountNo,
                                login,
                                content: [
                                    "# Cuenta_a_la_que_se_asignara_el_servicio;Tipo_servicio_a_crear;Nombre_del_programa_solicitante;Login_servicio_Numero_linea;IMEI;Plan_migrable",
                                    `${accountNo};${serviceType};${programName};${login};${imei};${planMigrable}`
                                ].join("\\r\\n") + "\\r\\n"
                            };
                        }

                        const napLines = [
                            "r << EOF 1",
                            `0 PIN_FLD_POID                          POID [0] 0.0.0.1 ${serviceType} -1 0`,
                            `0 PIN_FLD_ACCOUNT_NO            STR [0] "${escapeNap(accountNo)}"`,
                            `0 PIN_FLD_PROGRAM_NAME          STR [0] "${escapeNap(programName)}"`,
                            `0 PIN_FLD_LOGIN                         STR [0] "${escapeNap(login)}"`
                        ];
                        if (imei) {
                            napLines.push(`0 PIN_FLD_IMEI                          STR [0] "${escapeNap(imei)}"`);
                        }
                        napLines.push(`0 PIN_FLD_AAC_PACKAGE               ENUM [0] ${planMigrable}`);
                        napLines.push("EOF");
                        napLines.push(`xop ${ADD_ASSET_OPCODE} 0 1`);

                        return {
                            outputType,
                            accountNo,
                            login,
                            content: napLines.join("\\r\\n") + "\\r\\n"
                        };
                    }

                    function replaceFlistStringField(content, fieldName, value) {
                        return content.split("\\n").map(line => {
                            if (line.includes(fieldName) && line.includes("STR [0]")) {
                                return line.replace(/"[^"]*"/, `"${escapeNap(value)}"`);
                            }
                            return line;
                        }).join("\\n");
                    }

                    function templateContent(templateId) {
                        return document.getElementById(templateId).value.trimEnd() + "\\r\\n";
                    }

                    function syncCreateCaTemplate() {
                        const accountNo = valueOf("createCaAccountNo");
                        const template = document.getElementById("createCaTemplate");
                        if (accountNo) {
                            template.value = replaceFlistStringField(template.value, "PIN_FLD_ACCOUNT_NO", accountNo);
                        }
                    }

                    function syncCreateBaTemplate() {
                        const accountNo = valueOf("createBaAccountNo");
                        const parentName = valueOf("createBaParentName");
                        const template = document.getElementById("createBaTemplate");
                        if (accountNo) {
                            template.value = replaceFlistStringField(template.value, "PIN_FLD_ACCOUNT_NO", accountNo);
                        }
                        if (parentName) {
                            template.value = replaceFlistStringField(template.value, "PIN_FLD_PARENT_NAME", parentName);
                        }
                    }

                    function renderPreview(targetId, successMessage, content) {
                        document.getElementById(targetId).innerHTML = `
                            ${message("success", successMessage)}
                            <h2>Vista previa</h2>
                            <pre class="bc-code-preview">${escapeHtml(content)}</pre>
                        `;
                    }

                    function downloadText(fileName, content, type = "text/plain;charset=utf-8") {
                        const blob = new Blob([content], { type });
                        const url = URL.createObjectURL(blob);
                        const link = document.createElement("a");
                        link.href = url;
                        link.download = fileName;
                        document.body.appendChild(link);
                        link.click();
                        link.remove();
                        URL.revokeObjectURL(url);
                    }

                    function fileNameFromDisposition(disposition, fallback) {
                        if (!disposition) {
                            return fallback;
                        }
                        const utf8Match = disposition.match(/filename\\*=UTF-8''([^;]+)/i);
                        if (utf8Match) {
                            return decodeURIComponent(utf8Match[1]);
                        }
                        const plainMatch = disposition.match(/filename="?([^";]+)"?/i);
                        return plainMatch ? plainMatch[1] : fallback;
                    }

                    document.querySelectorAll(".bc-operation-tab").forEach(button => {
                        button.addEventListener("click", () => {
                            document.querySelectorAll(".bc-operation-tab").forEach(tab => tab.classList.remove("active"));
                            document.querySelectorAll(".entel-operation-panel").forEach(panel => panel.classList.remove("active"));
                            button.classList.add("active");
                            document.getElementById(button.dataset.panel).classList.add("active");
                        });
                    });

                    document.getElementById("entelForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        try {
                            const generated = buildTxt();
                            const extension = generated.outputType === "IEL" ? "iel" : "nap";
                            const fileName = `entel_change_number_${generated.outputType.toLowerCase()}_${sanitizedFilePart(generated.accountNo)}_${sanitizedFilePart(generated.currentNumber)}_to_${sanitizedFilePart(generated.newNumber)}.${extension}`;
                            renderPreview("changeNumberResult", `${generated.outputType} CHANGE_NUMBER generado correctamente.`, generated.content);
                            downloadText(fileName, generated.content);
                        } catch (error) {
                            document.getElementById("changeNumberResult").innerHTML = message("error", error.message);
                        }
                    });

                    document.getElementById("addAssetForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        try {
                            const generated = buildAddAsset();
                            const extension = generated.outputType === "IEL" ? "iel" : "nap";
                            const fileName = `entel_add_asset_${generated.outputType.toLowerCase()}_${sanitizedFilePart(generated.accountNo)}_${sanitizedFilePart(generated.login)}.${extension}`;
                            renderPreview("addAssetResult", `${generated.outputType} ADD_ASSET generado correctamente.`, generated.content);
                            downloadText(fileName, generated.content);
                        } catch (error) {
                            document.getElementById("addAssetResult").innerHTML = message("error", error.message);
                        }
                    });

                    document.getElementById("createCaAccountNo").addEventListener("input", syncCreateCaTemplate);
                    document.getElementById("createCaForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        try {
                            const accountNo = valueOf("createCaAccountNo");
                            if (!accountNo) {
                                throw new Error("Debe diligenciar PIN_FLD_ACCOUNT_NO para la cuenta cliente.");
                            }
                            syncCreateCaTemplate();
                            const content = templateContent("createCaTemplate");
                            const fileName = `entel_create_ca_${sanitizedFilePart(accountNo)}.nap`;
                            renderPreview("createCaResult", "NAP CREATE_CA generado correctamente.", content);
                            downloadText(fileName, content);
                        } catch (error) {
                            document.getElementById("createCaResult").innerHTML = message("error", error.message);
                        }
                    });

                    document.getElementById("createBaAccountNo").addEventListener("input", syncCreateBaTemplate);
                    document.getElementById("createBaParentName").addEventListener("input", syncCreateBaTemplate);
                    document.getElementById("createBaForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        try {
                            const accountNo = valueOf("createBaAccountNo");
                            const parentName = valueOf("createBaParentName");
                            if (!accountNo || !parentName) {
                                throw new Error("Debe diligenciar PIN_FLD_ACCOUNT_NO y PIN_FLD_PARENT_NAME para la cuenta pagadora.");
                            }
                            syncCreateBaTemplate();
                            const content = templateContent("createBaTemplate");
                            const fileName = `entel_create_ba_${sanitizedFilePart(accountNo)}_parent_${sanitizedFilePart(parentName)}.nap`;
                            renderPreview("createBaResult", "NAP CREATE_BA generado correctamente.", content);
                            downloadText(fileName, content);
                        } catch (error) {
                            document.getElementById("createBaResult").innerHTML = message("error", error.message);
                        }
                    });

                    document.getElementById("podlForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const result = document.getElementById("podlResult");
                        result.innerHTML = message("warning", "Generando PODL desde Excel...");
                        try {
                            const formData = new FormData(event.currentTarget);
                            const response = await fetch("/api/entel/podl", {
                                method: "POST",
                                body: formData
                            });
                            const text = await response.text();
                            if (!response.ok) {
                                throw new Error(text || "No fue posible generar el PODL.");
                            }
                            const fileName = fileNameFromDisposition(response.headers.get("Content-Disposition"), "entel_podl.podl");
                            renderPreview("podlResult", "PODL generado correctamente.", text);
                            downloadText(fileName, text);
                        } catch (error) {
                            result.innerHTML = message("error", error.message.replace(/\\n/g, "<br>"));
                        }
                    });
                </script>
                """);
    }

    /**
     * Renderiza la vista global de servicios.
     *
     * @return pagina HTML de consulta de servicios y productos asociados.
     */
    @GetMapping(value = "/services", produces = MediaType.TEXT_HTML_VALUE)
    String services() {
        return page("Consultar services creados", """
                <p>Administra el catalogo general de servicios y activa servicios en cuentas.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <a class="secondary" href="/products">Products</a>
                </div>
                <section class="bc-operation-panel">
                    <h2>Crear servicio general</h2>
                    <p class="bc-time-note">Primero define el servicio del catalogo. Despues puedes activarlo en una cuenta.</p>
                    <form id="catalogServiceForm" class="bc-inline-form">
                        <label>
                            Nombre del servicio
                            <input id="catalogName" name="catalogName" type="text" placeholder="Plan movil 40GB" required>
                        </label>
                        <label>
                            Tipo de servicio
                            <select id="catalogServiceType" name="catalogServiceType" required>
                                <option value="MOBILE">MOBILE</option>
                                <option value="INTERNET">INTERNET</option>
                                <option value="TV">TV</option>
                                <option value="GENERIC">GENERIC</option>
                            </select>
                        </label>
                        <label>
                            Estado
                            <select id="catalogStatus" name="catalogStatus">
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
                        </label>
                        <label>
                            Descripcion
                            <input id="catalogDescription" name="catalogDescription" type="text" placeholder="Servicio comercial disponible para activacion">
                        </label>
                        <button id="catalogSubmitButton" type="submit">Crear servicio</button>
                        <button id="cancelCatalogEditButton" class="muted" type="button" onclick="cancelCatalogEdit()" style="display:none;">Cancelar edicion</button>
                    </form>
                    <section id="catalogResult"></section>
                </section>
                <section class="bc-operation-panel">
                    <h2>Activar servicio en cuenta</h2>
                    <form id="activateServiceForm" class="bc-inline-form">
                        <label>
                            Cuenta
                            <select id="accountId" name="accountId" required>
                                <option value="">Seleccione una cuenta</option>
                            </select>
                        </label>
                        <label>
                            Servicio general
                            <select id="catalogServiceId" name="catalogServiceId" required>
                                <option value="">Seleccione un servicio general</option>
                            </select>
                        </label>
                        <button type="submit">Activar servicio</button>
                    </form>
                </section>
                <section class="bc-operation-panel" id="serviceEditPanel" style="display:none;">
                    <h2>Editar servicio activado</h2>
                    <form id="serviceForm" class="bc-inline-form">
                        <label>
                            Nombre del servicio
                            <input id="serviceName" name="serviceName" type="text" required>
                        </label>
                        <label>
                            Tipo de servicio
                            <select id="serviceType" name="serviceType" required>
                                <option value="MOBILE">MOBILE</option>
                                <option value="INTERNET">INTERNET</option>
                                <option value="TV">TV</option>
                                <option value="GENERIC">GENERIC</option>
                            </select>
                        </label>
                        <button id="serviceSubmitButton" type="submit">Guardar servicio</button>
                        <button id="cancelServiceEditButton" class="muted" type="button" onclick="cancelServiceEdit()">Cancelar edicion</button>
                    </form>
                </section>
                <form id="filterServicesForm" class="filter-form">
                    <label>
                        Cuenta
                        <select id="filterAccountId" name="filterAccountId">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <label>
                        Tipo de servicio
                        <select id="filterServiceType" name="filterServiceType">
                            <option value="">Todos</option>
                            <option value="MOBILE">MOBILE</option>
                            <option value="INTERNET">INTERNET</option>
                            <option value="TV">TV</option>
                            <option value="GENERIC">GENERIC</option>
                        </select>
                    </label>
                    <label>
                        Estado
                        <select id="filterStatus" name="filterStatus">
                            <option value="">Todos</option>
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="SUSPENDED">SUSPENDED</option>
                            <option value="TERMINATED">TERMINATED</option>
                        </select>
                    </label>
                    <button type="submit">Consultar services creados</button>
                </form>
                <form id="serviceProductForm" class="bc-inline-form">
                    <label>
                        Servicio creado
                        <select id="assignServiceId" name="assignServiceId" required>
                            <option value="">Seleccione un servicio</option>
                        </select>
                    </label>
                    <label>
                        Producto activo
                        <select id="assignProductId" name="assignProductId" required>
                            <option value="">Seleccione un producto</option>
                        </select>
                    </label>
                    <button id="assignProductButton" type="submit">Asociar producto</button>
                </form>
                <section id="result"></section>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    let currentProducts = [];
                    let currentServices = [];
                    let currentCatalog = [];
                    let editingCatalogId = null;
                    let editingServiceId = null;
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }
                    async function fetchJson(url, options) {
                        const response = await fetch(url, options);
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible completar la operacion."));
                        }
                        return response.json();
                    }
                    function formatDate(value) {
                        if (!value) {
                            return "N/A";
                        }
                        return new Date(value).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" });
                    }
                    async function loadAccounts() {
                        const accounts = await fetchJson("/api/accounts");
                        const select = document.getElementById("accountId");
                        const filterSelect = document.getElementById("filterAccountId");
                        select.innerHTML = "<option value=''>Seleccione una cuenta</option>";
                        filterSelect.innerHTML = "<option value=''>Todas las cuentas</option>";
                        accounts.forEach(account => {
                            const option = document.createElement("option");
                            option.value = account.id;
                            option.textContent = `${account.id} - ${account.ownerName} - ${account.status}`;
                            if (params.get("accountId") === account.id) {
                                option.selected = true;
                            }
                            select.appendChild(option);

                            const filterOption = document.createElement("option");
                            filterOption.value = account.id;
                            filterOption.textContent = `${account.id} - ${account.ownerName}`;
                            if (params.get("accountId") === account.id) {
                                filterOption.selected = true;
                            }
                            filterSelect.appendChild(filterOption);
                        });
                    }
                    async function loadProducts() {
                        currentProducts = await fetchJson("/api/products");
                        updateAssignmentControls();
                    }
                    async function loadCatalog() {
                        currentCatalog = await fetchJson("/api/service-catalog");
                        renderCatalog();
                        updateCatalogSelect();
                    }
                    function updateCatalogSelect() {
                        const select = document.getElementById("catalogServiceId");
                        const activeCatalog = currentCatalog.filter(item => item.status === "ACTIVE");
                        select.innerHTML = "<option value=''>Seleccione un servicio general</option>";
                        activeCatalog.forEach(item => {
                            const option = document.createElement("option");
                            option.value = item.id;
                            option.textContent = `${item.id} - ${item.name} - ${item.serviceType}`;
                            select.appendChild(option);
                        });
                        select.disabled = activeCatalog.length === 0;
                    }
                    function renderCatalog() {
                        const target = document.getElementById("catalogResult");
                        if (currentCatalog.length === 0) {
                            target.innerHTML = message("info", "No hay servicios generales creados.");
                            return;
                        }
                        const rows = currentCatalog.map(item => `
                            <tr>
                                <td>${item.id}</td>
                                <td>${item.name}</td>
                                <td>${item.serviceType}</td>
                                <td><span class="status ${item.status.toLowerCase()}">${item.status}</span></td>
                                <td>${item.description || ""}</td>
                                <td>${formatDate(item.pinVirtualTimeT || item.createdAt)}</td>
                                <td>${formatDate(item.createdT)}</td>
                                <td><button type="button" onclick="startCatalogEdit('${item.id}')">Editar</button></td>
                            </tr>
                        `).join("");
                        target.innerHTML = `
                            <h2>Catalogo de servicios</h2>
                            <table>
                                <thead><tr><th>Service ID</th><th>Nombre</th><th>Tipo</th><th>Estado</th><th>Descripcion</th><th>PVT creacion</th><th>Fecha real</th><th>Acciones</th></tr></thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }
                    function updateAssignmentControls() {
                        const serviceSelect = document.getElementById("assignServiceId");
                        const productSelect = document.getElementById("assignProductId");
                        const button = document.getElementById("assignProductButton");
                        const assignableServices = currentServices.filter(service => service.status !== "TERMINATED");
                        const selectedServiceId = serviceSelect.value;
                        const selectedService = currentServices.find(service => service.id === selectedServiceId);
                        const activeProducts = currentProducts
                            .filter(product => product.status === "ACTIVE")
                            .filter(product => !selectedService || !product.accountId || product.accountId === selectedService.accountId);
                        serviceSelect.innerHTML = "<option value=''>Seleccione un servicio</option>";
                        productSelect.innerHTML = "<option value=''>Seleccione un producto</option>";
                        assignableServices.forEach(service => {
                            const option = document.createElement("option");
                            option.value = service.id;
                            option.textContent = `${service.serviceCode} - ${service.serviceType} - ${service.ownerName}`;
                            option.selected = service.id === selectedServiceId;
                            serviceSelect.appendChild(option);
                        });
                        activeProducts.forEach(product => {
                            const option = document.createElement("option");
                            option.value = product.id;
                            const scope = product.accountId ? `Cuenta ${product.accountNumber}` : "Global";
                            option.textContent = `${product.displayId || product.id} - ${product.code} - ${product.name} - ${scope} - ${Number(product.price).toFixed(2)} ${product.currency}`;
                            productSelect.appendChild(option);
                        });
                        const disabled = assignableServices.length === 0 || activeProducts.length === 0;
                        serviceSelect.disabled = assignableServices.length === 0;
                        productSelect.disabled = activeProducts.length === 0;
                        button.disabled = disabled;
                    }
                    document.getElementById("assignServiceId").addEventListener("change", updateAssignmentControls);
                    async function loadServices() {
                        const accountFilter = document.getElementById("filterAccountId").value;
                        const typeFilter = document.getElementById("filterServiceType").value;
                        const statusFilter = document.getElementById("filterStatus").value;
                        const query = new URLSearchParams();
                        if (accountFilter) query.set("accountId", accountFilter);
                        if (typeFilter) query.set("serviceType", typeFilter);
                        if (statusFilter) query.set("status", statusFilter);
                        const services = await fetchJson(`/api/services${query.toString() ? "?" + query.toString() : ""}`);
                        currentServices = services;
                        updateAssignmentControls();
                        if (services.length === 0) {
                            document.getElementById("result").innerHTML = message("info", "No hay servicios creados.");
                            return;
                        }
                        const rows = services.map(service => `
                            <tr>
                                <td>${service.id}</td>
                                <td>${service.accountId}</td>
                                <td>${service.ownerName}</td>
                                <td>${service.serviceName || ""}</td>
                                <td>${service.serviceCode}</td>
                                <td>${service.serviceType}</td>
                                <td><span class="status ${service.status.toLowerCase()}">${service.status}</span></td>
                                <td>${service.products.map(product => `${product.productCode} (${product.status})<br><span class="bc-time-note">PVT compra: ${formatDate(product.pinVirtualTimeT || product.assignedAt)}</span>`).join("<br>")}</td>
                                <td>${formatDate(service.pinVirtualTimeT || service.activationDate || service.createdAt)}</td>
                                <td>${formatDate(service.createdT)}</td>
                                <td>
                                    <div class="actions">
                                        <button type="button" onclick="startServiceEdit('${service.id}')">Editar</button>
                                        <button type="button" onclick="prepareAssignProduct('${service.id}')">Asignar producto</button>
                                        ${service.status === "ACTIVE" ? `<button class="muted" type="button" onclick="serviceAction('${service.id}', 'suspend')">Suspender</button>` : ""}
                                        ${service.status === "SUSPENDED" ? `<button class="secondary" type="button" onclick="serviceAction('${service.id}', 'reactivate')">Reactivar</button>` : ""}
                                        ${service.status !== "TERMINATED" ? `<button class="danger" type="button" onclick="serviceAction('${service.id}', 'terminate')">Terminar</button>` : ""}
                                    </div>
                                </td>
                            </tr>
                        `).join("");
                        document.getElementById("result").innerHTML = `
                            <h2>Services creados</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Id</th>
                                        <th>Cuenta</th>
                                        <th>Titular</th>
                                        <th>Nombre</th>
                                        <th>Codigo</th>
                                        <th>Tipo</th>
                                        <th>Estado</th>
                                        <th>Productos</th>
                                        <th>PVT alta</th>
                                        <th>Fecha real</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }
                    document.getElementById("catalogServiceForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        try {
                            await fetchJson(editingCatalogId ? `/api/service-catalog/${editingCatalogId}` : "/api/service-catalog", {
                                method: editingCatalogId ? "PUT" : "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({
                                    name: document.getElementById("catalogName").value,
                                    serviceType: document.getElementById("catalogServiceType").value,
                                    description: document.getElementById("catalogDescription").value,
                                    status: document.getElementById("catalogStatus").value
                                })
                            });
                            const wasEditing = editingCatalogId !== null;
                            cancelCatalogEdit();
                            await loadCatalog();
                            document.getElementById("catalogResult").insertAdjacentHTML("afterbegin", message("success", wasEditing ? "Servicio general actualizado correctamente." : "Servicio general creado correctamente."));
                        } catch (error) {
                            document.getElementById("catalogResult").innerHTML = message("error", error.message);
                        }
                    });
                    document.getElementById("activateServiceForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const accountId = document.getElementById("accountId").value;
                        const catalogServiceId = document.getElementById("catalogServiceId").value;
                        if (!accountId || !catalogServiceId) {
                            document.getElementById("result").innerHTML = message("warning", "Seleccione una cuenta y un servicio general.");
                            return;
                        }
                        try {
                            await fetchJson(`/api/accounts/${accountId}/services/catalog/${catalogServiceId}/activate`, { method: "POST" });
                            await loadServices();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Servicio activado correctamente en la cuenta."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });
                    document.getElementById("filterServicesForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        loadServices().catch(error => document.getElementById("result").innerHTML = message("error", error.message));
                    });
                    document.getElementById("serviceProductForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const serviceId = document.getElementById("assignServiceId").value;
                        const productId = document.getElementById("assignProductId").value;
                        if (!serviceId || !productId) {
                            document.getElementById("result").innerHTML = message("warning", "Seleccione un servicio y un producto activo.");
                            return;
                        }
                        try {
                            await fetchJson(`/api/services/${serviceId}/products/${productId}`, { method: "POST" });
                            await loadServices();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Producto asociado correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });
                    document.getElementById("serviceForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        if (!editingServiceId) {
                            document.getElementById("result").innerHTML = message("warning", "Seleccione un servicio para editar.");
                            return;
                        }
                        const service = currentServices.find(item => item.id === editingServiceId);
                        try {
                            await fetchJson(`/api/services/${editingServiceId}`, {
                                method: "PUT",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({
                                    serviceName: document.getElementById("serviceName").value,
                                    serviceType: document.getElementById("serviceType").value,
                                    serviceCode: service ? service.serviceCode : ""
                                })
                            });
                            cancelServiceEdit();
                            await loadServices();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Servicio actualizado correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });
                    function prepareAssignProduct(serviceId) {
                        document.getElementById("assignServiceId").value = serviceId;
                        document.getElementById("assignProductId").focus();
                    }
                    function startServiceEdit(serviceId) {
                        const service = currentServices.find(item => item.id === serviceId);
                        if (!service) {
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("warning", "No fue posible encontrar el servicio seleccionado."));
                            return;
                        }
                        editingServiceId = service.id;
                        document.getElementById("serviceEditPanel").style.display = "";
                        document.getElementById("serviceName").value = service.serviceName || service.serviceCode;
                        document.getElementById("serviceType").value = service.serviceType;
                        document.getElementById("serviceName").focus();
                    }
                    function cancelServiceEdit() {
                        editingServiceId = null;
                        document.getElementById("serviceForm").reset();
                        document.getElementById("serviceEditPanel").style.display = "none";
                    }
                    function startCatalogEdit(catalogId) {
                        const item = currentCatalog.find(service => service.id === catalogId);
                        if (!item) {
                            document.getElementById("catalogResult").insertAdjacentHTML("afterbegin", message("warning", "No fue posible encontrar el servicio general seleccionado."));
                            return;
                        }
                        editingCatalogId = item.id;
                        document.getElementById("catalogName").value = item.name;
                        document.getElementById("catalogServiceType").value = item.serviceType;
                        document.getElementById("catalogStatus").value = item.status;
                        document.getElementById("catalogDescription").value = item.description || "";
                        document.getElementById("catalogSubmitButton").textContent = "Guardar servicio";
                        document.getElementById("cancelCatalogEditButton").style.display = "";
                        document.getElementById("catalogName").focus();
                    }
                    function cancelCatalogEdit() {
                        editingCatalogId = null;
                        document.getElementById("catalogServiceForm").reset();
                        document.getElementById("catalogServiceType").value = "MOBILE";
                        document.getElementById("catalogStatus").value = "ACTIVE";
                        document.getElementById("catalogSubmitButton").textContent = "Crear servicio";
                        document.getElementById("cancelCatalogEditButton").style.display = "none";
                    }
                    async function serviceAction(serviceId, action) {
                        try {
                            await fetchJson(`/api/services/${serviceId}/${action}`, { method: "POST" });
                            await loadServices();
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }
                    Promise.all([loadAccounts(), loadProducts(), loadCatalog()]).then(loadServices).catch(error => document.getElementById("result").innerHTML = message("error", error.message));
                </script>
                """);
    }

    /**
     * Renderiza la pagina de billing.
     *
     * @return pagina HTML para ejecutar billing y consultar corridas.
     */
    @GetMapping(value = "/billing", produces = MediaType.TEXT_HTML_VALUE)
    String billing() {
        return page("Billing", """
                <p>Proceso basico de billing usando productos activos, servicios activos y fecha virtual.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <a class="secondary" href="/virtual-time">Virtual Time</a>
                </div>
                <form id="billingForm">
                    <label>
                        Cuenta opcional
                        <select id="accountId" name="accountId">
                            <option value="">Billing general</option>
                        </select>
                    </label>
                    <button type="submit">Ejecutar billing</button>
                </form>
                <section id="result"></section>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }
                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }
                    async function fetchJson(url, options) {
                        const response = await fetch(url, options);
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible completar la operacion."));
                        }
                        return response.json();
                    }
                    async function loadAccounts() {
                        const accounts = await fetchJson("/api/accounts");
                        const select = document.getElementById("accountId");
                        accounts.forEach(account => {
                            const option = document.createElement("option");
                            option.value = account.id;
                            option.textContent = `${account.id} - ${account.ownerName} - ${account.status}`;
                            if (params.get("accountId") === account.id) option.selected = true;
                            select.appendChild(option);
                        });
                    }
                    async function loadRuns() {
                        const runs = await fetchJson("/api/billing/runs");
                        if (runs.length === 0) {
                            document.getElementById("result").innerHTML = message("info", "No hay billing runs ejecutados.");
                            return;
                        }
                        const rows = runs.map(run => `
                            <tr>
                                <td>${run.runCode}</td>
                                <td>${run.runType}</td>
                                <td><span class="status ${run.status.toLowerCase()}">${run.status}</span></td>
                                <td>${run.virtualTime}</td>
                                <td>${run.accountsProcessed}</td>
                                <td>${run.chargesCreated}</td>
                                <td>${money(run.totalAmount, "COP")}</td>
                                <td>${run.charges.map(charge => `
                                    <strong>${charge.billNo || "Sin bill"}</strong><br>
                                    DOM ${charge.billingDom || "N/A"} / ciclo ${charge.billingCycle || "N/A"} / mes ${charge.billingPeriodLabel || "N/A"}<br>
                                    ${charge.billPeriodStart || ""} a ${charge.billPeriodEnd || ""}<br>
                                    ${charge.productCode}: ${money(charge.amount, charge.currency)}
                                `).join("<hr>") || "Sin cargos"}</td>
                            </tr>
                        `).join("");
                        document.getElementById("result").innerHTML = `
                            <h2>Billing runs</h2>
                            <p>Cada cargo muestra el DOM, el ciclo configurado y el mes o rango de meses facturado.</p>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Run</th>
                                        <th>Tipo</th>
                                        <th>Estado</th>
                                        <th>Fecha virtual</th>
                                        <th>Cuentas</th>
                                        <th>Cargos</th>
                                        <th>Total</th>
                                        <th>Detalle de ciclo facturado</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }
                    document.getElementById("billingForm").addEventListener("submit", async function (event) {
                        event.preventDefault();
                        const accountId = document.getElementById("accountId").value;
                        try {
                            const url = accountId ? `/api/billing/accounts/${accountId}/run` : "/api/billing/run";
                            const run = await fetchJson(url, { method: "POST" });
                            await loadRuns();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", `Billing ejecutado. Cargos creados: ${run.chargesCreated}.`));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    });
                    loadAccounts().then(loadRuns).catch(error => document.getElementById("result").innerHTML = message("error", error.message));
                </script>
                """);
    }

    /**
     * Renderiza el modulo de reportes.
     *
     * @return pagina HTML para filtros, exportacion CSV y consultas de disputas.
     */
    @GetMapping(value = "/reports", produces = MediaType.TEXT_HTML_VALUE)
    String reports() {
        return page("Reportes", """
                <p>Vista consolidada de transacciones y disputas por cuenta.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <a class="secondary" href="/transactions">Historial completo</a>
                    <a class="secondary" href="/disputes">Gestionar disputas</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="reportForm" class="filter-form">
                    <label>
                        Cuenta
                        <select id="accountId" name="accountId">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <label>
                        Tipo de transaccion
                        <select id="transactionType" name="transactionType">
                            <option value="">Todas</option>
                            <option value="PAYMENT">Pagos</option>
                            <option value="REFUND">Reembolsos</option>
                            <option value="WRITE_OFF">Write-offs</option>
                        </select>
                    </label>
                    <label>
                        Estado disputa
                        <select id="disputeStatus" name="disputeStatus">
                            <option value="">Todos</option>
                            <option value="PENDING">Pendiente</option>
                            <option value="APPROVED">Aprobada</option>
                            <option value="REJECTED">Rechazada</option>
                            <option value="SETTLED">Settlement</option>
                        </select>
                    </label>
                    <button type="submit">Generar reporte</button>
                </form>
                <section id="summary"></section>
                <section id="transactionsReport"></section>
                <section id="disputesReport"></section>
                <script>
                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    async function loadAccounts() {
                        const select = document.getElementById("accountId");
                        const response = await fetch("/api/accounts");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible cargar las cuentas."));
                        }

                        const accounts = await response.json();
                        select.innerHTML = "<option value=''>Todas las cuentas</option>";
                        accounts.forEach(account => {
                            const option = document.createElement("option");
                            option.value = account.id;
                            option.textContent = `${account.id} - ${account.ownerName}`;
                            select.appendChild(option);
                        });
                    }

                    function query(params) {
                        const value = params.toString();
                        return value ? `?${value}` : "";
                    }

                    async function loadReport() {
                        const accountId = document.getElementById("accountId").value;
                        const transactionType = document.getElementById("transactionType").value;
                        const disputeStatus = document.getElementById("disputeStatus").value;
                        const transactionParams = new URLSearchParams();
                        const disputeParams = new URLSearchParams();

                        if (accountId) {
                            transactionParams.set("accountId", accountId);
                            disputeParams.set("accountId", accountId);
                        }
                        if (transactionType) {
                            transactionParams.set("type", transactionType);
                        }
                        if (disputeStatus) {
                            disputeParams.set("status", disputeStatus);
                        }

                        document.getElementById("summary").innerHTML = message("info", "Generando reporte...");
                        document.getElementById("transactionsReport").innerHTML = "";
                        document.getElementById("disputesReport").innerHTML = "";

                        try {
                            const [transactionsResponse, disputesResponse] = await Promise.all([
                                fetch(`/api/transactions${query(transactionParams)}`),
                                fetch(`/api/disputes${query(disputeParams)}`)
                            ]);

                            if (!transactionsResponse.ok) {
                                throw new Error(await readError(transactionsResponse, "No fue posible consultar transacciones."));
                            }
                            if (!disputesResponse.ok) {
                                throw new Error(await readError(disputesResponse, "No fue posible consultar disputas."));
                            }

                            const transactions = await transactionsResponse.json();
                            const disputes = await disputesResponse.json();
                            const totalTransactions = transactions.reduce((sum, item) => sum + Number(item.amount || 0), 0);
                            const totalDisputes = disputes.reduce((sum, item) => sum + Number(item.amount || 0), 0);

                            document.getElementById("summary").innerHTML = `
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Transacciones</th>
                                            <th>Total transacciones COP</th>
                                            <th>Disputas</th>
                                            <th>Total disputas COP</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td>${transactions.length}</td>
                                            <td>${money(totalTransactions, "COP")}</td>
                                            <td>${disputes.length}</td>
                                            <td>${money(totalDisputes, "COP")}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            `;

                            renderTransactions(transactions);
                            renderDisputes(disputes);
                        } catch (error) {
                            document.getElementById("summary").innerHTML = message("error", error.message);
                        }
                    }

                    function renderTransactions(transactions) {
                        const target = document.getElementById("transactionsReport");
                        if (transactions.length === 0) {
                            target.innerHTML = `<h2>Transacciones</h2>${message("info", "No hay transacciones para los filtros seleccionados.")}`;
                            return;
                        }

                        const rows = transactions.map(transaction => `
                            <tr>
                                <td>${transaction.accountId}</td>
                                <td>${transaction.ownerName}</td>
                                <td>${transaction.id}</td>
                                <td>${transaction.type}</td>
                                <td>${money(transaction.amount, transaction.currency)}</td>
                                <td>${transaction.paymentMethod || ""}</td>
                                <td>${transaction.description || ""}</td>
                                <td>${transaction.createdAt}</td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>Transacciones</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Cuenta</th>
                                        <th>Titular</th>
                                        <th>Transaccion</th>
                                        <th>Tipo</th>
                                        <th>Monto</th>
                                        <th>Metodo pago</th>
                                        <th>Descripcion</th>
                                        <th>Fecha</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    function renderDisputes(disputes) {
                        const target = document.getElementById("disputesReport");
                        if (disputes.length === 0) {
                            target.innerHTML = `<h2>Disputas</h2>${message("info", "No hay disputas para los filtros seleccionados.")}`;
                            return;
                        }

                        const rows = disputes.map(dispute => `
                            <tr>
                                <td>${dispute.accountId}</td>
                                <td>${dispute.ownerName}</td>
                                <td>${dispute.id}</td>
                                <td>${money(dispute.amount, dispute.currency)}</td>
                                <td>${dispute.reason}</td>
                                <td><span class="status ${dispute.status.toLowerCase()}">${dispute.status}</span></td>
                                <td>${dispute.resolutionNote || ""}</td>
                                <td>${dispute.createdAt}</td>
                            </tr>
                        `).join("");

                        target.innerHTML = `
                            <h2>Disputas</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>Cuenta</th>
                                        <th>Titular</th>
                                        <th>Disputa</th>
                                        <th>Monto</th>
                                        <th>Motivo</th>
                                        <th>Estado</th>
                                        <th>Nota</th>
                                        <th>Fecha</th>
                                    </tr>
                                </thead>
                                <tbody>${rows}</tbody>
                            </table>
                        `;
                    }

                    document.getElementById("reportForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        loadReport();
                    });

                    loadAccounts()
                        .then(loadReport)
                        .catch(error => {
                            document.getElementById("summary").innerHTML = message("error", error.message);
                        });
                </script>
                """);
    }

    /**
     * Renderiza el historial completo de transacciones.
     *
     * @return pagina HTML de busqueda y exportacion de movimientos.
     */
    @GetMapping(value = "/transactions", produces = MediaType.TEXT_HTML_VALUE)
    String transactions() {
        return page("Historial de transacciones", """
                <p>Filtra pagos, reembolsos o todos los movimientos. Tambien puedes exportar el resultado a CSV.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="filterForm" class="filter-form">
                    <label>
                        Cuenta
                        <select id="accountId" name="accountId">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <label>
                        Tipo
                        <select id="type" name="type">
                            <option value="">Todos</option>
                            <option value="PAYMENT">Pago</option>
                            <option value="REFUND">Reembolso</option>
                            <option value="WRITE_OFF">Write-off</option>
                        </select>
                    </label>
                    <label>
                        Fecha desde
                        <input id="dateFrom" name="dateFrom" type="datetime-local">
                    </label>
                    <label>
                        Fecha hasta
                        <input id="dateTo" name="dateTo" type="datetime-local">
                    </label>
                    <label>
                        Monto minimo
                        <input id="minAmount" name="minAmount" type="number" min="0" step="0.01">
                    </label>
                    <label>
                        Monto maximo
                        <input id="maxAmount" name="maxAmount" type="number" min="0" step="0.01">
                    </label>
                    <button type="submit">Filtrar</button>
                </form>
                <div class="toolbar">
                    <button class="secondary" type="button" onclick="exportCsv()">Exportar CSV</button>
                </div>
                <section id="result"></section>
                <script>
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    function buildQuery() {
                        const params = new URLSearchParams();
                        const accountId = document.getElementById("accountId").value;
                        const type = document.getElementById("type").value;
                        const dateFrom = document.getElementById("dateFrom").value;
                        const dateTo = document.getElementById("dateTo").value;
                        const minAmount = document.getElementById("minAmount").value;
                        const maxAmount = document.getElementById("maxAmount").value;

                        if (accountId) params.set("accountId", accountId);
                        if (type) params.set("type", type);
                        if (dateFrom) params.set("dateFrom", dateFrom);
                        if (dateTo) params.set("dateTo", dateTo);
                        if (minAmount) params.set("minAmount", minAmount);
                        if (maxAmount) params.set("maxAmount", maxAmount);

                        return params.toString();
                    }

                    async function loadAccounts() {
                        const select = document.getElementById("accountId");
                        const response = await fetch("/api/accounts");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible cargar las cuentas."));
                        }

                        const accounts = await response.json();
                        select.innerHTML = "<option value=''>Todas las cuentas</option>";
                        accounts.forEach(account => {
                            const option = document.createElement("option");
                            option.value = account.id;
                            option.textContent = `${account.id} - ${account.ownerName}`;
                            select.appendChild(option);
                        });
                    }

                    async function searchTransactions() {
                        const result = document.getElementById("result");
                        result.innerHTML = message("info", "Consultando transacciones...");

                        try {
                            const query = buildQuery();
                            const response = await fetch(`/api/transactions${query ? `?${query}` : ""}`);
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar transacciones."));
                            }

                            const transactions = await response.json();
                            if (transactions.length === 0) {
                                result.innerHTML = message("info", "No hay transacciones para los filtros seleccionados.");
                                return;
                            }

                            const rows = transactions.map(transaction => `
                                <tr>
                                    <td>${transaction.accountId}</td>
                                    <td>${transaction.ownerName}</td>
                                    <td>${transaction.id}</td>
                                    <td>${transaction.type}</td>
                                    <td>${money(transaction.amount, transaction.currency)}</td>
                                    <td>${money(transaction.originalAmount, transaction.originalCurrency)} / TRM ${Number(transaction.exchangeRate || 1).toFixed(2)}</td>
                                    <td>${transaction.description || ""}</td>
                                    <td>${transaction.createdAt}</td>
                                </tr>
                            `).join("");

                            result.innerHTML = `
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Cuenta</th>
                                            <th>Titular</th>
                                            <th>Transaccion</th>
                                            <th>Tipo</th>
                                            <th>Monto COP</th>
                                            <th>Original / TRM</th>
                                            <th>Descripcion</th>
                                            <th>Fecha</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }

                    function exportCsv() {
                        const accountId = document.getElementById("accountId").value;
                        if (!accountId) {
                            document.getElementById("result").innerHTML = message("warning", "Debe seleccionar una cuenta para exportar CSV.");
                            return;
                        }

                        const query = buildQuery();
                        window.location.href = `/api/transactions/export${query ? `?${query}` : ""}`;
                    }

                    document.getElementById("filterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        searchTransactions();
                    });

                    loadAccounts()
                        .then(searchTransactions)
                        .catch(error => {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        });
                </script>
                """);
    }

    /**
     * Renderiza la pagina de disputas y settlements.
     *
     * @return pagina HTML para crear, aprobar, rechazar y cerrar disputas por settlement.
     */
    @GetMapping(value = "/disputes", produces = MediaType.TEXT_HTML_VALUE)
    String disputes() {
        return page("Disputas", """
                <p>Crea disputas y decide si se aprueban o se rechazan. Las decisiones quedan registradas en eventos.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <a class="muted" href="/events">Ver eventos</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="disputeForm" class="movement-form">
                    <label>
                        Cuenta
                        <select id="accountId" name="accountId" required>
                            <option value="">Seleccione una cuenta</option>
                        </select>
                    </label>
                    <label>
                        Monto
                        <input id="amount" name="amount" type="number" min="0.01" step="0.01" value="25.00" required>
                    </label>
                    <label class="description-field">
                        Motivo
                        <input id="reason" name="reason" type="text" maxlength="240" value="Cliente reporta cobro no reconocido" required>
                    </label>
                    <button class="secondary" type="submit">Crear disputa</button>
                </form>
                <form id="filterForm" class="filter-form">
                    <label>
                        Cuenta
                        <select id="filterAccountId" name="filterAccountId">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <label>
                        Estado
                        <select id="status" name="status">
                            <option value="">Todos</option>
                            <option value="PENDING">Pendiente</option>
                            <option value="APPROVED">Aprobada</option>
                            <option value="REJECTED">Rechazada</option>
                            <option value="SETTLED">Settlement</option>
                        </select>
                    </label>
                    <button type="submit">Consultar disputas</button>
                </form>
                <section id="result"></section>
                <script>
                    const params = new URLSearchParams(window.location.search);
                    const initialStatus = params.get("status");

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    async function loadAccounts() {
                        const createSelect = document.getElementById("accountId");
                        const filterSelect = document.getElementById("filterAccountId");
                        const response = await fetch("/api/accounts");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible cargar las cuentas."));
                        }

                        const accounts = await response.json();
                        const selectedId = params.get("accountId");
                        createSelect.innerHTML = "<option value=''>Seleccione una cuenta</option>";
                        filterSelect.innerHTML = "<option value=''>Todas las cuentas</option>";

                        accounts.forEach(account => {
                            const label = `${account.id} - ${account.ownerName} - saldo ${Number(account.balance).toFixed(2)} ${account.currency || "COP"}`;
                            const createOption = document.createElement("option");
                            createOption.value = account.id;
                            createOption.textContent = label;
                            if (selectedId === account.id) {
                                createOption.selected = true;
                            }
                            createSelect.appendChild(createOption);

                            const filterOption = document.createElement("option");
                            filterOption.value = account.id;
                            filterOption.textContent = label;
                            if (selectedId === account.id) {
                                filterOption.selected = true;
                            }
                            filterSelect.appendChild(filterOption);
                        });
                    }

                    function buildQuery() {
                        const params = new URLSearchParams();
                        const accountId = document.getElementById("filterAccountId").value;
                        const status = document.getElementById("status").value;
                        if (accountId) params.set("accountId", accountId);
                        if (status) params.set("status", status);
                        return params.toString();
                    }

                    if (initialStatus) {
                        document.getElementById("status").value = initialStatus;
                    }

                    document.getElementById("disputeForm").addEventListener("submit", async function (event) {
                        event.preventDefault();

                        const result = document.getElementById("result");
                        const accountId = document.getElementById("accountId").value;
                        const amount = Number(document.getElementById("amount").value);
                        const reason = document.getElementById("reason").value.trim();

                        if (!accountId) {
                            result.innerHTML = message("warning", "Debe seleccionar una cuenta.");
                            return;
                        }

                        if (Number.isNaN(amount) || amount <= 0) {
                            result.innerHTML = message("warning", "El monto debe ser mayor a cero.");
                            return;
                        }

                        if (!reason) {
                            result.innerHTML = message("warning", "Debe ingresar el motivo de la disputa.");
                            return;
                        }

                        result.innerHTML = message("info", "Creando disputa...");

                        try {
                            const response = await fetch("/api/disputes", {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ accountId, amount, reason })
                            });

                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible crear la disputa."));
                            }

                            document.getElementById("filterAccountId").value = accountId;
                            await loadDisputes();
                            result.insertAdjacentHTML("afterbegin", message("success", "Disputa creada correctamente."));
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    });

                    async function resolveDispute(disputeId, action) {
                        const actionText = action === "approve" ? "aprobar" : "rechazar";
                        const resolutionNote = window.prompt(`Nota para ${actionText} la disputa`, "");
                        if (resolutionNote === null) {
                            return;
                        }

                        try {
                            const response = await fetch(`/api/disputes/${disputeId}/${action}`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ resolutionNote })
                            });

                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible resolver la disputa."));
                            }

                            await loadDisputes();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Disputa actualizada correctamente."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }

                    async function createSettlement(disputeId) {
                        const amountText = window.prompt("Monto del settlement en COP", "");
                        if (amountText === null) {
                            return;
                        }

                        const amount = Number(amountText);
                        if (Number.isNaN(amount) || amount <= 0) {
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("warning", "El monto del settlement debe ser mayor a cero."));
                            return;
                        }

                        const note = window.prompt("Nota del settlement", "");
                        if (note === null) {
                            return;
                        }

                        try {
                            const response = await fetch(`/api/disputes/${disputeId}/settlements`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ amount, note })
                            });

                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible crear el settlement."));
                            }

                            await loadDisputes();
                            document.getElementById("result").insertAdjacentHTML("afterbegin", message("success", "Settlement creado correctamente. La disputa quedo cerrada como SETTLED."));
                        } catch (error) {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        }
                    }

                    async function loadDisputes() {
                        const result = document.getElementById("result");
                        result.innerHTML = message("info", "Consultando disputas...");

                        try {
                            const query = buildQuery();
                            const response = await fetch(`/api/disputes${query ? `?${query}` : ""}`);
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar las disputas."));
                            }

                            const disputes = await response.json();
                            if (disputes.length === 0) {
                                result.innerHTML = message("info", "No hay disputas para los filtros seleccionados.");
                                return;
                            }

                            const rows = disputes.map(dispute => `
                                <tr>
                                    <td>${dispute.id}</td>
                                    <td>${dispute.accountId}</td>
                                    <td>${dispute.ownerName}</td>
                                    <td>${Number(dispute.amount).toFixed(2)} ${dispute.currency || "COP"}</td>
                                    <td>${dispute.reason}</td>
                                    <td><span class="status ${dispute.status.toLowerCase()}">${dispute.status}</span></td>
                                    <td>${dispute.resolutionNote || ""}</td>
                                    <td>${dispute.createdAt}</td>
                                    <td>
                                        ${dispute.status === "PENDING" ? `
                                            <div class="actions">
                                                <button type="button" onclick="createSettlement('${dispute.id}')">Settlement</button>
                                                <button class="secondary" type="button" onclick="resolveDispute('${dispute.id}', 'approve')">Aprobar</button>
                                                <button class="danger" type="button" onclick="resolveDispute('${dispute.id}', 'reject')">Rechazar</button>
                                            </div>
                                        ` : ""}
                                    </td>
                                </tr>
                            `).join("");

                            result.innerHTML = `
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Disputa</th>
                                            <th>Cuenta</th>
                                            <th>Titular</th>
                                            <th>Monto</th>
                                            <th>Motivo</th>
                                            <th>Estado</th>
                                            <th>Nota</th>
                                            <th>Fecha</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }

                    document.getElementById("filterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        loadDisputes();
                    });

                    loadAccounts()
                        .then(loadDisputes)
                        .catch(error => {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        });
                </script>
                """);
    }

    /**
     * Renderiza la pagina de eventos de auditoria.
     *
     * @return pagina HTML para consultar eventos por cuenta y tipo.
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_HTML_VALUE)
    String events() {
        return page("Eventos del sistema", """
                <p>Consulta la tabla completa de eventos generados por cuentas, pagos, reembolsos, cierres y disputas.</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <a class="secondary" href="/disputes">Disputas</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="filterForm" class="filter-form">
                    <label>
                        Cuenta
                        <select id="accountId" name="accountId">
                            <option value="">Todas las cuentas</option>
                        </select>
                    </label>
                    <label>
                        Tipo de evento
                        <select id="type" name="type">
                            <option value="">Todos</option>
                            <option value="ACCOUNT_CREATED">Cuenta creada</option>
                            <option value="PAYMENT_RECEIVED">Pago registrado</option>
                            <option value="REFUND_SENT">Reembolso registrado</option>
                            <option value="WRITE_OFF_APPLIED">Write-off aplicado</option>
                            <option value="ACCOUNT_CLOSED">Cuenta cerrada</option>
                            <option value="DISPUTE_CREATED">Disputa creada</option>
                            <option value="DISPUTE_SETTLEMENT_CREATED">Settlement de disputa</option>
                            <option value="DISPUTE_SETTLED">Disputa cerrada por settlement</option>
                            <option value="DISPUTE_APPROVED">Disputa aprobada</option>
                            <option value="DISPUTE_REJECTED">Disputa rechazada</option>
                            <option value="VIRTUAL_TIME_UPDATED">Virtual time actualizado</option>
                            <option value="VIRTUAL_TIME_RESET">Virtual time reset</option>
                            <option value="PRODUCT_CREATED">Producto creado</option>
                            <option value="PRODUCT_UPDATED">Producto actualizado</option>
                            <option value="PRODUCT_ACTIVATED">Producto activado</option>
                            <option value="PRODUCT_DEACTIVATED">Producto desactivado</option>
                            <option value="SERVICE_CATALOG_CREATED">Servicio general creado</option>
                            <option value="SERVICE_CATALOG_UPDATED">Servicio general actualizado</option>
                            <option value="SERVICE_ACTIVATED">Servicio activado en cuenta</option>
                            <option value="SERVICE_CREATED">Servicio creado</option>
                            <option value="SERVICE_UPDATED">Servicio actualizado</option>
                            <option value="SERVICE_SUSPENDED">Servicio suspendido</option>
                            <option value="SERVICE_REACTIVATED">Servicio reactivado</option>
                            <option value="SERVICE_TERMINATED">Servicio terminado</option>
                            <option value="SERVICE_PRODUCT_ASSIGNED">Producto asignado a servicio</option>
                            <option value="SERVICE_PRODUCT_CANCELLED">Producto cancelado de servicio</option>
                            <option value="BILLING_RUN_STARTED">Billing iniciado</option>
                            <option value="BILLING_RUN_COMPLETED">Billing completado</option>
                            <option value="BILLING_RUN_FAILED">Billing fallido</option>
                            <option value="BILLING_CHARGE_CREATED">Billing charge creado</option>
                            <option value="BILL_ITEM_CREATED">Bill item creado</option>
                            <option value="INVENTORY_ITEM_CREATED">Inventario creado</option>
                            <option value="INVENTORY_ITEM_UPDATED">Inventario actualizado</option>
                            <option value="INVENTORY_AVAILABILITY_CHANGED">Disponibilidad inventario</option>
                            <option value="INVOICE_GENERATED">Invoice generada</option>
                            <option value="INVOICE_LINE_CREATED">Linea de invoice creada</option>
                            <option value="INVOICE_SENT">Invoice enviada</option>
                            <option value="INVOICE_CANCELLED">Invoice cancelada</option>
                            <option value="INVOICE_PAID">Invoice pagada</option>
                            <option value="INVOICE_PARTIALLY_PAID">Invoice parcialmente pagada</option>
                            <option value="CREDIT_NOTE_CREATED">Nota de credito creada</option>
                            <option value="CREDIT_NOTE_LINE_CREATED">Linea de nota de credito creada</option>
                            <option value="CREDIT_NOTE_APPLIED">Nota de credito aplicada</option>
                            <option value="CREDIT_NOTE_CANCELLED">Nota de credito cancelada</option>
                        </select>
                    </label>
                    <button type="submit">Consultar eventos</button>
                </form>
                <section id="result"></section>
                <script>
                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    async function loadAccounts() {
                        const select = document.getElementById("accountId");
                        const response = await fetch("/api/accounts");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible cargar las cuentas."));
                        }

                        const accounts = await response.json();
                        select.innerHTML = "<option value=''>Todas las cuentas</option>";
                        accounts.forEach(account => {
                            const option = document.createElement("option");
                            option.value = account.id;
                            option.textContent = `${account.id} - ${account.ownerName}`;
                            select.appendChild(option);
                        });
                    }

                    function buildQuery() {
                        const params = new URLSearchParams();
                        const accountId = document.getElementById("accountId").value;
                        const type = document.getElementById("type").value;
                        if (accountId) params.set("accountId", accountId);
                        if (type) params.set("type", type);
                        return params.toString();
                    }

                    async function loadEvents() {
                        const result = document.getElementById("result");
                        result.innerHTML = message("info", "Consultando eventos...");

                        try {
                            const query = buildQuery();
                            const response = await fetch(`/api/events${query ? `?${query}` : ""}`);
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar los eventos."));
                            }

                            const events = await response.json();
                            if (events.length === 0) {
                                result.innerHTML = message("info", "No hay eventos para los filtros seleccionados.");
                                return;
                            }

                            const rows = events.map(event => `
                                <tr>
                                    <td>${event.id}</td>
                                    <td>${event.type}</td>
                                    <td>${event.entityType || ""}</td>
                                    <td>${event.entityId || ""}</td>
                                    <td>${event.accountId || ""}</td>
                                    <td>${event.description}</td>
                                    <td>${event.createdAt}</td>
                                </tr>
                            `).join("");

                            result.innerHTML = `
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Evento</th>
                                            <th>Tipo</th>
                                            <th>Entidad</th>
                                            <th>Id entidad</th>
                                            <th>Cuenta</th>
                                            <th>Descripcion</th>
                                            <th>Fecha</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }

                    document.getElementById("filterForm").addEventListener("submit", function (event) {
                        event.preventDefault();
                        loadEvents();
                    });

                    loadAccounts()
                        .then(loadEvents)
                        .catch(error => {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        });
                </script>
                """);
    }

    /**
     * Renderiza la pagina de pagos.
     *
     * @return pagina HTML para registrar pagos.
     */
    @GetMapping(value = "/payments", produces = MediaType.TEXT_HTML_VALUE)
    String payments() {
        return movementPage(
                "Crear pago",
                "Registra un pago recibido para una cuenta.",
                "payments",
                "Pago recibido por servicio",
                "Registrar pago",
                "Pagos registrados",
                "secondary"
        );
    }

    /**
     * Renderiza la pagina de reembolsos.
     *
     * @return pagina HTML para reembolsar pagos completos.
     */
    @GetMapping(value = "/refunds", produces = MediaType.TEXT_HTML_VALUE)
    String refunds() {
        return movementPage(
                "Crear reembolso",
                "Registra un reembolso asociado a un pago existente.",
                "refunds",
                "Reembolso parcial al cliente",
                "Registrar reembolso",
                "Reembolsos registrados",
                "refund"
        );
    }

    /**
     * Renderiza la pagina de write-offs.
     *
     * @return pagina HTML para aplicar ajustes independientes.
     */
    @GetMapping(value = "/write-offs", produces = MediaType.TEXT_HTML_VALUE)
    String writeOffs() {
        return movementPage(
                "Crear write-off",
                "Registra un write-off como ajuste independiente del pago original.",
                "write-offs",
                "Ajuste write-off operativo",
                "Registrar write-off",
                "Write-offs registrados",
                "danger"
        );
    }

    private String movementPage(
            String title,
            String description,
            String endpoint,
            String defaultDescription,
            String buttonText,
            String listTitle,
            String buttonClass
    ) {
        var currencyField = endpoint.equals("payments")
                ? """
                    <label>
                        Moneda del pago
                        <select id="currency" name="currency">
                            <option value="COP">COP</option>
                            <option value="USD">USD</option>
                        </select>
                    </label>
                    """
                : """
                    <input id="currency" name="currency" type="hidden" value="COP">
                    """;
        var amountField = endpoint.equals("refunds")
                ? """
                    <p class="message info">El reembolso se realiza por la totalidad del pago seleccionado.</p>
                    """
                : """
                    <label>
                        Monto
                        <input id="amount" name="amount" type="number" min="0.01" step="0.01" value="50.00" required>
                    </label>
                    """;
        var paymentField = endpoint.equals("refunds")
                ? """
                    <label class="description-field">
                        Pago origen
                        <select id="paymentId" name="paymentId" required>
                            <option value="">Seleccione primero una cuenta</option>
                        </select>
                    </label>
                    """
                : """
                    <input id="paymentId" name="paymentId" type="hidden" value="">
                    """;
        var paymentMethodField = endpoint.equals("payments")
                ? """
                    <label class="description-field">
                        Metodo de pago CL
                        <select id="paymentMethod" name="paymentMethod">
                            <option value="CASH">Efectivo</option>
                            <option value="CHECK_DAY">Cheque Dia</option>
                            <option value="DEBIT_CARD">Tarjeta Debito</option>
                            <option value="CREDIT_CARD">Tarjeta Credito</option>
                            <option value="MANUAL_DEBIT_CARD">Tarjeta Debito Manual</option>
                            <option value="MANUAL_CREDIT_CARD">Tarjeta Credito Manual</option>
                            <option value="ELECTRONIC_TRANSFER">Transferencia Electronica</option>
                            <option value="SIMPLE_ADJUSTMENT">Ajuste por Sencillo</option>
                            <option value="POINT_EXCHANGE">Canje de Punto</option>
                        </select>
                    </label>
                    """
                : """
                    <input id="paymentMethod" name="paymentMethod" type="hidden" value="">
                    """;

        return page(title, """
                <p>%s</p>
                <div class="toolbar">
                    <a class="muted" href="/">Volver a la principal</a>
                    <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                        <button class="logout" type="submit">Cerrar sesion</button>
                    </form>
                </div>
                <form id="movementForm" class="movement-form">
                    <label>
                        Cuenta
                        <select id="accountId" name="accountId" required>
                            <option value="">Seleccione una cuenta</option>
                        </select>
                    </label>
                    %s
                    %s
                    %s
                    %s
                    <label class="description-field">
                        Descripcion
                        <input id="description" name="description" type="text" maxlength="240" value="%s">
                    </label>
                    <button class="%s" type="submit">%s</button>
                </form>
                <section id="trmInfo"></section>
                <div class="toolbar">
                    <button type="button" onclick="loadMovements()">Consultar registros</button>
                </div>
                <section id="result"></section>
                <script>
                    const endpoint = "%s";
                    const listTitle = "%s";
                    const successText = endpoint === "payments"
                        ? "Pago registrado correctamente."
                        : endpoint === "refunds"
                            ? "Reembolso registrado correctamente."
                            : "Write-off registrado correctamente.";
                    const params = new URLSearchParams(window.location.search);

                    function money(value, currency = "COP") {
                        return `<span class='money'>${Number(value).toFixed(2)} ${currency || "COP"}</span>`;
                    }

                    function message(type, text) {
                        return `<p class='message ${type}'>${text}</p>`;
                    }

                    async function readError(response, fallback) {
                        const error = await response.json().catch(() => ({}));
                        return error.detail || fallback;
                    }

                    document.getElementById("movementForm").addEventListener("submit", async function (event) {
                        event.preventDefault();

                        const result = document.getElementById("result");
                        const accountId = document.getElementById("accountId").value;
                        const amountInput = document.getElementById("amount");
                        const amount = amountInput ? Number(amountInput.value) : null;
                        const currency = document.getElementById("currency").value;
                        const paymentMethod = document.getElementById("paymentMethod").value;
                        const paymentId = document.getElementById("paymentId").value;
                        const description = document.getElementById("description").value.trim();

                        if (!accountId) {
                            result.innerHTML = message("warning", "Debe seleccionar una cuenta.");
                            return;
                        }

                        if (endpoint === "refunds" && !paymentId) {
                            result.innerHTML = message("warning", "Debe seleccionar el pago origen del reembolso.");
                            return;
                        }

                        if (endpoint !== "refunds" && (Number.isNaN(amount) || amount <= 0)) {
                            result.innerHTML = message("warning", "El monto debe ser mayor a cero.");
                            return;
                        }

                        result.innerHTML = message("info", "Registrando movimiento...");

                        try {
                            const payload = endpoint === "refunds"
                                ? { paymentId, description }
                                : { amount, currency, paymentMethod, paymentId, description };
                            const response = await fetch(`/api/accounts/${accountId}/${endpoint}`, {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify(payload)
                            });

                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible registrar el movimiento."));
                            }

                            await loadMovements();
                            result.insertAdjacentHTML("afterbegin", message("success", successText));
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    });

                    async function loadAccounts() {
                        const select = document.getElementById("accountId");
                        const response = await fetch("/api/accounts");
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible cargar las cuentas."));
                        }

                        const accounts = await response.json();
                        const selectedId = params.get("accountId");
                        select.innerHTML = "<option value=''>Seleccione una cuenta</option>";

                        accounts.forEach(account => {
                            const option = document.createElement("option");
                            option.value = account.id;
                            option.textContent = `${account.id} - ${account.ownerName} - saldo ${Number(account.balance).toFixed(2)} ${account.currency || "COP"}`;
                            if (selectedId === account.id) {
                                option.selected = true;
                            }
                            select.appendChild(option);
                        });

                        if (accounts.length === 0) {
                            document.getElementById("result").innerHTML = message("info", "No hay cuentas creadas. Vuelve a la pagina principal y crea una cuenta primero.");
                        }

                        if (endpoint === "refunds") {
                            await loadRefundPayments();
                        }
                    }

                    async function loadRefundPayments() {
                        if (endpoint !== "refunds") {
                            return;
                        }

                        const accountId = document.getElementById("accountId").value;
                        const paymentSelect = document.getElementById("paymentId");
                        paymentSelect.innerHTML = "<option value=''>Seleccione un pago</option>";
                        if (!accountId) {
                            paymentSelect.innerHTML = "<option value=''>Seleccione primero una cuenta</option>";
                            return;
                        }

                        const response = await fetch(`/api/accounts/${accountId}/payments`);
                        if (!response.ok) {
                            throw new Error(await readError(response, "No fue posible cargar los pagos."));
                        }

                        const payments = await response.json();
                        if (payments.length === 0) {
                            paymentSelect.innerHTML = "<option value=''>La cuenta no tiene pagos</option>";
                            return;
                        }

                        payments.forEach(payment => {
                            const option = document.createElement("option");
                            option.value = payment.id;
                            option.textContent = `${payment.id} - reembolso total ${Number(payment.amount).toFixed(2)} ${payment.currency || "COP"} - ${payment.description || ""}`;
                            paymentSelect.appendChild(option);
                        });
                    }

                    async function loadMovements() {
                        const result = document.getElementById("result");
                        const accountId = document.getElementById("accountId").value;

                        if (!accountId) {
                            result.innerHTML = message("warning", "Debe seleccionar una cuenta.");
                            return;
                        }

                        result.innerHTML = message("info", `Consultando ${listTitle.toLowerCase()}...`);

                        try {
                            const response = await fetch(`/api/accounts/${accountId}/${endpoint}`);
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar los movimientos."));
                            }

                            const movements = await response.json();
                            if (movements.length === 0) {
                                result.innerHTML = `<h2>${listTitle}</h2>${message("info", "No hay movimientos registrados.")}`;
                                return;
                            }

                            const rows = movements.map(movement => `
                                <tr>
                                    <td>${movement.id}</td>
                                    <td>${movement.type}</td>
                                    <td>${money(movement.amount, movement.currency)}</td>
                                    <td>${money(movement.originalAmount, movement.originalCurrency)} / TRM ${Number(movement.exchangeRate || 1).toFixed(2)}</td>
                                    <td>${movement.paymentMethod || ""}</td>
                                    <td>${movement.description || ""}</td>
                                    <td>${movement.createdAt}</td>
                                </tr>
                            `).join("");

                            result.innerHTML = `
                                <h2>${listTitle}</h2>
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Id</th>
                                            <th>Tipo</th>
                                            <th>Monto COP</th>
                                            <th>Original / TRM</th>
                                            <th>Metodo pago</th>
                                            <th>Descripcion</th>
                                            <th>Fecha</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            `;
                        } catch (error) {
                            result.innerHTML = message("error", error.message);
                        }
                    }

                    async function loadTrm() {
                        const trmInfo = document.getElementById("trmInfo");
                        const currency = document.getElementById("currency").value;
                        if (endpoint !== "payments" || currency !== "USD") {
                            trmInfo.innerHTML = "";
                            return;
                        }

                        trmInfo.innerHTML = message("info", "Consultando TRM del dia...");
                        try {
                            const response = await fetch("/api/exchange-rates/usd-cop");
                            if (!response.ok) {
                                throw new Error(await readError(response, "No fue posible consultar la TRM."));
                            }

                            const trm = await response.json();
                            trmInfo.innerHTML = `<p class='trm-box'>TRM del dia: <strong>${Number(trm.rate).toFixed(2)} COP</strong> por 1 USD. Fuente: ${trm.source}.</p>`;
                        } catch (error) {
                            trmInfo.innerHTML = message("warning", error.message);
                        }
                    }

                    document.getElementById("currency").addEventListener("change", loadTrm);
                    document.getElementById("accountId").addEventListener("change", function () {
                        loadTrm();
                        loadRefundPayments().catch(error => {
                            document.getElementById("result").innerHTML = message("error", error.message);
                        });
                    });
                    loadAccounts().catch(error => {
                        document.getElementById("result").innerHTML = message("error", error.message);
                    });
                    loadTrm();
                </script>
                """.formatted(
                        description,
                        amountField,
                        currencyField,
                        paymentField,
                        paymentMethodField,
                        defaultDescription,
                        buttonClass,
                        buttonText,
                        endpoint,
                        listTitle
                ));
    }

    private String page(String title, String body) {
        var username = userContextService.currentUsername();
        var adminLink = userContextService.isAdmin() ? "<a href=\"/users\">Usuarios</a>" : "";
        return """
                <!doctype html>
                <html lang="es">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                    %s
                    <link rel="stylesheet" href="/css/brmc-billing-care.css">
                </head>
                <body>
                    <header class="bc-topbar">
                        <div class="bc-brand">BRMC Billing Care</div>
                        <form class="bc-search" action="/accounts" method="get">
                            <input name="q" type="search" placeholder="Search account, customer, bill or service">
                        </form>
                        <div class="bc-user">
                            <span>%s</span>
                            <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                                <button class="logout" type="submit">Salir</button>
                            </form>
                        </div>
                    </header>
                    <div class="bc-shell">
                        <aside class="bc-sidebar">
                            <nav>
                                <a href="/">Dashboard</a>
                                <a href="/accounts">Accounts</a>
                                <a href="/payments">Payments</a>
                                <a href="/refunds">Refunds</a>
                                <a href="/write-offs">Write-Offs</a>
                                <a href="/disputes">Disputes</a>
                                <a href="/products">Products</a>
                                <a href="/services">Services</a>
                                <a href="/billing">Billing</a>
                                <a href="/bills">Bills</a>
                                <a href="/invoices">Invoices</a>
                                <a href="/virtual-time">Virtual Time</a>
                                <a href="/inventory">Inventory</a>
                                <a href="/entel">ENTEL</a>
                                <a href="/reports">Reports</a>
                                <a href="/events">Events</a>
                                %s
                            </nav>
                        </aside>
                        <main class="bc-content">
                            <section class="bc-page-card">
                                <h1 class="bc-page-title">%s</h1>
                                %s
                            </section>
                        </main>
                    </div>
                </body>
                </html>
                """.formatted(title, STYLES, username, adminLink, title, body);
    }

    private String adminDashboardCard() {
        if (!userContextService.isAdmin()) {
            return "";
        }
        return """
                    <article class="module">
                        <span class="bc-module-meta">Admin</span>
                        <h2>Gestor de usuarios</h2>
                        <p>Crea usuarios de acceso y define si son ADMIN o USER.</p>
                        <div class="actions">
                            <a class="secondary" href="/users">Gestionar usuarios</a>
                        </div>
                    </article>
                """;
    }
}
