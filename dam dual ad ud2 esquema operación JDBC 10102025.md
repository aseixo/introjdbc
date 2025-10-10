
 **Diagrama visual** mostrando como `Connection`, `Statement`, `PreparedStatement` e `ResultSet` se relacionan dentro do fluxo JDBC.

Diagrama simples mostrando **o fluxo de uso dos obxectos JDBC** e como estes se relacionan:

---

```
        ┌────────────────────────┐
        │   DriverManager         │
        │ getConnection(url,...)  │
        └──────────┬─────────────┘
                   │  (cria)
                   ▼
        ┌────────────────────────┐
        │     Connection          │
        ├────────────────────────┤
        │ createStatement()       │──► Statement ──► executeQuery(sql) → ResultSet
        │ prepareStatement(sql)   │──► PreparedStatement ──► executeQuery()/executeUpdate()
        │ setAutoCommit(false)    │
        │ commit()/rollback()     │
        │ close()                 │
        └──────────┬─────────────┘
                   │
                   ▼
        ┌────────────────────────┐
        │  Statement /            │
        │  PreparedStatement      │
        ├────────────────────────┤
        │ executeQuery()          │──► retorna ResultSet
        │ executeUpdate()         │──► retorna nº liñas afectadas
        │ execute()               │──► boolean (tem ResultSet?)
        │ close()                 │
        └──────────┬─────────────┘
                   │
                   ▼
        ┌────────────────────────┐
        │       ResultSet         │
        ├────────────────────────┤
        │ next()                  │  → move cursor
        │ getString(), getInt()   │  → le valores das colunas
        │ close()                 │
        └────────────────────────┘
```

---

**Fluxo típico**:

1. `Connection` abre ligazón co banco de datos.
2. Creamos `Statement` ou `PreparedStatement`.
3. Executamos SQL → recebemos `ResultSet` (se SELECT).
4. Percorremos `ResultSet` para ler os datos.
5. Fechamos `ResultSet`, `Statement` e `Connection` (nesa ordem).

---

