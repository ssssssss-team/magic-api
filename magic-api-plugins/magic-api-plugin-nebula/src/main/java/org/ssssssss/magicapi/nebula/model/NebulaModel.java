package org.ssssssss.magicapi.nebula.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ssssssss.script.annotation.Comment;

import java.util.*;

/**
 * 经过加工后的nebula数据结构, 用于前端数据展示
 * 目前很多前端组件库支持这种数据, 并可视化展示, 如ntV G6等
 * @see <a href="@link:http://antv-2018.alipay.com/zh-cn/g6/3.x/demo/index.html">AntV G6</a>
 */
public class NebulaModel {

    @JsonIgnore
    private List<String> nodeIds = new ArrayList<>();

    /**
     * 包含的节点集合
     */
    @Comment("包含的节点集合")
    private List<Node> nodes = new ArrayList<>();

    /**
     * 包含的边集合
     */

    @Comment("包含的边集合")
    private List<Edge> edges = new ArrayList<>();

    public List<String> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    /**
     * 添加节点, 根据id去重
     * @param node
     */
    @Comment("添加节点, 根据id去重")
    public void addNode(Node node) {
        String nodeId = Objects.toString(node.getId(), null);
        if (nodeIds.contains(nodeId)) {
            return;
        }
        nodeIds.add(nodeId);
        nodes.add(node);
    }

}