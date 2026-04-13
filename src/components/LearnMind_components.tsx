import jsMind from "jsmind";
import {
    LoadingOutlined,
    PlusOutlined,
    MinusOutlined,
} from "@ant-design/icons";
import "jsmind/style/jsmind.css";
import { useEffect, useRef, useState, type FC } from "react";
import { getDoc, getDocList } from "../api/Doc_api";
import { Spin, theme, Button, Space } from "antd";
import { useNavigate } from "react-router-dom";
import { isMobile } from "react-device-detect";
import useIsDark from "../status/IsDark_status"; // 导入深色模式状态

// 自定义主题样式
const getCustomThemeStyles = (pColor: string, isDarkMode: boolean) => `
  /* 优化后的主题 - 与网站风格一致 */
  jmnodes.theme-cfww jmnode {
    background: ${isDarkMode ? "#1f1f1f" : "#ffffff"};
    color: ${isDarkMode ? "#f5f5f5" : "#1f1f1f"};
    border-radius: 8px;
    box-shadow: 0 1px 3px ${isDarkMode ? "rgba(255, 255, 255, 0.06)" : "rgba(0, 0, 0, 0.06)"};
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
    font-size: 13px;
    line-height: 1.4;
    padding: 8px 12px;
    border: 2px solid ${isDarkMode ? "#303030" : "#f0f0f0"};
    transition: all 0.2s ease;
    backdrop-filter: blur(4px);
  }
  
  jmnodes.theme-cfww jmnode:hover {
    background: ${isDarkMode ? "#2a2a2a" : "#f9f9f9"};
    box-shadow: 0 2px 6px ${isDarkMode ? "rgba(255, 255, 255, 0.08)" : "rgba(0, 0, 0, 0.08)"};
    border-color: ${isDarkMode ? `${pColor}80` : `${pColor}40`};
    transform: translateY(-1px);
  }
  
  jmnodes.theme-cfww jmnode.selected {
    background: ${isDarkMode ? `${pColor}30` : `${pColor}10`};
    color: ${isDarkMode ? "#ffffff" : "#141414"};
    box-shadow: 0 2px 8px ${isDarkMode ? `${pColor}40` : `${pColor}20`};
    border-color: ${isDarkMode ? `${pColor}80` : `${pColor}60`};
    transform: translateY(-1px);
  }
  
  jmnodes.theme-cfww jmnode.root {
    font-size: 18px;
    font-weight: 600;
    background: ${isDarkMode ? `${pColor}20` : `${pColor}08`};
    padding: 12px 16px;
    border-radius: 10px;
    border: 1px solid ${isDarkMode ? `${pColor}50` : `${pColor}30`};
    box-shadow: 0 2px 6px ${isDarkMode ? `${pColor}30` : `${pColor}15`};
    backdrop-filter: blur(8px);
  }
  
  jmnodes.theme-cfww jmexpander {
    background: ${isDarkMode ? `${pColor}30` : `${pColor}20`};
    border: none;
    color: ${isDarkMode ? "white" : pColor};
    font-weight: 600;
    font-size: 10px;
    width: 16px;
    height: 16px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 3px;
    cursor: pointer;
    transition: all 0.2s ease;
    vertical-align: middle;
    margin: 0 4px 0 0;
    flex-shrink: 0;
    font-family: inherit;
  }
  
  jmnodes.theme-cfww jmexpander:hover {
    background: ${isDarkMode ? `${pColor}60` : `${pColor}40`};
    color: ${isDarkMode ? "black" : "white"};
    transform: scale(1.1);
  }
  
  /* 连接线样式 */
  .theme-cfww path.jmnode-link {
    stroke: ${isDarkMode ? "#4a4a4a" : "#e8e8e8"};
    stroke-width: 1.2;
    stroke-linecap: round;
    stroke-linejoin: round;
  }
  
  /* 左右分支使用相同颜色 */
  .theme-cfww path.jmnode-link-left {
    stroke: ${isDarkMode ? "#4a4a4a" : "#e8e8e8"};
  }
  
  .theme-cfww path.jmnode-link-right {
    stroke: ${isDarkMode ? "#4a4a4a" : "#e8e8e8"};
  }
  
  /* 容器样式 */
  .mindmap-container {
    background: ${isDarkMode ? "#141414" : "#ffffff"};
    border-radius: 12px;
    border: 1px solid ${isDarkMode ? "#2a2a2a" : "#f5f5f5"};
    overflow: hidden;
    // box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
    backdrop-filter: blur(4px);
  }
  
//   .mindmap-container:hover {
//     border-color: #e8e8e8;
//     box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
//   }
  
  /* 确保文本清晰可读 */
  jmnodes.theme-cfww jmnode,
  jmnodes.theme-cfww jmexpander {
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
  
  /* 响应式优化 */
  @media (max-width: 768px) {
    jmnodes.theme-cfww jmnode {
      font-size: 12px;
      padding: 6px 10px;
    }
    
    jmnodes.theme-cfww jmnode.root {
      font-size: 16px;
      padding: 10px 14px;
    }
    
    jmnodes.theme-cfww jmexpander {
      width: 14px;
      height: 14px;
      font-size: 9px;
    }
  }
`;

