package com.paytracker;

public class Format {
    public static String money(double v) {
        String sign = v < 0 ? "-" : "";
        v = Math.abs(v);
        if (v >= 1e12) return sign + "$" + trim(v / 1e12) + "T";
        if (v >= 1e9)  return sign + "$" + trim(v / 1e9) + "B";
        if (v >= 1e6)  return sign + "$" + trim(v / 1e6) + "M";
        if (v >= 1e3)  return sign + "$" + trim(v / 1e3) + "K";
        return sign + "$" + trim(v);
    }

    private static String trim(double v) {
        String s = String.format("%.2f", v);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    public static String time(long ms) {
        long sec = ms / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
}
