# 🛒 POS Papelería - Sistema de Punto de Venta y Auditoría Estricta

Un sistema de Punto de Venta (POS) de nivel corporativo diseñado específicamente para papelerías y centros de copiado. La arquitectura del sistema está centrada en la **prevención de pérdidas (robo hormiga)**, el **control granular de consumibles**, y la **auditoría financiera estricta**.

## 🎯 Objetivos Principales del Sistema

* **Prevención de Pérdidas:** Mitiga robos internos y discrepancias mediante controles estrictos de flujo de efectivo y mercancía.
* **Gestión de Consumibles:** Inventaría insumos como tinta, tóner y papel, vinculándolos dinámicamente a la venta de servicios.
* **Control de Servicios de Valor Agregado:** Administra el cobro por impresiones, copias y engargolados integrando el costo real del material.

## ✨ Características Arquitectónicas Clave

* **🔒 Corte de Caja a Ciegas:** El cajero debe declarar el monto físico contabilizado al final del día sin conocer el saldo esperado por el sistema, calculando el sistema automáticamente la diferencia para auditoría y levantando alertas.
* **📦 Ventas Mixtas Polimórficas:** Capacidad de cobrar productos físicos e intangibles (servicios) bajo un mismo folio, utilizando una relación polimórfica en el detalle de venta (`tipo_item`) para diferenciar el catálogo de origen.
* **🧪 Motor de "Recetas de Insumos":** Los servicios descuentan automáticamente fracciones de inventario (Ej. El servicio "Copia B/N" descuenta matemáticamente 1 unidad de papel del almacén virtual por cada venta).
* **🛡️ Estrategia Dual de Inventario:** El inventario de papel y soportes es estricto por sistema, mientras que la tinta/tóner se controla visualmente mediante botellas cerradas administradas bajo llave, exigiendo la entrega del envase vacío para su resurtido.
* **🔄 Auditoría Diferida de Devoluciones (Bandeja Roja):** El cajero tiene autonomía para procesar reintegros de efectivo agilizando el mostrador, pero el sistema levanta un flag de auditoría (`auditada = false`) para que el administrador apruebe el movimiento posteriormente cotejando el ticket y el producto físico.
* **💳 Métodos de Pago Inclusivos:** Soporte para Efectivo, Tarjetas Bancarias, Transferencias, y métodos específicos como Tarjetas Gubernamentales y pagos mediante Códigos QR del gobierno.
* **🚨 Módulo de Mermas Auditables:** Sistema de ajuste de inventario (físico e interno) que permite registrar productos o insumos dañados con justificación obligatoria, descontando el stock y dejando un registro inmutable en la bitácora (`AJUSTE_MERMA`).
* **👁️ Auditoría Omnipresente (Espía Silencioso):** Registro invisible en base de datos de acciones críticas como eliminación de artículos del carrito (`ELIMINAR_DEL_CARRITO`), cancelaciones completas, modificaciones de precio manuales, login de usuarios y aperturas de caja.
* **🛑 Escudo de Seguridad en Tiempo Real (Interceptor):** Un interceptor global de Spring Security que monitorea cada petición; si detecta que un usuario ha sido desactivado o ha cambiado su rol en la base de datos, destruye instantáneamente la sesión en la memoria RAM y expulsa al usuario al login lanzando un error `401 Unauthorized`.

## 💻 Stack Tecnológico

El proyecto está diseñado para funcionar de manera óptima en un entorno local Windows, empaquetado en formato JAR.

**Backend:**
* Java 21 con Spring Boot (3.2.x / 3.3.x)
* Spring Data JPA & Hibernate
* Spring Security (Manejo de Roles ADMIN/CAJERO, Interceptores de sesión y Hashing con BCrypt)
* JavaMailSender con `@EnableAsync` para reportes en segundo plano
* Lombok & Jakarta Validation

**Frontend:**
* HTML5, CSS3, JavaScript (Vanilla)
* Bootstrap 5 & Chart.js (Para analítica visual)
* Thymeleaf (Renderizado del lado del servidor y plantillas de correo electrónico)
* Arquitectura SPA simulada en el POS con *debounce* para escáneres láser y búsqueda manual asíncrona.

**Base de Datos:**
* MariaDB 12.2.x operando como servicio nativo en Windows
* Motor de almacenamiento: InnoDB (para integridad referencial y prevención de datos huérfanos)
* Cotejamiento: UTF8MB4

## 🗄️ Esquema de Base de Datos (Estructura Core)

El sistema cuenta con un modelo de datos transaccional de 9 tablas principales con motor InnoDB:
1. `usuarios`: Gestión de identidad, acceso y roles.
2. `productos`: Inventario de venta física directa con control de stock mínimo.
3. `insumos`: Almacén interno operativo con precisión de 4 decimales.
4. `servicios`: Catálogo de intangibles y cobros variables.
5. `recetas_insumos`: Tabla pivote (`@EmbeddedId`) entre servicios e insumos.
6. `ventas` y `detalle_ventas`: Motor transaccional financiero.
7. `cortes_caja`: Auditoría de turnos (declarado vs esperado).
8. `auditoria_logs`: Bitácora inmutable (Append-Only) de eventos sensibles.
9. `configuracion_sistema`: Almacén dinámico para preferencias como el envío automatizado de correos.

## 🚀 Hitos de Implementación Completados (Go-Live Ready)

El sistema ha superado con éxito todas las fases de desarrollo y pruebas, incluyendo las actualizaciones críticas corporativas:

* ✅ **Actualización V43 (Motor de Reportes y Hardware):** Integración de `Chart.js` para analítica visual, motor asíncrono de correos electrónicos (`@Async`), traducción dinámica de datos en el UI y soporte para impresoras térmicas de 58mm (ZJ-5890U) mediante hacks de CSS (42mm de área segura).
* ✅ **Actualización V44 (Inteligencia de Negocio):** Motor de cálculo financiero que cruza el `precio_venta` y el `precio_compra` para calcular e informar la Ganancia Neta Real (utilidad) en el dashboard y en los reportes automatizados semanales.
* ✅ **Actualización V45 (Cierre de Seguridad y Trazabilidad):** Implementación de la vista `personal.html` para el CRUD completo de empleados con "Borrado Lógico", inyección del "Espía del Carrito" para alertar sobre manipulaciones previo al cobro y finalización del sistema inquebrantable de control de mermas.

---
*Desarrollado con arquitectura sólida para garantizar trazabilidad, prevención de pérdidas y escalabilidad corporativa.*
