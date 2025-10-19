import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransaccionApp {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/banco";
        String user = "aseixo";
        String password = "123";
        double transferAmount = 200.00;
        int fromAccountId = 1;
        int toAccountId = 2;

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
            
            // Inabilitar auto-commit para comezar a transacción
            connection.setAutoCommit(false);
            
            // Paso 1: Consultar o saldo do conta orixe; pagadora, debe ter fondos!!
            String balanceQuery = "SELECT balance FROM accounts WHERE id = ?";
            try (PreparedStatement balanceStmt = connection.prepareStatement(balanceQuery)) {
                balanceStmt.setInt(1, fromAccountId);
                ResultSet rs = balanceStmt.executeQuery();
                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");
                    if (currentBalance < transferAmount) {
                        throw new SQLException("Fondos insuficientes na conta " + fromAccountId);
                    }
                } else {
                    throw new SQLException("Conta c/c " + fromAccountId + " non encontrada");
                }
            }
            
            // Paso 2: Se hai suficientes fondos, proceder coa deducción a a adición
            String deductSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            String addSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            
            try (PreparedStatement deductStmt = connection.prepareStatement(deductSql);
                 PreparedStatement addStmt = connection.prepareStatement(addSql)) {
                
                // Deducir da conta de orixe
                deductStmt.setDouble(1, transferAmount);
                deductStmt.setInt(2, fromAccountId);
                deductStmt.executeUpdate();
                
                // Adicionar a conta destino ou obxectivo
                addStmt.setDouble(1, transferAmount);
                addStmt.setInt(2, toAccountId);
                addStmt.executeUpdate();
                
                // Realizar Commit se todo foi correcto 
                connection.commit();
                System.out.println("Transferencia realizada con éxito!");
            }
        } catch (SQLException e) {
            // Rollback on any error (e.g., insufficient funds or DB issue)
            try {
                if (connection != null) {
                    connection.rollback();
                    System.err.println("Transferencia fallou: " + e.getMessage() + ". Desfixéronse todas as operacións previas (rollback)");
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // Clean up
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }
}
