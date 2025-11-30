# Verificación y Corrección de Invariante de Dominio: Tweets y Usuarios

## 📋 Invariante de Dominio

> **"Los tweets de un usuario deben eliminarse cuando el usuario es eliminado. No pueden existir tweets sin usuario activo."**

---

## 🔍 1. DIAGNÓSTICO DEL BACKEND

### ❌ **Problema Identificado: INVARIANTE ROTA**

#### Estado Previo a la Corrección

**Modelo de Dominio:**

- ✅ `Tweet` tiene autor obligatorio (`@ManyToOne(optional = false)`)
- ✅ `Tweet` y `Usuario` usan **soft-delete** (campos `eliminado` y `activo`)
- ❌ `Usuario.desactivar()` solo cambia `activo = false` sin eliminar tweets
- ❌ Queries JPA **NO filtraban `eliminado = false`**

**Código Problemático (Usuario.java):**

```java
public void desactivar() {
    this.activo = false;
    // ❌ NO elimina los tweets del usuario
}
```

**Queries Sin Filtro (JpaTweetsSpringData.java):**

```java
// ❌ Retornaba tweets eliminados
List<Tweet> findByAutorOrderByFechaCreacionDesc(Usuario autor);

@Query("SELECT t FROM Tweet t WHERE t.autor IN :autores ORDER BY t.fechaCreacion DESC")
List<Tweet> findByAutorInOrderByFechaCreacionDesc(@Param("autores") List<Usuario> autores, Pageable pageable);
```

#### Consecuencias del Bug

1. **Tweets huérfanos:** Al desactivar usuario, sus tweets quedan visibles
2. **Violación de invariante:** Existen tweets de usuarios inactivos
3. **Inconsistencia de datos:** Timeline muestra tweets de usuarios desactivados
4. **Lógica de negocio rota:** No se respeta el modelo de dominio

---

## ✅ 2. SOLUCIÓN IMPLEMENTADA (Arquitectura DDD Correcta)

### Principios Aplicados

1. ⚠️ **NO inyectar repositorio en entidad** (anti-patrón)
2. ✅ **Orquestar desde Servicio de Aplicación**
3. ✅ **Operación atómica con `@Transactional`**
4. ✅ **Mantener soft-delete** (no cascades físicos)
5. ✅ **Filtrar en todas las queries**

### Modificaciones Realizadas

#### A) **Interfaz RepositorioTweets** (Agregar método)

```java
/**
 * Marca como eliminados todos los tweets de un usuario.
 * Mantiene la invariante de dominio: tweets no pueden existir sin usuario activo.
 *
 * @param idUsuario ID del usuario cuyos tweets deben marcarse como eliminados
 */
void marcarTweetsComoEliminadosDe(Long idUsuario);
```

#### B) **Implementación RepositorioTweetsJpa**

```java
@Override
public void marcarTweetsComoEliminadosDe(Long idUsuario) {
    jpa.marcarTweetsComoEliminadosDe(idUsuario);
}
```

#### C) **Query JPA con @Modifying**

```java
/**
 * Marca como eliminados todos los tweets de un usuario.
 * Operación transaccional que mantiene la invariante de dominio.
 *
 * IMPORTANTE:
 * - clearAutomatically=true: Limpia cache L1 después de ejecutar
 * - flushAutomatically=true: Sincroniza antes de ejecutar UPDATE
 */
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE Tweet t SET t.eliminado = true WHERE t.autor.id = :idUsuario")
void marcarTweetsComoEliminadosDe(@Param("idUsuario") Long idUsuario);
```

#### D) **Filtrado en Queries (Prevenir tweets eliminados en resultados)**

```java
/**
 * Busca tweets de un usuario que NO estén eliminados.
 * Mantiene invariante: solo mostrar tweets activos.
 */
@Query("SELECT t FROM Tweet t WHERE t.autor = :autor AND t.eliminado = false ORDER BY t.fechaCreacion DESC")
List<Tweet> findByAutorOrderByFechaCreacionDesc(@Param("autor") Usuario autor);

/**
 * Busca tweets de lista de autores que NO estén eliminados.
 * Mantiene invariante: solo mostrar tweets activos.
 */
@Query("SELECT t FROM Tweet t WHERE t.autor IN :autores AND t.eliminado = false ORDER BY t.fechaCreacion DESC")
List<Tweet> findByAutorInOrderByFechaCreacionDesc(@Param("autores") List<Usuario> autores, Pageable pageable);
```

#### E) **Servicio de Aplicación (Orquestación Transaccional)**

**Interfaz ServicioUsuarios:**

