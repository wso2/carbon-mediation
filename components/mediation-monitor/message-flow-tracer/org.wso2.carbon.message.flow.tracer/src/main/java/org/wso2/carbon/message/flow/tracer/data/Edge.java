package org.wso2.carbon.message.flow.tracer.data;

public class Edge{

    private String node1;
    private String node2;

    public Edge(String node1, String node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public String getNode1() {
        return node1;
    }

    public String getNode2() {
        return node2;
    }

    @Override
    public boolean equals(Object obj) {
        Edge other = (Edge) obj;

        return this.node1.equals(other.getNode1()) && this.node2.equals(other.getNode2());
    }
}
