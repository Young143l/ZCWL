import { useState, type FC } from "react";
import { Button, Card, theme, ConfigProvider } from "antd";
import {
    LeftOutlined,
    RightOutlined,
    QuestionCircleOutlined,
    EyeOutlined,
    EyeInvisibleOutlined,
} from "@ant-design/icons";
import useIsDark from "../status/IsDark_status";

// 模拟问答数据
interface QAData {
    question: string;
    answer: string;
}

const qaDatabase: Record<string, QAData> = {
    "2026-04-08": {
        question: "什么是 React 的虚拟 DOM？",
        answer:
            "虚拟 DOM（Virtual DOM）是 React 中的一种编程概念，它是真实 DOM 的轻量级副本。当数据发生变化时，React 会先比较虚拟 DOM 的差异（Diff 算法），然后只更新需要变化的真实 DOM 部分，而不是重新渲染整个页面。这种方式大大提高了应用的性能。",
    },
    "2026-04-09": {
        question: "TypeScript 中的 interface 和 type 有什么区别？",
        answer:
            "interface 和 type 都可以用来定义对象类型，主要区别包括：1. interface 可以声明合并（同名 interface 会自动合并），type 不行；2. type 可以定义基本类型、联合类型、元组等，interface 主要用于对象；3. interface 支持 extends 继承，type 使用 & 进行交叉类型。",
    },
    "2026-04-10": {
        question: "什么是闭包（Closure）？",
        answer:
            "闭包是指有权访问另一个函数作用域中的变量的函数。创建闭包的常见方式是在一个函数内部创建另一个函数。闭包可以让你在一个内层函数中访问到其外层函数的作用域，即使外层函数已经执行完毕。闭包常用于数据封装、模块化、柯里化等场景。",
    },
    "2026-04-11": {
        question: "CSS 中的 Flexbox 布局有哪些常用属性？",
        answer:
            "Flexbox 常用属性包括：容器属性：display: flex、flex-direction（主轴方向）、justify-content（主轴对齐）、align-items（交叉轴对齐）、flex-wrap（换行）、gap（间距）；项目属性：flex-grow（放大比例）、flex-shrink（缩小比例）、flex-basis（基础大小）、align-self（单独对齐）。",
    },
    "2026-04-12": {
        question: "什么是 HTTP 的 RESTful API？",
        answer:
            "RESTful API 是一种基于 HTTP 协议的 API 设计风格，它使用 HTTP 方法（GET、POST、PUT、DELETE 等）对资源进行操作。REST 强调无状态、统一接口、资源标识等原则，使得 API 设计更加规范、易于理解和维护。",
    },
    "2026-04-13": {
        question: "JavaScript 中的 Promise 是什么？",
        answer:
            "Promise 是 JavaScript 中处理异步操作的对象，代表一个尚未完成但预期将来会完成的操作。它有三种状态：pending（进行中）、fulfilled（已成功）、rejected（已失败）。Promise 提供了 .then()、.catch()、.finally() 等方法来处理异步结果，避免了回调地狱问题。",
    },
    "2026-04-14": {
        question: "什么是 JavaScript 的事件循环（Event Loop）？",
        answer:
            "事件循环是 JavaScript 实现异步编程的机制。它不断检查调用栈是否为空，如果为空则从任务队列中取出回调函数执行。JavaScript 是单线程的，通过事件循环可以非阻塞地处理异步操作，如定时器、网络请求、DOM 事件等。",
    },
    "2026-04-15": {
        question: "React 中的 useEffect 钩子有什么作用？",
        answer:
            "useEffect 是 React 中用于处理副作用的 Hook，可以在函数组件中执行数据获取、订阅、手动修改 DOM 等操作。它接收两个参数：一个执行副作用的函数和一个依赖数组。当依赖项变化时，副作用函数会重新执行。可以返回一个清理函数来清理副作用。",
    },
    "2026-04-16": {
        question: "什么是 CSS 的盒模型？",
        answer:
            "CSS 盒模型是网页布局的基础，每个元素都被视为一个矩形盒子，由 content（内容）、padding（内边距）、border（边框）、margin（外边距）四部分组成。标准盒模型中，width/height 只包含 content，而 IE 盒模型中 width/height 包含 content + padding + border。",
    },
    "2026-04-17": {
        question: "Git 中的 merge 和 rebase 有什么区别？",
        answer:
            "merge 和 rebase 都是 Git 中整合分支的方法。merge 会创建一个新的合并提交，保留完整的历史记录和分支结构；rebase 会将当前分支的提交重新应用到目标分支上，形成线性历史。merge 更安全但历史较乱，rebase 历史整洁但会改变提交历史。",
    },
    "2026-04-18": {
        question: "什么是 JavaScript 的原型链？",
        answer:
            "原型链是 JavaScript 实现继承的机制。每个对象都有一个内部属性 __proto__ 指向其原型对象，原型对象也有自己的原型，形成链式结构。当访问对象属性时，如果对象本身没有该属性，会沿着原型链向上查找，直到找到或到达原型链顶端（null）。",
    },
    "2026-04-19": {
        question: "React 中的 useState 是如何工作的？",
        answer:
            "useState 是 React 中用于在函数组件中添加状态的 Hook。它接收初始状态值，返回一个数组 [state, setState]。React 会在每次渲染时保持状态，setState 函数用于更新状态并触发重新渲染。状态更新可能是异步的，多次调用 setState 可能会被合并。",
    },
    "2026-04-20": {
        question: "什么是跨域（CORS）？如何解决？",
        answer:
            "跨域（Cross-Origin Resource Sharing）是浏览器安全策略，限制不同源（协议、域名、端口不同）的网页互相访问资源。解决方法包括：后端设置 CORS 响应头、使用 JSONP、配置代理服务器、使用 WebSocket 等。开发环境常用代理方式解决。",
    },
    "2026-04-21": {
        question: "什么是防抖（debounce）和节流（throttle）？",
        answer:
            "防抖和节流是优化高频触发事件的两种技术。防抖是在事件停止触发后的一段时间才执行，适用于搜索框输入等场景；节流是在固定时间间隔内只执行一次，适用于滚动监听等场景。两者都能减少函数执行次数，提升性能。",
    },
    "2026-04-22": {
        question: "Node.js 中的 CommonJS 和 ES Module 有什么区别？",
        answer:
            "CommonJS 是 Node.js 传统模块系统，使用 require/module.exports，运行时同步加载；ES Module 是 ES6 标准模块系统，使用 import/export，编译时静态分析，支持 Tree Shaking。Node.js 12+ 支持 ES Module，但配置和使用上有差异。",
    },
    "2026-04-23": {
        question: "什么是 Webpack 的 Loader 和 Plugin？",
        answer:
            "Loader 用于转换模块源代码，如 babel-loader 转换 ES6+，css-loader 处理 CSS；Plugin 用于扩展 Webpack 功能，如 HtmlWebpackPlugin 生成 HTML，CleanWebpackPlugin 清理输出目录。Loader 在模块加载时工作，Plugin 在整个构建生命周期中工作。",
    },
    "2026-04-24": {
        question: "React 中 keys 的作用是什么？",
        answer:
            "keys 用于帮助 React 识别列表中的每个元素，优化 diff 算法性能。当列表重新渲染时，React 通过 keys 判断元素是新增、删除还是移动，而不是销毁重建。keys 应该在列表中保持唯一且稳定，避免使用数组索引作为 key，特别是列表项可能变化时。",
    },
    "2026-04-25": {
        question: "什么是浏览器缓存？有哪些类型？",
        answer:
            "浏览器缓存用于存储网页资源，减少网络请求提升加载速度。主要分为：强缓存（Expires、Cache-Control，不请求服务器）、协商缓存（Last-Modified/If-Modified-Since、ETag/If-None-Match，询问服务器是否变化）、Service Worker 缓存等。",
    },
    "2026-04-26": {
        question: "什么是 CSS 的 BEM 命名规范？",
        answer:
            "BEM（Block Element Modifier）是 CSS 类命名规范，使样式更易维护。Block（块）是独立组件，如 .button；Element（元素）是块的组成部分，如 .button__icon；Modifier（修饰符）表示状态或变体，如 .button--primary。命名清晰，避免样式冲突。",
    },
    "2026-04-27": {
        question: "JavaScript 中 == 和 === 有什么区别？",
        answer:
            "== 是宽松相等，会进行类型转换后再比较；=== 是严格相等，要求值和类型都相同才相等。例如 1 == '1' 为 true，1 === '1' 为 false。推荐使用 === 避免类型转换带来的意外结果，代码更清晰可靠。",
    },
    "2026-04-28": {
        question: "什么是 React 的 Context API？",
        answer:
            "Context API 是 React 中跨层级传递数据的方法，避免 props drilling（层层传递 props）。通过 React.createContext 创建上下文，Provider 组件提供数据，Consumer 或 useContext Hook 消费数据。适合主题、用户认证等全局状态共享。",
    },
    "2026-04-29": {
        question: "什么是 HTTPS？为什么需要它？",
        answer:
            "HTTPS 是 HTTP 的安全版本，通过 SSL/TLS 协议加密传输数据。它提供：1. 数据加密，防止窃听；2. 身份认证，验证服务器身份；3. 数据完整性，防止篡改。HTTPS 使用 443 端口，需要 SSL 证书，现代网站必须使用 HTTPS 保障安全。",
    },
    "2026-04-30": {
        question: "什么是前端路由？React Router 如何实现？",
        answer:
            "前端路由是不刷新页面实现页面切换的技术，通过监听 URL 变化渲染不同组件。React Router 提供 BrowserRouter（history API）、HashRouter（hash 模式）等组件，使用 Route 定义路径映射，Link/NavLink 实现导航，useParams 获取路由参数。",
    },
    "2026-05-01": {
        question: "什么是 OAuth 2.0 授权协议？",
        answer:
            "OAuth 2.0 是开放授权协议，允许第三方应用在不获取用户密码的情况下访问用户资源。流程包括：授权请求、用户同意、获取授权码、换取访问令牌。常用于第三方登录（微信、GitHub 登录），安全且用户体验好。",
    },
    "2026-05-02": {
        question: "什么是 Webpack 的热模块替换（HMR）？",
        answer:
            "HMR（Hot Module Replacement）是 Webpack 功能，允许在应用运行时替换、添加、删除模块而无需刷新页面。开发时修改代码后，只更新变化的模块，保持应用状态，大幅提升开发效率。需要配置 devServer.hot 和相应 loader。",
    },
    "2026-05-03": {
        question: "CSS 中的 position 属性有哪些值？",
        answer:
            "position 定义元素定位方式：static（默认，正常文档流）、relative（相对定位，相对于自身原位置）、absolute（绝对定位，相对于最近的非 static 祖先元素）、fixed（固定定位，相对于视口）、sticky（粘性定位，相对和固定的混合，滚动到阈值时固定）。",
    },
    "2026-05-04": {
        question: "什么是 JavaScript 的垃圾回收机制？",
        answer:
            "垃圾回收是 JavaScript 自动内存管理机制，主要使用标记清除算法：定期遍历内存，标记活动对象，清除未标记对象。现代引擎还使用引用计数、分代回收等优化。开发者应注意避免内存泄漏，如及时清除定时器、事件监听器，避免循环引用。",
    },
    "2026-05-05": {
        question: "React 中的 useMemo 和 useCallback 有什么区别？",
        answer:
            "useMemo 缓存计算结果，避免重复复杂计算；useCallback 缓存函数引用，避免子组件不必要的重渲染。useMemo 返回计算值，useCallback 返回记忆化函数。两者都接收依赖数组，只有依赖变化时才重新计算，用于性能优化。",
    },
    "2026-05-06": {
        question: "什么是 DOM 事件委托？",
        answer:
            "事件委托是利用事件冒泡机制，将子元素的事件处理委托给父元素统一处理。优点：减少事件监听器数量、动态添加的元素也能响应事件、内存占用更少。实现方式是在父元素监听事件，通过 event.target 判断具体触发元素。",
    },
    "2026-05-07": {
        question: "什么是 TypeScript 的泛型？",
        answer:
            "泛型（Generics）是 TypeScript 中创建可复用组件的工具，支持多种类型同时保持类型安全。使用 <T> 定义类型参数，函数或类可以使用该参数声明类型。例如 function identity<T>(arg: T): T。泛型使代码更灵活且保持类型检查。",
    },
    "2026-05-08": {
        question: "什么是微前端（Micro Frontends）？",
        answer:
            "微前端是将前端应用拆分为独立部署的子应用的架构风格，类似微服务。每个团队可以独立开发、部署自己的模块。实现方式包括：iframe、Web Components、Module Federation 等。优点是团队独立、技术栈灵活、逐步升级，缺点是增加了复杂度。",
    },
    "2026-05-09": {
        question: "什么是 WebSocket？与 HTTP 有什么区别？",
        answer:
            "WebSocket 是全双工通信协议，建立连接后客户端和服务器可以互相主动发送消息，适合实时应用如聊天、游戏。HTTP 是半双工，只能客户端发起请求。WebSocket 建立后头部开销小，延迟低，但需要处理连接状态、心跳检测等。",
    },
    "2026-05-10": {
        question: "什么是 JavaScript 的 this 关键字？",
        answer:
            "this 指向函数执行的上下文对象，其值取决于调用方式：全局调用指向 window/global，对象方法调用指向该对象，构造函数指向新实例，call/apply/bind 可以显式绑定，箭头函数继承外层 this。this 是 JavaScript 中最容易混淆的概念之一。",
    },
    "2026-05-11": {
        question: "什么是 React 的 Refs？",
        answer:
            "Refs 是 React 中访问 DOM 元素或组件实例的方式。使用 createRef 或 useRef 创建，通过 ref 属性绑定。常用于：获取输入框焦点、触发强制动画、集成第三方 DOM 库。函数组件使用 useRef，类组件使用 createRef，避免过度使用 Refs。",
    },
    "2026-05-12": {
        question: "什么是 CSS Grid 布局？",
        answer:
            "CSS Grid 是二维布局系统，可同时处理行和列。通过 display: grid 启用，使用 grid-template-columns/rows 定义行列，grid-gap 设置间距，grid-area 放置元素。适合复杂页面布局，比 Flexbox 更强大，两者可结合使用。",
    },
    "2026-05-13": {
        question: "什么是 JavaScript 的模块化？",
        answer:
            "模块化是将代码分割为独立、可复用的单元。ES6 之前使用立即执行函数、CommonJS、AMD 等；ES6 引入 import/export 标准模块系统。模块有自己的作用域，通过导出导入共享功能，避免全局污染，提高代码可维护性。",
    },
    "2026-05-14": {
        question: "什么是前端性能优化？有哪些方法？",
        answer:
            "前端性能优化提升网页加载和运行速度。方法包括：代码分割懒加载、资源压缩合并、使用 CDN、图片优化/懒加载、浏览器缓存、减少重排重绘、Web Worker、Service Worker 缓存、Tree Shaking 等。使用 Lighthouse 等工具检测性能指标。",
    },
    "2026-05-15": {
        question: "什么是 Docker？前端开发中如何使用？",
        answer:
            "Docker 是容器化平台，将应用及其依赖打包为容器，确保环境一致性。前端使用场景：构建环境统一、多项目隔离、CI/CD 流程、本地开发环境快速搭建。通过 Dockerfile 定义镜像，docker-compose 编排多容器应用。",
    },
    "2026-05-16": {
        question: "什么是 React 的高阶组件（HOC）？",
        answer:
            "高阶组件是接收组件并返回新组件的函数，用于复用组件逻辑。例如 withRouter、connect。HOC 不会修改原组件，而是通过包装传递 props。现在更多使用自定义 Hooks 替代 HOC，Hooks 更灵活且没有组件嵌套地狱问题。",
    },
    "2026-05-17": {
        question: "什么是 SQL 注入？如何防范？",
        answer:
            "SQL 注入是攻击者通过输入恶意 SQL 代码操纵数据库的攻击方式。防范方法：使用参数化查询/预处理语句、ORM 框架、输入验证和过滤、最小权限原则、错误信息隐藏。永远不要将用户输入直接拼接到 SQL 语句中。",
    },
    "2026-05-18": {
        question: "什么是 CSS 预处理器？Sass/Less 的优点？",
        answer:
            "CSS 预处理器扩展 CSS 语法，提供变量、嵌套、混合、继承等功能，编译为普通 CSS。Sass/Scss、Less、Stylus 是主流方案。优点：代码复用、更易维护、模块化、数学运算、条件循环。需要构建工具编译。",
    },
    "2026-05-19": {
        question: "什么是 React 的 Portal？",
        answer:
            "Portal 允许将子组件渲染到父组件之外的 DOM 节点，使用 ReactDOM.createPortal。常用于模态框、提示框等需要脱离当前层级的场景。Portal 中的事件仍会冒泡到 React 树，行为与普通组件一致，只是 DOM 位置不同。",
    },
    "2026-05-20": {
        question: "什么是 XSS 攻击？如何防范？",
        answer:
            "XSS（跨站脚本攻击）是注入恶意脚本到网页中执行的攻击。类型：存储型、反射型、DOM 型。防范：输入过滤、输出转义、Content Security Policy、HttpOnly Cookie、框架自动转义（如 React 的 JSX）。不要信任任何用户输入。",
    },
    "2026-05-21": {
        question: "什么是 GraphQL？与 REST 的区别？",
        answer:
            "GraphQL 是 API 查询语言，客户端精确指定需要的数据，一次请求获取多个资源。相比 REST：减少过度获取、强类型 Schema、单个端点、内省文档。但增加了复杂度，缓存不如 REST 成熟，适合复杂数据关系的应用。",
    },
    "2026-05-22": {
        question: "什么是 JavaScript 的 Symbol 类型？",
        answer:
            "Symbol 是 ES6 新增的基本数据类型，表示唯一的标识符。每个 Symbol 值都是唯一的，即使描述相同。用途：对象唯一属性键、定义常量、实现私有属性。Symbol 不能用 new，有全局注册表 Symbol.for()。",
    },
    "2026-05-23": {
        question: "什么是 React 的 Suspense 和 lazy？",
        answer:
            "React.lazy 实现组件懒加载，配合动态 import 分割代码。Suspense 包裹 lazy 组件，显示 fallback UI（如加载动画）直到组件加载完成。配合 Error Boundary 处理加载错误。有效减小首屏加载体积，提升性能。",
    },
    "2026-05-24": {
        question: "什么是 CDN？工作原理是什么？",
        answer:
            "CDN（内容分发网络）通过全球分布的服务器节点缓存和分发内容，用户从最近节点获取资源。原理：DNS 解析到最近节点、边缘服务器缓存静态资源、源站更新缓存。优点：加速访问、减轻源站压力、提高可用性、防御 DDoS。",
    },
    "2026-05-25": {
        question: "什么是 TypeScript 的类型断言？",
        answer:
            "类型断言是告诉 TypeScript 编译器某个值的类型，使用 as 语法或尖括号语法。例如 (value as string).length。用于：访问联合类型的特定属性、将未知类型转为具体类型。注意：断言只在编译时有效，运行时不做检查，错误的断言会导致错误。",
    },
    "2026-05-26": {
        question: "什么是 Webpack 的 Tree Shaking？",
        answer:
            "Tree Shaking 是消除死代码的优化技术，删除未使用的导出代码。基于 ES Module 的静态结构，Webpack 分析依赖图，标记未使用的导出并在生产模式移除。需要：使用 ES Module、配置 sideEffects、开启 optimization.usedExports。",
    },
    "2026-05-27": {
        question: "什么是 React 的 Error Boundary？",
        answer:
            "Error Boundary 是捕获子组件树 JavaScript 错误的 React 组件，防止整个应用崩溃。通过 componentDidCatch 或 static getDerivedStateFromError 实现，显示备用 UI。只能捕获渲染、生命周期、构造函数中的错误，不能捕获事件处理、异步代码、服务端渲染错误。",
    },
    "2026-05-28": {
        question: "什么是 Service Worker？",
        answer:
            "Service Worker 是浏览器在后台运行的脚本，可拦截网络请求、缓存资源、推送通知。是 PWA 的核心技术，支持离线访问。生命周期：安装、激活、运行。需要 HTTPS，使用 Cache API 存储响应，可实现复杂的缓存策略。",
    },
    "2026-05-29": {
        question: "什么是 JavaScript 的 async/await？",
        answer:
            "async/await 是 Promise 的语法糖，使异步代码看起来像同步代码。async 函数返回 Promise，await 暂停执行等待 Promise 完成。优点：代码可读性好、易于调试、可用 try/catch 处理错误。是处理异步操作的标准方式。",
    },
    "2026-05-30": {
        question: "什么是 React 的 Fiber 架构？",
        answer:
            "Fiber 是 React 16 重写的新协调引擎，将渲染工作拆分为小单元，可暂停、恢复、优先处理。解决旧版递归渲染阻塞主线程问题，支持：异步渲染、优先级调度、错误边界、Suspense。Fiber 是链表结构，每个节点代表一个 React 元素。",
    },
    "2026-05-31": {
        question: "什么是 WebAssembly？前端如何使用？",
        answer:
            "WebAssembly（Wasm）是浏览器可执行的二进制指令格式，性能接近原生代码。用于：计算密集型任务（游戏、图像处理）、移植 C/C++/Rust 代码、与 JavaScript 协作。通过 WebAssembly API 加载 .wasm 文件，与 JS 共享内存、互相调用。",
    },
};

