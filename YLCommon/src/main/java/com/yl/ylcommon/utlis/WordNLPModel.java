package com.yl.ylcommon.utlis;

import java.util.ArrayList;
import java.util.List;

public class WordNLPModel {

    private List<String> n = new ArrayList<>(); // 普通名词
    private List<String> nr = new ArrayList<>(); // 人名
    private List<String> ns = new ArrayList<>(); // 地名
    private List<String> nt = new ArrayList<>(); // 机构名
    private List<String> nz = new ArrayList<>(); // 其他专名
    private List<String> nl = new ArrayList<>(); // 名词性惯用语
    private List<String> ng = new ArrayList<>(); // 名词性语素
    private List<String> t = new ArrayList<>(); // 时间词
    private List<String> v = new ArrayList<>(); // 动词
    private List<String> vd = new ArrayList<>(); // 副动词
    private List<String> vn = new ArrayList<>(); // 名动词
    private List<String> vshi = new ArrayList<>(); // 动词"是"
    private List<String> vyou = new ArrayList<>(); // 动词"有"
    private List<String> a = new ArrayList<>(); // 形容词
    private List<String> ad = new ArrayList<>(); // 副动词
    private List<String> d = new ArrayList<>(); // 副词
    private List<String> r = new ArrayList<>(); // 从词
    private List<String> rr = new ArrayList<>(); // 人称代词
    private List<String> rz = new ArrayList<>(); // 指示代词
    private List<String> rzt = new ArrayList<>(); // 时间指示代词
    private List<String> c = new ArrayList<>(); // 连词
    private List<String> u = new ArrayList<>(); // 助词
    private List<String> m = new ArrayList<>(); // 数词
    private List<String> q = new ArrayList<>(); // 量词
    private List<String> y = new ArrayList<>(); // 语气词
    private List<String> e = new ArrayList<>(); // 弹词
    private List<String> o = new ArrayList<>(); // 拟声词
    private List<String> f = new ArrayList<>(); // 方位词
    private List<String> z = new ArrayList<>(); // 状态词
    private List<String> p = new ArrayList<>(); // 介词
    private List<String> h = new ArrayList<>(); // 前缀
    private List<String> k = new ArrayList<>(); // 后缀
    private List<String> w = new ArrayList<>(); // 标点符号
    private List<String> other = new ArrayList<>(); // 其他

    public List<String> getN() {
        return n;
    }

    public void addN(String n) {
        this.n.add(n);
    }

    public List<String> getNr() {
        return nr;
    }

    public void addNr(String nr) {
        this.nr.add(nr);
    }

    public List<String> getNs() {
        return ns;
    }

    public void addNs(String ns) {
        this.ns.add(ns);
    }

    public List<String> getNt() {
        return nt;
    }

    public void addNt(String nt) {
        this.nt.add(nt);
    }

    public List<String> getNz() {
        return nz;
    }

    public void addNz(String nz) {
        this.nz.add(nz);
    }

    public List<String> getNl() {
        return nl;
    }

    public void addNl(String nl) {
        this.nl.add(nl);
    }

    public List<String> getNg() {
        return ng;
    }

    public void addNg(String ng) {
        this.ng.add(ng);
    }

    public List<String> getT() {
        return t;
    }

    public void addT(String t) {
        this.t.add(t);
    }

    public List<String> getV() {
        return v;
    }

    public void addV(String v) {
        this.v.add(v);
    }

    public List<String> getVd() {
        return vd;
    }

    public void addVd(String vd) {
        this.vd.add(vd);
    }

    public List<String> getVn() {
        return vn;
    }

    public void addVn(String vn) {
        this.vn.add(vn);
    }

    public List<String> getVshi() {
        return vshi;
    }

    public void addVshi(String vshi) {
        this.vshi.add(vshi);
    }

    public List<String> getVyou() {
        return vyou;
    }

    public void addVyou(String vyou) {
        this.vyou.add(vyou);
    }

    public List<String> getA() {
        return a;
    }

    public void addA(String a) {
        this.a.add(a);
    }

    public List<String> getAd() {
        return ad;
    }

    public void addAd(String ad) {
        this.ad.add(ad);
    }

    public List<String> getD() {
        return d;
    }

    public void addD(String d) {
        this.d.add(d);
    }

    public List<String> getR() {
        return r;
    }

    public void addR(String r) {
        this.r.add(r);
    }

    public List<String> getRr() {
        return rr;
    }

    public void addRr(String rr) {
        this.rr.add(rr);
    }

    public List<String> getRz() {
        return rz;
    }

    public void addRz(String rz) {
        this.rz.add(rz);
    }

    public List<String> getRzt() {
        return rzt;
    }

    public void addRzt(String rzt) {
        this.rzt.add(rzt);
    }

    public List<String> getC() {
        return c;
    }

