package org.ssssssss.magicapi.nebula;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.nebula.model.Edge;
import org.ssssssss.magicapi.nebula.model.NebulaModel;
import org.ssssssss.magicapi.nebula.model.Node;
import org.ssssssss.magicapi.nebula.response.*;
import org.ssssssss.script.annotation.Comment;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@MagicModule("nebula")
public class NebulaModule {

    @Autowired
    private NebulaPool nebulaPool;

    @Autowired
    private NebulaPoolProperties nebulaPoolProperties;

    private static final Logger logger = LoggerFactory.getLogger(NebulaModule.class);


    /**
     * 执行ngsl脚本, 返回json格式结果
     *
     * @param script
     * @return
     */
    @Comment("执行ngsl脚本, 返回json格式结果")
    public Object executeJson(String script) {
        Session session = getNebulaSession();
        try {
            String json = session.executeJson(script);
            return json;
        } catch (Exception e) {
            logger.error("执行Nebula脚本异常, script: {}", script, e);
            throw new RuntimeException(e);
        } finally {
            Optional.ofNullable(session).ifPresent(Session::release);
        }
    }


    /**
     * 执行ngsl脚本, 并解析为可视化格式
     *
     * @param script
     * @return
     */
    @Comment("执行ngsl脚本, 返回json格式结果, 并解析为可视化格式")
    public NebulaModel executeNebulaModel(String script) {
        Session session = getNebulaSession();
        try {
            String json = session.executeJson(script);
            return convert(json);
        } catch (Exception e) {
            logger.error("执行Nebula脚本异常, script: {}", script, e);
            throw new RuntimeException(e);
        } finally {
            Optional.ofNullable(session).ifPresent(Session::release);
        }
    }


    /**
     * 执行ngsl脚本, 返回ResultSet格式结果, 不可直接使用
     *
     * @param script
     * @return
     */
    @Comment("执行ngsl脚本, 返回ResultSet格式结果, 无法直接使用")
    public Object execute(String script) {
        Session session = getNebulaSession();
        try {
            ResultSet resultSet = session.execute(script);
            return resultSet;
        } catch (Exception e) {
            logger.error("执行Nebula脚本异常, script: {}", script, e);
            throw new RuntimeException(e);
        } finally {
            Optional.ofNullable(session).ifPresent(Session::release);
        }
    }

    public Session getNebulaSession() {
        try {
            return nebulaPool.getSession(nebulaPoolProperties.getUserName(), nebulaPoolProperties.getPassword(), nebulaPoolProperties.isReconnect());
        } catch (NoSuchBeanDefinitionException e) {
            throw new RuntimeException(String.format("NebulaPool 未初始化, 或初始化异常, 请检查配置文件"));
        } catch (Exception e) {
            logger.error("获取nebula session 异常", e);
            throw new RuntimeException(e);
        }
    }

    @Comment("解析nebula结果为可视化格式")
    public NebulaModel convert(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        NebulaJsonBody response = objectMapper.readValue(json, NebulaJsonBody.class);

        //状态码不为0则为异常, 解析提示异常信息
        if (response.getErrorCode() != 0) {
            logger.error("执行Nebula脚本异常, script: {}, errorMsg: {}", json, response.getErrorMsg());
            throw new RuntimeException(response.getErrorMsg());
        }

        NebulaModel nebulaModel = new NebulaModel();
        HashMap<String, Integer> nodeEdges = new HashMap<>();
        List<NebulaJsonBody.Data> datas = response.getResults().get(0).getData();
        for (int index = 0; index < datas.size(); index++) {
            List<List<Element>> meta = datas.get(index).getMeta();
            List<List<HashMap<String, Object>>> row = datas.get(index).getRow();
            for (int i = 0; i < meta.get(0).size(); i++) {
                Element element = meta.get(0).get(i);
                HashMap<String, Object> elementDetail = row.get(0).get(i);
                Node node = new Node();
                Edge edge = new Edge();

                if (element instanceof Vertex) {
                    node.setId(((Vertex) element).getId());
                    node.getProp().putAll(elementDetail);
                    nebulaModel.addNode(node);

                } else if (element instanceof EdgeElement) {
                    edge.getProp().putAll(elementDetail);
                    EdgeId id = ((EdgeElement) element).getId();
                    edge.setTarget(id.getDst());
                    edge.setSource(id.getSrc());
                    edge.setLabel(id.getName());
                    edge.setValue(id.getRanking());
                    nebulaModel.getEdges().add(edge);

                    nodeEdges.put(id.getDst(), nodeEdges.getOrDefault(id.getDst(), 0) + 1);
                    nodeEdges.put(id.getSrc(), nodeEdges.getOrDefault(id.getSrc(), 0) + 1);
                }
            }
            // 补充节点边的数量值
            for (Node node : nebulaModel.getNodes()) {
                node.setEdgeSize(nodeEdges.getOrDefault(node.getId(), 0));
            }
        }
        return nebulaModel;
    }

}
