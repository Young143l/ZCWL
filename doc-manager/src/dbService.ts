import { Pool } from "pg";
import { PGSQL_CONFIG } from "./config.js";

export class dbServer{
    private pool:Pool;
    constructor(){
        this.pool=new Pool(PGSQL_CONFIG)
    }
    public newDoc= async (name:string,sum:string,icon:string)=>{
        const client = await this.pool.connect();
        try{
            const result = await client.query("INSERT INTO doc(doc_name,summary,icon) VALUES($1,$2,$3) RETURNING doc_id",[name,sum,icon]);
            console.log(result.rows);
            if(result.rows.length>0){
                return result.rows[0].doc_id;
            }else{
                throw new Error("插入失败，未返回ID。")
            }
        }catch(err){
            console.log("错误："+ err);
            return null;
        }finally{
            client.release();
        }
    }
    public addContent= async(d_id:string,c_id:string,title:string,content:string)=>{
        const client = await this.pool.connect();
        try{
            await client.query("INSERT INTO doc_contents VALUES($1,$2,$3,$4)",[d_id,c_id,title,content]);
            return true;
        }catch(err){
            console.log("错误："+ err);
            return false;
        }finally{
            client.release();
        }
    }
}