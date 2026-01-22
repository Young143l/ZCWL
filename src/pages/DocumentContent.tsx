import type { FC } from "react";
import { useParams } from "react-router-dom";

const DocumentContent:FC = () => {
    const { d_id, c_id } = useParams();

    return (
        <>
        </>
    )
};
export default DocumentContent;
