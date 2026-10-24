import { apiRequest } from "./httpClient";
import type { Product } from "../types";
export type ChatReply={reply:string;products:Product[];mode:"ai"|"catalogue"};
export const askNova=(message:string)=>apiRequest<ChatReply>("/assistant/chat",{method:"POST",body:JSON.stringify({message})});
