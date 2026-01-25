import {create} from "zustand"
import { persist } from "zustand/middleware";

interface AIChatDocStatus{
    show:boolean;
    chat:{
        ask:string,
        ans:string,
        
    }
}

const useAIChatDoc = create<AIChatDocStatus>((set)=>{

},);


export default useAIChatDoc;