const options = {
    container: "jsmind_container",
    editable: false,
    theme: "cfww",
    mode: "full" as "full" | "side" | undefined,
    view: {
        engine: "svg" as "canvas" | "svg" | undefined,
        draggable: true,
        hide_scrollbars_when_draggable: !isMobile,
        zoom: {
            min: 0.5,
            max: 2.0,
            step: 0.1,
        },
    },
};

const LearnMind_components: FC = () => {
    const jmRef = useRef<HTMLDivElement>(null);
    const jmInstance = useRef<jsMind | null>(null);
    const { isDark } = useIsDark(); // 使用深色模式状态
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const nav = useNavigate();
    const [loading, setLoading] = useState<boolean>(true);
    // 注入自定义样式
    useEffect(() => {
        const style = document.createElement("style");
        style.textContent = getCustomThemeStyles(pColor, isDark);
        document.head.appendChild(style);

        return () => {
            document.head.removeChild(style);
        };
    }, [pColor, isDark]);

    const buildMindMap = async (jm: jsMind) => {
        const res = await getDocList();
        if (res.ok) {
            const children: Array<{
                id: string;
                topic: string;
                isroot?: boolean;
                parentid?: string;
                expanded?: boolean;
                direction?: string;
            }> = [];
            children.push({ id: "root", topic: "Cfww", isroot: true });

            let lr = 0;
            for (const doc of res.docList) {
                const docRes = await getDoc(doc.id);
                if (docRes.ok) {
                    const docNode = {
                        id: doc.id,
                        parentid: "root",
                        expanded: false,
                        direction: lr ? "right" : "left",
                        topic: doc.name,
                    };
                    children.push(docNode);

                    if (docRes.ok) {
                        docRes.docDir.forEach((chapter) =>
                            children.push({
                                id: `${doc.id}/${chapter.id}`,
                                topic: chapter.name,
                                parentid: doc.id,
                            }),
                        );
                    }
                    lr = ~lr;
                    // children.push(docNode);
                }
            }

            jm.show({
                meta: {
                    name: "Cfww",
                    author: "cfww",
                    version: "1.0",
                },
                format: "node_array",
                data: children,
            });
            setTimeout(() => {
                setLoading(false);
            }, 100);
        }
    };

    useEffect(() => {
        const jm = new jsMind(options);
        jmInstance.current = jm;
        const handleWheel = (e: WheelEvent) => {
            e.preventDefault();
            if (e.deltaY < 0) {
                jm.view.zoom_in();
            } else {
                jm.view.zoom_out();
            }
        };
        buildMindMap(jm);
        if (jmRef.current) {
            jmRef.current.addEventListener("wheel", handleWheel, {
                passive: false,
            });
        }
        jm.add_event_listener(function (type, data) {
            if (type === 4 && data.node !== "root") {
                nav("/document/" + data.node);
            }
        });
        const containerNode = jmRef.current;

        return () => {
            if (containerNode) {
                containerNode.removeEventListener("wheel", handleWheel);
                containerNode.innerHTML = "";
            }
            jmInstance.current = null;
        };
    }, [nav]);

    const handleZoomIn = () => {
        if (jmInstance.current) {
            jmInstance.current.view.zoom_in();
        }
    };

    const handleZoomOut = () => {
        if (jmInstance.current) {
            jmInstance.current.view.zoom_out();
        }
    };

    return (
        <>
            <div
                className={`flex justify-center items-center w-full h-100 lg:h-full ${!loading ? "hidden" : ""}`}
            >
                <Spin indicator={<LoadingOutlined spin />} size="large" />
            </div>
            <div
                className={`w-full h-100 lg:h-full relative ${loading ? "hidden" : ""}`}
            >
                <div
                    ref={jmRef}
                    id="jsmind_container"
                    className="mindmap-container"
                    style={{
                        height: "100%",
                        width: "100%",
                        overflow: "hidden",
                    }}
                ></div>
                {/* 缩放按钮 */}
                <Space
                    orientation="horizontal"
                    style={{
                        position: "absolute",
                        bottom: "20px",
                        left: "20px",
                        zIndex: 10,
                    }}
                >
                    <Button
                        // type="primary"
                        icon={<MinusOutlined />}
                        onClick={handleZoomOut}
                        size="large"
                    />
                    <Button
                        // type="primary"
                        icon={<PlusOutlined />}
                        onClick={handleZoomIn}
                        size="large"
                    />
                </Space>
            </div>
        </>
    );
};

export default LearnMind_components;
