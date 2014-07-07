/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.util;

/**
 *
 * @author frankpeeters
 * @param <T>
 */
public class DataSet<T extends Number> {

    private final T[] data;
    private Double sum;

    public DataSet(T[] data) {
        this.data = data;
        sum = null;
    }

    public int size() {
        return data.length;
    }

    public double average() {
        if (data.length == 0) {
            throw new ArithmeticException("empty data collection");
        }
        sum();
        return sum / data.length;
    }

    public double sum() {
        if (sum == null) {
            sum = 0.0;
            for (T t : data) {
                sum += t.doubleValue();
            }

        }
        return sum;
    }

    double sumQuadrats() {
        double sumQuadrats = 0;
        for (T t : data) {
            sumQuadrats += t.doubleValue() * t.doubleValue();
        }

        return sumQuadrats;
    }

    public double standardDeviation() {
        if (data.length < 1) {
            throw new ArithmeticException("data collection too small");
        }
        double av = average();
        return Math.sqrt((sumQuadrats() - size() * av * av) / (size() - 1));
    }

}
