export type Role = "ADMIN" | "CUSTOMER";
export interface SignupRequest { name:string; email:string; password:string; }
export interface SigninRequest { email:string; password:string; }
export interface AuthResponse { id:number; name:string; email:string; role:Role; token:string; }
