package smart.budget.expense.exceptions;

public class ZeroBalanceDifferenceException extends Exception {
    public ZeroBalanceDifferenceException(String text) {
        super(text);
    }
}
