export const cdn:string=import.meta.env.VITE_CDN
export const isCdn:boolean=(import.meta.env.VITE_USE_CDN=='true'?true:false);