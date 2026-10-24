import { askNova } from "../api/chatApi";
import type { Product } from "../types";
import { localAssistantReply } from "../components/chat/assistantUtils";
export async function getAssistantReply(products:Product[],message:string){try{return await askNova(message)}catch{return localAssistantReply(products,message)}}
