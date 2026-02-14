export interface DocItem {
    id: string;
    name: string;
    summary: string;
    img: string;
}

export interface DocResponse {
    docList: DocItem[];
}

export interface DocDirItem {
    id: string;
    name: string;
}

export interface DocInfoResponse {
    docInfo: DocItem;
    docDir: DocDirItem[];
}

export interface DocContentResponse {
    d_id: string;
    c_id: string;
    title: string;
    content: string;
}

export interface ChunkVector {
    d_id: number;
    c_id: number;
    chunk: string;
    vector: number[];
}
