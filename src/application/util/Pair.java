package application.util;

/**
 * Created by Admin on 17.07.2017.
 */
public class Pair<F, S> {
    private F first;
    private S second;
    public Pair(F f, S s){
        first = f;
        second = s;
    }

    public S getValue(){
        return second;
    }
    public F getKey(){
        return first;
    }

    public static <A, B> Pair <A, B> create(A a, B b) {
        return new Pair<A, B>(a, b);
    }
}
