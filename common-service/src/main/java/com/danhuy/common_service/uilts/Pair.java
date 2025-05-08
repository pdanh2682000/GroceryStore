package com.danhuy.common_service.uilts;

public class Pair<P1, P2> {

  private final P1 first;
  private final P2 second;

  private Pair(P1 first, P2 second) {
    this.first = first;
    this.second = second;
  }

  public static <P1, P2> Pair<P1, P2> of(P1 first, P2 second) {
    return new Pair<P1, P2>(first, second);
  }

  public P1 getFirst() {
    return this.first;
  }

  public P2 getSecond() {
    return this.second;
  }

}
