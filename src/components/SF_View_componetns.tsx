import { message, theme } from "antd";
import { useEffect, useRef, useState, type FC } from "react";

interface View {
    code: {
        html: string;
        css: string;
        javascript: string;
    };
    isSelect: boolean;
    reLoadKey: number;
}

const SF_View_componetns: FC<View> = ({ code, isSelect, reLoadKey}) => {
    const debounceTimer = useRef<number | null>(null);
    const [htmlsrc, setHtmlSrc] = useState<string>("");
    const [messageApi, contextHolder] = message.useMessage();
    const view = useRef<HTMLIFrameElement>(null);
    const pColor = theme.useToken().token.colorPrimaryBorder;
    useEffect(() => {
        const handleMessage = (e: MessageEvent) => {
            if (e.data.type === "timeout") {
                messageApi.error("代码超时，内有死循环或者循环过大！");
            }
        };

        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, [messageApi]);

    useEffect(() => {
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }

        debounceTimer.current = setTimeout(() => {
            const transformCode = (userJs: string) => {
                return userJs;
                // .replace(
                //     /(for|while)\s*\(([\s\S]*?)\)\s*\{/g,
                //     "$1 ($2) { window.__LOOP_PROTECT__.check(); ",
                // )
                // .replace(
                //     /do\s*\{/g,
                //     "do { window.__LOOP_PROTECT__.check(); ",
                // );
            };

            const htmlContent = code.html;

            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, "text/html");

            const userCss = doc.createElement("style");
            userCss.id = "userCss";
            userCss.textContent = code.css;
            doc.head.appendChild(userCss);

            const userJs = doc.createElement("script");
            userJs.id = "userJs";
            const libJs = doc.createElement("script");
            libJs.id = "libJs";

            userJs.text = transformCode(code.javascript);
            libJs.text = `
window.__LOOP_PROTECT__ = {
    start: Date.now(),
    check: function() {
        if (Date.now() - this.start > 1000) { 
            window.parent.postMessage({
                type: 'timeout'
            }, "*");
            throw new Error('检测到代码运行超时，可能存在死循环！');
        }
    }
};

const fakeStorage = {
    _data:{},
    setItem: function (key, value) {
        this._data[key] = String(value);
    },
    getItem: function (key) {
        return this._data.hasOwnProperty(key) ? this._data[key] : null;
    },
    removeItem: function (key) {
        delete this._data[key];
    },
    clear: function () {
        for (let k in this._data) delete this._data[k];
    },
    get length() {
        return Object.keys(_data).length;
        
    },
    key: function (i) {
        return Object.keys(this._data)[i] || null;
    }
};
Object.defineProperty(window, 'localStorage', {
    value: fakeStorage,
    writable: false,
    configurable: true
});
Object.defineProperty(window, 'sessionStorage', {
    value: fakeStorage,
    writable: false,
    configurable: true
});
    
const methods = ['log', 'warn', 'error', 'info', 'debug', 'table','clear','count','assert'];

const originalMethods = {};
methods.forEach(method => {
    originalMethods[method] = console[method];
});

methods.forEach(method => {
    console[method] = function (...args) {
        
        const logData = {
            type: 'consoleLog',      
            level: method,           
            payload: args,           
            timestamp: Date.now()
        };

            window.parent.postMessage(logData, '*');

        if (originalMethods[method]) {
            originalMethods[method].apply(console, args);
        }
    };
});

window.addEventListener('unhandledrejection', event => {
    const errorData = {
        type: 'consoleLog',
        level: 'error',
        payload: [\`Unhandled Promise Rejection: \${event.reason}\`],
        timestamp: Date.now()
    };
    window.parent.postMessage(errorData, '*');
});



const devCss = document.createElement("style");
devCss.id = "dev";
devCss.textContent =\`body *:hover:not(:has(:hover)) {
    outline: 2px dashed #4a9eff;
    background-color: rgba(74, 158, 255, 0.08) !important;
    box-shadow: 0 0 8px rgba(74, 158, 255, 0.2);
    transition: all 0.1s ease-in-out;
}\`;

function DsvClick(e){         
        e.preventDefault(); 
        e.stopPropagation(); 
        e.stopImmediatePropagation();
        if (e.target != document.childNodes[0] && e.target != document.childNodes[0].childNodes[2]){
            window.parent.postMessage({
                type: 'iframe-click',
                id: e.target.id,
                tagName:e.target.tagName,
                timestamp: Date.now()
            }, "*");
        }
    }

window.addEventListener('message',(e)=>{
    if(e.data.type=="start_dev"){
        document.head.appendChild(devCss);
        document.addEventListener('click',DsvClick,true);
    }else if(e.data.type=="end_dev"){
        document.getElementById("dev").remove()
        document.removeEventListener('click',DsvClick,true);
    }
})
`;
            doc.head.appendChild(libJs);
            if (doc.body) {
                doc.body.appendChild(userJs);
            } else {
                doc.head.appendChild(userJs);
            }

            setHtmlSrc(doc.documentElement.outerHTML);
        }, 300);

        return () => {
            if (debounceTimer.current) {
                clearTimeout(debounceTimer.current);
            }
        };
    }, [code]);

    useEffect(() => {
        if (isSelect) {
            view.current?.contentWindow?.postMessage(
                {
                    type: "start_dev",
                },
                "*",
            );
        } else {
            view.current?.contentWindow?.postMessage(
                {
                    type: "end_dev",
                },
                "*",
            );
        }
    }, [isSelect]);

    return (
        <>
            {contextHolder}
            <div className=" h-full p-1">
                <iframe
                    className="w-full h-full border-2  rounded-xl "
                    style={{
                        borderColor: pColor,
                    }}
                    srcDoc={htmlsrc}
                    src="zcwl-iframe"
                    sandbox={
                        "allow-scripts allow-popups allow-modals allow-forms"
                    }
                    ref={view}
                    key={reLoadKey}
                ></iframe>
            </div>
        </>
    );
};

export default SF_View_componetns;
