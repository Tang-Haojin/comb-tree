package site.tanghaojin.combTree;

import java.util.Stack;

public class CombiningTree {
  private Node[] nodes;

  public CombiningTree(int size) {
    nodes = new Node[size];
    nodes[0] = new Node();
    for (int i = 1; i < nodes.length; ++i) {
      nodes[i] = new Node(nodes[(i - 1) / 2]);
    }
  }

  public int get() {
    return nodes[0].result;
  }

  public int getAndIncrement(int threadID) throws InterruptedException {
    int prior;
    Stack<Node> stack = new Stack<Node>();
    Node myLeaf = nodes[nodes.length - threadID / 2 - 1];

    // precombine
    Node node = myLeaf;
    while (node.precombine()) {
      node = node.parent;
    }
    Node stop = node;

    // combine
    node = myLeaf;
    int combined = 1;
    while (node != stop) {
      combined = node.combine(combined);
      stack.push(node);
      node = node.parent;
    }

    // op
    prior = stop.op(combined);

    // distribute
    while (!stack.empty()) {
      node = stack.pop();
      node.distribute(prior);
    }
    return prior;
  }
}
