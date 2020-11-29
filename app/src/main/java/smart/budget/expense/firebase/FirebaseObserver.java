package smart.budget.expense.firebase;

public interface FirebaseObserver<T> {
    void onChanged(T t);
}