```java
/**
 * Desactiva un usuario y marca todos sus tweets como eliminados.
 * Mantiene la invariante de dominio:
 * "Los tweets de un usuario deben eliminarse cuando el usuario es eliminado."
 *
 * Operación transaccional y atómica.
 *
 * @param keycloakId ID de Keycloak del usuario a desactivar
 */
void desactivarUsuario(String keycloakId);
```

**Implementación ServicioUsuariosAplicacion:**

```java
/**
 * Desactiva un usuario y marca todos sus tweets como eliminados.
 *
 * Esta operación mantiene la invariante de dominio:
 * "Los tweets de un usuario deben eliminarse cuando el usuario es eliminado."
 *
 * Arquitectura DDD:
 * - Usuario.desactivar() maneja el estado del agregado Usuario
 * - El servicio de aplicación orquesta la eliminación de tweets
 * - NO se inyecta repositorio en la entidad (anti-patrón)
 *
 * Garantías:
 * - Operación atómica (transaccional)
 * - Si falla eliminación de tweets, rollback completo
 * - No quedan tweets huérfanos de usuarios inactivos
 */
@Override
@Transactional
public void desactivarUsuario(String keycloakId) {
    Usuario usuario = repositorioUsuarios.buscarPorKeycloakId(keycloakId);

    // 1. Desactivar usuario (cambio de estado en el agregado)
    usuario.desactivar();
    repositorioUsuarios.guardar(usuario);

    // 2. Marcar todos sus tweets como eliminados (orquestación desde servicio)
    // Esto mantiene la invariante: no pueden existir tweets de usuarios inactivos
    repositorioTweets.marcarTweetsComoEliminadosDe(usuario.id());
}
```

---

## 🧪 3. VERIFICACIÓN DEL FRONTEND

### Estado del Frontend

✅ **El frontend YA maneja correctamente tweets eliminados**

**TweetCard.tsx:**

```tsx
<article className={`${styles.card} ${tweet.eliminado ? styles.deleted : ""}`}>
  {tweet.eliminado && <span className={styles.deletedBadge}>Eliminado</span>}
</article>
```

**RespuestaItem.tsx:**

```tsx
<div
  className={`${styles.respuestaItem} ${
    respuesta.eliminado ? styles.deleted : ""
  }`}
>
  {respuesta.eliminado && (
    <span className={styles.deletedBadge}>Eliminado</span>
  )}
</div>
```

**Tipos (types.ts):**

```typescript
export interface Tweet {
  id: number;
  autor: string;
  contenido: string;
  fechaCreacion: string;
  eliminado: boolean; // ✅ Ya está tipado
  // ...
}
```

### Conclusión Frontend

- ✅ No requiere modificaciones
- ✅ Maneja tweets eliminados con estilos visuales
- ✅ Filtrado correcto en HomePage (solo tweets no retweet)
- ✅ No hay rutas que "saltee" validación

---

## ✅ 4. TEST DE INTEGRACIÓN

**Archivo:** `ServicioUsuariosIT.java`

### Test Principal: Verificar Invariante

```java
@Test
void desactivarUsuario_debeEliminarTodosLosTweetsDelUsuario() {
    // ARRANGE: Crear usuario con varios tweets
    Usuario usuario = new Usuario(/* ... */);
    usuario = repositorioUsuarios.guardar(usuario);

    Tweet tweet1 = usuario.publicarTweet("Tweet 1 del usuario");
    Tweet tweet2 = usuario.publicarTweet("Tweet 2 del usuario");
    Tweet tweet3 = usuario.publicarTweet("Tweet 3 del usuario");

    tweet1 = repositorioTweets.guardar(tweet1);
    tweet2 = repositorioTweets.guardar(tweet2);
    tweet3 = repositorioTweets.guardar(tweet3);

    // Verificar estado inicial: tweets activos
    assertFalse(tweet1.estaEliminado());
    assertTrue(usuario.estaActivo());

    // ACT: Desactivar usuario
    servicioUsuarios.desactivarUsuario(usuario.keycloakId());

    // ASSERT: Verificar que usuario está inactivo
    Usuario usuarioDesactivado = repositorioUsuarios.buscarPorKeycloakId(usuario.keycloakId());
    assertFalse(usuarioDesactivado.estaActivo());

    // ASSERT: Verificar que tweets están marcados como eliminados
    Tweet tweet1Actualizado = repositorioTweets.buscarPorId(tweet1.id());
    assertTrue(tweet1Actualizado.estaEliminado());

    // ASSERT: Queries NO retornan tweets del usuario inactivo
    List<Tweet> tweetsVisibles = repositorioTweets.tweetsDeUsuario(usuarioDesactivado);
    assertTrue(tweetsVisibles.isEmpty()); // ✅ INVARIANTE MANTENIDA
}
```

### Resultados

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

**Suite Completa:**

