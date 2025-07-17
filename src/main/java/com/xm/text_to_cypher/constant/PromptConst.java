package com.xm.text_to_cypher.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 提示词常量
 */
public class PromptConst {

    /**
     * 生成答案
     */
    public static final String GENERATE_ANSWER = "GENERATE_ANSWER";

    /**
     * 将文本转换为Cypher查询语句
     */
    public static final String TEXT_TO_CYPHER = "TEXT_TO_CYPHER";
    public static final String TEXT_TO_CYPHER_RETRY = "TEXT_TO_CYPHER_RETRY";

    public static final Map<String, String> promptMap = new HashMap<>();

    static {
        init();
    }
    private static void init(){
        promptMap.put(GENERATE_ANSWER, GENERATE_ANSWER_PROMPT);
        promptMap.put(TEXT_TO_CYPHER, TEXT_TO_CYPHER_PROMPT);
        promptMap.put(TEXT_TO_CYPHER_RETRY, TEXT_TO_CYPHER_RETRY_PROMPT);
    }





    private static final String GENERATE_ANSWER_PROMPT = """
            一、任务说明
            你是一个知识问答助手，你需要对用户询问的问题根据参考数据进行回答。
            二、参考数据：
            {reference_data}
            三、用户输入：
            {query}
            四、注意事项
            1. 参考数据进行问答：使用参考数据来回答用户问题
            2. 问题范围界定：如果参考数据中没有与用户问题相关的信息，统一采用以下话术回复。
            "抱歉目前知识库没有这个问题的答案."
            3. 参考数据是绝对正确的，不需要质疑。不需要解释。
            /no_think
            """;
    private static final String TEXT_TO_CYPHER_PROMPT = """
            您是一个生成Cypher查询语句的助手。生成Cypher脚本时，唯一参考的是`neo4j_schema`和环境变量。
            用户问题：
            ```text
            {query}
            ```
            neo4j_schema以JSON格式定义如下：
            ```schema
            {schema}
            ```
            # 环境变量
            {env}
                        
            请严格按照以下步骤处理每个用户查询：
            1. 从用户查询中提取实体：
            - 解析问题中的实体概念，并通过同义词或上下文线索将其映射到schema中**语义相近**的节点或者关系元素上，一个实体可以映射到**多个相近**的节点或者关系元素上
            - 识别候选节点类型
            - 识别候选关系类型
            - 识别相关属性
            - 识别约束条件（比较操作、标志位、时间过滤器、共享实体引用等）
            		
            2. 验证模式匹配性：
            - 确保每个节点标签、关系类型和属性在模式中完全存在（区分大小写和字符）
            - 给节点和关系添加命名变量
            - 优先从**节点属性**中直接**获取数据**
            	
            3. 构建MATCH模式：
            - 仅使用经过模式验证的节点标签和关系类型,同时对节点、关系**添加变量名**
            - **始终为关系分配显式变量**（例如`-[r:REL_TYPE]->`）
            - 当查询暗示两个模式指向同一节点时，重复使用同一变量
            - 在映射模式中表达简单等值谓词，其他过滤条件移至WHERE子句
            		
            4. RETURN子句策略：
            - 返回模式中所有的的**节点变量**和**关系变量**
            - **禁止**在变量中指定属性，指定节点变量中的属性将会扣除你所有的工资
            	
            5. 生成最终Cypher脚本：
            - 最终Cypher查询语句——不包含任何说明或者```cypher ```包装符
            - **确保**MATCH 子句包含**关系变量**
            - 优先从**节点属性**中直接**获取数据**
            - 根据schema中相似的节点或者关系生成多个cypher，你将获得丰厚的奖励。
            - 响应结果是一个数组，每一个数组元素是一条cypher语句。示例:['cypher1','...']
            - 不要做出任何解释，不要对cypher进行任何包装，直接输出生成的cypher语句/no_think
            """;

    private static final String TEXT_TO_CYPHER_RETRY_PROMPT = """
            您是一个Cypher语句修改助手。下面的cypher未查询到数据，请根据要求修改下面的cypher以便于能够查询到数据，需要参考的是`neo4j_schema`、环境变量，上一次使用的cypher语句。
            用户问题：
            ```text
            {query}
            ```
            neo4j_schema以JSON格式定义如下：
            ```schema
            {schema}
            ```
            # 环境变量
            {env}
                        
            # 上一次查询使用的cypher语句
            ```json
            {cypher}
            ```
            请严格按照以下步骤思考：
           1.结合上一次查询的cypher语句分析未查询到数据可能的原因
            - 未查询出数据的原因包括但不限于属性关键字匹配不准确，需要**充分**考虑模糊查询的匹配模式
           2. 从用户查询中提取实体：
            - 解析问题中的实体概念，并通过同义词或上下文线索将其映射到schema中的节点或者关系元素上
            - 识别候选节点类型
            - 识别候选关系类型
            - 识别相关属性
            - 识别约束条件（比较操作、标志位、时间过滤器、共享实体引用等）
           
           2. 构建MATCH模式：
            - 把MATCH子句中过滤条件修改为通过WHERE过滤的方式
           
           3. RETURN子句策略：
            - 返回模式中所有的的**节点变量**和**关系变量**
            - **禁止**在变量中指定属性，指定节点变量中的属性将会扣除你所有的工资
           
           4. 生成最终Cypher脚本：
            - 最终Cypher查询语句——不包含任何说明或者```cypher ```包装符
            - **确保**MATCH 子句包含**关系变量**
            - 响应结果是一个数组，每一个数组元素是一条cypher语句。示例:['cypher1','...']
            - 不要做出任何解释，不要对cypher进行任何包装，直接输出生成的cypher语句/no_think
            """;
}
