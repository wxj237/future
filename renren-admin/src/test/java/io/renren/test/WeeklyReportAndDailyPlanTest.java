import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 周报和每日学习计划功能测试类
 * 
 * 本测试类用于测试签到管理系统中的周报提交、查询和每日学习计划相关功能
 * 包含以下四个主要测试方法：
 * 1. testSaveDailyPlan - 保存每日学习计划
 * 2. testSubmitWeeklyReport - 提交周报
 * 3. testGetWeeklyReports - 查询周报列表
 * 4. testGetDailyPlan - 根据日期查询学习计划
 * 
 * Postman测试说明：
 * 1. 所有接口都需要在Headers中添加token认证信息
 * 2. Content-Type根据请求类型设置为application/json
 * 3. URL需要包含完整的上下文路径/renren-admin
 */
public class WeeklyReportAndDailyPlanTest {
    private static final String BASE_URL = "http://localhost:8080/renren-admin";
    private static final String TOKEN = "da75b6541863fd8016f0951d66e8e953";
    
    public static void main(String[] args) {
        try {
            // 测试提交每日学习计划
            testSaveDailyPlan();
            
            // 测试提交周报
            testSubmitWeeklyReport();
            
            // 测试查询周报列表
            testGetWeeklyReports();
            
            // 测试根据日期查询学习计划
            testGetDailyPlan();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 测试保存每日学习计划
     * 
     * 接口地址: POST /attendance/dailyplan/save
     * 请求参数:
     * - userId: 用户ID
     * - planDate: 计划日期 (格式: yyyy-MM-dd)
     * - content: 学习内容
     * - completion: 完成情况
     * 
     * Postman测试配置:
     * 1. Method: POST
     * 2. URL: http://localhost:8080/renren-admin/attendance/dailyplan/save
     * 3. Headers:
     *    - Content-Type: application/json
     *    - token: da75b6541863fd8016f0951d66e8e953
     * 4. Body (raw, JSON):
     *    {
     *      "userId": 1,
     *      "planDate": "2025-10-14",
     *      "content": "学习Java并发编程，完成第5章内容",
     *      "completion": "已完成80%"
     *    }
     * 
     * 测试步骤:
     * 1. 构造包含学习计划信息的JSON请求体
     * 2. 设置必要的请求头 (Content-Type, token)
     * 3. 发送POST请求到指定接口
     * 4. 输出响应结果
     * 
     * 预期结果:
     * - 状态码200
     * - 返回成功信息: {"code": 200, "msg": "保存成功"}
     * - 数据库中新增对应的每日学习计划记录
     */
    public static void testSaveDailyPlan() throws Exception {
        System.out.println("=== 测试保存每日学习计划 ===");
        
        HttpClient client = HttpClient.newHttpClient();
        
        // 构造请求体
        String requestBody = "{\n" +
            "  \"userId\": 1,\n" +
            "  \"planDate\": \"" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\",\n" +
            "  \"content\": \"学习Java并发编程，完成第5章内容\",\n" +
            "  \"completion\": \"已完成80%\"\n" +
            "}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/attendance/dailyplan/save"))
            .header("Content-Type", "application/json")
            .header("token", TOKEN)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("保存每日学习计划响应: " + response.body());
        System.out.println("测试说明: 检查响应中code是否为200，表示保存成功");
        System.out.println();
    }
    