```
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📊 5. RESUMEN DE CAMBIOS

### Archivos Modificados

| Archivo                           | Cambio                                                   |
| --------------------------------- | -------------------------------------------------------- |
| `RepositorioTweets.java`          | ➕ Método `marcarTweetsComoEliminadosDe(Long idUsuario)` |
| `RepositorioTweetsJpa.java`       | ➕ Implementación del método                             |
| `JpaTweetsSpringData.java`        | ➕ Query `@Modifying` + Filtros `eliminado = false`      |
| `ServicioUsuarios.java`           | ➕ Método `desactivarUsuario(String keycloakId)`         |
| `ServicioUsuariosAplicacion.java` | ➕ Implementación transaccional                          |
| `ServicioUsuariosIT.java`         | ➕ Tests de integración (2 tests)                        |

### Tests Actualizados (Obsoletos por Phase 15)

| Test                                  | Motivo                                | Acción                                      |
| ------------------------------------- | ------------------------------------- | ------------------------------------------- |
| `ReTweetTest`                         | Usernames de 1 carácter ("a", "r")    | ✅ Cambiado a "autor_tweet", "retweeter1"   |
| `RespuestaTweetTest`                  | Usernames cortos ("au", "resp")       | ✅ Cambiado a "autor_tweet1", "autor_resp1" |
| `TweetTest`                           | Username de 1 carácter ("a")          | ✅ Cambiado a "autor_test"                  |
| `ServicioTweetsAplicacionRetweetTest` | Test esperaba permitir retweet propio | ✅ Cambiado a verificar excepción           |

---

## 🎯 6. EXPLICACIÓN FINAL

### ¿Por Qué Esta Solución Mantiene la Invariante?

1. **Operación Atómica:**

   - `@Transactional` garantiza que desactivación + eliminación de tweets es atómica
   - Si falla eliminación de tweets → rollback completo

2. **Arquitectura DDD Correcta:**

   - `Usuario.desactivar()` solo cambia estado del agregado
   - **Servicio de aplicación** orquesta la eliminación de tweets
   - NO se inyecta repositorio en entidad (respeta DDD)

3. **Soft-Delete Consistente:**

   - No se usan cascades físicos de JPA
   - Se mantiene patrón existente: `eliminado = true`
   - Preserva datos para auditoría

4. **Filtrado en Todas las Queries:**

   - `eliminado = false` en TODAS las queries de tweets
   - Timeline nunca muestra tweets eliminados
   - Frontend recibe datos ya filtrados

5. **Tests Verifican Invariante:**
   - Test de integración confirma: desactivar usuario → elimina tweets
   - No quedan tweets visibles de usuarios inactivos

### ¿Por Qué NO Se Hace en la Entidad Usuario?

**❌ Anti-patrón (NO hacer):**

```java
// INCORRECTO: inyectar repositorio en entidad
public class Usuario {
    private RepositorioTweets repositorioTweets; // ❌ MALO

    public void desactivar() {
        this.activo = false;
        repositorioTweets.marcarTweetsComoEliminadosDe(this.id); // ❌ MALO
    }
}
```

**Razones:**

1. **Viola separación de responsabilidades:** Entidades no deben tener lógica de persistencia
2. **Rompe DDD:** Agregados no deben conocer repositorios
3. **Dificulta testing:** Entidades con dependencias externas son difíciles de testear
4. **Acoplamiento alto:** Entidad acoplada a infraestructura

**✅ Patrón correcto (implementado):**

- Entidad: Solo lógica de dominio pura
- Servicio de Aplicación: Orquestación transaccional
- Repositorio: Operaciones de persistencia

---

## 🔒 7. GARANTÍAS FINALES

### ✅ Invariante Verificada

- [x] Usuario desactivado → tweets marcados como eliminados
- [x] Queries filtran `eliminado = false`
- [x] Frontend maneja tweets eliminados
- [x] Operación transaccional (atómica)
- [x] No quedan tweets huérfanos
- [x] Tests passing (30/30)
- [x] Arquitectura DDD respetada
- [x] Soft-delete mantenido

### 🎉 Conclusión

La invariante de dominio **"Los tweets de un usuario deben eliminarse cuando el usuario es eliminado"** está ahora **correctamente implementada y verificada** mediante:

1. Servicio transaccional que orquesta la operación
2. Query masiva que marca tweets como eliminados
3. Filtros en todas las consultas
4. Tests de integración que verifican el comportamiento
5. Arquitectura DDD correcta (sin anti-patrones)

**Estado Final: ✅ INVARIANTE MANTENIDA**

---

**Fecha de implementación:** 30 de noviembre de 2025  
**Autor:** GitHub Copilot (Claude Sonnet 4.5)  
**Branch:** `usuario-puede-crear-tweets`
