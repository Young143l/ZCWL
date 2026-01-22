export interface DocInfo {
    id: string;
    name: string;
    summary: string;
    img: string;
}
export interface DocDir {
    id: string;
    name: string;
}

export interface DocContent {
    id: string;
    title: string;
    content: string;
}

export const getDocList: () => DocInfo[] = () => {
    return [
        {
            id: "1",
            name: "2",
            summary: "3",
            img: "https://img.young143.top/young143/a.jpg",
        },
        {
            id: "1",
            name: "2",
            summary: "3",
            img: "https://img.young143.top/young143/a.jpg",
        },
        {
            id: "1",
            name: "2",
            summary: "3",
            img: "https://img.young143.top/young143/a.jpg",
        },
        {
            id: "1",
            name: "2",
            summary: "3",
            img: "https://img.young143.top/young143/a.jpg",
        },
        {
            id: "1",
            name: "2",
            summary: "3",
            img: "https://img.young143.top/young143/a.jpg",
        },
        
    ];
};

export const getDocDirectory: (d_id: string) => DocDir = (d_id: string) => {};

export const getDocContent: (d_id: string, c_id: string) => DocContent = (
    d_id: string,
    c_id: string,
) => {};