    /**
     * 测试提交周报
     * 
     * 接口地址: POST /attendance/weeklyreport/submit
     * 请求参数:
     * - userId: 用户ID
     * - startDate: 周报开始日期
     * - endDate: 周报结束日期
     * - content: 周报内容
     * 
     * Postman测试配置:
     * 1. Method: POST
     * 2. URL: http://localhost:8080/renren-admin/attendance/weeklyreport/submit
     * 3. Headers:
     *    - Content-Type: application/json
     *    - token: da75b6541863fd8016f0951d66e8e953
     * 4. Body (raw, JSON):
     *    {
     *      "userId": 1,
     *      "startDate": "2025-10-06",
     *      "endDate": "2025-10-12",
     *      "content": "本周主要学习了Java并发编程相关内容，完成了第5章的学习，理解了线程池的使用方法和注意事项。同时完成了项目中的签到功能优化。"
     *    }
     * 
     * 测试步骤:
     * 1. 计算本周的开始和结束日期
     * 2. 构造包含周报信息的JSON请求体
     * 3. 设置必要的请求头 (Content-Type, token)
     * 4. 发送POST请求到指定接口
     * 5. 输出响应结果
     * 
     * 预期结果:
     * - 状态码200
     * - 返回成功信息: {"code": 200, "msg": "周报提交成功"}
     * - 数据库中新增或更新对应的周报记录
     */
    public static void testSubmitWeeklyReport() throws Exception {
        System.out.println("=== 测试提交周报 ===");
        
        HttpClient client = HttpClient.newHttpClient();
        
        // 计算本周的开始和结束日期
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfWeek().getValue() - 1); // 本周一
        LocalDate endDate = startDate.plusDays(6); // 本周日
        
