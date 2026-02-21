import { message } from "antd";
import { useEffect, useRef, useState, type FC } from "react";

interface View {
    code: {
        html: string;
        css: string;
        javascript: string;
    };
    isSelect: boolean;
}

const SF_View_componetns: FC<View> = ({ code, isSelect }) => {
    const debounceTimer = useRef<number | null>(null);
    const [htmlsrc, setHtmlSrc] = useState<string>("");
    const [messageApi, contextHolder] = message.useMessage();
    const view = useRef<HTMLIFrameElement>(null);
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
                return userJs.replace(
                    /(for|while|do)\s*\(([\s\S]*?)\)\s*\{/g,
                    "$1 ($2) { window.__LOOP_PROTECT__.check(); ",
                );
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

const devCss = document.createElement("style");
devCss.id = "devCss";
devCss.textContent =\`body *:hover:not(:has(:hover)) {
    outline: 2px dashed #4a9eff;
    background-color: rgba(74, 158, 255, 0.08) !important;
    box-shadow: 0 0 8px rgba(74, 158, 255, 0.2);
    transition: all 0.1s ease-in-out;
}\`;

const devJs = document.createElement("script");
devJs.id="devJs";
devJs.text = \`document.addEventListener('click',(e) => {               
    e.stopImmediatePropagation();
    if (e.target != document.childNodes[0] && e.target != document.childNodes[0].childNodes[2])
        window.parent.postMessage({
            type: 'iframe-click',
            id: e.target.id,
            tagName:e.target.tagName,
            timestamp: Date.now()
        }, "*");
},true);\`;

window.addEventListener('message',(e)=>{
    console.log(1);
    if(e.data.type=="start_dev"){
        document.head.appendChild(devCss);
        document.head.appendChild(devJs);
    }else if(e.data.type=="end_dev"){
        document.getElementById("devCss").remove()
        document.getElementById("devJs").remove()
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
                    className="w-full h-full border-2 border-gray-300 rounded-xl"
                    srcDoc={htmlsrc}
                    sandbox={
                        "allow-scripts allow-popups allow-modals allow-forms"
                    }
                    ref={view}
                ></iframe>
            </div>
        </>
    );
};

export default SF_View_componetns;
