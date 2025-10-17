
public class TransaccionBancaria {
    private static final String URL = "jdbc:mysql://localhost:3306/banco";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public boolean transferirFondos(int cuentaOrigen, int cuentaDestino, BigDecimal monto) {
        String sqlRetirar = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ? AND saldo >= ?";
        String sqlDepositar = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";
        String sqlLog = "INSERT INTO transferencias (cuenta_origen, cuenta_destino, monto, fecha) VALUES (?, ?, ?, NOW())";
        
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // 1. Desactivar auto-commit
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtRetirar = conn.prepareStatement(sqlRetirar);
                 PreparedStatement pstmtDepositar = conn.prepareStatement(sqlDepositar);
                 PreparedStatement pstmtLog = conn.prepareStatement(sqlLog, Statement.RETURN_GENERATED_KEYS)) {
                
                // 2. Retirar de conta orixe
                pstmtRetirar.setBigDecimal(1, monto);
                pstmtRetirar.setInt(2, cuentaOrigen);
                pstmtRetirar.setBigDecimal(3, monto);
                
                int filasAfectadas = pstmtRetirar.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new SQLException("Fondos insuficientes ou a conta non existe");
                }
                
                // 3. Depositar na conta destino
                pstmtDepositar.setBigDecimal(1, monto);
                pstmtDepositar.setInt(2, cuentaDestino);
                pstmtDepositar.executeUpdate();
                
                // 4. Rexistrar no log
                pstmtLog.setInt(1, cuentaOrigen);
                pstmtLog.setInt(2, cuentaDestino);
                pstmtLog.setBigDecimal(3, monto);
                pstmtLog.executeUpdate();
                
                // 5. Si todo ben, confirmar transacción
                conn.commit();
                System.out.println("Transferencia realizada: " + monto + " de " + cuentaOrigen + " a " + cuentaDestino);
                return true;
                
            } catch (SQLException e) {
                // 6. Si algo falla, reverter todo
                conn.rollback();
                System.out.println("Transferencia fallida: " + e.getMessage());
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar auto-commit
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


// --------------------------------------------------------------


// 1. Desactivar auto-commit
conn.setAutoCommit(false);
//Comprobación do resultado da primeira operación, neste caso update, para anular ou non
int filasAfectadas = pstmtRetirar.executeUpdate();
if (filasAfectadas == 0) {
  throw new SQLException("Fondos insuficientes ou a conta non existe");
}

 // 5. Si todo ben, confirmar transacción
conn.commit();
// En caso negativo, lanzamos antes unha excepción que capturamos no catch
catch (SQLException e) {
    // 6. Si algo falla, reverter todo
    conn.rollback(); 
    return false;
}