        // 构造请求体
        String requestBody = "{\n" +
            "  \"userId\": 1,\n" +
            "  \"startDate\": \"" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\",\n" +
            "  \"endDate\": \"" + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\",\n" +
            "  \"content\": \"本周主要学习了Java并发编程相关内容，完成了第5章的学习，理解了线程池的使用方法和注意事项。同时完成了项目中的签到功能优化。\"\n" +
            "}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/attendance/weeklyreport/submit"))
            .header("Content-Type", "application/json")
            .header("token", TOKEN)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("提交周报响应: " + response.body());
        System.out.println("测试说明: 检查响应中code是否为200，表示提交成功");
        System.out.println();
    }
    
    /**
     * 测试查询周报列表
     * 
     * 接口地址: GET /attendance/weeklyreport/list
     * 请求参数:
     * - userId: 用户ID
     * - page: 页码 (默认: 1)
     * - limit: 每页条数 (默认: 10)
     * 
     * Postman测试配置:
     * 1. Method: GET
     * 2. URL: http://localhost:8080/renren-admin/attendance/weeklyreport/list?userId=1&page=1&limit=10
     * 3. Headers:
     *    - token: da75b6541863fd8016f0951d66e8e953
     * 
     * 测试步骤:
     * 1. 构造包含查询参数的URL
     * 2. 设置必要的请求头 (token)
     * 3. 发送GET请求到指定接口
     * 4. 输出响应结果
     * 
     * 预期结果:
     * - 状态码200
     * - 返回周报列表信息: {"code": 200, "msg": "查询成功", "data": [...], "total": ...}
     * - data字段包含周报列表数据
     * - total字段表示总记录数
     */
    public static void testGetWeeklyReports() throws Exception {
        System.out.println("=== 测试查询周报列表 ===");
        
        HttpClient client = HttpClient.newHttpClient();
        
        // 构造查询参数
        String url = BASE_URL + "/attendance/weeklyreport/list?userId=1&page=1&limit=10";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("token", TOKEN)
            .GET()
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("查询周报列表响应: " + response.body());
        System.out.println("测试说明: 检查响应中code是否为200，data是否包含周报列表数据");
        System.out.println();
    }
    
    /**
     * 测试根据日期查询学习计划
     * 
     * 接口地址: GET /attendance/dailyplan/get
     * 请求参数:
     * - userId: 用户ID
     * - planDate: 计划日期 (格式: yyyy-MM-dd)
     * 
     * Postman测试配置:
     * 1. Method: GET
     * 2. URL: http://localhost:8080/renren-admin/attendance/dailyplan/get?userId=1&planDate=2025-10-14
     * 3. Headers:
     *    - token: da75b6541863fd8016f0951d66e8e953
     * 
     * 测试步骤:
     * 1. 构造包含查询参数的URL
     * 2. 设置必要的请求头 (token)
     * 3. 发送GET请求到指定接口
     * 4. 输出响应结果
     * 
     * 预期结果:
     * - 状态码200
     * - 返回指定日期的学习计划信息: {"code": 200, "msg": "查询成功", "data": {...}}
     * - data字段包含对应日期的学习计划详情
     */
    public static void testGetDailyPlan() throws Exception {
        System.out.println("=== 测试根据日期查询学习计划 ===");
        
        HttpClient client = HttpClient.newHttpClient();
        
        // 构造查询参数
        String planDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String url = BASE_URL + "/attendance/dailyplan/get?userId=1&planDate=" + planDate;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("token", TOKEN)
            .GET()
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("根据日期查询学习计划响应: " + response.body());
        System.out.println("测试说明: 检查响应中code是否为200，data是否包含指定日期的学习计划");
        System.out.println();
    }
    
    /**
     * Postman测试完整指南
     * 
     * 1. 环境配置:
     *    - 确保后端服务正在运行
     *    - 确认服务地址 (默认为 http://localhost:8080)
     *    - 获取有效的token (通过登录接口获取)
     * 
     * 2. 公共Headers设置:
     *    所有接口都需要在Headers中添加:
     *    - Key: token
     *      Value: da75b6541863fd8016f0951d66e8e953 (或您获取到的实际token)
     *    - Key: Content-Type (仅POST/PUT请求需要)
     *      Value: application/json
     * 
     * 3. 接口测试步骤:
     *    
     *    3.1 保存每日学习计划 (/attendance/dailyplan/save)
     *        - Method: POST
     *        - URL: http://localhost:8080/renren-admin/attendance/dailyplan/save
     *        - Body选择raw -> JSON格式，输入请求体
     *        - 点击Send按钮发送请求
     *    
     *    3.2 提交周报 (/attendance/weeklyreport/submit)
     *        - Method: POST
     *        - URL: http://localhost:8080/renren-admin/attendance/weeklyreport/submit
     *        - Body选择raw -> JSON格式，输入请求体
     *        - 点击Send按钮发送请求
     *    
     *    3.3 查询周报列表 (/attendance/weeklyreport/list)
     *        - Method: GET
     *        - URL: http://localhost:8080/renren-admin/attendance/weeklyreport/list?userId=1&page=1&limit=10
     *        - 不需要Body
     *        - 点击Send按钮发送请求
     *    
     *    3.4 根据日期查询学习计划 (/attendance/dailyplan/get)
     *        - Method: GET
     *        - URL: http://localhost:8080/renren-admin/attendance/dailyplan/get?userId=1&planDate=2025-10-14
     *        - 不需要Body
     *        - 点击Send按钮发送请求
     * 
     * 4. 预期结果验证:
     *    - Status: 200 OK
     *    - Response Body中code字段为200表示操作成功
     *    - 根据不同接口检查data字段内容是否符合预期
     */
     
    /**
     * 新增：测试删除日报功能
     * 
     * 接口地址: DELETE /attendance/dailyplan/{id}
     * 请求参数:
     * - id: 日报ID
     * 
     * Postman测试配置:
     * 1. Method: DELETE
     * 2. URL: http://localhost:8080/renren-admin/attendance/dailyplan/1
     * 3. Headers:
     *    - token: da75b6541863fd8016f0951d66e8e953
     */
    public static void testDeleteDailyPlan() throws Exception {
        System.out.println("=== 测试删除日报 ===");
        
        HttpClient client = HttpClient.newHttpClient();
        
        // 构造请求
        String url = BASE_URL + "/attendance/dailyplan/1"; // 假设要删除ID为1的日报
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("token", TOKEN)
            .DELETE()
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("删除日报响应: " + response.body());
        System.out.println("测试说明: 检查响应中code是否为200，表示删除成功");
        System.out.println();
    }
}