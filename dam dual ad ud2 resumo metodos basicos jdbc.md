Objetos JDBC e seus principais métodos, explicando **o que fazem** e **quando usar**.

---

## **1. Connection**

O objeto **`Connection`** representa uma conexão ativa com o banco de dados.
Ele é usado para **abrir**, **fechar** e **gerenciar transações**.

**Principais métodos:**

* **`createStatement()`** → Cria um objeto `Statement` para executar consultas SQL simples.
* **`prepareStatement(String sql)`** → Cria um `PreparedStatement` para consultas parametrizadas (mais seguras e eficientes).
* **`setAutoCommit(boolean)`** → Ativa/desativa commit automático (útil para transações).
* **`commit()`** → Confirma todas as alterações feitas desde o último commit.
* **`rollback()`** → Desfaz alterações feitas desde o último commit.
* **`close()`** → Fecha a conexão, liberando recursos.

---

## **2. Statement**

O **`Statement`** é usado para executar instruções SQL **sem parâmetros**.
Não é recomendado para entradas de usuário (risco de SQL Injection).

**Principais métodos:**

* **`executeQuery(String sql)`** → Executa uma consulta SELECT e retorna um `ResultSet`.
* **`executeUpdate(String sql)`** → Executa INSERT, UPDATE ou DELETE, retornando o número de linhas afetadas.
* **`execute(String sql)`** → Executa qualquer instrução SQL; retorna `true` se houver `ResultSet`.
* **`close()`** → Fecha o `Statement` e libera recursos.

---

## **3. PreparedStatement**

O **`PreparedStatement`** é uma extensão de `Statement` para **consultas parametrizadas** (com `?` como placeholders).
Mais seguro (evita SQL Injection) e pode melhorar desempenho (pré-compilação).

**Principais métodos:**

* **`setString(int parameterIndex, String value)`** → Define um parâmetro `String`.
* **`setInt(int parameterIndex, int value)`** → Define um parâmetro `int`.
* **(e outros como `setDouble`, `setDate`, etc.)** → Define parâmetros de outros tipos.
* **`executeQuery()`** → Executa SELECT e retorna um `ResultSet`.
* **`executeUpdate()`** → Executa INSERT, UPDATE ou DELETE, retornando linhas afetadas.
* **`execute()`** → Executa qualquer SQL (útil para DDL).
* **`close()`** → Fecha o `PreparedStatement`.

---

## **4. ResultSet**

O **`ResultSet`** representa o conjunto de resultados retornado por uma consulta SQL.

**Principais métodos:**

* **`next()`** → Move para a próxima linha. Retorna `false` quando não há mais dados.
* **`getString(String columnLabel)` / `getString(int columnIndex)`** → Obtém valor de uma coluna como `String`.
* **`getInt(...)` / `getDouble(...)`** / etc. → Obtém valores em outros tipos.
* **`beforeFirst()`** → Move o cursor para antes da primeira linha (se o tipo de `ResultSet` permitir).
* **`close()`** → Fecha o `ResultSet`.

---

**Resumo rápido da função de cada um:**

* **Connection** → Abre conexão e gerencia transações.
* **Statement** → Executa SQL simples.
* **PreparedStatement** → Executa SQL com parâmetros.
* **ResultSet** → Lê os resultados de uma consulta.

---

Exemplo prático em Java usando **Connection**, **PreparedStatement**, **ResultSet** e também mostrar onde o **Statement** se encaixaria.

---

## Exemplo: Consulta e Inserção de Usuários com JDBC

```java
import java.sql.*;

public class ExemploJDBC {
    public static void main(String[] args) {
        // Dados de conexão
        String url = "jdbc:mysql://localhost:3306/meu_banco";
        String user = "root";
        String password = "senha";

        try {
            // Abrir conexão
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexão estabelecida!");

            // Usando Statement (consulta simples, sem parâmetros)
            Statement stmt = conn.createStatement();
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS total FROM usuarios");
            if (rs1.next()) {
                System.out.println("Total de usuários: " + rs1.getInt("total"));
            }
            rs1.close();
            stmt.close();

            // Usando PreparedStatement para inserir dados com parâmetros
            String insertSQL = "INSERT INTO usuarios (nome, email) VALUES (?, ?)";
            PreparedStatement pstmtInsert = conn.prepareStatement(insertSQL);
            pstmtInsert.setString(1, "Maria");
            pstmtInsert.setString(2, "maria@email.com");
            int linhasAfetadas = pstmtInsert.executeUpdate();
            System.out.println("Linhas inseridas: " + linhasAfetadas);
            pstmtInsert.close();

            // Usando PreparedStatement para consultar dados
            String selectSQL = "SELECT id, nome, email FROM usuarios WHERE nome LIKE ?";
            PreparedStatement pstmtSelect = conn.prepareStatement(selectSQL);
            pstmtSelect.setString(1, "%Maria%");
            ResultSet rs2 = pstmtSelect.executeQuery();

            while (rs2.next()) {
                int id = rs2.getInt("id");
                String nome = rs2.getString("nome");
                String email = rs2.getString("email");
                System.out.printf("ID: %d | Nome: %s | Email: %s%n", id, nome, email);
            }

            rs2.close();
            pstmtSelect.close();

            // Fechar conexão
            conn.close();
            System.out.println("Conexão fechada.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

### O que está acontecendo:

1. **Connection** → Abre o canal com o banco (`DriverManager.getConnection(...)`).
2. **Statement** → Executa uma consulta simples (contagem de usuários).
3. **PreparedStatement** (inserção) → SQL com `?` para parâmetros seguros.
4. **PreparedStatement** (seleção) → Busca dados com filtro parametrizado.
5. **ResultSet** → Itera sobre os resultados (`while (rs.next()) { ... }`).
6. No final, **tudo é fechado** para evitar vazamento de recursos.

---

 **Diagrama visual** mostrando como `Connection`, `Statement`, `PreparedStatement` e `ResultSet` se relacionam dentro do fluxo JDBC.

Diagrama simples mostrando **o fluxo de uso dos objetos JDBC** e como eles se relacionam:

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
        │ executeUpdate()         │──► retorna nº linhas afetadas
        │ execute()               │──► boolean (tem ResultSet?)
        │ close()                 │
        └──────────┬─────────────┘
                   │
                   ▼
        ┌────────────────────────┐
        │       ResultSet         │
        ├────────────────────────┤
        │ next()                  │  → move cursor
        │ getString(), getInt()   │  → lê valores das colunas
        │ close()                 │
        └────────────────────────┘
```

---

**Fluxo típico**:

1. `Connection` abre ligação com o banco.
2. Criamos `Statement` ou `PreparedStatement`.
3. Executamos SQL → recebemos `ResultSet` (se SELECT).
4. Percorremos `ResultSet` para ler dados.
5. Fechamos `ResultSet`, `Statement` e `Connection` (nessa ordem).

---





