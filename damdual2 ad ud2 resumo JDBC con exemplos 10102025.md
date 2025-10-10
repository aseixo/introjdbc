## Información básica de JDBC con MySQL

### 1. Configuración inicial

### Dependencias necesarias
```xml
<!-- Se usas Maven -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

### 2. Exemplo básico

```java
import java.sql.*;

public class ExemploJDBC {
    
    // Datos de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/basedatos";
    private static final String USUARIO = "usuario";
    private static final String CONTRASINAL = "contrasinal";
    
    public static void main(String[] args) {
        Connection conexion = null;
        Statement declaracion = null;
        ResultSet resultado = null;
        
        try {
            // 1. Establecer conexión
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASINAL);
            System.out.println("Conexión establecida!");
            
            // 2. Crear declaración
            declaracion = conexion.createStatement();
            
            // 3. Executar consulta
            String sql = "SELECT id, nome, email FROM usuarios";
            resultado = declaracion.executeQuery(sql);
            
            // 4. Procesar resultados
            while (resultado.next()) {
                int id = resultado.getInt("id");
                String nome = resultado.getString("nome");
                String email = resultado.getString("email");
                
                System.out.println("ID: " + id + ", Nome: " + nome + ", Email: " + email);
            }
            
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        } finally {
            // 5. Pechar recursos
            try {
                if (resultado != null) resultado.close();
                if (declaracion != null) declaracion.close();
                if (conexion != null) conexion.close();
            } catch (SQLException e) {
                System.out.println("Erro pechando recursos: " + e.getMessage());
            }
        }
    }
}
```

### 3. Exemplo con inserción de datos

```java
public class InsercionDatos {
    
    public static void inserirUsuario(String nome, String email) {
        String sql = "INSERT INTO usuarios (nome, email) VALUES (?, ?)";
        
        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, CONTRASINAL);
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            
            // Establecer parámetros
            pstmt.setString(1, nome);
            pstmt.setString(2, email);
            
            // Executar inserción
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println(filasAfectadas + " fila(s) inserida(s)");
            
        } catch (SQLException e) {
            System.out.println("Erro na inserción: " + e.getMessage());
        }
    }
}
```

### 4. Exemplo con transaccións

```java
public class TransaccionExemplo {
    
    public static void transferencia(int idOrigen, int idDestino, double cantidade) {
        Connection conexion = null;
        
        try {
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASINAL);
            
            // Desactivar auto-commit para control manual
            conexion.setAutoCommit(false);
            
            // Restar cantidade da conta orixe
            String sqlRestar = "UPDATE contas SET saldo = saldo - ? WHERE id = ?";
            PreparedStatement pstmt1 = conexion.prepareStatement(sqlRestar);
            pstmt1.setDouble(1, cantidade);
            pstmt1.setInt(2, idOrigen);
            pstmt1.executeUpdate();
            
            // Sumar cantidade á conta destino
            String sqlSumar = "UPDATE contas SET saldo = saldo + ? WHERE id = ?";
            PreparedStatement pstmt2 = conexion.prepareStatement(sqlSumar);
            pstmt2.setDouble(1, cantidade);
            pstmt2.setInt(2, idDestino);
            pstmt2.executeUpdate();
            
            // Confirmar transacción
            conexion.commit();
            System.out.println("Transferencia realizada correctamente");
            
        } catch (SQLException e) {
            try {
                if (conexion != null) {
                    conexion.rollback(); // Reverter en caso de erro
                }
                System.out.println("Erro na transacción: " + e.getMessage());
            } catch (SQLException ex) {
                System.out.println("Erro facendo rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conexion != null) conexion.close();
            } catch (SQLException e) {
                System.out.println("Erro pechando conexión: " + e.getMessage());
            }
        }
    }
}
```

### 5. Versión moderna con try-with-resources

```java
public class JDBCModerno {
    
    public static void consultaModerno() {
        String sql = "SELECT * FROM produtos WHERE prezo > ?";
        
        // try-with-resources pecha automaticamente os recursos
        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, CONTRASINAL);
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            
            pstmt.setDouble(1, 50.0);
            
            try (ResultSet resultado = pstmt.executeQuery()) {
                while (resultado.next()) {
                    System.out.println("Produto: " + resultado.getString("nome") + 
                                     ", Prezo: " + resultado.getDouble("prezo"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
```

### Puntos chave

- **Connection**: Representa a conexión co banco de datos
- **Statement/PreparedStatement**: Para executar consultas SQL
- **ResultSet**: Contén os resultados das consultas SELECT
- **Sempre fechar os recursos** no bloque finally ou **usar try-with-resources** que é sempre a mellor opción
- **PreparedStatement é máis seguro (evita SQL injection) e eficiente**
- **Transaccións** permiten executar múltiples operacións atomicamente

