# 🛒 POS Papelería - Sistema de Punto de Venta y Auditoría Estricta

[cite_start]Un sistema de Punto de Venta (POS) de nivel corporativo diseñado específicamente para papelerías y centros de copiado[cite: 2, 15]. [cite_start]La arquitectura del sistema está centrada en la **prevención de pérdidas (robo hormiga)**, el **control granular de consumibles**, y la **auditoría financiera estricta**[cite: 11, 13].

## 🎯 Objetivos Principales del Sistema

* [cite_start]**Prevención de Pérdidas:** Mitiga robos internos y discrepancias mediante controles estrictos de flujo de efectivo y mercancía[cite: 11].
* [cite_start]**Gestión de Consumibles:** Inventaría insumos como tinta, tóner y papel, vinculándolos dinámicamente a la venta de servicios[cite: 13].
* [cite_start]**Control de Servicios de Valor Agregado:** Administra el cobro por impresiones, copias y engargolados integrando el costo real del material[cite: 15].

## ✨ Características Arquitectónicas Clave

* [cite_start]**🔒 Corte de Caja a Ciegas:** El cajero debe declarar el monto físico contabilizado al final del día sin conocer el saldo esperado por el sistema[cite: 33]. [cite_start]El sistema calcula automáticamente la diferencia para auditoría y levanta alertas[cite: 34, 558].
* [cite_start]**📦 Ventas Mixtas Polimórficas:** Capacidad de cobrar productos físicos e intangibles (servicios) bajo un mismo folio[cite: 25]. [cite_start]El sistema utiliza una relación polimórfica en el detalle de venta (`tipo_item`) para diferenciar el catálogo de origen[cite: 26].
* [cite_start]**🧪 Motor de "Recetas de Insumos":** Los servicios descuentan automáticamente fracciones de inventario[cite: 20]. [cite_start]Ejemplo: El servicio "Copia B/N" descuenta matemáticamente 1 unidad de papel del almacén virtual por cada venta[cite: 20, 543].
* [cite_start]**🛡️ Estrategia Dual de Inventario (Bodega vs Máquina):** El inventario de hojas es estricto por sistema, mientras que la tinta/tóner se controla visualmente mediante botellas cerradas administradas bajo llave, evitando justificaciones de robo por rendimiento de la impresora[cite: 547, 549, 550].
* [cite_start]**🔄 Auditoría Diferida de Devoluciones (Bandeja Roja):** El cajero tiene autonomía para procesar reintegros de efectivo agilizando el mostrador, pero el sistema levanta un flag de auditoría (`auditada = false`)[cite: 533, 534]. [cite_start]El administrador aprueba el movimiento posteriormente cotejando el ticket y el producto físico en una "Bandeja Roja"[cite: 535, 536].
* [cite_start]**💳 Métodos de Pago Inclusivos:** Soporte para Efectivo, Tarjetas Bancarias, Transferencias, y métodos específicos como Tarjetas Gubernamentales y pagos mediante Códigos QR del gobierno[cite: 40, 42, 43, 44, 45, 46, 47].

## 💻 Stack Tecnológico

[cite_start]El proyecto está diseñado para funcionar de manera óptima en un entorno local Windows [cite: 66][cite_start], empaquetado en formato JAR[cite: 65].

**Backend:**
* [cite_start]Java 21 [cite: 62]
* [cite_start]Spring Boot (3.2.x / 3.3.x) [cite: 62]
* [cite_start]Spring Data JPA & Hibernate [cite: 64]
* [cite_start]Spring Security (Manejo de Roles ADMIN/CAJERO y Hashing con BCrypt) [cite: 64, 473, 505]
* [cite_start]Lombok & Jakarta Validation [cite: 64, 161]

**Frontend:**
* [cite_start]HTML5, CSS3, JavaScript (Vanilla) [cite: 63, 513]
* [cite_start]Bootstrap 5 [cite: 513]
* [cite_start]Thymeleaf (Renderizado del lado del servidor) [cite: 63, 513]
* [cite_start]Arquitectura SPA simulada en el POS con *debounce* para escáneres láser y búsqueda manual[cite: 519, 520].

**Base de Datos:**
* [cite_start]MariaDB 12.2.x [cite: 69]
* [cite_start]Motor de almacenamiento: InnoDB (para integridad referencial y llaves foráneas) [cite: 70]
* [cite_start]Charset: UTF8MB4 [cite: 71]

## 🗄️ Esquema de Base de Datos (Estructura Core)

[cite_start]El sistema cuenta con un modelo de datos transaccional de 9 tablas principales[cite: 59]:
1. [cite_start]`usuarios`: Gestión de identidad y acceso[cite: 80].
2. [cite_start]`productos`: Inventario de venta física directa[cite: 88].
3. [cite_start]`insumos`: Almacén interno operativo[cite: 98].
4. [cite_start]`servicios`: Catálogo de intangibles[cite: 105].
5. [cite_start]`recetas_insumos`: Tabla pivote (llave compuesta) entre servicios e insumos[cite: 111, 264].
6. [cite_start]`ventas` y `detalle_ventas`: Motor transaccional financiero[cite: 120, 129].
7. [cite_start]`cortes_caja`: Auditoría de turnos[cite: 140].
8. [cite_start]`auditoria_logs`: Bitácora inmutable (Append-Only) de eventos sensibles[cite: 6, 7, 151].
9. [cite_start]`devoluciones`: Registro auditable de reversos financieros y mermas[cite: 532].

## 🚀 Próximos Pasos (Roadmap)

[cite_start]Fases en desarrollo activo para la versión "Go-Live"[cite: 560]:
- [ ] [cite_start]**Paso 1:** Interfaz UI de Gestión de Personal (CRUD de cajeros)[cite: 561, 573].
- [ ] [cite_start]**Paso 2:** Panel de Reportes Financieros y lectura visual de Auditoría[cite: 563, 575].
- [ ] [cite_start]**Paso 3:** Integración y maquetado de Ticket para Impresora Térmica (80mm)[cite: 565, 566, 578].
- [ ] [cite_start]**Paso 4:** Implementación de "Espionaje Silencioso" en el Front-End (envío asíncrono de logs al eliminar artículos del POS)[cite: 567, 568, 581].
- [ ] [cite_start]**Paso 5:** Configuración de integraciones SMTP (`spring-boot-starter-mail`) para envío automático de alertas de stock y descuadres en caja[cite: 569, 570, 584].

---
*Desarrollado con arquitectura sólida para garantizar trazabilidad y escalabilidad corporativa.*
