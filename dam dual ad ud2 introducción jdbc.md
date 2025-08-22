Exemplo **simples e funcional** usando **JDBC + Maven** para realizar operações CRUD em um banco **H2**


* `pom.xml` com dependência do **H2 Database**
* Uma classe `Main` que:

  * Cria a tabela
  * Insere dados
  * Lista dados
  * Atualiza registros
  * Deleta registros

---

### `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.exemplo</groupId>
    <artifactId>jdbc-h2-crud</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <!-- Dependência do H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
    </dependencies>

</project>
```

---

### `Main.java`

```java
package com.exemplo;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:h2:~/testdb"; // Arquivo no diretório home
        String user = "sa";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conectado ao H2!");

            criarTabela(conn);
            inserirDados(conn, "João", 30);
            inserirDados(conn, "Maria", 25);
            listarDados(conn);
            atualizarIdade(conn, 1, 35);
            listarDados(conn);
            deletarPessoa(conn, 2);
            listarDados(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void criarTabela(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS pessoa (" +
                     "id IDENTITY PRIMARY KEY," +
                     "nome VARCHAR(255)," +
                     "idade INT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela criada!");
        }
    }

    static void inserirDados(Connection conn, String nome, int idade) throws SQLException {
        String sql = "INSERT INTO pessoa (nome, idade) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setInt(2, idade);
            pstmt.executeUpdate();
            System.out.println("Inserido: " + nome);
        }
    }

    static void listarDados(Connection conn) throws SQLException {
        String sql = "SELECT * FROM pessoa";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("---- Lista de Pessoas ----");
            while (rs.next()) {
                System.out.printf("%d - %s (%d anos)%n",
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getInt("idade"));
            }
        }
    }

    static void atualizarIdade(Connection conn, long id, int novaIdade) throws SQLException {
        String sql = "UPDATE pessoa SET idade = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, novaIdade);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
            System.out.println("Idade atualizada para ID " + id);
        }
    }

    static void deletarPessoa(Connection conn, long id) throws SQLException {
        String sql = "DELETE FROM pessoa WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            System.out.println("Pessoa com ID " + id + " deletada");
        }
    }
}
```

---

### Executar

No terminal, dentro do diretório do projeto:

```bash
mvn compile exec:java -Dexec.mainClass="com.exemplo.Main"
```

---

Esse exemplo:

* Usa o **H2 em modo arquivo** (`~/testdb`)
* Cria a tabela se não existir
* Insere dois registros
* Lista todos
* Atualiza um registro
* Deleta outro

Se quiser usar o **H2 em memória** (tabela desaparece ao fechar conexão), basta trocar:

```java
String url = "jdbc:h2:mem:testdb";
```

---

### ** Versão com repositório DAO **

Exemplo para usar um **DAO** (Data Access Object), **o `Main` fica limpo e toda a lógica de acesso ao banco de dados fica isolada**.

---

## ** pom.xml** (mesmo do exemplo anterior)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.exemplo</groupId>
    <artifactId>jdbc-h2-crud-dao</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.exemplo.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## ** Classe modelo: Pessoa.java**

```java
package com.exemplo;

public class Pessoa {
    private Long id;
    private String nome;
    private int idade;

    public Pessoa(Long id, String nome, int idade) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
    }

    public Pessoa(String nome, int idade) {
        this(null, nome, idade);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getIdade() {
        return idade;
    }

    @Override
    public String toString() {
        return id + " - " + nome + " (" + idade + " anos)";
    }
}
```

---

## ** DAO: PessoaDAO.java**

```java
package com.exemplo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PessoaDAO {
    private final Connection conn;

    public PessoaDAO(Connection conn) {
        this.conn = conn;
    }

    public void criarTabela() {
        String sql = """
            CREATE TABLE IF NOT EXISTS pessoa (
                id IDENTITY PRIMARY KEY,
                nome VARCHAR(255),
                idade INT
            )
            """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela", e);
        }
    }

    public void inserir(Pessoa p) {
        String sql = "INSERT INTO pessoa (nome, idade) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNome());
            pstmt.setInt(2, p.getIdade());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pessoa", e);
        }
    }

    public List<Pessoa> listar() {
        String sql = "SELECT * FROM pessoa";
        List<Pessoa> lista = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Pessoa(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getInt("idade")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pessoas", e);
        }
        return lista;
    }

    public void atualizarIdade(long id, int novaIdade) {
        String sql = "UPDATE pessoa SET idade = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, novaIdade);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar idade", e);
        }
    }

    public void deletar(long id) {
        String sql = "DELETE FROM pessoa WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar pessoa", e);
        }
    }
}
```

---

## ** Main.java**

```java
package com.exemplo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // Banco em memória
        String user = "sa";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PessoaDAO dao = new PessoaDAO(conn);

            dao.criarTabela();

            dao.inserir(new Pessoa("João", 30));
            dao.inserir(new Pessoa("Maria", 25));

            System.out.println("Lista inicial:");
            dao.listar().forEach(System.out::println);

            dao.atualizarIdade(1, 35);
            System.out.println("\nApós atualização:");
            dao.listar().forEach(System.out::println);

            dao.deletar(2);
            System.out.println("\nApós exclusão:");
            dao.listar().forEach(System.out::println);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco", e);
        }
    }
}
```

---

## **O que melhorou com o DAO**

* O `Main` agora **não sabe nada sobre SQL**.
* Toda a lógica de acesso a dados (`CREATE TABLE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE`) está no `PessoaDAO`.
* Se amanhã mudar de H2 para PostgreSQL, só mexemos no DAO.
* `throw new RuntimeException(e)` mantém o stack trace e simplifica o código.

---




