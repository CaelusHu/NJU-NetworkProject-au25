### Server 部分（负责人：胡奕博 241250035）

主要负责整个 HTTP 服务端的基础框架，包括：

1. HttpServer.java —— 服务器入口

    创建 ServerSocket，监听指定端口

    循环接受来自客户端的连接

    为每个连接创建一个 ClientHandler 线程进行处理

    在服务器启动时打印启动信息

    支持关闭服务器（可通过 Ctrl+C 或后续扩展）

    目前实现：

    * 端口监听正常

    * 接收到连接后，会把 Socket 交给 ClientHandler

    * Server 本身不解析 HTTP，只负责调度

    * 后续同学可基于此添加：

    * 日志输出

    * 线程池（替换直接 new Thread 的方式）

2. ClientHandler.java —— 请求处理核心

    负责：

    从 Socket 中读取客户端发送的 HTTP 报文

    调用 HttpRequestParser（下一位同学负责）解析报文

    调用 Router（业务同学负责）找到对应接口

    将业务得到的响应封装为 HttpResponse 并写回给客户端

    支持短连接 / 长连接（通过解析 Connection 头判断）

    目前实现：

   * 已能准确接收原始 HTTP 报文（请求行 + 头部 + body）

   * 已能把报文传给解析模块（暂时留空）

   * 已写好向客户端返回原始字符串的基础结构



后续需要补的：

>在 HttpRequestParser.parse() 中解析出 method / path / headers / body
> 
>在 Router.route(request) 中处理 GET/POST 路径逻辑
> 
>在 HttpResponse 中生成完整 HTTP 响应报文

3. 已完成的接口
    ```
    HttpRequest request = HttpRequestParser.parse(inputStream);
    HttpResponse response = Router.route(request);
    writeResponse(outputStream, response);
    ```

    只需要按照这三个步骤填充功能即可，不需要修改服务器主框架。

4. 注意事项

    解析模块不要在 HttpServer 或 ClientHandler 中写！
    遵循模块分离，不要耦合逻辑。

    请求必须按照 HTTP/1.1 标准解析，否则长连接无法正常工作。

    GET 请求没有 body，POST 需要根据 Content-Length 读取 body。

    返回响应时必须严格按照以下格式写：
    ```
    HTTP/1.1 200 OK
    Content-Type: text/plain
    Content-Length: 5

    hello
    ```

    保持模块之间只通过 request/response 对象通信，不要直接访问 socket。

### User 部分 （负责人：周立涛 241250098）

1. UserService.java ----处理注册和登录请求 

### Res、Req部分 （负责人：庄永琪 241250005）

主要负责 HTTP 请求解析和响应构造，包括：

1. HttpRequest.java —— HTTP请求数据对象

    存储解析后的HTTP请求信息，包括：

    * method：请求方法（GET/POST）
    * path：请求路径（如 /login）
    * protocol：协议版本（HTTP/1.1）
    * headers：请求头字典
    * body：请求体内容
    * parameters：URL查询参数和表单参数

    目前实现：

    * 完整的 getter/setter 方法
    * addHeader() / getHeader() 支持大小写不敏感查找
    * addParameter() / getParameter() 支持参数存取
    * toString() 方法便于调试输出

2. HttpRequestParser.java —— HTTP请求解析器

    负责将原始HTTP报文字符串解析为 HttpRequest 对象

    目前实现：

    * parse(String) —— 解析字符串格式的HTTP请求
    * parse(InputStream) —— 解析输入流格式的HTTP请求
    * parseRequestLine() —— 解析请求行，提取 method/path/protocol
    * parseHeaders() —— 解析请求头，按 : 分割存入字典
    * parseBody() —— 根据 Content-Length 读取请求体
    * parseQueryParameters() —— 解析URL查询参数（?a=1&b=2）
    * parseQueryString() —— 解析表单参数（application/x-www-form-urlencoded）
    * 提供测试用的 createSampleRequestString() 和 createSamplePostRequestString()

3. HttpResponse.java —— HTTP响应构造

    负责构造符合HTTP协议的响应报文

    目前实现：

    * toBytes() —— 核心方法，将响应对象转换为字节数组发送给客户端
    * setBody() / setHeader() —— 设置响应体和响应头

    已支持的状态码：

    * 200 OK —— okText() / okJson() / okImage() / okBinary()
    * 201 Created —— created()
    * 301 Moved Permanently —— movedPermanently(url) 永久重定向
    * 302 Found —— found(url) 临时重定向
    * 304 Not Modified —— notModified() 缓存未修改
    * 400 Bad Request —— badRequest()
    * 401 Unauthorized —— unauthorized()
    * 404 Not Found —— notFound()
    * 405 Method Not Allowed —— methodNotAllowed()
    * 409 Conflict —— conflict()
    * 500 Internal Server Error —— internalServerError()

    已支持的 MIME 类型：

    * text/plain —— 纯文本
    * text/html —— HTML页面
    * application/json —— JSON数据
    * image/png、image/jpeg、image/gif —— 图片（非文本类型）
    * application/octet-stream —— 二进制文件下载

    辅助功能：

    * getMimeType(filename) —— 根据文件扩展名自动识别MIME类型
    * setETag() / setLastModified() —— 支持缓存验证头

4. 注意事项

    请求解析遵循 HTTP/1.1 标准格式：
    ```
    GET /path?param=value HTTP/1.1\r\n
    Host: localhost\r\n
    Content-Type: application/x-www-form-urlencoded\r\n
    \r\n
    body内容
    ```

    响应构造严格按照以下格式：
    ```
    HTTP/1.1 200 OK\r\n
    Content-Type: text/plain; charset=utf-8\r\n
    Content-Length: 5\r\n
    \r\n
    hello
    ```

    模块之间通过 HttpRequest / HttpResponse 对象通信，不直接操作 Socket。
    