import { create } from "zustand";
import type { AuthResponse } from "../types/auth";
type AuthState={user:AuthResponse|null;hydrate:()=>void;login:(user:AuthResponse)=>void;logout:()=>void};
export const useAuthStore=create<AuthState>((set)=>({user:null,hydrate:()=>{try{const user=JSON.parse(localStorage.getItem("nova-user")||"null");set({user});}catch{set({user:null});}},login:(user)=>{localStorage.setItem("nova-user",JSON.stringify(user));localStorage.setItem("nova-token",user.token);set({user});},logout:()=>{localStorage.removeItem("nova-user");localStorage.removeItem("nova-token");set({user:null});}}));