// 获取日期的格式化字符串 YYYY-MM-DD
const formatDate = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
};

// 获取星期几
const getWeekDay = (date: Date): string => {
    const weekDays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
    return weekDays[date.getDay()];
};

const EvedayAsk_components: FC = () => {
    const [currentDate, setCurrentDate] = useState<Date>(new Date());
    const [isAnswerVisible, setIsAnswerVisible] = useState<boolean>(false);
    const { isDark } = useIsDark();
    const pColor = theme.useToken().token.colorPrimaryBorder;

    const dateStr = formatDate(currentDate);
    const qaData = qaDatabase[dateStr] || {
        question: "今日暂无问题，敬请期待！",
        answer: "",
    };

    // 切换到前一天
    const handlePrevDay = () => {
        const newDate = new Date(currentDate);
        newDate.setDate(newDate.getDate() - 1);
        setCurrentDate(newDate);
        setIsAnswerVisible(false);
    };

    // 切换到后一天（限制不能大于今天）
    const handleNextDay = () => {
        const newDate = new Date(currentDate);
        newDate.setDate(newDate.getDate() + 1);
        // 限制不能选择未来的日期
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        newDate.setHours(0, 0, 0, 0);
        if (newDate.getTime() <= today.getTime()) {
            setCurrentDate(new Date(newDate));
            setIsAnswerVisible(false);
        }
    };

    // 判断是否超过今天
    const isAfterToday = () => {
        const nextDate = new Date(currentDate);
        nextDate.setDate(nextDate.getDate() + 1);
        nextDate.setHours(0, 0, 0, 0);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return nextDate.getTime() > today.getTime();
    };

    // 切换答案显示状态
    const toggleAnswer = () => {
        setIsAnswerVisible(!isAnswerVisible);
    };

    // 判断是否为今天
    const isToday = formatDate(new Date()) === dateStr;

    return (
        <ConfigProvider
            theme={{
                components: {
                    Card: {
                        borderRadiusLG: 12,
                    },
                },
            }}
        >
            <Card
                className={`w-full ${isDark ? "bg-[#1E1E1E] border-gray-700" : "bg-white border-gray-200"}`}
                styles={{
                    body: {
                        padding: 0,
                    },
                }}
            >
                {/* Header 部分 - 日期切换 */}
                <div
                    className={`flex items-center justify-between px-5 py-4 border-b ${isDark ? "border-gray-700" : "border-gray-200"}`}
                >
                    <div
                        className="flex flex-col border-l-4 pl-2.5"
                        style={{ borderColor: pColor }}
                    >
                        <div className="flex items-center gap-2">
                            <span
                                className={`text-lg font-bold whitespace-nowrap ${isDark ? "text-white" : "text-gray-800"}`}
                            >
                                每日问答
                            </span>
                            {isToday && (
                                <span
                                    className="text-xs px-2 py-0.5 rounded-full bg-blue-500 text-white"
                                >
                                    今日
                                </span>
                            )}
                        </div>
                        <span
                            className="text-base font-medium tracking-wider opacity-60"
                            style={{ color: pColor }}
                        >
                            Daily Q&A
                        </span>
                    </div>

                    {/* 日期切换控件 */}
                    <div className="flex items-center gap-0.5 sm:gap-1 shrink-0">
                        <Button
                            type="text"
                            icon={<LeftOutlined />}
                            onClick={handlePrevDay}
                            className={isDark ? "text-gray-300" : "text-gray-600"}
                            size="small"
                        />
                        <div className="flex flex-col items-center min-w-18 sm:min-w-20">
                            <span
                                className={`text-xs sm:text-sm font-semibold whitespace-nowrap ${isDark ? "text-white" : "text-gray-800"}`}
                            >
                                {dateStr}
                            </span>
                            <span
                                className={`text-xs ${isDark ? "text-gray-400" : "text-gray-500"}`}
                            >
                                {getWeekDay(currentDate)}
                            </span>
                        </div>
                        <Button
                            type="text"
                            icon={<RightOutlined />}
                            onClick={handleNextDay}
                            disabled={isAfterToday()}
                            className={isDark ? "text-gray-300" : "text-gray-600"}
                            size="small"
                        />
                    </div>
                </div>

                {/* Content 部分 - 问题和答案 */}
                <div className="px-5 py-6">
                    {/* 问题区域 */}
                    <div className="mb-4">
                        <div className="flex items-start gap-3">
                            <QuestionCircleOutlined
                                className={`text-xl mt-1 ${isDark ? "text-blue-400" : "text-blue-500"}`}
                            />
                            <div className="flex-1">
                                <span
                                    className={`text-base font-medium ${isDark ? "text-gray-200" : "text-gray-700"}`}
                                >
                                    问题：
                                </span>
                                <span
                                    className={`text-lg font-semibold ${isDark ? "text-white" : "text-gray-900"}`}
                                >
                                    {qaData.question}
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* 显示/隐藏答案按钮 */}
                    {qaData.answer && (
                        <div className="flex justify-center mb-4">
                            <Button
                                type="primary"
                                icon={isAnswerVisible ? <EyeInvisibleOutlined /> : <EyeOutlined />}
                                onClick={toggleAnswer}
                                className="rounded-full px-6"
                            >
                                {isAnswerVisible ? "隐藏答案" : "查看答案"}
                            </Button>
                        </div>
                    )}

                    {/* 答案区域 - 固定高度，支持滚动 */}
                    <div className="relative h-50 mt-4">
                        {/* 答案内容 - 始终显示但透明度变化 */}
                        <div
                            className={`absolute inset-0 p-4 rounded-xl overflow-y-auto transition-opacity duration-300 ${
                                isDark
                                    ? "bg-[#2a2a2a] border-gray-600"
                                    : "bg-gray-50 border-gray-200"
                            } border ${isAnswerVisible ? "opacity-100" : "opacity-30 blur-[2px]"}`}
                        >
                            <div className="flex items-start gap-3">
                                <div
                                    className={`w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold shrink-0 ${
                                        isDark ? "bg-green-600 text-white" : "bg-green-500 text-white"
                                    }`}
                                >
                                    A
                                </div>
                                <div className="flex-1">
                                    <span
                                        className={`text-base font-medium ${isDark ? "text-gray-300" : "text-gray-600"}`}
                                    >
                                        答案：
                                    </span>
                                    <p
                                        className={`mt-2 text-base leading-relaxed ${isDark ? "text-gray-200" : "text-gray-800"}`}
                                    >
                                        {qaData.answer}
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* 未显示答案时的遮罩层 */}
                        {!isAnswerVisible && (
                            <div
                                className={`absolute inset-0 rounded-xl flex flex-col items-center justify-center gap-3 ${
                                    isDark
                                        ? "bg-linear-to-t from-[#1E1E1E] via-[#1E1E1E]/80 to-transparent"
                                        : "bg-linear-to-t from-white via-white/80 to-transparent"
                                }`}
                            >
                                {/* 锁图标/图案 */}
                                <div
                                    className={`w-16 h-16 rounded-full flex items-center justify-center ${
                                        isDark ? "bg-gray-700" : "bg-gray-200"
                                    }`}
                                >
                                    <svg
                                        className={`w-8 h-8 ${isDark ? "text-gray-400" : "text-gray-500"}`}
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                                        />
                                    </svg>
                                </div>
                                <span
                                    className={`text-sm font-medium ${
                                        isDark ? "text-gray-400" : "text-gray-500"
                                    }`}
                                >
                                    点击上方按钮查看答案
                                </span>
                            </div>
                        )}
                    </div>
                </div>
            </Card>
        </ConfigProvider>
    );
};

export default EvedayAsk_components;
