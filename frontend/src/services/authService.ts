import { signin, signup } from "../api/authApi";
import type { SigninRequest, SignupRequest } from "../types/auth";
export const authService={signIn:(input:SigninRequest)=>signin(input),signUp:(input:SignupRequest)=>signup(input)};
