package com.github.hiwepy.openclaw.cli.opts;

import com.github.hiwepy.openclaw.cli.args.CliSubArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code openclaw browser}：经 Gateway 控制的嵌入式浏览器：进程启停、命名 profile、标签页与页面交互、快照与网络/存储自省。
 * <p>与 Gateway 子命令常见选项共享 {@code --url}、{@code --token}、{@code --timeout}、{@code --expect-final}；{@code --browser-profile} 选用配置集。文档列出的其余子命令可经 {@link Builder#extra(String...)} 追加。</p>
 *
 * @see <a href="https://docs.openclaw.ai/cli/browser">browser CLI</a>
 */
public final class BrowserOptions implements CliSubArgs {

    /**
     * 将 Builder 状态映射为 {@code openclaw browser ...} 子命令序列（内部使用）。
     */
    private enum Cmd {
        /**
         * 仅附加全局 Gateway 与 JSON flag，不追加子命令动词。
         */
        NONE,
        /**
         * {@code browser status}：回报浏览器/CDP 是否已连接并就绪。
         */
        STATUS,
        /**
         * {@code browser start}：按当前 profile 启动浏览器进程。
         */
        START,
        /**
         * {@code browser stop}：停止受管浏览器实例。
         */
        STOP,
        /**
         * {@code browser reset-profile}：丢弃用户数据目录或等价重置（见官方说明）。
         */
        RESET_PROFILE,
        /**
         * {@code browser profiles}：列出已配置的命名 profile。
         */
        PROFILES,
        /**
         * {@code browser create-profile}：新建命名 profile 并可选驱动、CDP、用户数据路径。
         */
        CREATE_PROFILE,
        /**
         * {@code browser delete-profile}：删除命名 profile。
         */
        DELETE_PROFILE,
        /**
         * {@code browser tabs}：列出当前窗口中的标签页。
         */
        TABS,
        /**
         * {@code browser tab new}：新开标签页并可指定初始 URL。
         */
        TAB_NEW,
        /**
         * {@code browser tab select}：按索引激活标签页。
         */
        TAB_SELECT,
        /**
         * {@code browser tab close}：关闭指定索引的标签页。
         */
        TAB_CLOSE,
        /**
         * {@code browser open}：在指定 target 上打开 URL。
         */
        OPEN,
        /**
         * {@code browser focus}：将焦点切到某页面 target。
         */
        FOCUS,
        /**
         * {@code browser close}：关闭某页面 target。
         */
        CLOSE,
        /**
         * {@code browser snapshot}：抓取可访问性树或结构化页面快照。
         */
        SNAPSHOT,
        /**
         * {@code browser screenshot}：对整页或元素截图。
         */
        SCREENSHOT,
        /**
         * {@code browser navigate}：在当前页导航到 URL。
         */
        NAVIGATE,
        /**
         * {@code browser click}：对 ref 指向的节点执行点击。
         */
        CLICK,
        /**
         * {@code browser type}：向可编辑元素输入文本。
         */
        TYPE,
        /**
         * {@code browser press}：发送键盘按键（如 Enter）。
         */
        PRESS,
        /**
         * {@code browser hover}：悬停在 ref 元素上。
         */
        HOVER,
        /**
         * {@code browser scrollintoview}：将 ref 元素滚动到视口内。
         */
        SCROLL_INTO_VIEW,
        /**
         * {@code browser drag}：从起点 ref 拖到终点 ref。
         */
        DRAG,
        /**
         * {@code browser select}：在下拉或多选中选取给定值。
         */
        SELECT,
        /**
         * {@code browser fill}：按 JSON 字段映射批量填充表单。
         */
        FILL,
        /**
         * {@code browser wait}：等待出现指定文本或条件。
         */
        WAIT,
        /**
         * {@code browser evaluate}：在页面上下文执行函数并可选绑定 ref。
         */
        EVALUATE,
        /**
         * {@code browser upload}：通过文件选择器上传本地路径。
         */
        UPLOAD,
        /**
         * {@code browser waitfordownload}：阻塞直到下载开始或超时。
         */
        WAIT_FOR_DOWNLOAD,
        /**
         * {@code browser download}：将已开始的下载保存为指定文件名。
         */
        DOWNLOAD,
        /**
         * {@code browser dialog --accept}：处理原生对话框（接受或取消，见参数）。
         */
        DIALOG_ACCEPT,
        /**
         * {@code browser resize}：调整窗口尺寸（像素）。
         */
        RESIZE,
        /**
         * {@code browser set viewport}：设置视口宽高（与窗口可独立）。
         */
        SET_VIEWPORT,
        /**
         * {@code browser set offline}：切换离线网络仿真。
         */
        SET_OFFLINE,
        /**
         * {@code browser set media}：覆盖 prefers-reduced-motion 等媒体查询仿真。
         */
        SET_MEDIA,
        /**
         * {@code browser set timezone}：设置 IANA 时区仿真。
         */
        SET_TIMEZONE,
        /**
         * {@code browser set locale}：设置语言区域仿真。
         */
        SET_LOCALE,
        /**
         * {@code browser set geo}：设置地理位置与精度（用于权限提示）。
         */
        SET_GEO,
        /**
         * {@code browser set device}：按预设设备名应用 UA 与视口。
         */
        SET_DEVICE,
        /**
         * {@code browser set headers}：为后续请求注入额外 HTTP 头（JSON）。
         */
        SET_HEADERS,
        /**
         * {@code browser set credentials}：为指定源设置 HTTP 基本认证。
         */
        SET_CREDENTIALS,
        /**
         * {@code browser cookies}：列出或检查 cookie（子路径见实现）。
         */
        COOKIES,
        /**
         * {@code browser cookies set}：写入命名 cookie 及作用域 URL。
         */
        COOKIES_SET,
        /**
         * {@code browser cookies clear}：按域或模式清除 cookie。
         */
        COOKIES_CLEAR,
        /**
         * {@code browser storage local get}：读取 localStorage 键值。
         */
        STORAGE_LOCAL_GET,
        /**
         * {@code browser storage local set}：写入 localStorage。
         */
        STORAGE_LOCAL_SET,
        /**
         * {@code browser storage session clear}：清空 sessionStorage。
         */
        STORAGE_SESSION_CLEAR,
        /**
         * {@code browser console}：拉取或订阅控制台日志，可按 level 过滤。
         */
        CONSOLE,
        /**
         * {@code browser pdf}：将当前页导出为 PDF。
         */
        PDF,
        /**
         * {@code browser responsebody}：按模式匹配并返回某请求的响应体。
         */
        RESPONSE_BODY,
        /**
         * {@code browser highlight}：在页面上高亮 ref 对应节点（调试用）。
         */
        HIGHLIGHT,
        /**
         * {@code browser errors}：收集页面 JS 错误，可选清除缓冲区。
         */
        ERRORS,
        /**
         * {@code browser requests}：列出或过滤网络请求记录。
         */
        REQUESTS,
        /**
         * {@code browser trace start}：开始 HAR 或性能跟踪。
         */
        TRACE_START,
        /**
         * {@code browser trace stop}：停止跟踪并写出归档（如 zip）。
         */
        TRACE_STOP
    }

    /**
     * {@code --url}：Gateway HTTP(S) 基址，浏览器 RPC 经此转发。
     */
    private final String gatewayUrl;
    /**
     * {@code --token}：Gateway 认证令牌（若启用）。
     */
    private final String gatewayToken;
    /**
     * {@code --timeout}：单次 RPC 超时毫秒数。
     */
    private final int timeoutMs;
    /**
     * {@code --expect-final}：流式响应需等到最终帧再返回（与 Gateway 共享语义）。
     */
    private final boolean expectFinal;
    /**
     * {@code --browser-profile}：选用已配置的浏览器 profile 名称。
     */
    private final String browserProfile;
    /**
     * {@code --json}：机器可读输出。
     */
    private final boolean json;
    /**
     * 当前映射到的 browser 子命令（见 {@link Cmd}）。
     */
    private final Cmd cmd;
    /**
     * create/delete-profile：{@code --name} profile 名称。
     */
    private final String profileName;
    /**
     * create-profile：{@code --color} 终端或 UI 中的展示色（若有）。
     */
    private final String profileColor;
    /**
     * create-profile：{@code --driver} 浏览器后端（如 chromium 系）。
     */
    private final String profileDriver;
    /**
     * create-profile：{@code --cdp-url} 附加到已有 CDP 端点而非自启进程。
     */
    private final String profileCdpUrl;
    /**
     * create-profile：{@code --user-data-dir} 用户数据目录路径。
     */
    private final String profileUserDataDir;
    /**
     * tab select/close：按从 0 开始的标签索引。
     */
    private final Integer tabIndex;
    /**
     * open / navigate：目标 URL 字符串。
     */
    private final String openUrl;
    /**
     * focus / close：页面或 target 标识符（由 snapshot 返回）。
     */
    private final String targetId;
    /**
     * click/type/hover 等：无障碍快照中的元素 ref。
     */
    private final String ref;
    /**
     * type：要键入的文本内容。
     */
    private final String text;
    /**
     * press：按键名（如 {@code Enter}、{@code Tab}）。
     */
    private final String key;
    /**
     * drag：拖拽终点元素的 ref。
     */
    private final String refEnd;
    /**
     * select：要选中的 option 值列表。
     */
    private final List<String> selectValues;
    /**
     * fill：{@code --fields} 表单字段名到值的 JSON 映射。
     */
    private final String fieldsJson;
    /**
     * wait：{@code --text} 等待出现的可见文本子串。
     */
    private final String waitText;
    /**
     * evaluate：{@code --fn} 在页面内执行的函数字符串。
     */
    private final String evaluateFn;
    /**
     * evaluate：{@code --ref} 可选，作为函数执行上下文根节点。
     */
    private final String evaluateRef;
    /**
     * upload：本地待上传文件路径。
     */
    private final String uploadPath;
    /**
     * download：保存磁盘时使用的文件名提示。
     */
    private final String downloadFilename;
    /**
     * resize / set viewport：宽度像素。
     */
    private final Integer resizeW;
    /**
     * resize / set viewport：高度像素。
     */
    private final Integer resizeH;
    /**
     * set offline：{@code on} 或 {@code off} 等文档约定取值。
     */
    private final String offlineState;
    /**
     * set media：媒体特性仿真值（如 reduced-motion）。
     */
    private final String media;
    /**
     * set timezone：IANA 时区名。
     */
    private final String timezone;
    /**
     * set locale：BCP 47 语言标签。
     */
    private final String locale;
    /**
     * set geo：纬度（十进制度）。
     */
    private final Double geoLat;
    /**
     * set geo：经度（十进制度）。
     */
    private final Double geoLon;
    /**
     * set geo：{@code --accuracy} 米为单位的定位精度。
     */
    private final Integer geoAccuracy;
    /**
     * set device：预设设备配置名称。
     */
    private final String deviceName;
    /**
     * set headers：HTTP 头键值 JSON。
     */
    private final String headersJson;
    /**
     * set credentials：HTTP 基本认证用户名。
     */
    private final String credUser;
    /**
     * set credentials：HTTP 基本认证密码。
     */
    private final String credPass;
    /**
     * cookies set：Cookie 名称。
     */
    private final String cookieName;
    /**
     * cookies set：Cookie 值。
     */
    private final String cookieValue;
    /**
     * cookies set：作用域 URL（用于域与路径匹配）。
     */
    private final String cookieUrl;
    /**
     * storage local set：localStorage 键。
     */
    private final String storageKey;
    /**
     * storage local set：localStorage 值。
     */
    private final String storageValue;
    /**
     * console：{@code --level} 日志级别过滤（如 error、warning）。
     */
    private final String consoleLevel;
    /**
     * responsebody：用于匹配目标响应 URL 或 id 的模式字符串。
     */
    private final String responsePattern;
    /**
     * errors：{@code --clear} 在读取前清空已缓冲错误列表。
     */
    private final boolean errorsClear;
    /**
     * requests：{@code --filter} 子串或模式过滤请求列表。
     */
    private final String requestsFilter;
    /**
     * trace stop：{@code --out} 写出跟踪归档的路径（常为 zip）。
     */
    private final String traceOutZip;
    /**
     * screenshot：{@code --full-page} 截取完整可滚动页面。
     */
    private final boolean screenshotFullPage;
    /**
     * screenshot：{@code --ref} 仅截取该元素边界框。
     */
    private final String screenshotRef;
    /**
     * 其它 argv。
     */
    private final List<String> extra;

    /**
     * @param b 构建器快照
     */
    private BrowserOptions(Builder b) {
        this.gatewayUrl = b.gatewayUrl;
        this.gatewayToken = b.gatewayToken;
        this.timeoutMs = b.timeoutMs;
        this.expectFinal = b.expectFinal;
        this.browserProfile = b.browserProfile;
        this.json = b.json;
        this.cmd = b.cmd;
        this.profileName = b.profileName;
        this.profileColor = b.profileColor;
        this.profileDriver = b.profileDriver;
        this.profileCdpUrl = b.profileCdpUrl;
        this.profileUserDataDir = b.profileUserDataDir;
        this.tabIndex = b.tabIndex;
        this.openUrl = b.openUrl;
        this.targetId = b.targetId;
        this.ref = b.ref;
        this.text = b.text;
        this.key = b.key;
        this.refEnd = b.refEnd;
        this.selectValues = b.selectValues == null ? List.of() : List.copyOf(b.selectValues);
        this.fieldsJson = b.fieldsJson;
        this.waitText = b.waitText;
        this.evaluateFn = b.evaluateFn;
        this.evaluateRef = b.evaluateRef;
        this.uploadPath = b.uploadPath;
        this.downloadFilename = b.downloadFilename;
        this.resizeW = b.resizeW;
        this.resizeH = b.resizeH;
        this.offlineState = b.offlineState;
        this.media = b.media;
        this.timezone = b.timezone;
        this.locale = b.locale;
        this.geoLat = b.geoLat;
        this.geoLon = b.geoLon;
        this.geoAccuracy = b.geoAccuracy;
        this.deviceName = b.deviceName;
        this.headersJson = b.headersJson;
        this.credUser = b.credUser;
        this.credPass = b.credPass;
        this.cookieName = b.cookieName;
        this.cookieValue = b.cookieValue;
        this.cookieUrl = b.cookieUrl;
        this.storageKey = b.storageKey;
        this.storageValue = b.storageValue;
        this.consoleLevel = b.consoleLevel;
        this.responsePattern = b.responsePattern;
        this.errorsClear = b.errorsClear;
        this.requestsFilter = b.requestsFilter;
        this.traceOutZip = b.traceOutZip;
        this.screenshotFullPage = b.screenshotFullPage;
        this.screenshotRef = b.screenshotRef;
        this.extra = b.extra == null ? List.of() : List.copyOf(b.extra);
    }

    /**
     * @return 新 {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toSubcommandArguments() {
        List<String> out = new ArrayList<>();
        OpenClawCliArgv.addIfPresent(out, "--url", gatewayUrl);
        OpenClawCliArgv.addIfPresent(out, "--token", gatewayToken);
        OpenClawCliArgv.addIfPositive(out, "--timeout", timeoutMs);
        OpenClawCliArgv.addFlag(out, "--expect-final", expectFinal);
        OpenClawCliArgv.addIfPresent(out, "--browser-profile", browserProfile);
        OpenClawCliArgv.addFlag(out, "--json", json);

        switch (cmd) {
            case NONE:
                break;
            case STATUS:
                out.add("status");
                break;
            case START:
                out.add("start");
                break;
            case STOP:
                out.add("stop");
                break;
            case RESET_PROFILE:
                out.add("reset-profile");
                break;
            case PROFILES:
                out.add("profiles");
                break;
            case CREATE_PROFILE:
                out.add("create-profile");
                OpenClawCliArgv.addIfPresent(out, "--name", profileName);
                OpenClawCliArgv.addIfPresent(out, "--color", profileColor);
                OpenClawCliArgv.addIfPresent(out, "--driver", profileDriver);
                OpenClawCliArgv.addIfPresent(out, "--cdp-url", profileCdpUrl);
                OpenClawCliArgv.addIfPresent(out, "--user-data-dir", profileUserDataDir);
                break;
            case DELETE_PROFILE:
                out.add("delete-profile");
                OpenClawCliArgv.addIfPresent(out, "--name", profileName);
                break;
            case TABS:
                out.add("tabs");
                break;
            case TAB_NEW:
                out.add("tab");
                out.add("new");
                break;
            case TAB_SELECT:
                out.add("tab");
                out.add("select");
                if (tabIndex != null) {
                    out.add(Integer.toString(tabIndex));
                }
                break;
            case TAB_CLOSE:
                out.add("tab");
                out.add("close");
                if (tabIndex != null) {
                    out.add(Integer.toString(tabIndex));
                }
                break;
            case OPEN:
                out.add("open");
                if (openUrl != null && !openUrl.isBlank()) {
                    out.add(openUrl.trim());
                }
                break;
            case FOCUS:
                out.add("focus");
                if (targetId != null && !targetId.isBlank()) {
                    out.add(targetId.trim());
                }
                break;
            case CLOSE:
                out.add("close");
                if (targetId != null && !targetId.isBlank()) {
                    out.add(targetId.trim());
                }
                break;
            case SNAPSHOT:
                out.add("snapshot");
                break;
            case SCREENSHOT:
                out.add("screenshot");
                OpenClawCliArgv.addFlag(out, "--full-page", screenshotFullPage);
                OpenClawCliArgv.addIfPresent(out, "--ref", screenshotRef);
                break;
            case NAVIGATE:
                out.add("navigate");
                if (openUrl != null && !openUrl.isBlank()) {
                    out.add(openUrl.trim());
                }
                break;
            case CLICK:
                out.add("click");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                break;
            case TYPE:
                out.add("type");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                if (text != null) {
                    out.add(text);
                }
                break;
            case PRESS:
                out.add("press");
                if (key != null && !key.isBlank()) {
                    out.add(key.trim());
                }
                break;
            case HOVER:
                out.add("hover");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                break;
            case SCROLL_INTO_VIEW:
                out.add("scrollintoview");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                break;
            case DRAG:
                out.add("drag");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                if (refEnd != null && !refEnd.isBlank()) {
                    out.add(refEnd.trim());
                }
                break;
            case SELECT:
                out.add("select");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                for (String v : selectValues) {
                    if (v != null && !v.isBlank()) {
                        out.add(v.trim());
                    }
                }
                break;
            case FILL:
                out.add("fill");
                OpenClawCliArgv.addIfPresent(out, "--fields", fieldsJson);
                break;
            case WAIT:
                out.add("wait");
                OpenClawCliArgv.addIfPresent(out, "--text", waitText);
                break;
            case EVALUATE:
                out.add("evaluate");
                OpenClawCliArgv.addIfPresent(out, "--fn", evaluateFn);
                OpenClawCliArgv.addIfPresent(out, "--ref", evaluateRef);
                break;
            case UPLOAD:
                out.add("upload");
                if (uploadPath != null && !uploadPath.isBlank()) {
                    out.add(uploadPath.trim());
                }
                OpenClawCliArgv.addIfPresent(out, "--ref", ref);
                break;
            case WAIT_FOR_DOWNLOAD:
                out.add("waitfordownload");
                break;
            case DOWNLOAD:
                out.add("download");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                if (downloadFilename != null && !downloadFilename.isBlank()) {
                    out.add(downloadFilename.trim());
                }
                break;
            case DIALOG_ACCEPT:
                out.add("dialog");
                out.add("--accept");
                break;
            case RESIZE:
                out.add("resize");
                if (resizeW != null && resizeH != null) {
                    out.add(Integer.toString(resizeW));
                    out.add(Integer.toString(resizeH));
                }
                break;
            case SET_VIEWPORT:
                out.add("set");
                out.add("viewport");
                if (resizeW != null && resizeH != null) {
                    out.add(Integer.toString(resizeW));
                    out.add(Integer.toString(resizeH));
                }
                break;
            case SET_OFFLINE:
                out.add("set");
                out.add("offline");
                if (offlineState != null && !offlineState.isBlank()) {
                    out.add(offlineState.trim());
                }
                break;
            case SET_MEDIA:
                out.add("set");
                out.add("media");
                if (media != null && !media.isBlank()) {
                    out.add(media.trim());
                }
                break;
            case SET_TIMEZONE:
                out.add("set");
                out.add("timezone");
                if (timezone != null && !timezone.isBlank()) {
                    out.add(timezone.trim());
                }
                break;
            case SET_LOCALE:
                out.add("set");
                out.add("locale");
                if (locale != null && !locale.isBlank()) {
                    out.add(locale.trim());
                }
                break;
            case SET_GEO:
                out.add("set");
                out.add("geo");
                if (geoLat != null && geoLon != null) {
                    out.add(Double.toString(geoLat));
                    out.add(Double.toString(geoLon));
                }
                OpenClawCliArgv.addIfNotNull(out, "--accuracy", geoAccuracy);
                break;
            case SET_DEVICE:
                out.add("set");
                out.add("device");
                if (deviceName != null && !deviceName.isBlank()) {
                    out.add(deviceName.trim());
                }
                break;
            case SET_HEADERS:
                out.add("set");
                out.add("headers");
                if (headersJson != null && !headersJson.isBlank()) {
                    out.add(headersJson.trim());
                }
                break;
            case SET_CREDENTIALS:
                out.add("set");
                out.add("credentials");
                if (credUser != null && !credUser.isBlank()) {
                    out.add(credUser.trim());
                }
                if (credPass != null) {
                    out.add(credPass);
                }
                break;
            case COOKIES:
                out.add("cookies");
                break;
            case COOKIES_SET:
                out.add("cookies");
                out.add("set");
                if (cookieName != null && !cookieName.isBlank()) {
                    out.add(cookieName.trim());
                }
                if (cookieValue != null && !cookieValue.isBlank()) {
                    out.add(cookieValue.trim());
                }
                OpenClawCliArgv.addIfPresent(out, "--url", cookieUrl);
                break;
            case COOKIES_CLEAR:
                out.add("cookies");
                out.add("clear");
                break;
            case STORAGE_LOCAL_GET:
                out.add("storage");
                out.add("local");
                out.add("get");
                break;
            case STORAGE_LOCAL_SET:
                out.add("storage");
                out.add("local");
                out.add("set");
                if (storageKey != null && !storageKey.isBlank()) {
                    out.add(storageKey.trim());
                }
                if (storageValue != null && !storageValue.isBlank()) {
                    out.add(storageValue.trim());
                }
                break;
            case STORAGE_SESSION_CLEAR:
                out.add("storage");
                out.add("session");
                out.add("clear");
                break;
            case CONSOLE:
                out.add("console");
                OpenClawCliArgv.addIfPresent(out, "--level", consoleLevel);
                break;
            case PDF:
                out.add("pdf");
                break;
            case RESPONSE_BODY:
                out.add("responsebody");
                if (responsePattern != null && !responsePattern.isBlank()) {
                    out.add(responsePattern.trim());
                }
                break;
            case HIGHLIGHT:
                out.add("highlight");
                if (ref != null && !ref.isBlank()) {
                    out.add(ref.trim());
                }
                break;
            case ERRORS:
                out.add("errors");
                OpenClawCliArgv.addFlag(out, "--clear", errorsClear);
                break;
            case REQUESTS:
                out.add("requests");
                OpenClawCliArgv.addIfPresent(out, "--filter", requestsFilter);
                break;
            case TRACE_START:
                out.add("trace");
                out.add("start");
                break;
            case TRACE_STOP:
                out.add("trace");
                out.add("stop");
                OpenClawCliArgv.addIfPresent(out, "--out", traceOutZip);
                break;
            default:
                break;
        }
        OpenClawCliArgv.addExtra(out, extra);
        return Collections.unmodifiableList(out);
    }

    /**
     * {@link BrowserOptions} 构建器。
     */
    public static final class Builder {
        private Cmd cmd = Cmd.NONE;
        private String gatewayUrl;
        private String gatewayToken;
        private int timeoutMs;
        private boolean expectFinal;
        private String browserProfile;
        private boolean json;
        private String profileName;
        private String profileColor;
        private String profileDriver;
        private String profileCdpUrl;
        private String profileUserDataDir;
        private Integer tabIndex;
        private String openUrl;
        private String targetId;
        private String ref;
        private String text;
        private String key;
        private String refEnd;
        private List<String> selectValues = new ArrayList<>();
        private String fieldsJson;
        private String waitText;
        private String evaluateFn;
        private String evaluateRef;
        private String uploadPath;
        private String downloadFilename;
        private Integer resizeW;
        private Integer resizeH;
        private String offlineState;
        private String media;
        private String timezone;
        private String locale;
        private Double geoLat;
        private Double geoLon;
        private Integer geoAccuracy;
        private String deviceName;
        private String headersJson;
        private String credUser;
        private String credPass;
        private String cookieName;
        private String cookieValue;
        private String cookieUrl;
        private String storageKey;
        private String storageValue;
        private String consoleLevel;
        private String responsePattern;
        private boolean errorsClear;
        private String requestsFilter;
        private String traceOutZip;
        private boolean screenshotFullPage;
        private String screenshotRef;
        private List<String> extra = new ArrayList<>();

        private void resetCmd() {
            profileName = null;
            profileColor = null;
            profileDriver = null;
            profileCdpUrl = null;
            profileUserDataDir = null;
            tabIndex = null;
            openUrl = null;
            targetId = null;
            ref = null;
            text = null;
            key = null;
            refEnd = null;
            selectValues.clear();
            fieldsJson = null;
            waitText = null;
            evaluateFn = null;
            evaluateRef = null;
            uploadPath = null;
            downloadFilename = null;
            resizeW = null;
            resizeH = null;
            offlineState = null;
            media = null;
            timezone = null;
            locale = null;
            geoLat = null;
            geoLon = null;
            geoAccuracy = null;
            deviceName = null;
            headersJson = null;
            credUser = null;
            credPass = null;
            cookieName = null;
            cookieValue = null;
            cookieUrl = null;
            storageKey = null;
            storageValue = null;
            consoleLevel = null;
            responsePattern = null;
            errorsClear = false;
            requestsFilter = null;
            traceOutZip = null;
            screenshotFullPage = false;
            screenshotRef = null;
        }

        /**
         * @param url {@code --url}
         * @return {@code this}
         */
        public Builder gatewayUrl(String url) {
            this.gatewayUrl = url;
            return this;
        }

        /**
         * @param token {@code --token}
         * @return {@code this}
         */
        public Builder gatewayToken(String token) {
            this.gatewayToken = token;
            return this;
        }

        /**
         * @param timeoutMs {@code --timeout}（毫秒）
         * @return {@code this}
         */
        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        /**
         * @param expectFinal {@code --expect-final}
         * @return {@code this}
         */
        public Builder expectFinal(boolean expectFinal) {
            this.expectFinal = expectFinal;
            return this;
        }

        /**
         * @param profile {@code --browser-profile}
         * @return {@code this}
         */
        public Builder browserProfile(String profile) {
            this.browserProfile = profile;
            return this;
        }

        /**
         * @param json {@code --json}
         * @return {@code this}
         */
        public Builder json(boolean json) {
            this.json = json;
            return this;
        }

        /**
         * @return {@code this}（{@code browser status}）
         */
        public Builder status() {
            resetCmd();
            this.cmd = Cmd.STATUS;
            return this;
        }

        /**
         * @return {@code this}（{@code browser start}）
         */
        public Builder start() {
            resetCmd();
            this.cmd = Cmd.START;
            return this;
        }

        /**
         * @return {@code this}（{@code browser stop}）
         */
        public Builder stop() {
            resetCmd();
            this.cmd = Cmd.STOP;
            return this;
        }

        /**
         * @return {@code this}（{@code browser reset-profile}）
         */
        public Builder resetProfile() {
            resetCmd();
            this.cmd = Cmd.RESET_PROFILE;
            return this;
        }

        /**
         * @return {@code this}（{@code browser profiles}）
         */
        public Builder profiles() {
            resetCmd();
            this.cmd = Cmd.PROFILES;
            return this;
        }

        /**
         * @param name create-profile：{@code --name}
         * @return {@code this}
         */
        public Builder createProfile(String name) {
            resetCmd();
            this.cmd = Cmd.CREATE_PROFILE;
            this.profileName = name;
            return this;
        }

        /**
         * @param color create-profile：{@code --color}
         * @return {@code this}
         */
        public Builder profileColor(String color) {
            this.profileColor = color;
            return this;
        }

        /**
         * @param driver create-profile：{@code --driver}
         * @return {@code this}
         */
        public Builder profileDriver(String driver) {
            this.profileDriver = driver;
            return this;
        }

        /**
         * @param cdpUrl create-profile：{@code --cdp-url}
         * @return {@code this}
         */
        public Builder profileCdpUrl(String cdpUrl) {
            this.profileCdpUrl = cdpUrl;
            return this;
        }

        /**
         * @param userDataDir create-profile：{@code --user-data-dir}
         * @return {@code this}
         */
        public Builder profileUserDataDir(String userDataDir) {
            this.profileUserDataDir = userDataDir;
            return this;
        }

        /**
         * @param name delete-profile：{@code --name}
         * @return {@code this}
         */
        public Builder deleteProfile(String name) {
            resetCmd();
            this.cmd = Cmd.DELETE_PROFILE;
            this.profileName = name;
            return this;
        }

        /**
         * @return {@code this}（{@code browser tabs}）
         */
        public Builder tabs() {
            resetCmd();
            this.cmd = Cmd.TABS;
            return this;
        }

        /**
         * @return {@code this}（{@code browser tab new}）
         */
        public Builder tabNew() {
            resetCmd();
            this.cmd = Cmd.TAB_NEW;
            return this;
        }

        /**
         * @param index 标签索引
         * @return {@code this}
         */
        public Builder tabSelect(int index) {
            resetCmd();
            this.cmd = Cmd.TAB_SELECT;
            this.tabIndex = index;
            return this;
        }

        /**
         * @param index 标签索引
         * @return {@code this}
         */
        public Builder tabClose(int index) {
            resetCmd();
            this.cmd = Cmd.TAB_CLOSE;
            this.tabIndex = index;
            return this;
        }

        /**
         * @param url 打开 URL
         * @return {@code this}
         */
        public Builder open(String url) {
            resetCmd();
            this.cmd = Cmd.OPEN;
            this.openUrl = url;
            return this;
        }

        /**
         * @param targetId target ID
         * @return {@code this}
         */
        public Builder focus(String targetId) {
            resetCmd();
            this.cmd = Cmd.FOCUS;
            this.targetId = targetId;
            return this;
        }

        /**
         * @param targetId target ID
         * @return {@code this}
         */
        public Builder closeTarget(String targetId) {
            resetCmd();
            this.cmd = Cmd.CLOSE;
            this.targetId = targetId;
            return this;
        }

        /**
         * @return {@code this}（{@code browser snapshot}）
         */
        public Builder snapshot() {
            resetCmd();
            this.cmd = Cmd.SNAPSHOT;
            return this;
        }

        /**
         * @param fullPage {@code --full-page}
         * @param ref {@code --ref}
         * @return {@code this}
         */
        public Builder screenshot(boolean fullPage, String ref) {
            resetCmd();
            this.cmd = Cmd.SCREENSHOT;
            this.screenshotFullPage = fullPage;
            this.screenshotRef = ref;
            return this;
        }

        /**
         * @param url 导航 URL
         * @return {@code this}
         */
        public Builder navigate(String url) {
            resetCmd();
            this.cmd = Cmd.NAVIGATE;
            this.openUrl = url;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @return {@code this}
         */
        public Builder click(String ref) {
            resetCmd();
            this.cmd = Cmd.CLICK;
            this.ref = ref;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @param text 输入文本
         * @return {@code this}
         */
        public Builder type(String ref, String text) {
            resetCmd();
            this.cmd = Cmd.TYPE;
            this.ref = ref;
            this.text = text;
            return this;
        }

        /**
         * @param key 按键
         * @return {@code this}
         */
        public Builder press(String key) {
            resetCmd();
            this.cmd = Cmd.PRESS;
            this.key = key;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @return {@code this}
         */
        public Builder hover(String ref) {
            resetCmd();
            this.cmd = Cmd.HOVER;
            this.ref = ref;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @return {@code this}
         */
        public Builder scrollIntoView(String ref) {
            resetCmd();
            this.cmd = Cmd.SCROLL_INTO_VIEW;
            this.ref = ref;
            return this;
        }

        /**
         * @param startRef 起点 ref
         * @param endRef 终点 ref
         * @return {@code this}
         */
        public Builder drag(String startRef, String endRef) {
            resetCmd();
            this.cmd = Cmd.DRAG;
            this.ref = startRef;
            this.refEnd = endRef;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @param options 选项值
         * @return {@code this}
         */
        public Builder select(String ref, String... options) {
            resetCmd();
            this.cmd = Cmd.SELECT;
            this.ref = ref;
            if (options != null) {
                Collections.addAll(selectValues, options);
            }
            return this;
        }

        /**
         * @param json fill：{@code --fields} JSON
         * @return {@code this}
         */
        public Builder fillFieldsJson(String json) {
            resetCmd();
            this.cmd = Cmd.FILL;
            this.fieldsJson = json;
            return this;
        }

        /**
         * @param text wait：{@code --text}
         * @return {@code this}
         */
        public Builder waitForText(String text) {
            resetCmd();
            this.cmd = Cmd.WAIT;
            this.waitText = text;
            return this;
        }

        /**
         * @param fn evaluate：{@code --fn}
         * @param ref evaluate：{@code --ref}
         * @return {@code this}
         */
        public Builder evaluate(String fn, String ref) {
            resetCmd();
            this.cmd = Cmd.EVALUATE;
            this.evaluateFn = fn;
            this.evaluateRef = ref;
            return this;
        }

        /**
         * @param localPath 本地文件路径
         * @param ref 元素 ref
         * @return {@code this}
         */
        public Builder upload(String localPath, String ref) {
            resetCmd();
            this.cmd = Cmd.UPLOAD;
            this.uploadPath = localPath;
            this.ref = ref;
            return this;
        }

        /**
         * @return {@code this}（{@code browser waitfordownload}）
         */
        public Builder waitForDownload() {
            resetCmd();
            this.cmd = Cmd.WAIT_FOR_DOWNLOAD;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @param filename 下载文件名
         * @return {@code this}
         */
        public Builder download(String ref, String filename) {
            resetCmd();
            this.cmd = Cmd.DOWNLOAD;
            this.ref = ref;
            this.downloadFilename = filename;
            return this;
        }

        /**
         * {@code openclaw browser dialog --accept}
         *
         * @return {@code this}
         */
        public Builder dialogAccept() {
            resetCmd();
            this.cmd = Cmd.DIALOG_ACCEPT;
            return this;
        }

        /**
         * @param w 宽度
         * @param h 高度
         * @return {@code this}
         */
        public Builder resize(int w, int h) {
            resetCmd();
            this.cmd = Cmd.RESIZE;
            this.resizeW = w;
            this.resizeH = h;
            return this;
        }

        /**
         * @param w 视口宽
         * @param h 视口高
         * @return {@code this}
         */
        public Builder setViewport(int w, int h) {
            resetCmd();
            this.cmd = Cmd.SET_VIEWPORT;
            this.resizeW = w;
            this.resizeH = h;
            return this;
        }

        /**
         * @param onOrOff offline 状态
         * @return {@code this}
         */
        public Builder setOffline(String onOrOff) {
            resetCmd();
            this.cmd = Cmd.SET_OFFLINE;
            this.offlineState = onOrOff;
            return this;
        }

        /**
         * @param media media 值
         * @return {@code this}
         */
        public Builder setMedia(String media) {
            resetCmd();
            this.cmd = Cmd.SET_MEDIA;
            this.media = media;
            return this;
        }

        /**
         * @param tz 时区
         * @return {@code this}
         */
        public Builder setTimezone(String tz) {
            resetCmd();
            this.cmd = Cmd.SET_TIMEZONE;
            this.timezone = tz;
            return this;
        }

        /**
         * @param locale 区域
         * @return {@code this}
         */
        public Builder setLocale(String locale) {
            resetCmd();
            this.cmd = Cmd.SET_LOCALE;
            this.locale = locale;
            return this;
        }

        /**
         * @param lat 纬度
         * @param lon 经度
         * @param accuracyMeters {@code --accuracy}（可为 null）
         * @return {@code this}
         */
        public Builder setGeo(double lat, double lon, Integer accuracyMeters) {
            resetCmd();
            this.cmd = Cmd.SET_GEO;
            this.geoLat = lat;
            this.geoLon = lon;
            this.geoAccuracy = accuracyMeters;
            return this;
        }

        /**
         * @param device 设备名
         * @return {@code this}
         */
        public Builder setDevice(String device) {
            resetCmd();
            this.cmd = Cmd.SET_DEVICE;
            this.deviceName = device;
            return this;
        }

        /**
         * @param json headers JSON
         * @return {@code this}
         */
        public Builder setHeadersJson(String json) {
            resetCmd();
            this.cmd = Cmd.SET_HEADERS;
            this.headersJson = json;
            return this;
        }

        /**
         * @param user 用户名
         * @param pass 密码
         * @return {@code this}
         */
        public Builder setCredentials(String user, String pass) {
            resetCmd();
            this.cmd = Cmd.SET_CREDENTIALS;
            this.credUser = user;
            this.credPass = pass;
            return this;
        }

        /**
         * @return {@code this}（{@code browser cookies}）
         */
        public Builder cookies() {
            resetCmd();
            this.cmd = Cmd.COOKIES;
            return this;
        }

        /**
         * @param name cookie 名
         * @param value cookie 值
         * @param forUrl {@code --url}
         * @return {@code this}
         */
        public Builder cookiesSet(String name, String value, String forUrl) {
            resetCmd();
            this.cmd = Cmd.COOKIES_SET;
            this.cookieName = name;
            this.cookieValue = value;
            this.cookieUrl = forUrl;
            return this;
        }

        /**
         * @return {@code this}（{@code browser cookies clear}）
         */
        public Builder cookiesClear() {
            resetCmd();
            this.cmd = Cmd.COOKIES_CLEAR;
            return this;
        }

        /**
         * @return {@code this}（storage local get）
         */
        public Builder storageLocalGet() {
            resetCmd();
            this.cmd = Cmd.STORAGE_LOCAL_GET;
            return this;
        }

        /**
         * @param key 键
         * @param value 值
         * @return {@code this}
         */
        public Builder storageLocalSet(String key, String value) {
            resetCmd();
            this.cmd = Cmd.STORAGE_LOCAL_SET;
            this.storageKey = key;
            this.storageValue = value;
            return this;
        }

        /**
         * @return {@code this}（storage session clear）
         */
        public Builder storageSessionClear() {
            resetCmd();
            this.cmd = Cmd.STORAGE_SESSION_CLEAR;
            return this;
        }

        /**
         * @param level {@code --level}
         * @return {@code this}
         */
        public Builder console(String level) {
            resetCmd();
            this.cmd = Cmd.CONSOLE;
            this.consoleLevel = level;
            return this;
        }

        /**
         * @return {@code this}（{@code browser pdf}）
         */
        public Builder pdf() {
            resetCmd();
            this.cmd = Cmd.PDF;
            return this;
        }

        /**
         * @param pattern responsebody 匹配模式
         * @return {@code this}
         */
        public Builder responseBody(String pattern) {
            resetCmd();
            this.cmd = Cmd.RESPONSE_BODY;
            this.responsePattern = pattern;
            return this;
        }

        /**
         * @param ref 元素 ref
         * @return {@code this}
         */
        public Builder highlight(String ref) {
            resetCmd();
            this.cmd = Cmd.HIGHLIGHT;
            this.ref = ref;
            return this;
        }

        /**
         * @param clear {@code --clear}
         * @return {@code this}
         */
        public Builder errors(boolean clear) {
            resetCmd();
            this.cmd = Cmd.ERRORS;
            this.errorsClear = clear;
            return this;
        }

        /**
         * @param filter {@code --filter}
         * @return {@code this}
         */
        public Builder requests(String filter) {
            resetCmd();
            this.cmd = Cmd.REQUESTS;
            this.requestsFilter = filter;
            return this;
        }

        /**
         * @return {@code this}（{@code browser trace start}）
         */
        public Builder traceStart() {
            resetCmd();
            this.cmd = Cmd.TRACE_START;
            return this;
        }

        /**
         * @param outZip trace stop：{@code --out}
         * @return {@code this}
         */
        public Builder traceStop(String outZip) {
            resetCmd();
            this.cmd = Cmd.TRACE_STOP;
            this.traceOutZip = outZip;
            return this;
        }

        /**
         * 追加额外 argv token。
         *
         * @param tokens 可为 null（忽略）
         * @return {@code this}
         */
        public Builder extra(String... tokens) {
            if (tokens != null) {
                Collections.addAll(extra, tokens);
            }
            return this;
        }

        /**
         * @return 不可变 {@link BrowserOptions}
         */
        public BrowserOptions build() {
            return new BrowserOptions(this);
        }
    }
}