    public void addC(String c) {
        this.c.add(c);
    }

    public List<String> getU() {
        return u;
    }

    public void addU(String u) {
        this.u.add(u);
    }

    public List<String> getM() {
        return m;
    }

    public void addM(String m) {
        this.m.add(m);
    }

    public List<String> getQ() {
        return q;
    }

    public void addQ(String q) {
        this.q.add(q);
    }

    public List<String> getY() {
        return y;
    }

    public void addY(String y) {
        this.y.add(y);
    }

    public List<String> getE() {
        return e;
    }

    public void addE(String e) {
        this.e.add(e);
    }

    public List<String> getO() {
        return o;
    }

    public void addO(String o) {
        this.o.add(o);
    }

    public List<String> getF() {
        return f;
    }

    public void addF(String f) {
        this.f.add(f);
    }

    public List<String> getZ() {
        return z;
    }

    public void addZ(String z) {
        this.z.add(z);
    }

    public List<String> getP() {
        return p;
    }

    public void addP(String p) {
        this.p.add(p);
    }

    public List<String> getH() {
        return h;
    }

    public void addH(String h) {
        this.h.add(h);
    }

    public List<String> getK() {
        return k;
    }

    public void addK(String k) {
        this.k.add(k);
    }

    public List<String> getW() {
        return w;
    }

    public void addW(String w) {
        this.w.add(w);
    }

    public List<String> getOther() {
        return other;
    }

    public void addOther(String other) {
        this.other.add(other);
    }

    @Override
    public String toString() {
        return "WordNLPModel{" +
                "n=" + n +
                ", nr=" + nr +
                ", ns=" + ns +
                ", nt=" + nt +
                ", nz=" + nz +
                ", nl=" + nl +
                ", ng=" + ng +
                ", t=" + t +
                ", v=" + v +
                ", vd=" + vd +
                ", vn=" + vn +
                ", vshi=" + vshi +
                ", vyou=" + vyou +
                ", a=" + a +
                ", ad=" + ad +
                ", d=" + d +
                ", r=" + r +
                ", rr=" + rr +
                ", rz=" + rz +
                ", rzt=" + rzt +
                ", c=" + c +
                ", u=" + u +
                ", m=" + m +
                ", q=" + q +
                ", y=" + y +
                ", e=" + e +
                ", o=" + o +
                ", f=" + f +
                ", z=" + z +
                ", p=" + p +
                ", h=" + h +
                ", k=" + k +
                ", w=" + w +
                ", other=" + other +
                '}';
    }

    public void setN(List<String> n) {
        this.n = n;
    }

    public void setNr(List<String> nr) {
        this.nr = nr;
    }

    public void setNs(List<String> ns) {
        this.ns = ns;
    }

    public void setNt(List<String> nt) {
        this.nt = nt;
    }

    public void setNz(List<String> nz) {
        this.nz = nz;
    }

    public void setNl(List<String> nl) {
        this.nl = nl;
    }

    public void setNg(List<String> ng) {
        this.ng = ng;
    }

    public void setT(List<String> t) {
        this.t = t;
    }

    public void setV(List<String> v) {
        this.v = v;
    }

    public void setVd(List<String> vd) {
        this.vd = vd;
    }

    public void setVn(List<String> vn) {
        this.vn = vn;
    }

    public void setVshi(List<String> vshi) {
        this.vshi = vshi;
    }

    public void setVyou(List<String> vyou) {
        this.vyou = vyou;
    }

    public void setA(List<String> a) {
        this.a = a;
    }

    public void setAd(List<String> ad) {
        this.ad = ad;
    }

    public void setD(List<String> d) {
        this.d = d;
    }

    public void setR(List<String> r) {
        this.r = r;
    }

    public void setRr(List<String> rr) {
        this.rr = rr;
    }

    public void setRz(List<String> rz) {
        this.rz = rz;
    }

    public void setRzt(List<String> rzt) {
        this.rzt = rzt;
    }

    public void setC(List<String> c) {
        this.c = c;
    }

    public void setU(List<String> u) {
        this.u = u;
    }

    public void setM(List<String> m) {
        this.m = m;
    }

    public void setQ(List<String> q) {
        this.q = q;
    }

    public void setY(List<String> y) {
        this.y = y;
    }

    public void setE(List<String> e) {
        this.e = e;
    }

    public void setO(List<String> o) {
        this.o = o;
    }

    public void setF(List<String> f) {
        this.f = f;
    }

    public void setZ(List<String> z) {
        this.z = z;
    }

    public void setP(List<String> p) {
        this.p = p;
    }

    public void setH(List<String> h) {
        this.h = h;
    }

    public void setK(List<String> k) {
        this.k = k;
    }

    public void setW(List<String> w) {
        this.w = w;
    }

    public void setOther(List<String> other) {
        this.other = other;
    }
}
